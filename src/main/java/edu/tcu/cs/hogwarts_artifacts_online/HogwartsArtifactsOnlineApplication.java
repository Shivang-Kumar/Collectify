package edu.tcu.cs.hogwarts_artifacts_online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.IdWorker;

@EnableCaching
@SpringBootApplication
public class HogwartsArtifactsOnlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(HogwartsArtifactsOnlineApplication.class, args);
	}
	
	@Bean
	public IdWorker idWorker() {
		return new IdWorker(1, 1);
	}

}
