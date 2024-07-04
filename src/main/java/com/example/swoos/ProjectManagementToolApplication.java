package com.example.swoos;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProjectManagementToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectManagementToolApplication.class, args);
	}

@Bean
	public ModelMapper getModelMapper(){
		return new ModelMapper();
	}
}
