package com.chuk_ca.sprint_initializer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;

@SpringBootApplication
@EnableCaching
public class CertificationAuthorityApplication {
	public static void main(String[] args){
		SpringApplication.run(CertificationAuthorityApplication.class, args);
	}
}
