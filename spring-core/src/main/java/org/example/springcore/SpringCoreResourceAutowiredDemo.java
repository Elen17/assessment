package org.example.springcore;

import org.example.springcore.component.MovieRecommender;
import org.example.springcore.component.PaymentTester;
import org.example.springcore.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringCoreResourceAutowiredDemo {

    private PaymentTester tester;

    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        PaymentTester tester = context.getBean(PaymentTester.class);
        tester.testServices();

        MovieRecommender movie = context.getBean(MovieRecommender.class);
        movie.getRecommendations();
    }
}

