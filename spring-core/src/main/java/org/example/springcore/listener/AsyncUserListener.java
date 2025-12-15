package org.example.springcore.listener;

import org.example.springcore.event.UserCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncUserListener {

    @Async
    @EventListener
    public void sendWelcomeEmail(UserCreatedEvent event) {
        System.out.println("Sending email for user " + event.getUserId());
    }
}

