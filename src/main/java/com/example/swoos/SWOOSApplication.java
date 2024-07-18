package com.example.swoos;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SWOOSApplication {

	public static void main(String[] args) {
		SpringApplication.run(SWOOSApplication.class, args);
	}

@Bean
	public ModelMapper getModelMapper(){
		return new ModelMapper();
	}
}
