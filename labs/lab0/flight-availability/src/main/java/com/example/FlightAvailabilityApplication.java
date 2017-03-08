package com.example;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class FlightAvailabilityApplication {

	@Configuration
	@Profile("custom")
	public class DataSourceConfig {
		@Primary
	    @Bean
	    public DataSource dataSource() {
	    	System.out.println("Created custom Datasource");
	        CloudFactory cloudFactory = new CloudFactory();
	        Cloud cloud = cloudFactory.getCloud();
	        List<ServiceInfo> serviceIDs = cloud.getServiceInfos(DataSource.class);
	        return cloud.getServiceConnector(serviceIDs.get(0).getId(), DataSource.class, null);
	    }
	}
	
	@Bean
	CommandLineRunner loadFlights(FlightRepository flightRepository) {
		return args -> {
			Stream.of("MAD/GTW", "MAD/FRA", "MAD/LHR", "MAD/ACE")
					.forEach(name -> flightRepository.save(new Flight(name)));
			flightRepository.findAll().forEach(System.out::println);
		};
	}
	
	public static void main(String[] args) {
		SpringApplication.run(FlightAvailabilityApplication.class, args);
	}
}

@RestController
class FlightRepositoryController {

	@Autowired
	private FlightRepository flightRepository;

	@RequestMapping("/")
	Collection<Flight> search(@RequestParam String origin, @RequestParam String destination ) {
		return flightRepository.findByOriginAndDestination(origin, destination);
	}

}
@Repository
interface FlightRepository extends JpaRepository<Flight, Long> {

	Collection<Flight> findByOriginAndDestination(String origin, String destination);
}
