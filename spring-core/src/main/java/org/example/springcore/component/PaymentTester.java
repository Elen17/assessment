package org.example.springcore.component;

import jakarta.annotation.Resource;
import org.example.springcore.service.api.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PaymentTester {

    // @Resource: NAME → TYPE
    // in this case checks for a bean named "visaPaymentService"
    // if resource's name=visa was not found, Exception will be thrown (no type check)
    @Resource(/*name="visa"*/)
    private PaymentService visaPaymentService;

    // @Resource: NAME → TYPE
    // in this case checks for a bean named "visaPaymentService3" -> nothing found
    // check for primary bean
    @Resource()
    private PaymentService visaPaymentService3;

    private final PaymentService visaPaymentService2;

    private final PaymentService paymentService;

    // @Autowired: TYPE → @Primary
    // checks by type: if one bean `PaymentService` is available, inject it
    // else if multiple and one is `@Primary`, inject it
    @Autowired
    public PaymentTester(PaymentService paymentService,
                         @Qualifier("visaPaymentService") PaymentService visaPaymentService2) {
        this.visaPaymentService2 = visaPaymentService2;
        this.paymentService = paymentService;
    }

    public void testServices() {
        System.out.println("@Resource injected bean  : " + visaPaymentService.process());
        System.out.println("@Resource injected bean (no name match) : " + visaPaymentService3.process());
        System.out.println("@Autowired (@Primary in this case) injected bean : " + paymentService.process());
        System.out.println("@Autowired + Qualifier injected bean : " + visaPaymentService2.process());

        /*
            @Resource injected bean  : Visa payment processed!
            @Resource injected bean (no name match) : Mastercard payment processed!
            @Autowired (@Primary in this case) injected bean : Mastercard payment processed!
            @Autowired + Qualifier injected bean : Visa payment processed!
         */
    }
}

