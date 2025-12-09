# Spring IoC Container - Complete Guide

## Overview

The **IoC (Inversion of Control) Container** is the core of the Spring Framework. It manages the lifecycle and
configuration of application objects (beans).

---

## Table of Contents

- [What is IoC?](#what-is-ioc)
- [Bean Definition](#bean-definition)
- [Dependency Injection](#dependency-injection)
- [Lazy Initialization](#lazy-initialization)
- [Method Injection](#method-injection)
- [Bean Scopes](#bean-scopes)
- [Bean Lifecycle](#bean-lifecycle)
- [Configuration Styles](#configuration-styles)
- [ApplicationContext](#applicationcontext)
- [Interview Questions](#interview-questions)

---

## What is IoC?

**Inversion of Control** means the framework controls the flow of the program and creates objects, rather than the
developer creating objects manually.

### Traditional Approach (No IoC)

```java
public class UserService {
    private UserRepository repository = new UserRepository(); // Tight coupling

    public void saveUser(User user) {
        repository.save(user);
    }
}
```

### Spring IoC Approach

```java

@Service
public class UserService {
    private final UserRepository repository;

    @Autowired  // Spring injects dependency
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

**Benefits of IoC:**

- ✅ Loose coupling
- ✅ Easy testing (can inject mocks)
- ✅ Better maintainability
- ✅ Centralized configuration

---

## Bean Definition

A **bean** is an object managed by the Spring IoC container. A bean is an object that is instantiated, assembled, and
managed by a Spring IoC container

In addition to bean definitions that contain information on how to create a specific bean, the ApplicationContext
implementations also permit the registration of existing objects that are created outside the container (by users).
This is done by accessing the ApplicationContext’s BeanFactory through the getAutowireCapableBeanFactory() method, which
returns the DefaultListableBeanFactory implementation.
DefaultListableBeanFactory supports this registration through the registerSingleton(..) and registerBeanDefinition(..)
methods.

However, typical applications work solely with beans defined through regular bean definition metadata.

### Instantiating Beans

A bean definition is essentially a recipe for creating one or more objects.
The container looks at the recipe for a named bean when asked and uses the configuration metadata encapsulated by that
bean definition to create (or acquire) an actual object.

### Instantiation with a Static Factory Method

When defining a bean that you create with a static factory method, use the class attribute to specify the class that
contains the static factory method and an attribute named factory-method to specify the name of the factory method
itself.
You should be able to call this method (with optional arguments, as described later) and return a live object, which
subsequently is treated as if it had been created through a constructor.
One use for such a bean definition is to call static factories in legacy code.

The following bean definition specifies that the bean will be created by calling a factory method.
The definition does not specify the type (class) of the returned object, but rather the class containing the factory
method.
In this example, the createInstance() method must be a static method. The following example shows how to specify a
factory method:

```
<bean id="clientService"
class="examples.ClientService"
factory-method="createInstance"/>
```

A typical problematic case with factory method overloading is Mockito with its many overloads of the mock method. Choose
the most specific variant of mock possible:

````
<bean id="clientService" class="org.mockito.Mockito" factory-method="mock">
	<constructor-arg type="java.lang.Class" value="examples.ClientService"/>
	<constructor-arg type="java.lang.String" value="clientService"/>
</bean>
````

### Instantiation by Using an Instance Factory Method

Similar to instantiation through a static factory method , instantiation with an instance factory method invokes a
non-static method of an existing bean from the container to create a new bean. To use this mechanism, leave the class
attribute empty and, in the factory-bean attribute, specify the name of a bean in the current (or parent or ancestor)
container that contains the instance method that is to be invoked to create the object. Set the name of the factory
method itself with the factory-method attribute. The following example shows how to configure such a bean:

````xml
<!-- the factory bean, which contains a method called createClientServiceInstance() -->
<bean id="serviceLocator" class="examples.DefaultServiceLocator">
    <!-- inject any dependencies required by this locator bean -->
</bean>
````

````xml
<!-- the bean to be created via the factory bean -->
<bean id="clientService"
      factory-bean="serviceLocator"
      factory-method="createClientServiceInstance"/>
````

````java
public class DefaultServiceLocator {

    private static ClientService clientService = new ClientServiceImpl();

    public ClientService createClientServiceInstance() {
        return clientService;
    }
} 
````

### Determining a Bean’s Runtime Type

The recommended way to find out about the actual runtime type of a particular bean is a BeanFactory.getType call for the
specified bean name.

### Ways to Define Beans

#### 1. XML Configuration

```xml

<beans>
    <bean id="userService" class="com.example.UserService">
        <property name="userRepository" ref="userRepository"/>
    </bean>

    <bean id="userRepository" class="com.example.UserRepository"/>
</beans>
```

#### 2. Java Configuration (@Configuration)

```java

@Configuration
public class AppConfig {

    @Bean
    public UserService userService() {
        return new UserService(userRepository());
    }

    @Bean
    public UserRepository userRepository() {
        return new UserRepository();
    }
}
```

#### 3. Component Scanning (@Component, @Service, @Repository)

```java

@Service
public class UserService {
    // Automatically detected and registered as bean
}

@Repository
public class UserRepository {
    // Automatically detected and registered as bean
}
```

#### 4. @Bean Method

```java

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        ds.setUsername("user");
        ds.setPassword("pass");
        return ds;
    }
}
```

---

## Dependency Injection

Spring supports three types of dependency injection:

### 1. Constructor Injection (Recommended)

```java

@Service
public class OrderService {
    private final UserService userService;
    private final PaymentService paymentService;

    @Autowired  // Optional since Spring 4.3 if only one constructor
    public OrderService(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }
}
```

**Advantages:**

- ✅ Immutable (final fields)
- ✅ Required dependencies are clear
- ✅ Easy to test
- ✅ No reflection needed

### 2. Setter Injection

```java

@Service
public class EmailService {
    private TemplateEngine templateEngine;

    @Autowired
    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
}
```

**Use when:**

- Optional dependencies
- Dependencies can change at runtime

### 3. Field Injection (Not Recommended)

```java

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;  // Hard to test
}
```

**Disadvantages:**

- ❌ Cannot be final
- ❌ Hard to unit test
- ❌ Hidden dependencies

### Qualifier - Resolving Multiple Beans

```java

@Service
public class NotificationService {
    private final MessageSender sender;

    @Autowired
    public NotificationService(@Qualifier("emailSender") MessageSender sender) {
        this.sender = sender;
    }
}

@Component("emailSender")
public class EmailSender implements MessageSender {
}

@Component("smsSender")
public class SmsSender implements MessageSender {
}
```

### @Primary - Default Bean

```java

@Component
@Primary  // Use this by default
public class EmailSender implements MessageSender {
}

@Component
public class SmsSender implements MessageSender {
}
```

### depends-on

Sometimes dependencies between beans are less direct. In that cases you can use the depends-on attribute to specify a
dependency.
(beans specified in the depends-on attribute will be created before the bean that uses them).

```xml

<bean id="beanOne" class="ExampleBean" depends-on="manager,accountDao">
    <property name="manager" ref="manager"/>
</bean>

<bean id="manager" class="ManagerBean">
<bean id="accountDao" class="x.y.jdbc.JdbcAccountDao"/>
```

#### ⚠️ only for singleton beans

The depends-on attribute can specify both an initialization-time dependency and, in the case of singleton beans only, a
corresponding destruction-time dependency.
Dependent beans that define a depends-on relationship with a given bean are destroyed first, prior to the given bean
itself being destroyed. Thus, depends-on can also control shutdown order.

---

## Lazy Initialization

Spring supports lazy initialization of beans. This means that the bean is not created until the first time it is used (
first requested).

By default, ApplicationContext implementations eagerly create and configure all singleton beans as part of the
initialization process.

```java

@Bean
@Lazy
ExpensiveToCreateBean lazy() {
    return new ExpensiveToCreateBean();
}

@Bean
AnotherBean notLazy() {
    return new AnotherBean();
}
```

When a lazy-initialized bean is a dependency of a singleton bean that is not lazy-initialized, the ApplicationContext
creates the lazy-initialized bean at startup, because it must satisfy the singleton’s dependencies.

Configuration class can also be marked as lazy (all beans in the configuration class will be lazy):

```java

@Configuration
@Lazy
public class LazyConfiguration {
    // No bean will be pre-instantiated...
}
```

---

## Method Injection

Spring supports method injection for dependency injection.
Method injection is a way to inject dependencies into a bean using a method call.

In most application scenarios, most beans in the container are singletons. When a singleton bean needs to collaborate
with another singleton bean or a non-singleton bean needs to collaborate with another non-singleton bean,
you typically handle the dependency by defining one bean as a property of the other.
A problem arises when the bean lifecycles are different. Suppose singleton bean A needs to use non-singleton (prototype)
bean B, perhaps on each method invocation on A.
The container creates the singleton bean A only once, and thus only gets one opportunity to set the properties. The
container cannot provide bean A with a new instance of bean B every time one is needed.

```java
package fiona.apple;

// Spring-API imports

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A class that uses a stateful Command-style class to perform
 * some processing.
 */
public class CommandManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public Object process(Map commandState) {
        // grab a new instance of the appropriate Command
        Command command = createCommand();
        // set the state on the (hopefully brand new) Command instance
        command.setState(commandState);
        return command.execute();
    }

    protected Command createCommand() {
        // notice the Spring API dependency!
        return this.applicationContext.getBean("command", Command.class);
    }

    public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
```   

When a **singleton** bean has a dependency on a **prototype** (or any non-singleton) bean in Spring, it creates a very
important lifecycle/scope problem.

Below is a clear explanation of what happens, why it’s a problem, and what patterns Spring provides to solve it.

---

# ✅ **What Happens: Singleton → Prototype Dependency**

A **singleton bean is created only once**, at application startup.

During its creation, Spring resolves and injects all dependencies **once** — including prototype beans.

So:

```text
singletonBean ----inject----> prototypeBean  (only once)
```

This means:

* Even though `prototype` scope means *“a new instance each time it’s needed”*,
* The singleton gets **only one instance** at startup,
* And **keeps using the same prototype instance forever**.

➡️ **Prototype loses its meaning** in this injection scenario.

---

# ⚠️ Why This Is a Problem

Prototype beans are typically used when you want:

* new state per use
* new object every time
* short-lived objects
* different behavior per call

But dependency injection works at startup → so the singleton ends up with:

* a *single* prototype instance
* shared across all uses
* violating prototype's intended semantics

---

# 📌 Example: Singleton Injects Prototype

```java

@Component
public class SingletonBean {

    @Autowired
    private PrototypeBean prototypeBean;

    public void doWork() {
        System.out.println(prototypeBean.hashCode());
    }
}
```

Calling `doWork()` 10 times prints the **same hashCode** → same instance.

---

# ✅ How To Correctly Inject Prototype Into Singleton

Spring provides **three solutions**.

---

## **1️⃣ Method Injection (@Lookup)** → recommended for simple cases

Spring dynamically overrides this method to return a **new prototype instance each call**.

```java

@Component
public abstract class SingletonBean {

    public void doWork() {
        PrototypeBean p = getPrototypeBean();
        System.out.println(p.hashCode());
    }

    @Lookup
    protected abstract PrototypeBean getPrototypeBean();
}
```

---

## **2️⃣ ObjectFactory / ObjectProvider**

Lightweight factory for retrieving fresh beans:

```java

@Component
public class SingletonBean {

    @Autowired
    private ObjectProvider<PrototypeBean> provider;

    public void doWork() {
        PrototypeBean p = provider.getObject(); // new instance every call
    }
}
```

---

## **3️⃣ Provider (JSR-330)**

From `javax.inject`:

```java

@Component
public class SingletonBean {

    @Inject
    private Provider<PrototypeBean> provider;

    public void doWork() {
        PrototypeBean p = provider.get();
    }
}
```

Both ObjectProvider and Provider are clean, thread-safe, and recommended.

---

# ❌ What *Not* To Do

### Don’t directly inject prototype bean into singleton:

```java

@Autowired
private PrototypeBean prototype; // WRONG if you expect new instances
```

It gives you **one prototype instance**, and the singleton uses it forever.

---

# 🚨 Edge Case: Other Non-Singleton Scopes

| Singleton Depends On | Problem?  | Why                                               |
|----------------------|-----------|---------------------------------------------------|
| **prototype**        | ❌ Yes     | loses new-instance behavior                       |
| **request**          | ❌ Yes     | singleton created before any request scope exists |
| **session**          | ❌ Yes     | same as above                                     |
| **application**      | ❌ No      | both are singletons                               |
| **custom scopes**    | ❌ Usually | lifecycle mismatch                                |

If a singleton depends on request/session scoped beans → **Spring will throw an error unless you use scoped proxies** (
`proxyMode = TARGET_CLASS`).

---

# 🧠 Why Does Spring Not “Automatically” Fix It?

Because Spring must follow **dependency injection rules**:

* Singleton created once → dependencies resolved once
* Spring does not create new dependencies each time a method is called
* To get dynamic behavior, you must explicitly request it (lookup, provider, proxy)

---

# 🔥 Summary

### **A singleton depending directly on a prototype bean is a broken design — the singleton will NOT get a new prototype

each time.**

To fix it:

✔ Use `@Lookup`
✔ Use `ObjectProvider`
✔ Use `Provider`
✔ Use `@Scope(proxyMode = ...)` to lazily resolve scoped beans

---

Great — here are the extended explanations and examples you might want (based on your “yeap”).

I’ll cover **all the important edge cases**:

# ✅ 1. How Scoped Proxies Fix "Singleton → Request/Session" Dependencies

A **singleton cannot depend directly on request or session scoped beans**, because those scopes do not exist at
application startup.
Spring solves this via **scoped proxies**.

Example:

```java

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {
    // new instance per HTTP request
}
```

Now when a singleton injects it:

```java

@Component
public class SingletonBean {
    @Autowired
    private RequestScopedBean requestBean;
}
```

What actually gets injected is:

* a **CGLIB proxy**
* stored in the singleton
* the proxy internally fetches the real request-scoped bean **on each request**

This fixes lifecycle mismatch.

---

# ✅ 2. How Proxies Affect AOP, Transactions, and Auto-Proxying

### **If a bean is not proxied when Spring expects it to be, AOP will NOT work.**

Typical examples:

### Case 1 — Prototype → Transactional

```java

@Component
@Scope("prototype")
@Transactional
public class MyPrototype {
}
```

This will **NOT** work reliably:

* Spring applies transaction proxies during bean creation.
* Prototype beans are created *every time you call getBean()*.
* So transaction proxying must happen **each time**, but Spring does not do that automatically.

→ Solution: prototype must be retrieved through ObjectProvider or @Lookup so Spring can wrap it properly.

---

### Case 2 — Singleton receives prototype before proxying is applied

This is tied to what you asked earlier:

> “…which may result in injecting unexpected beans, affecting their eligibility for post-processing like auto-proxying.”

This happens when:

* A prototype bean is injected into a singleton **early**
* BeanPostProcessors (such as AOP proxy creators) run **afterward**
* That prototype bean is now **already fully created** and injected unproxied
* So **Spring cannot replace it with a proxy anymore**

Result:
Transaction, security, caching, aspects → **won’t work** for that bean.

---

# 🧪 Example: Wrong injection – prototype not proxied

```java

@Component
@Scope("prototype")
public class Worker {
    @Transactional
    public void process() {
    }
}
```

```java

@Component
public class Job {
    @Autowired
    private Worker worker;
}
```

What happens:

1. Worker created (non-proxied)
2. Injected into Job (singleton)
3. THEN proxying is attempted → Too late!
4. `@Transactional` does nothing → because the proxy is missing

---

# 🛠 Correct way: Use `ObjectProvider`

```java

@Component
public class Job {

    @Autowired
    private ObjectProvider<Worker> workerProvider;

    public void run() {
        Worker w = workerProvider.getObject();  // freshly created + proxied
        w.process();  // transactions now work
    }
}
```

This forces Spring to:

* create the bean *on demand*
* apply BPPs (including auto-proxying)
* return the fully wrapped bean to you

---

# 🛠 Correct way: Use `@Lookup`

```java

@Component
public abstract class Job {

    public void run() {
        Worker w = newWorker();
        w.process();
    }

    @Lookup
    protected abstract Worker newWorker();
}
```

---

# 🛠 Correct way: Use Scoped Proxy (for Web scopes)

```java

@Component
@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
public class RequestScopedService {
}

@Component
public class Controller {
    @Autowired
    private RequestScopedService service; // injected as proxy
}
```

---

# ✅ 3. Performance Considerations

### ObjectProvider

* Lightweight
* Zero performance penalty
* Recommended for prototype beans

### @Lookup

* Uses method overriding through CGLIB
* Slightly slower
* More “magic”, less explicit

### Scoped Proxies

* Perfect for web scopes
* Slight overhead due to proxying, but negligible

---

# ⭐ Final Summary

**A singleton bean must NOT directly depend on a prototype or request/session scoped bean**, because:

* the dependency is resolved **once**
* post-processing (AOP, proxying) may not be applied
* lifecycle boundaries are violated

### Use these instead:

| Problem                                     | Correct Solution                            |
|---------------------------------------------|---------------------------------------------|
| Singleton → Prototype                       | `ObjectProvider`, `Provider`, `@Lookup`     |
| Singleton → Request/Session                 | Scoped proxy (`proxyMode = TARGET_CLASS`)   |
| Need transactions/security inside prototype | Must be created on demand (provider/lookup) |
| Bean loses AOP proxy                        | Inject via provider, not field injection    |

---

## Bean Scopes

Spring provides several bean scopes:

### 1. Singleton (Default)

```java

@Service
@Scope("singleton")  // Optional, this is default
public class UserService {
    // Single instance per Spring container
}
```

![img_1.png](img_1.png)

**Characteristics:**

- One instance per ApplicationContext (multiple instances id multiple containers)
- Shared across all requests
- Stateless preferred

### 2. Prototype

```java

@Service
@Scope("prototype")
public class ShoppingCart {
    // New instance each time requested
}
```

![img_2.png](img_2.png)

**Use when:**

- Stateful beans
- Each client needs separate instance
- In contrast to the other scopes, Spring does not manage the complete lifecycle of a prototype bean. The container
  instantiates, configures, and otherwise assembles a prototype object and hands it to the client, with no further
  record of that prototype instance.(@PreDestroy is not called for prototype beans)
- The client code must clean up prototype-scoped objects and release expensive resources that the prototype beans hold.
- You can use a custom bean post-processor which holds a reference to beans that need to be cleaned up

####

Singleton Beans with Prototype-bean Dependencies
When you use singleton-scoped beans with dependencies on prototype beans, be aware that dependencies are resolved at
instantiation time.
Thus, if you dependency-inject a prototype-scoped bean into a singleton-scoped bean, a new prototype bean is
instantiated and then dependency-injected into the singleton bean.
The prototype instance is the sole instance that is ever supplied to the singleton-scoped bean.

However, suppose you want the singleton-scoped bean to acquire a new instance of the prototype-scoped bean repeatedly at
runtime.
You cannot dependency-inject a prototype-scoped bean into your singleton bean, because that injection occurs only once,
when the Spring container instantiates the singleton bean and resolves and injects its dependencies. (See
the [Method Injection](#method-injection) section for more information.)

### Request, Session, Application, and WebSocket Scopes

The request, session, application, and websocket scopes are available only if you use a web-aware Spring
ApplicationContext implementation (such as XmlWebApplicationContext).
If you use these scopes with regular Spring IoC containers, such as the ClassPathXmlApplicationContext,
an IllegalStateException that complains about an unknown bean scope is thrown.

If you access scoped beans within Spring Web MVC, in effect, within a request that is processed by the Spring
DispatcherServlet, no special setup is necessary. DispatcherServlet already exposes all relevant state.

If you use a Servlet web container, with requests processed outside of Spring’s DispatcherServlet (for example, when
using JSF),
you need to register the org.springframework.web.context.request.RequestContextListener ServletRequestListener.
This can be done programmatically by using the WebApplicationInitializer interface.
Alternatively, add the following declaration to your web application’s web.xml file:

```xml

<web-app>
    ...
    <listener>
        <listener-class>
            org.springframework.web.context.request.RequestContextListener
        </listener-class>
    </listener>
    ...
</web-app>
```

Alternatively, if there are issues with your listener setup, consider using Spring’s RequestContextFilter. The filter
mapping depends on the surrounding web application configuration, so you have to change it as appropriate.
The following listing shows the filter part of a web application:

```xml

<web-app>
    ...
    <filter>
        <filter-name>requestContextFilter</filter-name>
        <filter-class>org.springframework.web.filter.RequestContextFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>requestContextFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ...
</web-app>
```

DispatcherServlet, RequestContextListener, and RequestContextFilter all do exactly the same thing, namely bind the HTTP
request object to the Thread that is servicing that request.
This makes beans that are request- and session-scoped available further down the call chain.

#### 3. Request (Web Applications)

```java

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginForm {
    // New instance per HTTP request
}
```

#### 4. Session (Web Applications)

```java

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession {
    // New instance per HTTP session
}
```

#### 5. Application (Web Applications)

```java

@Component
@Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppCache {
    // Single instance per ServletContext
}
```

The Spring container creates a new instance of the AppPreferences bean by using the appPreferences bean definition once
for the entire web application.
That is, the appPreferences bean is scoped at the ServletContext level and stored as a regular ServletContext attribute.
This is somewhat similar to a Spring singleton bean but differs in two important ways:

- It is a singleton per ServletContext, not per Spring ApplicationContext (for which there may be several in any given
  web application), and it is actually exposed and therefore visible as a ServletContext attribute.

### Custom Scope

```java
public class TenantScope implements Scope {
    // Custom scope implementation
}
```

You should register your custom scope with the Spring container

1. xml approach
    ```xml
    
    <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="thread">
                    <bean class="org.springframework.context.support.SimpleThreadScope"/>
                </entry>
            </map>
        </property>
    </bean>
    ```

2. java approach

    ```java
    Scope threadScope = new SimpleThreadScope();
    beanFactory.registerScope("thread",threadScope);
    ```

📝 When you place <aop:scoped-proxy/> within a <bean> declaration for a FactoryBean implementation,
it is the factory bean itself that is scoped, not the object returned from getObject().

### Customizing the Nature of a Bean

* Lifecycle Callbacks

* ApplicationContextAware and BeanNameAware

* Other Aware Interfaces

#### Lifecycle Callbacks

[💡Tip]
The JSR-250 @PostConstruct and @PreDestroy annotations are generally considered best practice for receiving lifecycle
callbacks in a modern Spring application.
Using these annotations means that your beans are not coupled to Spring-specific interfaces.
For details, see Using @PostConstruct and @PreDestroy.
If you do not want to use the JSR-250 annotations, but you still want to remove coupling,
consider init-method and destroy-method bean definition metadata.

The Spring Framework uses BeanPostProcessor implementations to process any callback interfaces it can find and call the
appropriate methods.

##### Initialization Callbacks

The org.springframework.beans.factory.InitializingBean interface lets a bean perform initialization work after the
container has set all necessary properties on the bean.

```java
void afterPropertiesSet() throws Exception;
```

```note
[💡Tip]
We recommend that you do not use the InitializingBean interface, because it unnecessarily couples the code to Spring.
Alternatively, we suggest using the @PostConstruct annotation or specifying a POJO initialization method. In the case of XML-based configuration metadata,
you can use the init-method attribute to specify the name of the method that has a void no-argument signature.

Be aware that @PostConstruct and initialization methods in general are executed within the container’s singleton creation lock.
The bean instance is only considered as fully initialized and ready to be published to others after returning from the @PostConstruct method.
Such individual initialization methods are only meant for validating the configuration state and possibly preparing some data structures based on the given configuration but no further activity with external bean access.
Otherwise there is a risk for an initialization deadlock.

For a scenario where expensive post-initialization activity is to be triggered, for example, asynchronous database preparation steps, your bean should either implement SmartInitializingSingleton.afterSingletonsInstantiated()
or rely on the context refresh event: implementing ApplicationListener<ContextRefreshedEvent> or declaring its annotation equivalent @EventListener(ContextRefreshedEvent.class).
Those variants come after all regular singleton initialization and therefore outside of any singleton creation lock.

Alternatively, you may implement the (Smart)Lifecycle interface and integrate with the container’s overall lifecycle management,
including an auto-startup mechanism, a pre-destroy stop step, and potential stop/restart callbacks (see below).
```

---

## 🧩 The Problem Being Solved

Sometimes a Spring bean needs to:

✔ Trigger **post-initialization** logic
✔ Only after **all** Spring singletons are created
✔ **Outside** the bean-creation lock (so async tasks don’t block startup)

Examples:

* Async DB preparation
* Caches warming
* Scheduling background jobs
* External service sync

To do that, Spring provides **three proper hooks** 👇

---

## ✔ Option 1 — `SmartInitializingSingleton.afterSingletonsInstantiated()`

Called **after all singletons are created**, before app fully ready.
Perfect for async initialization tasks.

```java
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

@Component
public class CacheInitializer implements SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
        System.out.println("Running async setup...");
        // Start expensive tasks here
    }
}
```

🔹 No dependency on ApplicationContext
🔹 Guaranteed to run **once**
🔹 Runs **before** ContextRefreshedEvent

---

## ✔ Option 2 — `ApplicationListener<ContextRefreshedEvent>`

Runs when the Spring context is **fully** refreshed and ready.

```java
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("App is fully initialized!");
    }
}
```

Or shorter with annotations:

```java

@Component
public class StartupEventHandler {

    @EventListener(ContextRefreshedEvent.class)
    public void onRefresh() {
        System.out.println("App started — run init jobs here");
    }
}
```

---

## ✔ Option 3 — `Lifecycle` / `SmartLifecycle`

Good when the bean needs:
✔ Auto-start behavior
✔ Graceful stop on shutdown
✔ Restart capabilities

```java
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class MyLifecycleBean implements SmartLifecycle {

    private boolean running = false;

    @Override
    public void start() {
        running = true;
        System.out.println("Lifecycle start — job scheduling begins");
    }

    @Override
    public void stop() {
        running = false;
        System.out.println("Lifecycle stop — cleanup here");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 0; // Lower = start earlier
    }

    @Override
    public boolean isAutoStartup() {
        return true; // start automatically
    }
}
```

This one participates in Spring’s **full lifecycle management**.

---

## 🧭 Which Should You Use?

| Need                                           | Best Choice                  |
|------------------------------------------------|------------------------------|
| Trigger async init after all beans are created | `SmartInitializingSingleton` |
| Run when app is 100% ready                     | `ContextRefreshedEvent`      |
| Needs graceful stop/restart & auto startup     | `SmartLifecycle`             |

---

## 🔥 Quick Recommendation for Your Case

> "Async database preparation after startup"

✔ `SmartInitializingSingleton` is the cleanest
✔ Put async execution inside a separate thread / executor

---

Examples:

```java
// config/StartupInitializer.java
package com.example.project.config;

import com.example.project.service.DatabaseWarmupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupInitializer implements SmartInitializingSingleton {

    private final DatabaseWarmupService warmupService;

    @Override
    public void afterSingletonsInstantiated() {
        log.info("Application started — running warmup tasks asynchronously...");
        warmupService.prepareDatabaseAsync();
    }
}
```

```java
// service/DatabaseWarmupService.java
package com.example.project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DatabaseWarmupService {

    @Async
    public void prepareDatabaseAsync() {
        try {
            log.info("Starting DB warmup...");
            Thread.sleep(3000); // simulate heavy work
            log.info("DB warmup done!");
        } catch (Exception ex) {
            log.error("Warmup failed: {}", ex.getMessage());
        }
    }
}
```

```java

@EnableAsync
@SpringBootApplication
public class App {
}
```

```java

@Component
@Slf4j
public class AppStartupListener {

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationReady() {
        log.info("App fully initialized — starting message consumers...");
        // consumer.start();
    }
}
```

```java

@Component
@Slf4j
public class ConsumerLifecycleManager implements SmartLifecycle {

    private boolean running = false;

    @Override
    public void start() {
        running = true;
        log.info("Starting consumers...");
    }

    @Override
    public void stop() {
        running = false;
        log.info("Stopping consumers gracefully...");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return 0; // lower = starts earlier
    }
}
```

----

---

# 🚦 Comparison: `SmartInitializingSingleton` vs `@EventListener(ContextRefreshedEvent)` vs `SmartLifecycle`

| Feature / Behavior                    | **SmartInitializingSingleton**                            | **@EventListener(ContextRefreshedEvent)**                                | **SmartLifecycle**                         |
|---------------------------------------|-----------------------------------------------------------|--------------------------------------------------------------------------|--------------------------------------------|
| When triggered                        | After all singleton beans are created (BeanFactory stage) | After ApplicationContext **fully refreshed** (Servlet container started) | During application lifecycle startup phase |
| Runs before REST endpoints are ready? | ❌ REST may not be ready yet                               | ✔ REST + security + context ready                                        | ✔ Full system ready                        |
| Supports graceful stop?               | ❌ No                                                      | ❌ No                                                                     | ✔ Yes (stop callbacks)                     |
| Auto restart on context refresh?      | ❌ No                                                      | ✔ Yes                                                                    | ✔ Yes                                      |
| Use async?                            | Recommended if heavy                                      | Optional                                                                 | Not necessary                              |
| Purpose                               | Data warmup, internal init                                | Consumer start, logging, metrics                                         | Background services, schedulers            |
| Reacts to events?                     | ❌                                                         | ✔ can listen to multiple events                                          | ✔ lifecycle callbacks                      |
| Order control                         | Hard                                                      | Medium                                                                   | Full control via `getPhase()`              |
| Stops on shutdown?                    | No callback                                               | No callback                                                              | ✔ Supports clean shutdown                  |

---

## 🎯 When to use which — Practical rule of thumb

| Scenario                                                                    | Best Choice                    | Why                                    |
|-----------------------------------------------------------------------------|--------------------------------|----------------------------------------|
| Initialize caches, load reference data — **non-blocking**                   | `SmartInitializingSingleton`   | Runs early but async prevents blocking |
| Start message consumers (Kafka/Rabbit), metrics reporters                   | `ContextRefreshedEvent`        | Waits for app to be fully ready        |
| Long-running background tasks (schedulars, socket listeners, file watchers) | `SmartLifecycle`               | Ensures proper start/stop control      |
| Start logic after DB migrations (Flyway/Liquibase) complete                 | `ContextRefreshedEvent`        | Comes after datasource + migrations    |
| Need multiple ordered startup components                                    | `SmartLifecycle`               | `getPhase()` controls order            |
| Need restart on context refresh                                             | Event listener or lifecycle    | Both handle reinitialization           |
| Must finish before app becomes ready                                        | ✘ none async → not recommended | Instead block in `ApplicationRunner`   |

---

## 💣 What NOT to do

| Anti-pattern                                                       | Why it’s bad                          |
|--------------------------------------------------------------------|---------------------------------------|
| Heavy logic inside `SmartInitializingSingleton` **without @Async** | Blocks bean creation → startup freeze |
| Running consumers before context refresh                           | Internal services might not be ready  |
| No lifecycle control for long-running loops                        | You can't stop them gracefully        |

---

# 🧩 Simple Decision Flow

```
Do you need to stop/start it gracefully?
       | Yes → SmartLifecycle
       |
       No ↓
Is it heavy and should run async without blocking?
       | Yes → SmartInitializingSingleton + @Async
       |
       No ↓
Does it require the full app to be ready (web, security, flyway)?
       | Yes → @EventListener(ContextRefreshedEvent)
       | No → SmartInitializingSingleton
```

---

## 📌 Example real usage mapping to your domain

| Feature in your project                   | Mechanism                               | Reason                                  |
|-------------------------------------------|-----------------------------------------|-----------------------------------------|
| Preload `ProjectModule` metadata from DB  | `SmartInitializingSingleton`            | Heavy but internal                      |
| Start RabbitMQ message listener           | `@EventListener(ContextRefreshedEvent)` | Must wait until business services ready |
| Continuous schedule for DB cleanup        | `SmartLifecycle`                        | Needs controlled shutdown               |
| Register integration channels dynamically | `SmartLifecycle`                        | Start/stop + system phases matter       |

---

## 🚀 Final takeaway

> If the task starts a **continuous process** → use **SmartLifecycle**
> If the task must run when **everything is ready** → use **ContextRefreshedEvent**
> If the task must run only **once after bean creation** → use **SmartInitializingSingleton**
----

```note
The Spring container guarantees that a configured initialization callback is called immediately after a bean is supplied with all dependencies.
Thus, the initialization callback is called on the raw bean reference, which means that AOP interceptors and so forth are not yet applied to the bean.
A target bean is fully created first and then an AOP proxy (for example) with its interceptor chain is applied.
```

----

#### Combining Lifecycle Mechanisms

As of Spring 2.5, you have three options for controlling bean lifecycle behavior:

    * The InitializingBean and DisposableBean callback interfaces
    * Custom init() and destroy() methods (bean init-method and destroy-method metadata)
    * The @PostConstruct and @PreDestroy annotations

You can combine these mechanisms to control a given bean.

``` note
If multiple lifecycle mechanisms are configured for a bean and each mechanism is configured with a different method name, 
then each configured method is run in the order listed after this note. 
However, if the same method name is configured — for example, init() for an initialization method — for more than one of these lifecycle mechanisms,
 that method is run once, as explained in the preceding section.
 ```

Multiple lifecycle mechanisms configured for the same bean, with different initialization methods, are called as
follows:

1) Methods annotated with @PostConstruct

2) afterPropertiesSet() as defined by the InitializingBean callback interface

3) A custom configured init() method

Destroy methods are called in the same order:

1) Methods annotated with @PreDestroy

2) destroy() as defined by the DisposableBean callback interface

3) A custom configured destroy() method

----

#### Aware Interfaces

Spring's "Aware" interfaces provide a mechanism for Spring-managed beans to interact with the Spring container and
access specific framework objects or resources.
These interfaces are part of the Spring bean lifecycle and are typically implemented by a bean that requires access to a
particular Spring infrastructure object.
Besides ApplicationContextAware and BeanNameAware (discussed earlier), Spring offers a wide range of Aware callback
interfaces that let beans indicate to the container that they require a certain infrastructure dependency.
As a general rule, the name indicates the dependency type. The following table summarizes the most important Aware
interfaces:

![img_3.png](img_3.png)

[NOTE]

Note again that using these interfaces ties your code to the Spring API and does not follow the IoC style.
As a result, we recommend them for infrastructure beans that require programmatic access to the container.

![img_4.png](img_4.png)
https://springframework.guru/using-spring-aware-interfaces/

https://www.baeldung.com/spring-beanfactory-vs-applicationcontext

### Bean Definition Inheritance

A bean definition can contain a lot of configuration information, including constructor arguments, property values, and
container-specific information,
such as the initialization method, a static factory method name, and so on.
A child bean definition inherits configuration data from a parent definition.
The child definition can override some values or add others as needed.
Using parent and child bean definitions can save a lot of typing.
Effectively, this is a form of templating.

```xml

<bean id="inheritedTestBean" abstract="true"
      class="org.springframework.beans.TestBean">
    <property name="name" value="parent"/>
    <property name="age" value="1"/>
</bean>

<bean id="inheritsWithDifferentClass"
      class="org.springframework.beans.DerivedTestBean"
      parent="inheritedTestBean" init-method="initialize">
<property name="name" value="override"/>
<!-- the age property value of 1 will be inherited from parent -->
</bean>
```

A child bean definition inherits scope, constructor argument values, property values, and method overrides from the
parent, with the option to add new values.
Any scope, initialization method, destroy method, or static factory method settings that you specify override the
corresponding parent settings.

The parent bean cannot be instantiated on its own because it is incomplete, and it is also explicitly marked as
abstract.

[NOTE]

Always add an abstract attribute to the parent bean definition to prevent its instantiation, as
ApplicationContext pre-instantiates all singletons by default.
Therefore, it is important (at least for singleton beans) that if you have a (parent) bean definition which you intend
to use only as a template,
and this definition specifies a class, you must make sure to set the abstract attribute to true,
otherwise the application context will actually (attempt to) pre-instantiate the abstract bean.

----

### Container Extension Points

#### Customizing Beans by Using a BeanPostProcessor

[NOTE]

BeanPostProcessor instances operate on bean (or object) instances. That is, the Spring IoC container instantiates a bean
instance and then BeanPostProcessor instances do their work.
BeanPostProcessor instances are scoped per-container. This is relevant only if you use container hierarchies.
If you define a BeanPostProcessor in one container, it post-processes only the beans in that container.
In other words, beans that are defined in one container are not post-processed by a BeanPostProcessor defined in another
container, even if both containers are part of the same hierarchy.
To change the actual bean definition (that is, the blueprint that defines the bean), you instead need to use a
BeanFactoryPostProcessor, as described in Customizing Configuration Metadata with a BeanFactoryPostProcessor.

[NOTE]
Note that, when declaring a BeanPostProcessor by using an @Bean factory method on a configuration class,
the return type of the factory method should be the implementation class itself or at least the
org.springframework.beans.factory.config.BeanPostProcessor interface,
clearly indicating the post-processor nature of that bean.
Otherwise, the ApplicationContext cannot autodetect it by type before fully creating it.
Since a BeanPostProcessor needs to be instantiated early in order to apply to the initialization of other beans in the
context,
this early type detection is critical.

Programmatically registering BeanPostProcessor instances
While the recommended approach for BeanPostProcessor registration is through ApplicationContext auto-detection (as
described earlier),
you can register them programmatically against a ConfigurableBeanFactory by using the addBeanPostProcessor method.
This can be useful when you need to evaluate conditional logic before registration or even for copying bean post
processors across contexts in a hierarchy.
Note, however, that BeanPostProcessor instances added programmatically do not respect the Ordered interface.
Here, it is the order of registration that dictates the order of execution.
Note also that BeanPostProcessor instances registered programmatically are always processed before those registered
through auto-detection,
regardless of any explicit ordering.

##### Example: Hello World, BeanPostProcessor-style

```java
package scripting;

import org.springframework.beans.factory.config.BeanPostProcessor;

public class InstantiationTracingBeanPostProcessor implements BeanPostProcessor {

    // simply return the instantiated bean as-is
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean; // we could potentially return any object reference here...
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("Bean '" + beanName + "' created : " + bean.toString());
        return bean;
    }
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:lang="http://www.springframework.org/schema/lang"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/lang
		https://www.springframework.org/schema/lang/spring-lang.xsd">

    <lang:groovy id="messenger"
                 script-source="classpath:org/springframework/scripting/groovy/Messenger.groovy">
        <lang:property name="message" value="Fiona Apple Is Just So Dreamy."/>
    </lang:groovy>

    <!--
    when the above bean (messenger) is instantiated, this custom
    BeanPostProcessor implementation will output the fact to the system console
    -->
    <bean class="scripting.InstantiationTracingBeanPostProcessor"/>

</beans>
```

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scripting.Messenger;

public final class Boot {

    public static void main(final String[] args) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("scripting/beans.xml");
        Messenger messenger = ctx.getBean("messenger", Messenger.class);
        System.out.println(messenger);
    }

}
```

```output
    Bean 'messenger' created : org.springframework.scripting.groovy.GroovyMessenger@272961
    org.springframework.scripting.groovy.GroovyMessenger@272961
```

---

#### Customizing Configuration Metadata with a BeanFactoryPostProcessor

The next extension point that we look at is the org.springframework.beans.factory.config.BeanFactoryPostProcessor.
The semantics of this interface are similar to those of the BeanPostProcessor, with one major difference:
BeanFactoryPostProcessor operates on the bean configuration metadata.
That is, the Spring IoC container lets a BeanFactoryPostProcessor read the configuration metadata and potentially change
it
before the container instantiates any beans other than BeanFactoryPostProcessor instances.

You can configure multiple BeanFactoryPostProcessor instances, and you can control the order in which these
BeanFactoryPostProcessor
instances run by setting the order property.
However, you can only set this property if the BeanFactoryPostProcessor implements the Ordered interface.
If you write your own BeanFactoryPostProcessor, you should consider implementing the Ordered interface, too.

[NOTE]

When you need to ask a container for an actual FactoryBean instance itself instead of the bean it produces,
prefix the bean’s id with the ampersand symbol (&) when calling the getBean() method of the ApplicationContext.
So, for a given FactoryBean with an id of myBean, invoking getBean("myBean") on the container returns the product of the
FactoryBean,
whereas invoking getBean("&myBean") returns the FactoryBean instance itself.

[NOTE]

A bean factory post-processor is automatically run when it is declared inside an ApplicationContext, in order to apply
changes to the configuration metadata that define the container.
Spring includes a number of predefined bean factory post-processors, such as PropertyOverrideConfigurer and
PropertySourcesPlaceholderConfigurer. You can also use a custom BeanFactoryPostProcessor — for example, to register
custom property editors.

[NOTE]

As with BeanPostProcessors , you typically do not want to configure BeanFactoryPostProcessors for lazy initialization.
If no other bean references a Bean(Factory)PostProcessor, that post-processor will not get instantiated at all. Thus,
marking it for lazy initialization
will be ignored, and the Bean(Factory)PostProcessor will be instantiated eagerly even if you set the default-lazy-init
attribute to true on the declaration of your <beans /> element.

#### Example: Property Placeholder Substitution with PropertySourcesPlaceholderConfigurer

```xml

<bean class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">
    <property name="locations" value="classpath:com/something/jdbc.properties"/>
</bean>

<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
<property name="driverClassName" value="${jdbc.driverClassName}"/>
<property name="url" value="${jdbc.url}"/>
<property name="username" value="${jdbc.username}"/>
<property name="password" value="${jdbc.password}"/>
</bean>
```

Therefore, the ${jdbc.username} string is replaced at runtime with the value, 'sa', and the same applies for other
placeholder
values that match keys in the properties file. The PropertySourcesPlaceholderConfigurer checks for placeholders in most
properties
and attributes of a bean definition. Furthermore, you can customize the placeholder prefix, suffix, default value
separator, and escape character.
In addition, the default escape character can be changed or disabled globally by setting the
spring.placeholder.escapeCharacter.default property via a JVM system property
(or via the SpringProperties mechanism).

With the context namespace, you can configure property placeholders with a dedicated configuration element. You can
provide one or more locations as a comma-separated list in the location attribute, as the following example shows:

<context:property-placeholder location="classpath:com/something/jdbc.properties"/>

The PropertySourcesPlaceholderConfigurer not only looks for properties in the Properties file you specify.
By default, if it cannot find a property in the specified properties files, it checks against Spring Environment
properties and regular Java System properties.

[WARNING]

Only one such element should be defined for a given application with the properties that it needs. Several property
placeholders can be configured as long as they have distinct placeholder syntax (${…​}).
If you need to modularize the source of properties used for the replacement, you should not create multiple properties
placeholders. Rather, you should create your own
PropertySourcesPlaceholderConfigurer bean that gathers the properties to use.

#### Example: The PropertyOverrideConfigurer

The PropertyOverrideConfigurer, another bean factory post-processor, resembles the PropertySourcesPlaceholderConfigurer,
but unlike the latter,
the original definitions can have default values or no values at all for bean properties.
If an overriding Properties file does not have an entry for a certain bean property, the default context definition is
used.

Properties file configuration lines take the following format:

beanName.property=value

The following listing shows an example of the format:

dataSource.driverClassName=com.mysql.jdbc.Driver
dataSource.url=jdbc:mysql:mydb

```xml

<context:property-override location="classpath:override.properties"/>
```

#### Customizing Instantiation Logic with a FactoryBean

You can implement the org.springframework.beans.factory.FactoryBean interface for objects that are themselves factories.

The FactoryBean interface is a point of pluggability into the Spring IoC container’s instantiation logic. If you have
complex initialization code that is better
expressed in Java as opposed to a (potentially) verbose amount of XML,
you can create your own FactoryBean, write the complex initialization inside that class, and then plug your custom
FactoryBean into the container.

The FactoryBean<T> interface provides three methods:

1. T getObject(): Returns an instance of the object this factory creates. The instance can possibly be shared, depending
   on whether this factory returns singletons or prototypes.

2. boolean isSingleton(): Returns true if this FactoryBean returns singletons or false otherwise. The default
   implementation of this method returns true.

3. Class<?> getObjectType(): Returns the object type returned by the getObject() method or null if the type is not known
   in advance.

[NOTE]

context.getBean("&myBean") returns the FactoryBean instance itself.

----

### Annotation-based Container Configuration

Spring provides comprehensive support for annotation-based configuration, operating on metadata in the component class
itself by using annotations on the relevant class,
method, or field declaration. As mentioned in Example: The AutowiredAnnotationBeanPostProcessor,
Spring uses BeanPostProcessors in conjunction with annotations to make the core IOC container aware of specific
annotations.

[NOTE]

** Annotation configurations are overridden by XML configurations **

Annotation injection is performed before external property injection. Thus, external configuration (for example,
XML-specified bean properties) effectively overrides the annotations for properties when wired through mixed approaches.

*** Enable annotation-based configuration ***

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context
		https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

</beans>
```

The <context:annotation-config/> element implicitly registers the following post-processors:

**  ConfigurationClassPostProcessor

**  AutowiredAnnotationBeanPostProcessor

**  CommonAnnotationBeanPostProcessor

**  PersistenceAnnotationBeanPostProcessor

**  EventListenerMethodProcessor

----

#### @fallback beans in the Spring Framework.

We saw how to define primary and fallback beans and how to use them in a Spring application.
Fallback beans provide an alternative implementation when any other qualifying bean isn’t available.
This can be useful when switching between different implementations based on the active profile or other conditions.

https://www.baeldung.com/spring-fallback-beans

As of 6.2, there is a @Fallback annotation for demarcating any beans other than the regular ones to be injected.
If only one regular bean is left, it is effectively primary as well:

```java

@Configuration
public class MovieConfiguration {

    @Bean
    public MovieCatalog firstMovieCatalog() { ...}

    @Bean
    @Fallback
    public MovieCatalog secondMovieCatalog() { ...}

    // ...
}
```

With both variants of the preceding configuration, the following MovieRecommender is autowired with the
firstMovieCatalog:
(Fallback beans are not autowired by default, if at least one regular bean is present)

```java
public class MovieRecommender {

    @Autowired
    private MovieCatalog movieCatalog;

    // ...
}
```

@Primary and @Fallback are effective ways to use autowiring by type with several instances when one primary (or
non-fallback) candidate can be determined.

----

#### @Qualifier

When you need more control over the selection process, you can use Spring’s @Qualifier annotation.
You can associate qualifier values with specific arguments,
narrowing the set of type matches so that a specific bean is chosen for each argument

----
You're touching on a **classic Spring Web MVC gotcha** — and yes, that statement is **accurate**.
Here’s a clean and complete explanation 👇

---

#### Using Generics as Autowiring Qualifiers

You can use Java generic types as an implicit form of qualification.

Example:

```java

@Configuration
public class MyConfiguration {

    @Bean
    public StringStore stringStore() {
        return new StringStore();
    }

    @Bean
    public IntegerStore integerStore() {
        return new IntegerStore();
    }
}
```

Suppose you have an interface with a generic type: Store<T>

```java
public interface Store<T> {
    // some  methods
}
```

So during Autowiring Spring will look at a generic type and will try to find a bean that implements the Store;
StringStore implements Store<String> and IntegerStore implements Store<Integer>.

```java

@Autowired
private Store<String> s1; // <String> qualifier, injects the stringStore bean

@Autowired
private Store<Integer> s2; // <Integer> qualifier, injects the integerStore bean
```

This also applies to arrays, collections, and maps.

```java
// Inject all Store beans as long as they have an <Integer> generic
// Store<String> beans will not appear in this list
@Autowired
private List<Store<Integer>> s;
```

---

Using CustomAutowireConfigurer (XML configuration)
---

CustomAutowireConfigurer is a BeanFactoryPostProcessor that lets you register your own custom qualifier annotation
types,
even if they are not annotated with Spring’s @Qualifier annotation.

### ❗Important Correction

This configuration **does NOT** tell Spring which bean (`visaPaymentService` or `masterPaymentService`) to inject.

Instead, it tells Spring:

> **“When you see an annotation of type `@Pay`, treat it as a qualifier.”**

So Spring will still require a `@Pay` annotation on the injection point, like:

```java

@Autowired
@Pay
private PaymentService paymentService;
```

Without `@Pay`, Spring **still fails** because it does not know which bean to choose.

📌 That XML *only registers the annotation type* as a qualifier — it does not map the qualifier to a bean.

---

### ✔ A Correct and Complete Example

#### ① Define a custom qualifier annotation

```java

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Pay {
    String value() default "";
}
```

#### ② Define beans with matching qualifier

```java

@Bean
@Pay("visa")
public PaymentService visaPaymentService() {
    return new VisaPaymentService();
}

@Bean
@Pay("master")
public PaymentService masterPaymentService() {
    return new MasterPaymentService();
}
```

#### ③ Inject a specific one

```java

@Autowired
@Pay("visa")
private PaymentService paymentService;  // VISA will be selected
```

---

### ✓ What does `CustomAutowireConfigurer` do here?

In XML configuration:

```xml

<bean class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer">
    <property name="customQualifierTypes">
        <set>
            <value>com.example.Pay</value>
        </set>
    </property>
</bean>
```

📌 This tells Spring:

> "`@Pay` is a valid qualifier — treat it like `@Qualifier`"

Without this XML, Spring would ignore `@Pay` and fail autowiring.

---

### 🧠 Summary: What it DOES vs DOESN’T do

| Action                                           |                 Supported?                  |
|--------------------------------------------------|:-------------------------------------------:|
| Register a custom annotation as qualifier        |                      ✔                      |
| Let Spring choose a bean only by annotation name |                      ✔                      |
| Automatically pick *one* bean without annotation |                      ❌                      |
| Determine which bean to inject automatically     |                      ❌                      |
| Replace `@Qualifier`                             | ✔ (if using your custom annotation instead) |

---

### 📌 When should you use CustomAutowireConfigurer?

| Scenario                                              |          Recommendation           |
|-------------------------------------------------------|:---------------------------------:|
| You cannot edit the bean classes (3rd-party library)  |                 ✔                 |
| You want consistent qualifier behavior across modules |                 ✔                 |
| You already control your own beans                    | Prefer `@Qualifier` or `@Primary` |

---

### ⭐ Modern Spring Alternative (no XML)

You can do:

```java

@Bean
@Primary
public PaymentService visaPaymentService() {
    return new VisaPaymentService();
}
```

= always inject VISA when ambiguous → **no XML, no custom qualifier needed**.

---

If you want, I can also show **the second mode** of `CustomAutowireConfigurer` — where you map a bean name globally
without any annotation changes.

---


---
Injection with @Resource (jakarta.annotation.Resource)
---

@Resource takes a name attribute. By default, Spring interprets that value as the bean name to be injected.

```java
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Resource(name = "myMovieFinder")
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```

If no name is explicitly specified, the default name is derived from the field name or setter method. In case of a
field, it takes the field name.
in case of setter method, it takes the setter parameter name.

The name provided with the annotation is resolved as a bean name by the ApplicationContext of which the
CommonAnnotationBeanPostProcessor is aware.

```note
In the exclusive case of @Resource usage with no explicit name specified, and similar to @Autowired,
@Resource finds a primary type match instead of a specific named bean and resolves well known resolvable dependencies: 
    the BeanFactory, ApplicationContext, ResourceLoader, ApplicationEventPublisher, and MessageSource interfaces.```
```

```note 
Resorce:    Injection must be resolved by name first.
            Only if no bean matches by name → resolve by type.
```

| Step | What Spring Looks For                                             |
|------|-------------------------------------------------------------------|
| 1️⃣  | A bean named **"customerPreferenceDao"** exactly                  |
| 2️⃣  | If none → a **single** bean of type `CustomerPreferenceDao`       |
| 3️⃣  | If multiple → ❌ **Fails** (no disambiguation)                     |
| 4️⃣  | Special case: resolves framework types (ApplicationContext, etc.) |


➜ Why primary does not override @Resource name resolution?

Because per JSR-250 standards, name has priority — Primary is a Spring-only feature, and Spring must honor the standard first.

Primary only matters if type resolution happens, not when name resolves a match.


| Feature                       | `@Resource`                       | `@Autowired`                |
| ----------------------------- | --------------------------------- | --------------------------- |
| Standard                      | ✔ JSR-250                         | ❌ Spring-only               |
| Resolution priority           | **Name → Type**                   | **Type → Qualifier → Name** |
| Respects `@Primary`           | Only if type fallback occurs      | ✔ Yes                       |
| Multiple candidate resolution | ❌ Fails (must be unique or named) | ✔ Can disambiguate          |
| Field name influences         | ✔ Yes                             | Only as last fallback       |
| Required option               | ❌ No `required=false`             | ✔ Has `required=false`      |
| Constructor injection         | ❌ Not intended                    | ✔ Fully supported           |
| Works well for                | Simple cases                      | Complex dependency wiring   |


🆚 @Resource vs @Autowired + @Qualifier:

| Feature                       | `@Resource`                       | `@Autowired`                |
| ----------------------------- | --------------------------------- | --------------------------- |
| Standard                      | ✔ JSR-250                         | ❌ Spring-only               |
| Resolution priority           | **Name → Type**                   | **Type → Qualifier → Name** |
| Respects `@Primary`           | Only if type fallback occurs      | ✔ Yes                       |
| Multiple candidate resolution | ❌ Fails (must be unique or named) | ✔ Can disambiguate          |
| Field name influences         | ✔ Yes                             | Only as last fallback       |
| Required option               | ❌ No `required=false`             | ✔ Has `required=false`      |
| Constructor injection         | ❌ Not intended                    | ✔ Fully supported           |
| Works well for                | Simple cases                      | Complex dependency wiring   |

See [SpringCoreResourceAutowiredDemo.java](../src/main/java/org/example/springcore/SpringCoreResourceAutowiredDemo.java) for example.

---
Using @Value
---

@Value is used to inject externalized properties.

```java
@Component
public class MovieRecommender {

	private final String catalog;

	public MovieRecommender(@Value("${catalog.name}") String catalog) {
		this.catalog = catalog;
	}
}
```

Add @PropertySource("classpath:application.properties") annotation to the @Configuration class to load properties from resources.

PropertySourcesPlaceholderConfigurer - for resolving missing properties. (if not provided and variable is missing the value will be set to ${catalog.name})

```note 
When configuring a PropertySourcesPlaceholderConfigurer using JavaConfig, the @Bean method must be static.

A static @Bean method is primarily used for defining a BeanFactoryPostProcessor or BeanPostProcessor bean.
When defining such processors, making the @Bean method static ensures that the processor can be registered and applied early in the Spring application context lifecycle,
before other regular beans are fully initialized.
```

```note
Spring Boot configures by default a PropertySourcesPlaceholderConfigurer bean that will get properties from application.properties and application.yml files.
```

A Spring BeanPostProcessor uses a ConversionService behind the scenes to handle the process for converting the String value in @Value to the target type.
You can override the default ConversionService by specifying a custom ConversionService in the @Bean method.

```java
@Configuration
public class AppConfig {

	@Bean
	public ConversionService conversionService() {
		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		conversionService.addConverter(new MyCustomConverter());
		return conversionService;
	}
}
```

@Value also parses SpEL's  expressions runtime.

For example: 
[MovieRecommender.java](../src/main/java/org/example/springcore/component/MovieRecommender.java),
[SpringCoreResourceAutowiredDemo.java](../src/main/java/org/example/springcore/SpringCoreResourceAutowiredDemo.java)

---
Using @PostConstruct and @PreDestroy (jakarta.annotation.(PostConstruct, PreDestroy))
---

CommonAnnotationBeanPostProcessor - recognizes @PostConstruct and @PreDestroy annotations and registers the lifecycle methods with the BeanFactory.

```note
Like @Resource, the @PostConstruct and @PreDestroy annotation types were a part of the standard Java libraries from JDK 6 to 8.
However, the entire javax.annotation package got separated from the core Java modules in JDK 9 and eventually removed in JDK 11.
As of Jakarta EE 9, the package lives in jakarta.annotation now. If needed, the jakarta.annotation-api artifact needs to be obtained via Maven Central now,
simply to be added to the application’s classpath like any other library.
```

See https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html#beans-factory-lifecycle-combined-effects
for Lifecycle methods flow.



---
Classpath Scanning and Managed Components
---

Stereotype Annotations: 
    
    **  @Component - generic
    **  @Service - service
    **  @Controller - presentation layer
    **  @Resource - persistence; is already supported as a marker for automatic exception translation in your persistence layer.

#### Using Meta-annotations and Composed Annotations

You can combine annotations and create composed Annotations. For example 
@RestController is a Composed annotation (@Controller + @ResponseBody)

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component /*meta-annotation*/
public @interface Service /*composed annotation*/ {

	// ...
}
```

Composed annotations can redeclare attributes: 

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(WebApplicationContext.SCOPE_SESSION) /* hardcodes value */
public @interface SessionScope {

	/**
	 * Alias for {@link Scope#proxyMode}.
	 * <p>Defaults to {@link ScopedProxyMode#TARGET_CLASS}.
	 */
	@AliasFor(annotation = Scope.class)
	ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;

}
```

You can let proxyMode to default(ScopedProxyMode.TARGET_CLASS) or set manually: 

```java
@Service
@SessionScope
/*proxyMode = ScopedProxyMode.TARGET_CLASS*/
public class SessionScopedService {
	// ...
}

@Service
@SessionScope(proxyMode = ScopedProxyMode.INTERFACES)
/*proxyMode = ScopedProxyMode.INTERFACES*/
public class SessionScopedUserService implements UserService {
    // ...
}
```

#### Automatically Detecting Classes and Registering Bean Definitions

To autodetect classes and register the corresponding beans,
you need to add @ComponentScan to your @Configuration class,
where the basePackages attribute is configured with a common parent package for the two classes.

```java
@Configuration
@ComponentScan(basePackages = "org.example")
public class AppConfig  {
	// ...
}
```

```tip
The use of <context:component-scan> implicitly enables the functionality of <context:annotation-config>.
There is usually no need to include the <context:annotation-config> element when using <context:component-scan>
```

The AutowiredAnnotationBeanPostProcessor and CommonAnnotationBeanPostProcessor are both implicitly included when you use the <context:component-scan> element.
That means that the two components are autodetected and wired together — all without any bean configuration metadata provided in XML.


```java
@Configuration
/*property placeholder from environment*/
@ComponentScan("${app.scan.packages}")
public class AppConfig {
	// ...
}
```

```properties
app.scan.packages=org.example.config, org.example.service.**
```

#### Filtering component scan beans (exclude/include)

```java
@Configuration
@ComponentScan(basePackages = "org.example",
		includeFilters = @Filter(type = FilterType.REGEX, pattern = ".*Stub.*Repository"),
		excludeFilters = @Filter(Repository.class))
public class AppConfig {
	// ...
}
```

```java
@Service("myMovieLister")
/*bean name: myMovieLister*/
public class SimpleMovieLister {
	// ...
}


@Repository
/*bean name: movieFinderImpl*/
public class MovieFinderImpl implements MovieFinder {
    // ...
}
```
You can implement your own BeanGenerator to override name assignment: 

```java
@Configuration
@ComponentScan(basePackages = "org.example", nameGenerator = MyNameGenerator.class)
public class AppConfig {
	// ...
}
```

```Note
The @Scope of the bean is not inherited.
```

```hint
You may also compose your own scoping annotations by using Spring’s meta-annotation approach
```

```hint
    ScopeMetadataResolver - interface for resolving scopes
    
@Configuration
@ComponentScan(basePackages = "org.example", scopeResolver = MyScopeResolver.class)
public class AppConfig {
	// ...
}
    
```

```tip
In addition to its role for component initialization, you can also place the @Lazy annotation on injection points
marked with @Autowired or @Inject. In this context, it leads to the injection of a lazy-resolution proxy.
However, such a proxy approach is rather limited. For sophisticated lazy interactions, in particular in combination 
with optional dependencies, we recommend ObjectProvider<MyTargetBean> instead.
```

#### Naming Autodetected Components

When a component is autodetected as part of the scanning process,
its bean name is generated by the BeanNameGenerator strategy known to that scanner.

---

Great topic — this is an area where Spring behavior *changes significantly* depending on **where** and **how** you declare `@Bean`.

Below is the **full comparison**:

---

## ✅ 1️⃣ `@Bean` inside a **class annotated with `@Configuration`**

```java
@Configuration
public class AppConfig {

    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

### ✔ What happens

| Behavior                             | Result                                            |
| ------------------------------------ | ------------------------------------------------- |
| Full **CGLIB proxying**              | The config class is proxied                       |
| **Singleton enforcement**            | Multiple calls return same bean                   |
| **Bean inter-calls are intercepted** | Calls to other @Bean methods return managed beans |

Example:

```java
@Bean
public A a() { return new A(b()); }  // b() returns the SPRING bean, not `new B()`
```

➡ **This is the recommended and “real” Spring Java config style.**

---

## ⚠ 2️⃣ `@Bean` inside any `@Component` / `@Service` / `@Repository` (NOT `@Configuration`)

```java
@Component
public class SomeComponent {

    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

### ❗Key differences

| Behavior                                                                 | Result                                 |
| ------------------------------------------------------------------------ | -------------------------------------- |
| No CGLIB proxying                                                        | No enhancement                         |
| **Every method call creates a new instance**                             | Could break singleton                  |
| @Bean factory methods are **just called normally** inside the same class | Internal calls are **not intercepted** |

Example:

```java
@Bean
public A a() { return new A(b()); }  // b() creates NEW B(), not the Spring bean
```

✔ Spring still registers the bean
✘ But **internal dependency resolution becomes unsafe**

➡ Use only when you **must expose a bean** from a component — rare case.

---

## 🔹 3️⃣ `@Bean` with **static** method

```java
@Configuration
public class StaticConfig {

    @Bean
    public static MyStaticBean myBean() { return new MyStaticBean(); }
}
```

### Behavior

| Behavior                                                                | Result                                      |
| ----------------------------------------------------------------------- | ------------------------------------------- |
| Bean created **before** configuration class is instantiated             | No need for proxy                           |
| Used for **BeanFactoryPostProcessors** / **early infrastructure beans** | e.g. `PropertySourcesPlaceholderConfigurer` |
| Cannot call other non-static @Beans                                     | No Spring lifecycle access                  |

➡ Very useful for **bootstrapping Spring itself**

---

## 🔍 Short comparison table

| Feature / Behavior           | `@Bean` in `@Configuration` | `@Bean` in `@Component` | static `@Bean`               |
| ---------------------------- | --------------------------- | ----------------------- | ---------------------------- |
| Singleton enforced           | ✅ Yes                       | ❌ Not guaranteed        | ❌ Not via proxy              |
| Internal method interception | ✅ Yes                       | ❌ No                    | ❌ No                         |
| Config class proxied         | ✅ Yes                       | ❌ No                    | ❌ No                         |
| When bean is created         | Normal lifecycle            | Normal lifecycle        | **Before** lifecycle         |
| Use case                     | Application configs         | Rare, special cases     | Infra-level bean definitions |

---

## 📌 Practical Guidelines

| Use this when                                                              | Recommendation                            |
| -------------------------------------------------------------------------- | ----------------------------------------- |
| Creating normal app beans (services, clients, data source, mapper)         | ➜ Put `@Bean` inside `@Configuration`     |
| You already have a component and need to expose an additional bean         | ➜ `@Component` with @Bean (not preferred) |
| Creating `BeanFactoryPostProcessor`, `BeanDefinitionRegistryPostProcessor` | ➜ Use **static** @Bean                    |

---

## 🧪 Simple Demo — Why proxying matters

```java
@Configuration
class Config {
    @Bean A a() { return new A(b()); }
    @Bean B b() { return new B(); }
}

@Component
class BadConfig {
    @Bean A a() { return new A(b()); }
    @Bean B b() { return new B(); }
}
```

| Call                         | Result in `@Configuration` | Result in `@Component` |
| ---------------------------- | -------------------------- | ---------------------- |
| `a()` internally calls `b()` | Spring-managed singleton B | New B() each time      |

➡ **Your A’s dependency changes instance → bugs**

---

## 🧠 Summary

| Choose…                 | When                                                 |
| ----------------------- | ---------------------------------------------------- |
| `@Configuration`        | Always for proper Spring-managed beans (recommended) |
| static @Bean            | Early infrastructure beans                           |
| @Bean inside @Component | Only if no other choice — avoid                      |

---
JakartaAnnotations
---

####    Dependency Injection with @Inject and @Named

Inject instead of Autowired
```java
import jakarta.inject.Inject;

public class SimpleMovieLister {

	private MovieFinder movieFinder;

	@Inject
	public void setMovieFinder(@Named("main") MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}

	public void listMovies() {
		this.movieFinder.findMovies(...);
		// ...
	}
}
```

You can also use Provider<T>, to escape bean not-defined: 

```java
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class SimpleMovieLister {

	private Provider<MovieFinder> movieFinder;

	@Inject
	public void setMovieFinder(Provider<MovieFinder> movieFinder) {
		this.movieFinder = movieFinder;
	}

	public void listMovies() {
		this.movieFinder.get().findMovies(...);
		// ...
	}
}
```

Use @Named annotations instead of @Qualifier
@Inject can also be used with Optional or @Nullable

#### @Named and @ManagedBean: Standard Equivalents to the @Component Annotation

Use them instead of @Component, to register bean.

```java
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("movieListener")  // @ManagedBean("movieListener") could be used as well
public class SimpleMovieLister {

	private MovieFinder movieFinder;

	@Inject
	public void setMovieFinder(MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}

	// ...
}
```

```kotlin notebook
When you use @Named or @ManagedBean, you can use component scanning
in the exact same way as when you use Spring annotations.
```

![img_5.png](img_5.png)

## Java-based Container Configuration


##### The @Bean annotation is used to indicate that a method instantiates, configures, and initializes a new object to be managed by the Spring IoC container.

##### Annotating a class with @Configuration indicates that its primary purpose is as a source of bean definitions.
Furthermore, @Configuration classes let inter-bean dependencies be defined by calling other @Bean methods in the same class.


## 🚦 The Context Split in Spring MVC

In a traditional (non–Spring Boot) Spring MVC app, there are typically **two** application contexts:

| Context                     | Created by              | Contains                                    | Scope            |
|-----------------------------|-------------------------|---------------------------------------------|------------------|
| **Root ApplicationContext** | `ContextLoaderListener` | Services, Repositories, Middle-tier beans   | Application-wide |
| **WebApplicationContext**   | `DispatcherServlet`     | Controllers, HandlerMappings, ViewResolvers | Servlet-specific |

📌 The **WebApplicationContext has a parent** → the Root context

---

## 🔍 What `<context:annotation-config/>` actually does

It enables annotation-based features **only for beans defined in that specific context**, such as:

* `@Autowired`
* `@Required`
* `@PostConstruct` / `@PreDestroy`
* `@Resource`
* `@Qualifier`

---

### ✔ Correct behavior (scoped)

If you declare:

```xml

<beans> <!-- WebApplicationContext -->
    <context:annotation-config/>
</beans>
```

Then it will process annotations **only on beans inside the WebApplicationContext**, i.e.:

* Controllers ✔
* But **NOT** Services / Repositories ❌
  (because they are usually defined in the Root context)

---

## 🧨 Consequence

Your controllers might not get dependencies injected properly if
service beans are discovered only in the **Root context**:

```
@Autowired
private MyService service;  ❌ NOT injected
```

Because annotation processing **never reached parent context beans**.

---

## 🛠 Correct Best Practice

### ✔ Use `<context:component-scan>` instead of annotation-config alone

Example Root context (services/repositories):

```xml

<context:component-scan base-package="com.example.app"/>
```

Example Web context (controllers only):

```xml

<context:component-scan base-package="com.example.app.web"/>
```

👉 **Component scanning automatically performs annotation-config**

So **no need** for `<context:annotation-config/>` anymore.

---

## 📝 When should `<context:annotation-config/>` be used?

Only when:

* You **don’t want component scanning**
* You **manually declare beans in XML**, but still want:

    * `@Autowired`
    * lifecycle annotations

Example:

```xml

<bean class="com.example.ManualBean"/>
<context:annotation-config/>
```

---

## 🧠 Simple Rule to Remember

> `<context:annotation-config/>` affects only beans defined in **the same XML / same context**

> `<context:component-scan>` affects all components discovered in that package **and handles annotation-config
automatically**

---

### ❤️ Recommendation Today (Spring Boot and modern Spring)

➡️ Do **not** use XML configs
➡️ Let Boot automatically configure everything
➡️ Single ApplicationContext (no context split confusion)

---

Absolutely! Here’s a **clear visual diagram** showing how dependency resolution works between the **Root
ApplicationContext** and the **DispatcherServlet’s WebApplicationContext**:

---

## 🔍 Spring MVC Context Hierarchy — Visual Diagram

```
 ┌──────────────────────────────────────────┐
 │            Root ApplicationContext        │
 │------------------------------------------│
 │  • Services ( @Service )                  │
 │  • Repositories ( @Repository )           │
 │  • Security, DB Config, Middleware        │
 │                                          │
 │  Dependency Injection inside here works   │
 │  ONLY among beans defined in this context │
 └──────────────────────────────────────────┘
                    ▲
                    │ inherits beans
                    │ (can access parent)
                    │  ONLY for DI
                    │
 ┌──────────────────────────────────────────┐
 │     WebApplicationContext                │
 │     (per DispatcherServlet)              │
 │------------------------------------------│
 │  • Controllers ( @Controller )            │
 │  • ViewResolvers, HandlerMappings         │
 │                                          │
 │ DI search order:                          │
 │  1️⃣ Inside WebApplicationContext          │
 │  2️⃣ If missing → Lookup in Parent         │
 │     (Root ApplicationContext)             │
 └──────────────────────────────────────────┘
```

---

## 👍 Key Rules to Understand

| Rule                                                                      | Meaning                                       |
|---------------------------------------------------------------------------|-----------------------------------------------|
| Children **can access** parent beans                                      | Controller can @Autowired Service             |
| Parent **cannot access** child beans                                      | Service CANNOT @Autowired Controller          |
| `<context:annotation-config/>` affects only the context where declared    | Might leave services without proper injection |
| `<context:component-scan>` configures annotation processing automatically | Best practice                                 |

---

## Dependency Injection Flow Example

Controller tries to inject this:

```java

@Autowired
private MyService service;
```

Resolution:

1️⃣ Look in WebApplicationContext → Not found
2️⃣ Look in Root ApplicationContext → Found ✔ Injected

---

## ❌ What breaks

If `<context:annotation-config/>` exists **only in the Web context**:

* Service beans in Root context **won’t be annotation processed**
* Their own dependencies **won’t inject**
* Services may be `null` or improperly created

Example failure mechanism:

```
Root context beans do not get @Autowired applied because
annotation processing never ran there ❌
```

---

## 🧠 Memory Trick

> Parent knows nothing about children
> Children know everything about parents

Like inheritance in OOP.

----

Absolutely! Here is a **clear comparison table** for **Jakarta Inject** vs **Spring Core** dependency injection
annotations — covering similarities, differences, features, and when to use which.

---

## 🔄 Jakarta Inject vs Spring Core — Comparison Table

| Topic                       | **Jakarta Inject** (`jakarta.inject.*`)                          | **Spring Core** (`org.springframework.*`)                                    |
|-----------------------------|------------------------------------------------------------------|------------------------------------------------------------------------------|
| 🧩 Primary Purpose          | Standard dependency Injection under **Jakarta EE** specification | Full-featured DI and IoC in the **Spring Framework**                         |
| 🏛️ Standardization         | ✔ Vendor-neutral, Jakarta EE standard (formerly Javax)           | ❌ Proprietary to Spring                                                      |
| DI Annotation               | `@Inject`                                                        | `@Autowired`                                                                 |
| Optional Dependencies       | `@Inject` + `Provider<T>`                                        | `@Autowired(required = false)` or `Optional<T>`                              |
| Qualifying Bean             | `@Qualifier` (same name in both but from `jakarta.inject`)       | `@Qualifier` (Spring version)                                                |
| Default Injection Target    | **Only constructors and fields**                                 | Constructors, fields, setters, and arbitrary methods                         |
| Primary Candidate Selection | ❌ No concept of primary bean                                     | ✔ `@Primary` support                                                         |
| Life-Cycle Integration      | ❌ No lifecycle support                                           | ✔ Supports `@PostConstruct`, `@PreDestroy`, `InitializingBean`, etc.         |
| Scopes                      | Standard scopes like `@Singleton`                                | Advanced Scopes: `@RequestScope`, `@SessionScope`, `@ApplicationScope`, etc. |
| Value Injection             | ❌ No property placeholder support                                | ✔ `@Value("${config.key}")`                                                  |
| Proxy Creation              | ❌ Limited                                                        | ✔ Rich AOP & proxying with CGLIB / JDK proxies                               |
| Environment Support         | Jakarta EE runtime (WildFly, Payara, etc.)                       | Spring Managed DI in **any** environment                                     |
| Use in Spring Boot          | ✔ Supported, works fine                                          | ✔ Native and recommended                                                     |
| Autowiring Resolution       | Type-based                                                       | Type-based + advanced fallback rules                                         |
| Constructor Injection       | ✔ Preferred                                                      | ✔ Preferred, required if there is no default constructor                     |

---

## 🔍 Summary of Similarities

| Feature                         | Jakarta Inject | Spring Core |
|---------------------------------|:--------------:|:-----------:|
| Dependency Injection            |       ✔        |      ✔      |
| Field Injection                 |       ✔        |      ✔      |
| Constructor Injection           |       ✔        |      ✔      |
| `@Qualifier` for disambiguation |       ✔        |      ✔      |
| Can coexist in same project     |       ✔        |      ✔      |

---

## 🚫 Key Limitations of Jakarta Inject

| Missing Feature                     | Spring Alternative                              |
|-------------------------------------|-------------------------------------------------|
| No bean lifecycle events            | `@PostConstruct`, `@EventListener`, etc.        |
| No conditional auto-configuration   | `@ConditionalOnProperty`, etc.                  |
| No scope beyond Singleton/Dependent | `@RequestScope`, `@SessionScope`, custom scopes |
| No `@Value`                         | `@Value`, `Environment` abstraction             |
| No component scanning               | `@Component`, `@Service`, etc.                  |

---

## 🧠 Which Should You Use?

| Scenario                                                       | Recommendation                             |
|----------------------------------------------------------------|--------------------------------------------|
| Pure Jakarta EE application                                    | Use **Jakarta Inject**                     |
| Spring Boot / Spring Framework                                 | Use **Spring** annotations                 |
| Library that should work everywhere                            | Use **Jakarta Inject** (portable standard) |
| Need advanced features: events, scopes, profiles, placeholders | Use **Spring**                             |

---

## 🎯 Practical Guidance

In a Spring application:

✔ You **can** use `@Inject`
✔ But you **should** use `@Autowired` and Spring annotations

Reason:

> Spring gives more control, better tooling, and richer DI semantics.

Example in Spring 💡

```java

@Component
public class NotificationService {

    private final EmailService emailService;

    @Autowired
    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }
}
```

Equivalent using Jakarta Inject:

```java
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class NotificationService {

    private final EmailService emailService;

    @Inject
    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }
}
```

---

```java
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Autowired(required = false)
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

[NOTE]

A non-required method will not be called at all if its dependency (or one of its dependencies, in case of multiple
arguments) is not available.
A non-required field will not get populated at all in such cases, leaving its default value in place.
In other words, setting the required attribute to false indicates that the corresponding property is optional for
autowiring purposes,
and the property will be ignored if it cannot be autowired. This allows properties to be assigned default values that
can be optionally overridden via dependency injection.

----
Instantiating the Spring Container by Using AnnotationConfigApplicationContext
----

When @Configuration classes are provided as input, the @Configuration class itself is registered as a bean definition and all declared @Bean methods within the class are also registered as bean definitions.

AnnotationConfigApplicationContext is not limited to working only with @Configuration classes. Any @Component or JSR-330 annotated class may be supplied as input to the constructor,
as the following example shows:

```java
public static void main(String[] args) {
	ApplicationContext ctx = new AnnotationConfigApplicationContext(MyServiceImpl.class, Dependency1.class, Dependency2.class);
	MyService myService = ctx.getBean(MyService.class);
	myService.doStuff();
}
```

You can manually register beans with the ApplicationContext using the register() method:

```java
public static void main(String[] args) {
	AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
	ctx.register(AppConfig.class, OtherConfig.class);
	ctx.register(AdditionalConfig.class);
	ctx.refresh(); // update context after registration of beans and configurations
	MyService myService = ctx.getBean(MyService.class);
	myService.doStuff();
}
```

@ComponentScan(basePackages = "com.acme") - enable Component scanning for a given base package(and its subpackages)

You can also scan through application context: 

```java 
public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.scan("com.acme");
    ctx.refresh();
    MyService myService = ctx.getBean(MyService.class);
}
```

```note
A WebApplicationContext variant of AnnotationConfigApplicationContext is available with AnnotationConfigWebApplicationContext.
```

```web.xml
<web-app>
	<!-- Configure ContextLoaderListener to use AnnotationConfigWebApplicationContext
		instead of the default XmlWebApplicationContext -->
	<context-param>
		<param-name>contextClass</param-name>
		<param-value>
			org.springframework.web.context.support.AnnotationConfigWebApplicationContext
		</param-value>
	</context-param>

	<!-- Configuration locations must consist of one or more comma- or space-delimited
		fully-qualified @Configuration classes. Fully-qualified packages may also be
		specified for component-scanning -->
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>com.acme.AppConfig</param-value>
	</context-param>

	<!-- Bootstrap the root application context as usual using ContextLoaderListener -->
	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<!-- Declare a Spring MVC DispatcherServlet as usual -->
	<servlet>
		<servlet-name>dispatcher</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<!-- Configure DispatcherServlet to use AnnotationConfigWebApplicationContext
			instead of the default XmlWebApplicationContext -->
		<init-param>
			<param-name>contextClass</param-name>
			<param-value>
				org.springframework.web.context.support.AnnotationConfigWebApplicationContext
			</param-value>
		</init-param>
		<!-- Again, config locations must consist of one or more comma- or space-delimited
			and fully-qualified @Configuration classes -->
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>com.acme.web.MvcConfig</param-value>
		</init-param>
	</servlet>

	<!-- map all requests for /app/* to the dispatcher servlet -->
	<servlet-mapping>
		<servlet-name>dispatcher</servlet-name>
		<url-pattern>/app/*</url-pattern>
	</servlet-mapping>
</web-app>
```

---
Using the @Bean Annotation
---

@Bean - a method-level annotation. The annotation supports some of the attributes offered by <bean/>, such as:

    init-method
    destroy-method
    autowiring
    name

You can use the @Bean annotation in a @Configuration-annotated or in a @Component-annotated class.

#### Receiving Lifecycle Callbacks
Any classes defined with the @Bean annotation support the regular lifecycle callbacks and can use the @PostConstruct and @PreDestroy annotations from JSR-250.
See JSR-250 annotations for further details.

The regular Spring lifecycle callbacks are fully supported as well. If a bean implements InitializingBean, DisposableBean, or Lifecycle,
their respective methods are called by the container.

The standard set of *Aware interfaces (such as BeanFactoryAware, BeanNameAware, MessageSourceAware, ApplicationContextAware, and so on) are also fully supported.

```java
public class BeanOne {

	public void init() {
		// initialization logic
	}
}

public class BeanTwo {

	public void cleanup() {
		// destruction logic
	}
}

@Configuration
public class AppConfig {

	@Bean(initMethod = "init")
	public BeanOne beanOne() {
		return new BeanOne();
	}

	@Bean(destroyMethod = "cleanup")
	public BeanTwo beanTwo() {
		return new BeanTwo();
	}
}
```

```note importamt
By default, beans defined with Java configuration that have a public close or shutdown method are automatically enlisted with a destruction callback. 
If you have a public close or shutdown method and you do not wish for it to be called when the container shuts down, you can add @Bean(destroyMethod = "") 
to your bean definition to disable the default (inferred) mode.
```

```java (update destroy method, to not call close/shutdown (default))
@Bean(destroyMethod = "")
public DataSource dataSource() throws NamingException {
	return (DataSource) jndiTemplate.lookup("MyDS");
}
```

ScopedProxyMode - proxyMode support through @Scope annotation:

    ScopedProxyMode.DEFAULT - Default typically equals NO, unless a different default has been configured at the component-scan instruction level.
    ScopedProxyMode.TARGET_CLASS - Create a class-based proxy (uses CGLIB).
    ScopedProxyMode.INTERFACES - Create a JDK dynamic proxy implementing all interfaces exposed by the class of the target object.
    ScopedProxyMode.NO - Do not create a scoped proxy.


```note

@Target({TYPE,METHOD})
@Retention(RUNTIME)
@Documented
@Scope("session")
public @interface SessionScope

@SessionScope is a specialization of @Scope for a component whose lifecycle is bound to the current web session.

Specifically, @SessionScope is a composed annotation that acts as a shortcut for @Scope("session") with the default proxyMode() set to TARGET_CLASS. (Create a class-based proxy (uses CGLIB).)
```

### Scoped Beans as Dependencies

The Spring IoC container manages not only the instantiation of your objects (beans), but also the wiring up of
collaborators (or dependencies). If you want to inject (for example) an HTTP request-scoped bean into another bean of a
longer-lived scope,
you may choose to inject an AOP proxy in place of the scoped bean. That is, you need to inject a proxy object that
exposes the same public interface as the scoped object but that can also retrieve the real target object from the
relevant scope (such as an HTTP request) and delegate method calls onto the real object.

You may also use <aop:scoped-proxy/> between beans that are scoped as singleton, with the reference then going through
an intermediate proxy that is serializable and therefore able to re-obtain the target singleton bean on deserialization.

When declaring <aop:scoped-proxy/> against a bean of scope prototype, every method call on the shared proxy leads to the
creation of a new target instance to which the call is then being forwarded.

Also, scoped proxies are not the only way to access beans from shorter scopes in a lifecycle-safe fashion. You may also
declare your injection point (that is, the constructor or setter argument or autowired field) as
ObjectFactory<MyTargetBean>,
allowing for a getObject() call to retrieve the current instance on demand every time it is needed — without holding on
to the instance or storing it separately.

As an extended variant, you may declare ObjectProvider<MyTargetBean> which delivers several additional access variants,
including getIfAvailable and getIfUnique.

The JSR-330 variant of this is called Provider and is used with a Provider<MyTargetBean> declaration and a corresponding
get() call for every retrieval attempt. See here for more details on JSR-330 overall.

The configuration in the following example is only one line, but it is important to understand the “why” as well as the
“how” behind it:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/aop
		https://www.springframework.org/schema/aop/spring-aop.xsd">

    <!-- an HTTP Session-scoped bean exposed as a proxy -->
    <bean id="userPreferences" class="com.something.UserPreferences" scope="session">
        <!-- instructs the container to proxy the surrounding bean -->
        <aop:scoped-proxy/>
    </bean>

    <!-- a singleton-scoped bean injected with a proxy to the above bean -->
    <bean id="userService" class="com.something.SimpleUserService">
        <!-- a reference to the proxied userPreferences bean -->
        <property name="userPreferences" ref="userPreferences"/>
    </bean>
</beans>
```

To create such a proxy, you insert a child <aop:scoped-proxy/> element into a scoped bean definition (see Choosing the
Type of Proxy to Create and XML Schema-based configuration).

Why do definitions of beans scoped at the request, session and custom-scope levels require the <aop:scoped-proxy/>
element in common scenarios? Consider the following singleton bean definition and contrast it with what you need to
define for the aforementioned scopes (note that the following userPreferences bean definition as it stands is
incomplete):

````xml

<bean id="userPreferences" class="com.something.UserPreferences" scope="session"/>

<bean id="userManager" class="com.something.UserManager">
<property name="userPreferences" ref="userPreferences"/>
</bean>
````

In the preceding example, the singleton bean (userManager) is injected with a reference to the HTTP Session-scoped
bean (userPreferences). The salient point here is that the userManager bean is a singleton: it is instantiated exactly
once per container, and its dependencies (in this case only one, the userPreferences bean) are also injected only once.
This means that the userManager bean operates only on the exact same userPreferences object (that is, the one with which
it was originally injected).

This is not the behavior you want when injecting a shorter-lived scoped bean into a longer-lived scoped bean (for
example, injecting an HTTP Session-scoped collaborating bean as a dependency into singleton bean). Rather, you need a
single userManager object, and, for the lifetime of an HTTP Session, you need a userPreferences object that is specific
to the HTTP Session. Thus, the container creates an object that exposes the exact same public interface as the
UserPreferences class (ideally an object that is a UserPreferences instance), which can fetch the real UserPreferences
object from the scoping mechanism (HTTP request, Session, and so forth). The container injects this proxy object into
the userManager bean, which is unaware that this UserPreferences reference is a proxy. In this example, when a
UserManager instance invokes a method on the dependency-injected UserPreferences object, it is actually invoking a
method on the proxy. The proxy then fetches the real UserPreferences object from (in this case) the HTTP Session and
delegates the method invocation onto the retrieved real UserPreferences object.

Thus, you need the following (correct and complete) configuration when injecting request- and session-scoped beans into
collaborating objects, as the following example shows:

```xml

<bean id="userPreferences" class="com.something.UserPreferences" scope="session">
    <aop:scoped-proxy/>
</bean>
<bean id="userManager" class="com.something.UserManager">
<property name="userPreferences" ref="userPreferences"/>
</bean>
```

---

## Bean Lifecycle

### Lifecycle Phases

```
Container Started
    ↓
Bean Definition Loaded
    ↓
Bean Instantiated
    ↓
Dependencies Injected
    ↓
BeanNameAware.setBeanName()
    ↓
BeanFactoryAware.setBeanFactory()
    ↓
ApplicationContextAware.setApplicationContext()
    ↓
BeanPostProcessor.postProcessBeforeInitialization()
    ↓
@PostConstruct / InitializingBean.afterPropertiesSet()
    ↓
Custom init-method
    ↓
BeanPostProcessor.postProcessAfterInitialization()
    ↓
BEAN READY FOR USE
    ↓
Container Shutdown
    ↓
@PreDestroy / DisposableBean.destroy()
    ↓
Custom destroy-method
```

### Initialization Methods

#### 1. @PostConstruct

```java

@Service
public class DataService {

    @PostConstruct
    public void init() {
        System.out.println("Bean initialized");
        // Load data, connect to resources
    }
}
```

#### 2. InitializingBean Interface

```java

@Service
public class CacheService implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing cache");
    }
}
```

#### 3. Custom Init Method

```java

@Configuration
public class AppConfig {

    @Bean(initMethod = "init")
    public DataSource dataSource() {
        return new CustomDataSource();
    }
}

public class CustomDataSource {
    public void init() {
        System.out.println("Custom init");
    }
}
```

### Destruction Methods

#### 1. @PreDestroy

```java

@Service
public class ConnectionService {

    @PreDestroy
    public void cleanup() {
        System.out.println("Closing connections");
    }
}
```

#### 2. DisposableBean Interface

```java

@Service
public class ResourceManager implements DisposableBean {

    @Override
    public void destroy() throws Exception {
        System.out.println("Releasing resources");
    }
}
```

#### 3. Custom Destroy Method

```java

@Bean(destroyMethod = "close")
public DataSource dataSource() {
    return new HikariDataSource();
}
```

---

## Configuration Styles

### 1. Java-Based Configuration

```java

@Configuration
public class AppConfig {

    @Bean
    public UserService userService(UserRepository repository) {
        return new UserService(repository);
    }

    @Bean
    public UserRepository userRepository(DataSource dataSource) {
        return new JdbcUserRepository(dataSource);
    }
}
```

### 2. Annotation-Based Configuration

```java

@SpringBootApplication
@ComponentScan("com.example")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@Service
public class UserService {
}

@Repository
public class UserRepository {
}
```

### 3. XML Configuration (Legacy)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans">

    <bean id="userService" class="com.example.UserService">
        <constructor-arg ref="userRepository"/>
    </bean>

    <bean id="userRepository" class="com.example.UserRepository"/>

</beans>
```

### 4. Mixed Configuration

```java

@Configuration
@ImportResource("classpath:legacy-config.xml")
public class MixedConfig {

    @Bean
    public ModernService modernService() {
        return new ModernService();
    }
}
```

---

## ApplicationContext

The `ApplicationContext` is the central interface for providing configuration to a Spring application.
The org.springframework.context.ApplicationContext interface represents the Spring IoC container and is responsible for
instantiating, configuring, and assembling the beans.
The container gets its instructions on the components to instantiate, configure, and assemble by reading configuration
metadata.
The configuration metadata can be represented as annotated component classes, configuration classes with factory
methods, or external XML files or Groovy scripts.

### Types of ApplicationContext

#### 1. AnnotationConfigApplicationContext

```java
ApplicationContext context =
        new AnnotationConfigApplicationContext(AppConfig.class);

UserService service = context.getBean(UserService.class);
```

#### 2. ClassPathXmlApplicationContext

```java
ApplicationContext context =
        new ClassPathXmlApplicationContext("applicationContext.xml");
```

#### 3. AnnotationConfigWebApplicationContext (Web)

```java
// Used by Spring MVC DispatcherServlet
```

#### 4. WebApplicationContext (Web)

```java

@Autowired
private WebApplicationContext webApplicationContext;
```

### ApplicationContext Features

```java

@Service
public class ContextAwareService implements ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    public void doSomething() {
        // Access beans programmatically
        UserService service = context.getBean(UserService.class);

        // Get environment
        Environment env = context.getEnvironment();
        String dbUrl = env.getProperty("database.url");

        // Publish events
        context.publishEvent(new CustomEvent(this));

        // Access resources
        Resource resource = context.getResource("classpath:data.txt");
    }
}
```

### Common Operations

```java
// Get bean by type
UserService service = context.getBean(UserService.class);

// Get bean by name
UserService service = (UserService) context.getBean("userService");

// Get bean by name and type
UserService service = context.getBean("userService", UserService.class);

// Check if bean exists
boolean exists = context.containsBean("userService");

// Get all beans of type
Map<String, UserService> beans = context.getBeansOfType(UserService.class);

// Get bean names
String[] names = context.getBeanDefinitionNames();
```

Several implementations of the ApplicationContext interface are part of core Spring. In stand-alone applications, it is
common to create an instance of AnnotationConfigApplicationContext or ClassPathXmlApplicationContext.

### ApplicationContext Instantiation

In most application scenarios, explicit user code is not required to instantiate one or more instances of a Spring IoC
container.
For example, in a plain web application scenario, a simple boilerplate web descriptor XML in the web.xml file of the
application suffices (see Convenient ApplicationContext Instantiation for Web Applications).
In a Spring Boot scenario, the application context is implicitly bootstrapped for you based on common setup conventions.

![img.png](img.png)

### Composing XML-based Configuration Metadata

It can be useful to have bean definitions span multiple XML files.
Often, each individual XML configuration file represents a logical layer or module in your architecture.

You can use the ClassPathXmlApplicationContext constructor to load bean definitions from XML fragments.
This constructor takes multiple Resource locations, as was shown in the previous section.

````xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- services -->

    <bean id="petStore" class="org.springframework.samples.jpetstore.services.PetStoreServiceImpl">
        <property name="accountDao" ref="accountDao"/>
        <property name="itemDao" ref="itemDao"/>
        <!-- additional collaborators and configuration for this bean go here -->
    </bean>

    <!-- more bean definitions for services go here -->

</beans>
````

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="accountDao"
          class="org.springframework.samples.jpetstore.dao.jpa.JpaAccountDao">
        <!-- additional collaborators and configuration for this bean go here -->
    </bean>

    <bean id="itemDao" class="org.springframework.samples.jpetstore.dao.jpa.JpaItemDao">
        <!-- additional collaborators and configuration for this bean go here -->
    </bean>

    <!-- more bean definitions for data access objects go here -->

</beans>
```

Alternatively, use one or more occurrences of the <import/> element to load bean definitions from another file or files.

````xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <import resource="services.xml"/>
    <import resource="resources/messageSource.xml"/>

    <bean id="bean1" class="..."/>
    <bean id="bean2" class="..."/>
</beans>
````

---

## Interview Questions

### Q1: What is IoC and how does Spring implement it?

**Answer:** IoC (Inversion of Control) is a design principle where the control of object creation and lifecycle is
transferred from application code to a framework. Spring implements IoC through:

- Dependency Injection (DI)
- ApplicationContext/BeanFactory
- Bean lifecycle management
- Configuration metadata (XML, Java, Annotations)

### Q2: What's the difference between BeanFactory and ApplicationContext?

**Answer:**
| BeanFactory | ApplicationContext |
|-------------|-------------------|
| Basic container | Advanced container |
| Lazy initialization | Eager initialization |
| Manual registration | Automatic registration |
| No event publication | Event publication |
| No i18n support | i18n support |
| Limited features | Full enterprise features |

**ApplicationContext is preferred** for most applications.

### Q3: Explain Constructor vs Setter injection

**Answer:**

- **Constructor Injection:**
    - Immutable (final fields)
    - Required dependencies
    - Better for testing
    - Recommended by Spring team

- **Setter Injection:**
    - Optional dependencies
    - Can be changed after construction
    - Circular dependency resolution

### Q4: What are bean scopes?

**Answer:**

- **Singleton:** One instance per container (default)
- **Prototype:** New instance per request
- **Request:** One per HTTP request (web)
- **Session:** One per HTTP session (web)
- **Application:** One per ServletContext (web)

### Q5: How does Spring resolve circular dependencies?

**Answer:** Spring resolves circular dependencies in singleton beans using "early exposure" - partially constructed
beans are exposed before fully initialized. This works for setter/field injection but NOT constructor injection.

```java
// This WORKS (setter injection)
@Service
class A {
    @Autowired
    private B b;
}

@Service
class B {
    @Autowired
    private A a;
}

// This FAILS (constructor injection)
@Service
class A {
    @Autowired
    public A(B b) {
    }
}

@Service
class B {
    @Autowired
    public B(A a) {
    }  // BeanCurrentlyInCreationException
}
```

### Q6: What is @Qualifier and when to use it?

**Answer:** @Qualifier is used when multiple beans of the same type exist and you need to specify which one to inject.

```java

@Autowired
@Qualifier("emailSender")
private MessageSender sender;
```

### Q7: Explain bean lifecycle callbacks

**Answer:** Beans go through initialization and destruction phases:

**Initialization:**

1. @PostConstruct
2. InitializingBean.afterPropertiesSet()
3. Custom init-method

**Destruction:**

1. @PreDestroy
2. DisposableBean.destroy()
3. Custom destroy-method

### Q8: What is @Primary annotation?

**Answer:** @Primary indicates a default bean when multiple beans of the same type exist.

```java

@Component
@Primary  // This will be injected by default
class EmailSender implements MessageSender {
}
```

### Q9: How to inject collections?

**Answer:**

```java

@Service
public class NotificationService {

    @Autowired
    private List<MessageSender> allSenders;  // Injects all MessageSender beans

    @Autowired
    private Map<String, MessageSender> senderMap;  // Bean name -> Bean
}
```

### Q10: What is lazy initialization?

**Answer:** Lazy initialization defers bean creation until first use:

```java

@Service
@Lazy
public class HeavyService {
    // Created only when first requested
}

// Or globally
@Configuration
@Lazy
public class AppConfig {
}
```

---

## Best Practices

✅ **DO:**

- Use constructor injection for required dependencies
- Prefer @Component/@Service/@Repository over @Bean when possible
- Use @Primary for default implementations
- Keep beans stateless when possible
- Use appropriate bean scopes

❌ **DON'T:**

- Don't use field injection in production code
- Avoid circular dependencies
- Don't create stateful singleton beans
- Don't inject prototype beans into singleton without proxy
- Avoid too many dependencies (SRP violation)

---

## Notes Section

_Add your notes, observations, and examples here:_

---

---

---
