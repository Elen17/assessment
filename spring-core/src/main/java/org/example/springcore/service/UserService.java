package org.example.springcore.service;

import org.example.springcore.event.UserCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final ApplicationEventPublisher publisher;

    public UserService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void createUser(Long id) {
        publisher.publishEvent(new UserCreatedEvent(this, id));
    }
}

