package io.pivotal.demo;

import static org.springframework.boot.actuate.health.Health.up;
import static org.springframework.boot.actuate.health.Health.down;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo;

@EnableDiscoveryClient
@SpringBootApplication
public class QuoteServiceApplication {


	
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(QuoteServiceApplication.class, args);
	}
}

@RestController
@Configuration
class QuoteService {

	Logger logger = org.slf4j.LoggerFactory.getLogger(QuoteService.class);
			
	@Autowired
	RestTemplate restTemplate;
	
	volatile boolean up = true;
	
	@Bean
	HealthIndicator custom() {
		return () -> up ? up().build() : down().build();
	}
	
	// How to control 
	@Bean
	HealthCheckHandler healthChecker() {
		return (s) -> up ? InstanceInfo.InstanceStatus.UP : InstanceInfo.InstanceStatus.DOWN; 
	}
	@RequestMapping(path = "/")
	List<String> symbols() {
		logger.debug("retrieving symbols");
		return restTemplate.exchange("http://securities-service/securities",
				HttpMethod.GET, null,
				new ParameterizedTypeReference<Resources<Security>>() {
				})
			.getBody()
			.getContent()
			.stream()
			.map(Security::getSymbol)
			.collect(Collectors.toList());
	}
	
	@RequestMapping(path = "down")
	void turnDown() {
		up = false;
	}
	@RequestMapping(path = "up")
	void turUp() {
		up = true;
	}
	
}
class Security {
	String symbol;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
}
