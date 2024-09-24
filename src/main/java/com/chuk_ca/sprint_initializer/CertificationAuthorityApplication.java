package com.chuk_ca.sprint_initializer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.security.Security;

@SpringBootApplication
@EnableCaching
public class CertificationAuthorityApplication {
	public static void main(String[] args) {
		// Add BouncyCastle (BC) as the security provider
		Security.addProvider(new BouncyCastleProvider());

		// Initiate the Spring Boot application
		SpringApplication.run(CertificationAuthorityApplication.class, args);
	}
}
