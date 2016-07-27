# AppDev workshop

- 13:00 — 13:15 Developing with Spring Cloud          [Lecture]

- 13:15 — 13:45 Service Registration and Discovery    [Lecture]

- 13:45 - 14:15 Simple Discoverable applications      [Lab]

- 14:15 - 15:15 Service Discovery in the Cloud        [Lab]

- 15:15 - 15:45 Zero-Downtime Deployments for Discoverable services [Lab]

- 15:45 - 16:00 Break

- 16:00 - 16:30 Configuration Management              [Lecture]

- 16:30 — 17:00 Configuration Management in the Cloud [Lab]

- 17:00 — 17:15 Zuul                                  [Lecture / Lab]

- 17:15 — 17:30 RabbitMQ Deployment and Best practices  &amp; [Q&A]

<p>
<p>
## Developing with Spring Cloud

- Introduction to Spring Cloud and why it exists
- Spring Cloud OSS and Spring Cloud Services (PCF Tile)

## Service Registration and Discovery    [Lecture]

<a href="docs/SpringCloudServiceDiscovery.pdf">Slides</a>

- SCS gives the ability to have our applications talk each directly  without going thru the router. To do that we need have an application setting called  `spring.cloud.services.registrationMethod`. The values for this setting are : `route` and `direct`. If we use `route` (the default value), our applications register using their PCF route else if they register using their IP address.

```
NOTE: To enable direct registration, you must configure the PCF environment to allow traffic across containers or cells. In PCF 1.6, visit the Pivotal Cloud Foundry® Operations Manager®, click the Pivotal Elastic Runtime tile, and in the Security Config tab, ensure that the “Enable cross-container traffic” option is enabled.
```

##  Service Registration and Discovery    [Lab]

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

Go to the folder, labs/lab1 in the cloned git repo.

1. Run eureka-server (from STS boot dashboard or from command line)
2. Go to the eureka-server url:
`http://localhost:8761/`
3. Run cf-demo-app
4. Check that our application registered with Eureka via the Eureka Dashboard
5. Check that our app works
`curl localhost:8080/hello?name=Marcial`
6. Run cf-demo-client
7. Check that our application works, i.e. it automatically discover our demo app by its name and not by its url.
`curl localhost:8081/hi?name=Bob`
8. Check that our application can discover services using the `DiscoveryClient` api.
`curl localhost:8081/service-instances/demo | jq .``

9. stop the cf-demo-app
10. Check that it disappears from eureka but it is still visible to the client app.
`curl localhost:8081/service-instances/demo | jq .`
After 30 seconds it will disappear. This is because the client queries eureka every 30 seconds for a delta on what has happened since the last query.

11. stop eureka server, check in the logs of the demo app exceptions. Start the eureka server, and see that the service is restored, run to check it out:
`curl localhost:8081/service-instances/demo | jq .`

We know our application works, we can push it to the cloud.

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


## Zero-Downtime Deployments for Discoverable services   [Lab]

We cannot register two PCF applications with the same `spring.application.name` against the same SCS `central-registry` service instance (but with different service's bindings or credentials) because according to SCS (1.1 and earlier) that is considered a security breached (i.e. another unexpected application is trying to register with the same name as another already registered application but using different credentials).

To go around this issue, we cannot bind PCF applications (blue and green) to the service instance of the service-registry (`p-service-registry`) because that will automatically create a new set of credentials for each application.

Instead, we need to ask the service instance -i.e. the `service-registry` from SCS- to provide us a credential and we create a `User Provided Service` with that credential. Once we have the `UPS` we can then bind that single `UPS` with our 2 applications, `green` and `blue`. That works because both instances, even though they are uniquely named in PCF they have the same `spring.application.name` used to register the app with Eureka and both apps are using the same credentials to talk to the `service-registry`, i.e. Eureka.

### Step by Step 1)
1. Create a new manifest and modify the attribute 'name' and change it to `cf-demo-app-green` and push the app.
2. It will fail because Eureka does not allow two PCF apps to register with Eureka using the same  `spring.application.name`.


### Step by Step 2)
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


## 15:45 — 17:00 Configuration Management [Lecture]

<a href="docs/SpringCloudConfigSlides.pdf">Slides</a>



### Additional comments
- We can store our credentials encrypted in the repo and Spring Config Server will decrypt them before delivering them to the client.
- Spring Config Service (PCF Tile) does not support server-side decryption. Instead, we have to configure our client to do it. For that we need to make sure that the java buildpack is configured with `Java Cryptography Extension (JCE) Unlimited Strength policy files`. For further details check out the <a href="http://docs.pivotal.io/spring-cloud-services/config-server/writing-client-applications.html#use-client-side-decryption">docs</a>.


## 15:45 — 17:00 Configuration Management  [Lab]
Go to the folder, labs/lab2 in the cloned git repo.

1. Check the config server in the market place
`cf marketplace -s p-config-server`
2. Create a service instance
`cf create-service -c '{"git": { "uri": "https://github.com/MarcialRosales/spring-cloud-workshop-config" }, "count": 1 }' p-config-server standard config-server`

3. Modify our application so that it has a `bootstrap.yml` rather than `application.yml`. We don't really need an `application.yml`. If we have one, Spring Config client will take that as the default properties of the application.

4. Our repo already has our `demo.yml`. If we did not have our `spring.application.name`, the `spring-auto-configuration` jar injected by the java buildpack will automatically create a `spring.application.name` environment variable based on the env variable `VCAP_APPLICATION { ... "application_name": "cf-demo-app" ... }`.

5. Push our `cf-demo-app`.

6. Check that our application is now bound to the config server
`cf env cf-demo-app`

7. Check that it loaded the application's configuration from the config server.
`curl cf-demo-app.cfapps-02.haas-40.pez.pivotal.io/env | jq .`

We should have these configuration at the top :
```
"configService:https://github.com/MarcialRosales/spring-cloud-workshop-config/demo.yml": {
    "mymessage": "Good afternoon"
  },
  "configService:https://github.com/MarcialRosales/spring-cloud-workshop-config/application.yml": {
    "info.id": "${spring.application.name}"
  },
