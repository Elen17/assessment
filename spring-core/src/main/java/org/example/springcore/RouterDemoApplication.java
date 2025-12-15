package org.example.springcore;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Loads RegistrarConfiguration → MyBeanRegistrar
public class RouterDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RouterDemoApplication.class, args);
    }
}
