package com.klug.reactivemicroservice;

import org.springframework.boot.SpringApplication;

public class TestReactiveMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReactiveMicroserviceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