```  

8. Check that our application is actually loading the message from the central config and not the default message `Hello`.
`curl cf-demo-app.cfapps-02.haas-40.pez.pivotal.io/hello?name=Marcial`

9. We can modify the demo.yml in github, and ask our application to reload the settings.
`curl -X POST cf-demo-app.cfapps-02.haas-40.pez.pivotal.io/refresh`

Check the message again.
`curl cf-demo-app.cfapps-02.haas-40.pez.pivotal.io/hello?name=Marcial`


10. Add a new configuration for production : `demo-production.yml` to the repo.

11. Configure our application to use production profile by manually setting an environment variable in CF:
`cf set-env cf-demo-app SPRING_PROFILES_ACTIVE production`

we have to restage our application because we have modified the environment.

12. Check our application returns us a different value this type
`curl cf-demo-app.cfapps-02.haas-40.pez.pivotal.io/env | jq .`

We should have these configuration at the top :


Note about Reloading configuration: This works provided you only have one instance. Ideally, we want to configure our config server to receive a callback from Github (webhooks onto the actuator endpoint `/monitor`) when a change occurs. The config server (if bundled with the jar `spring-cloud-config-monitor`).
If we have more than one application instances you can still reload the configuration on all instances if you add the dependency `spring-cloud-starter-bus-amqp` to all the applications. It exposes a new endpoint called `/bus/refresh` . We would only need to go to reach one of the application instances and that instance will propagate the refresh request to all the other instances.

One configuration most people want to dynamically change is the logging level. Exercise is to modify the code to add a logger and add the logging level the demo.yml or demo-production.yml :
```
logging:
  level:
    io.pivotal.demo.CfDemoAppApplication: debug    

```

### How to organize my application's configuration around the concept of a central repository

#### Get started very quickly with spring config server : local file system (no git repo required)
```
---
spring.profiles: native
spring:
  cloud:
    config:
      server:
        native:
          searchLocations: ../../spring-cloud-workshop-config      
