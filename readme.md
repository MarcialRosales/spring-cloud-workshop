# AppDev workshop

## 14:00 — 14:15 Developing with Spring Cloud          [Lecture]

- Introduction to Spring Cloud and why it exists
- Spring Cloud OSS and Spring Cloud Services (PCF Tile)

## 14:15 — 14:45 Service Registration and Discovery    [Lecture]

<a href="docs/SpringCloudServiceDiscovery.pdf">Slides</a>

## 14:45 - 15:15 Simple Discoverable applications [Lab]

```
cf-demo-client ----{ http://demo/hi?name=Bob }--> cf-demo-app
                <----{ `hello Bob` }-------------
```

First we are going to get our 2 applications running locally. With our local Eureka server.
And the second part of the lab is to push our 2 applications to PCF and use PCF Service Registry to register our applications rather than our standalone Eureka server.

### Set up

1. You will need JDK 8, Maven and STS. If you don't use STS, you need to go to <a href="http://start.spring.io/">Spring Initilizr</a> to create your projects.
2. git clone https://github.com/MarcialRosales/spring-cloud-workshop

### Standalone Service Discovery

1.

### Service Discovery in the Cloud

1. login
`cf login -a https://api.run-02.haas-40.pez.pivotal.io --skip-ssl-validation`

2. create service (http://docs.pivotal.io/spring-cloud-services/service-registry/creating-an-instance.html)

`cf marketplace -s p-service-registry`
`cf create-service p-service-registry standard registry-service`

3. update manifest.yml (host, and CF_TARGET)
4. push the application
`cf push`

5. Check the app is working
`curl cf-demo-app.cfapps-02.haas-40.pez.pivotal.io/hello?name=Marcial`

6. Go to the Admin page of the registry-service and check that our service is there

7. Now we install our client application
8. update manifest.yml (host, and CF_TARGET)
9. push the application

10. Check the app is working
`cf-demo-client.cfapps-02.haas-40.pez.pivotal.io/hi?name=Marcial`

11. Check that our app is not actually registered with Eureka however it has discovered our `demo` app.

12. We can rely on RestTemplate to automatically resolve a service-name to a url. But we can also use the Discovery API to get their urls.
`curl cf-demo-client.cfapps-02.haas-40.pez.pivotal.io/service-instances/demo | jq .`

13. Comment out the annotation @LoadBalanced which decorates a RestTemplate with Ribbon capabilities so that we can use a service-name instead of a URL and push the app. you will see that the first request below but not the second one.
`cf-demo-client.cfapps-02.haas-40.pez.pivotal.io/hi?name=Marcial`
`curl cf-demo-client.cfapps-02.haas-40.pez.pivotal.io/service-instances/demo | jq .`

### Eureka and dependency on Jersey 1.19. Path to Jersey 2.0.

- New Features on Jersey 2.0. Spring Web/REST vs Jersey 2.
- WIP eureka2 project based on Jersey 2.0 (https://github.com/Netflix/eureka/tree/master/eureka-client-jersey2)
- We still have to remove Ribbon transitive dependency on Jersey 1.19. It should be possible to remove it given that it has pluggable transport but it is a big job though.
- If we really want to leverage Netflix's load balancing capabilities the preferred path would be to keep working with Jersey 1 until Netflix updates all its stack to Jersey 2.



## 15:00 — 15:30 Zero-Downtime Deployments for Discoverable services   [Lab]

We cannot register two PCF applications with the same `spring.application.name` against the same SCS `central-registry` service instance (but with different service's bindings or credentials) because according to SCS (1.1 and earlier) that is considered a security breached (i.e. another unexpected application is trying to register with the same name as another already registered application but using different credentials).

To go around this issue, we cannot bind PCF applications (blue and green) to the service instance of the service-registry (`p-service-registry`) because that will automatically create a new set of credentials for each application.

Instead, we need to ask the service instance -i.e. the `service-registry` from SCS- to provide us a credential and we create a `User Provided Service` with that credential. Once we have the `UPS` we can then bind that single `UPS` with our 2 applications, `green` and `blue`. That works because both instances, even though they are uniquely named in PCF they have the same `spring.application.name` used to register the app with Eureka and both apps are using the same credentials to talk to the `service-registry`, i.e. Eureka.

### Step by Step
1. Create a service instance of the service registry (skip this process if you already have a service instance)
   <br>`cf create-service p-service-registry standard central-registry`
2. Create a service key and call it `service-registry`
  <br>`cf create-service-key central-registry service-registry`
3. Read the actual key contained within the `service-registry` service key
  <br>`cf service-key central-registry service-registry`
  <br>It prints out something like this:

  ```
  Getting key service-registry for service instance central-registry as mrosales@pivotal.io...

{
 "access_token_uri": "https://p-spring-cloud-services.uaa.run.haas-35.pez.pivotal.io/oauth/token",
 "client_id": "p-service-registry-ce80e383-0691-4a0e-a48e-84df7035cb2e",
 "client_secret": "WGE829u3U7qt",
 "uri": "https://eureka-c890fdd0-18b5-4c5b-bc44-89ef2383dc08.cfapps.haas-35.pez.pivotal.io"
}
```
4. Create a `User Provided Service` with the credentials above
  <br>`cf cups service-registry -p '{"access_token_uri": "https://p-spring-cloud-services.uaa.run.haas-35.pez.pivotal.io/oauth/token","client_id": "p-service-registry-ce80e383-0691-4a0e-a48e-84df7035cb2e","client_secret": "WGE829u3U7qt","uri": "https://eureka-c890fdd0-18b5-4c5b-bc44-89ef2383dc08.cfapps.haas-35.pez.pivotal.io"}'`


5. Push your blue app : `cf push -f manifest-blue.yml`
```
...
applications:
- name: myappp-blue
  services:
  - service-registry
...
```
Make sure that `spring.application.name` equals to `myapp`.

6. Repeat the process with green app: `cf push -f manifest-green.yml`
```
...
applications:
- name: myappp-green
  services:
  - service-registry
...
```
Make sure that `spring.application.name` equals to `myapp`.


## 15:45 — 17:00 Configuration Management Lecture &amp; [Lab]

<a href="docs/SpringCloudConfigSlides.pdf">Slides</a>

## 17:00 — 17:30 RabbitMQ Deployment and Best practices  &amp; [Q&A]
