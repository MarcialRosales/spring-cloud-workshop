# AppDev workshop

## 13:00 — 14:00 Developing with Spring Cloud          [Lecture]

## 14:00 — 15:00 Service Registration and Discovery  [Lecture]

## 15:00 — 15:30 Zero-Downtime Deployments for Discoverable services   [Lab]

We cannot register two PCF applications with the same `spring.application.name` against the same SCS `central-registry` service instance (but with different service's bindings or credentials) because according to SCS (1.1 and earlier) that is considered a security breached (i.e. another unexpected application is trying to register with the same name as another already registered application but using different credentials).

To go around this issue, we cannot bind PCF applications (blue and green) to the service instance of the service-registry (`p-service-registry`) because that will automatically create a new set of credentials for each application.

Instead, we need to ask the service instance -i.e. the `service-registry` from SCS- to provide us a credential and we create a `User Provided Service` with that credential. Once we have the `UPS` we can then bind that single `UPS` with our 2 applications, `green` and `blue`. That works because both instances, even though they are uniquely named in PCF they have the same `spring.application.name` used to register the app with Eureka and both apps are using the same credentials to talk to the `service-registry`, i.e. Eureka.

Step by Step
1. Create a service instance of the service registry (skip this process if you already have a service)
   <br>`cf create-service p-service-registry standard central-registry`
2. Create a service key and call it `service-registry`
  <br>`cf create-service-key central-registry service-registry`
3. Read the actual key contained within the `service-registry` service key
  <br>`cf service-key central-registry service-registry`
  It prints out something like this:

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
  <br>`cf cups service-registry -p '{"access_token_uri": "https://p-spring-cloud-services.uaa.run.haas-35.pez.pivotal.io/oauth/token","client_id": "p-service-registry-ce80e383-0691-4a0e-a48e-84df7035cb2e","client_secret": "WGE829u3U7qt","uri": "https://eureka-c890fdd0-18b5-4c5b-bc44-89ef2383dc08.cfapps.haas-35.pez.pivotal.io"}'``


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


## 17:00 — 17:30 RabbitMQ Deployment and Best practices  &amp; [Q&A]