```

#### Use local git repo (all files must be committed!). One repo for all our applications and each application and profile has its own folder.
```
---
spring.profiles: git-local-common-repo
spring:
  cloud:
    config:
      server:
        git:
          uri: file:../../spring-cloud-workshop-config
          searchPaths: groupA-{application}-{profile}
```

#### Use local git repo. But different repos for different profiles
Spring Config server will try to resolve a pattern against ${application}/{profile}

```
---
spring.profiles: git-local-multi-repos-per-profile
spring:
  cloud:
    config:
      server:
        git:
          uri: file:../../emptyRepo
          repos:
            dev-repos:
              pattern: "*/dev"
              uri: file:../../dev-repo
            prod-repos:
              pattern: "*/prod"
              uri: file:../../prod-repo
```
 In this case, we have decided to have one repo specific for dev profile and another for prod profile              
 `curl localhost:8888/quote-service2/dev | jq .`

#### Use local git repo. Multiple repos per teams.

```
 ---
 spring.profiles: git-local-multi-repos-per-teams
 spring:
   cloud:
     config:
       server:
         git:
           uri: file:../../emptyRepo
           repos:
             trading:
               pattern: trading-*
               uri: file:../../trading
             pricing:
               pattern: pricing-*
               uri: file:../../pricing
             orders:
               pattern: orders-*
               uri: file:../../orders

```

We have 3 teams, trading, pricing, and orders. One repo per team responsible of a business capability.               
`curl localhost:8888/trading-execution-service/default | jq .`
`curl localhost:8888/pricing-quote-service/default | jq .`

#### Use lcoal git repo. One repo per application.
```
---
spring.profiles: git-local-one-repo-per-app
spring:
  cloud:
    config:
      server:
        git:
          uri: file:../../{application}-repo
```          

## Zuul server [Lecture]

- @EnableZuulProxy
- It automatically (no configuration required) proxies all your services registered with Eureka thru a single entry point.
e.g. When the zuul proxy receives this request http://localhost:8082/demo/hello?name=Marcial it automatically forwards this request http://localhost:8080/hello?name=Marcial
- We can configure zuul to only allow certain services regardless of the services registered in Eureka. This is done thru simple configuration.
- However, we can customize zuul. Zuul follows the idea of Servlet Filters. Every request is passed thru a number of filters and eventually the request is forwarded to destination or not. The filters allows us to intercept the requests at different stages: before the request is routed, after we receive a response from the destination service. There are special type of filters which we can use to override the routing logic.

To create a zuul server we simply create one like this:
```
@EnableZuulProxy
@SpringBootApplication
public class GatewayServiceApplication {

	Map<String, Object> basicCache = new ConcurrentHashMap<>();

	@Bean
	public ZuulFilter histogramAccess(RouteLocator routeLocator, MetricRegistry metricRegistry) {
		return new StatsCollector(routeLocator, metricRegistry);
	}
	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}
}
```
We implement our own filter which keeps track of number of requests per service:
```
class StatsCollector extends ZuulFilter {

	private static Logger log = LoggerFactory.getLogger(StatsCollector.class);
	private MetricRegistry metrics;
	private Map<String,String> serviceAliases = new HashMap<>();

	public StatsCollector(RouteLocator routeLocator, MetricRegistry registry) {
		super();
		this.metrics = registry;
		routeLocator.getRoutes().forEach(r -> {
			String alias = aliasForService(r.getLocation());
			serviceAliases.put(r.getLocation(), alias);
			metrics.counter(alias);			
		});
	}
	private String aliasForService(String name) {
		return String.format("metrics.%s.requestCount", name);
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public int filterOrder() {
		return 10;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();

		metrics.counter(serviceAliases.get((String)ctx.get("serviceId"))).inc();

		return null;
	}

}
```

And we configure it so that all requests must be prefixed with `/api` and we want to disable every eureka service except one called `securities-service` and the url is not the standard one `/api/securities-service/` but `/api/securities`.
```

zuul:
  prefix: /api
  ignored-services: '*'
  routes:
    securities-service: /securities/**
```    

## 17:00 — 17:30 RabbitMQ Deployment and Best practices  &amp; [Q&A]
