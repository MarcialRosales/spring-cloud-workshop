package io.pivotal.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@RefreshScope
class MyService {
	
	
	private Logger log = LoggerFactory.getLogger(CfDemoAppApplication.class);
	
	@Value("${mymessage:Hello}")
	private String mymessage;
	
	@RequestMapping("/hello")
	public String hello(@RequestParam(defaultValue = "nobody") String name) {
		log.debug("Received a hello request with name {}", name);
		return mymessage + name;
	}
}
