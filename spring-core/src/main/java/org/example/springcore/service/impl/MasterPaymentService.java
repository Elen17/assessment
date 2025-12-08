package org.example.springcore.service.impl;

import org.example.springcore.service.api.PaymentService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service("masterPaymentService")
@Primary
public class MasterPaymentService implements PaymentService {
    @Override
    public String process() {
        return "Mastercard payment processed!";
    }
}
