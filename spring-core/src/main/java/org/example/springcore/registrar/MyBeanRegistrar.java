package org.example.springcore.registrar;

import org.example.springcore.component.Bar;
import org.example.springcore.component.Baz;
import org.example.springcore.component.Foo;
import org.example.springcore.component.MyRepository;
import org.example.springcore.router.MyRouter;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.server.RouterFunction;

public class MyBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {

        // Simple bean registration
        registry.registerBean("foo", Foo.class);

        // Bean with advanced configuration (prototype + lazy + description + custom supplier)
        registry.registerBean("bar", Bar.class, spec -> spec
                .prototype()
                .lazyInit()
                .description("Custom Bar bean created via BeanRegistrar")
                .supplier(ctx -> new Bar(ctx.bean(Foo.class)))
        );

        // Conditional bean
        if (env.matchesProfiles("baz")) {
            registry.registerBean(Baz.class, spec ->
                    spec.supplier(ctx -> new Baz(env.getProperty("catalog.name")))
            );
        }

        // Simple bean without name
        registry.registerBean(MyRepository.class);

        // RouterFunction bean
        registry.registerBean(RouterFunction.class, spec ->
                spec.supplier(ctx -> MyRouter.router(ctx.bean(MyRepository.class)))
        );
    }
}

