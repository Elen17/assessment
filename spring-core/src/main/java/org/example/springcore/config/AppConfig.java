package org.example.springcore.config;

import org.example.springcore.converter.MyStringToDateConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = "org.example.springcore")
public class AppConfig {

    /*
        Overriding the Conversion service
    */

    @Bean
    public ConversionService conversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new MyStringToDateConverter());
        return conversionService;
    }
}
