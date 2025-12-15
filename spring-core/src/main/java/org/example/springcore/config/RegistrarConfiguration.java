package org.example.springcore.config;


import org.example.springcore.registrar.MyBeanRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MyBeanRegistrar.class)
public class RegistrarConfiguration {
}

