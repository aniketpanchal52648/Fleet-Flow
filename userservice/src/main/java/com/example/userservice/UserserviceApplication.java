package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
		"com.example", "org.example.error"
})
public class UserserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserserviceApplication.class, args);
	}

}

