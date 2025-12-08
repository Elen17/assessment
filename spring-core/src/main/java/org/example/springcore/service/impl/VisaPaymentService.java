package org.example.springcore.service.impl;

import org.example.springcore.service.api.PaymentService;
import org.springframework.stereotype.Service;

@Service("visaPaymentService")
public class VisaPaymentService implements PaymentService {
    @Override
    public String process() {
        return "Visa payment processed!";
    }
}
