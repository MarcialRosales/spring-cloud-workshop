package io.pivotal.demo;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@EnableDiscoveryClient
@SpringBootApplication
public class SecuritiesServiceApplication {

    @Bean
    CommandLineRunner runner(SecuritiesRepository repo) {
        return args ->
                Arrays.asList("EURUSD,USD;GBPUSD,USD;CHFEUR,EUR".split(";"))
                        .forEach(s -> {
                        	String[] security = s.split(",");
                        	repo.save(new Security(security[0], security[1]));
                        });

    }
    
	public static void main(String[] args) {
		SpringApplication.run(SecuritiesServiceApplication.class, args);
	}
}


@RepositoryRestResource()
interface SecuritiesRepository extends JpaRepository<Security, Long> {
	List<Security> findBySymbol(@Param("symbol") String symbol);
}
