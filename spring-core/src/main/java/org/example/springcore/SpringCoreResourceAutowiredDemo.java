package org.example.springcore;

import org.example.springcore.component.MovieRecommender;
import org.example.springcore.component.PaymentTester;
import org.example.springcore.config.AppConfig;
import org.example.springcore.model.Company;
import org.example.springcore.model.Employee;
import org.example.springcore.service.GreetingService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Locale;

public class SpringCoreResourceAutowiredDemo {

    private PaymentTester tester;

    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        PaymentTester tester = context.getBean(PaymentTester.class);
        tester.testServices();

        MovieRecommender movie = context.getBean(MovieRecommender.class);
        movie.getRecommendations();

        GreetingService service = context.getBean(GreetingService.class);
        System.out.printf("Greeting in default locale: %s%n", service.greet(Locale.US));
        System.out.printf("Greeting in UK: %s%n", service.greet(Locale.UK));
        System.out.printf("Greeting in french: %s%n", service.greet(Locale.FRANCE));

        // Property Binding with BeanWrapper

        BeanWrapper company = new BeanWrapperImpl(new Company());
// setting the company name..
        company.setPropertyValue("name", "Some Company Inc.");
// ... can also be done like this:
        PropertyValue value = new PropertyValue("name", "Some Company Inc.");
        company.setPropertyValue(value);

// ok, let's create the director and tie it to the company:
        BeanWrapper jim = new BeanWrapperImpl(new Employee());
        jim.setPropertyValue("name", "Jim Stravinsky");
        jim.setPropertyValue("salary", 100000f);
        company.setPropertyValue("managingDirector", jim.getWrappedInstance());

// retrieving the salary of the managingDirector through the company
        Float salary = (Float) company.getPropertyValue("managingDirector.salary");

        Company finalCompany = (Company) company.getWrappedInstance();
        System.out.printf("Salary of managing director: %s%n", finalCompany.getManagingDirector().getSalary());
        System.out.printf("Company name: %s%n", finalCompany.getName());
        System.out.printf("Company employee: %s%n", finalCompany.getManagingDirector().getName());
    }
}

