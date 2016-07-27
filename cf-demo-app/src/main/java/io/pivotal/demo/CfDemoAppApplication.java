package io.pivotal.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
public class CfDemoAppApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(CfDemoAppApplication.class, args);
	}
}

@RestController
class MyService {
	
	@RequestMapping("/hello")
	public String hello(@RequestParam(defaultValue = "nobody") String name) {
		return "Hello " + name;
	}
}
