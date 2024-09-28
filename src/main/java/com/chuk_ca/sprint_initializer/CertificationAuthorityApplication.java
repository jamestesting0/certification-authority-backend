package com.chuk_ca.sprint_initializer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CertificationAuthorityApplication {
	public static void main(String[] args){
		SpringApplication.run(CertificationAuthorityApplication.class, args);
	}
}
