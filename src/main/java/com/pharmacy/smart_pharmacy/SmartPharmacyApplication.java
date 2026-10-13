package com.pharmacy.smart_pharmacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SmartPharmacyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartPharmacyApplication.class, args);
	}
}
