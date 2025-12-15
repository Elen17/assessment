package org.example.springcore.router;

import org.example.springcore.component.MyRepository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

public class MyRouter {

    public static RouterFunction<ServerResponse> router(MyRepository repository) {
        return route()
                .GET("/items", req -> ServerResponse.ok().bodyValue(repository.findAll()))
                .build();
    }
}
