package io.pivotal.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;

import com.codahale.metrics.MetricRegistry;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

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

// curl localhost:8082/metrics | jq '.["metrics.demo.requestCount"]'
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