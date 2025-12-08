# Spring Core Assessment - Complete Guide

## 📚 Table of Contents

1. [Dependency Injection & IoC Container](#1-dependency-injection--ioc-container)
2. [Bean Definition & Configuration](#2-bean-definition--configuration)
3. [Bean Scopes](#3-bean-scopes)
4. [Bean Lifecycle](#4-bean-lifecycle)
5. [Dependency Resolution](#5-dependency-resolution)
6. [Configuration Metadata](#6-configuration-metadata)
7. [ApplicationContext](#7-applicationcontext)
8. [Resource Management](#8-resource-management)
9. [Environment & Profiles](#9-environment--profiles)
10. [Validation & Data Binding](#10-validation--data-binding)
11. [SpEL (Spring Expression Language)](#11-spel-spring-expression-language)
12. [AOP (Aspect Oriented Programming)](#12-aop-aspect-oriented-programming)
13. [Events](#13-events)
14. [Interview Questions](#14-interview-questions)

---

## 1. Dependency Injection & IoC Container

### What is IoC?

**Inversion of Control** means the framework controls object creation and lifecycle, not your code.

### Three Types of Dependency Injection

#### 1.1 Constructor Injection (RECOMMENDED ✅)

```java
@Service
public class OrderService {
    private final UserService userService;
    private final PaymentService paymentService;
    
    // Constructor injection - dependencies are immutable
    @Autowired  // Optional in Spring 4.3+ if single constructor
    public OrderService(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }
    
    public void createOrder(Order order) {
        User user = userService.findById(order.getUserId());
        paymentService.processPayment(order.getTotal());
    }
}
```

**Advantages:**
- ✅ Final fields (immutable)
- ✅ Required dependencies explicit
- ✅ Easy to test (pass mocks in constructor)
- ✅ No reflection needed
- ✅ Compile-time safety

#### 1.2 Setter Injection

```java
@Service
public class EmailService {
    private TemplateEngine templateEngine;
    private EmailConfig config;
    
    // Setter injection - for optional dependencies
    @Autowired(required = false)
    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
    
    @Autowired
    public void setConfig(EmailConfig config) {
        this.config = config;
    }
    
    public void sendEmail(String to, String message) {
        if (templateEngine != null) {
            message = templateEngine.process(message);
        }
        // Send email logic
    }
}
```

**Use Cases:**
- Optional dependencies
- Dependencies that can change at runtime
- Circular dependencies (not recommended)

#### 1.3 Field Injection (NOT RECOMMENDED ❌)

```java
@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;  // Hard to test, not immutable
    
    public Product findById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
```

**Disadvantages:**
- ❌ Cannot use final
- ❌ Hard to unit test (need Spring context)
- ❌ Hidden dependencies
- ❌ Violates SOLID principles

### Container Types

```java
// 1. BeanFactory (Basic, lazy)
BeanFactory factory = new XmlBeanFactory(new ClassPathResource("beans.xml"));
MyBean bean = (MyBean) factory.getBean("myBean");

// 2. ApplicationContext (Advanced, eager, recommended)
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
MyBean bean = context.getBean(MyBean.class);
```

**BeanFactory vs ApplicationContext:**

| Feature | BeanFactory | ApplicationContext |
|---------|-------------|-------------------|
| Initialization | Lazy | Eager |
| Event Publishing | No | Yes |
| i18n Support | No | Yes |
| AOP Integration | Manual | Automatic |
| BeanPostProcessor | Manual | Automatic |

---

## 2. Bean Definition & Configuration

### 2.1 Java-Based Configuration (@Configuration)

```java
@Configuration
public class AppConfig {
    
    @Bean
    public UserService userService() {
        return new UserServiceImpl(userRepository());
    }
    
    @Bean
    public UserRepository userRepository() {
        return new JdbcUserRepository(dataSource());
    }
    
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        ds.setUsername("user");
        ds.setPassword("password");
        return ds;
    }
    
    // Bean with dependencies auto-wired
    @Bean
    public OrderService orderService(UserService userService, 
                                     PaymentService paymentService) {
        return new OrderService(userService, paymentService);
    }
}

// Main class
public class Application {
    public static void main(String[] args) {
        ApplicationContext context = 
            new AnnotationConfigApplicationContext(AppConfig.class);
        
        UserService service = context.getBean(UserService.class);
        service.createUser(new User("John"));
    }
}
```

### 2.2 Annotation-Based Configuration

```java
// Enable component scanning
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {
}

// Service layer
@Service
public class UserService {
    private final UserRepository repository;
    
    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}

// Repository layer
@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
}

// Controller layer
@Controller
public class UserController {
    @Autowired
    private UserService userService;
}

// Component (generic)
@Component
public class EmailSender {
}
```

**Stereotype Annotations:**
- `@Component` - Generic Spring component
- `@Service` - Service layer (business logic)
- `@Repository` - DAO layer (data access)
- `@Controller` - Presentation layer (Spring MVC)
- `@RestController` - REST API controller

### 2.3 XML-Based Configuration (Legacy)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Simple bean -->
    <bean id="userService" class="com.example.UserServiceImpl">
        <constructor-arg ref="userRepository"/>
    </bean>
    
    <bean id="userRepository" class="com.example.UserRepository">
        <property name="dataSource" ref="dataSource"/>
    </bean>
    
    <!-- DataSource with properties -->
    <bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">
        <property name="jdbcUrl" value="jdbc:postgresql://localhost:5432/mydb"/>
        <property name="username" value="user"/>
        <property name="password" value="password"/>
    </bean>
    
    <!-- Component scanning in XML -->
    <context:component-scan base-package="com.example"/>
    
</beans>
```

### 2.4 Mixed Configuration

```java
@Configuration
@ImportResource("classpath:legacy-beans.xml")  // Import XML
@Import(DatabaseConfig.class)                   // Import other @Configuration
public class AppConfig {
    
    @Bean
    public ModernService modernService() {
        return new ModernService();
    }
}
```

---

## 3. Bean Scopes

### 3.1 Singleton (Default)

```java
@Service
@Scope("singleton")  // Default, can be omitted
public class ConfigurationService {
    // ONE instance per ApplicationContext
    // Shared across all requests
}

// Or in Java Config
@Configuration
public class AppConfig {
    @Bean
    @Scope("singleton")
    public ConfigService configService() {
        return new ConfigService();
    }
}
```

**Characteristics:**
- One instance per Spring container
- Created at startup (eager initialization)
- Thread-safe if stateless
- Most common scope

### 3.2 Prototype

```java
@Service
@Scope("prototype")
public class ShoppingCart {
    // NEW instance every time it's requested
    private List<Item> items = new ArrayList<>();
    
    public void addItem(Item item) {
        items.add(item);
    }
}

// Usage
@Service
public class CartService {
    @Autowired
    private ApplicationContext context;
    
    public ShoppingCart createNewCart() {
        // Gets new instance each time
        return context.getBean(ShoppingCart.class);
    }
}
```

**Characteristics:**
- New instance per request
- Not managed after creation (no destruction callbacks)
- Stateful beans

### 3.3 Request Scope (Web)

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, 
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginAttempt {
    // One instance per HTTP request
    private int attempts;
    private String ipAddress;
    
    public void recordAttempt(String ip) {
        this.ipAddress = ip;
        this.attempts++;
    }
}
```

**Important:** Need `proxyMode` to inject request-scoped beans into singleton beans.

### 3.4 Session Scope (Web)

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION,
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession {
    // One instance per HTTP session
    private User currentUser;
    private ShoppingCart cart;
    
    public void login(User user) {
        this.currentUser = user;
        this.cart = new ShoppingCart();
    }
}
```

### 3.5 Application Scope (Web)

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_APPLICATION,
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppMetrics {
    // One instance per ServletContext
    private long totalRequests;
    private long totalUsers;
    
    public void incrementRequests() {
        totalRequests++;
    }
}
```

### 3.6 Custom Scope

```java
// Define custom scope
public class TenantScope implements Scope {
    private Map<String, Object> scopedObjects = new ConcurrentHashMap<>();
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        String tenantId = TenantContext.getCurrentTenantId();
        String key = tenantId + "_" + name;
        
        return scopedObjects.computeIfAbsent(key, 
            k -> objectFactory.getObject());
    }
    
    @Override
    public Object remove(String name) {
        String tenantId = TenantContext.getCurrentTenantId();
        return scopedObjects.remove(tenantId + "_" + name);
    }
    
    // Other methods...
}

// Register custom scope
@Configuration
public class ScopeConfig {
    
    @Bean
    public static BeanFactoryPostProcessor customScopeConfigurer() {
        return beanFactory -> {
            beanFactory.registerScope("tenant", new TenantScope());
        };
    }
}

// Use custom scope
@Component
@Scope("tenant")
public class TenantConfig {
    // One instance per tenant
}
```

---

## 4. Bean Lifecycle

### Complete Lifecycle Diagram

```
Container Started
    ↓
Load Bean Definitions
    ↓
Instantiate Bean (Constructor)
    ↓
Populate Properties (Dependency Injection)
    ↓
BeanNameAware.setBeanName()
    ↓
BeanClassLoaderAware.setBeanClassLoader()
    ↓
BeanFactoryAware.setBeanFactory()
    ↓
EnvironmentAware.setEnvironment()
    ↓
ApplicationContextAware.setApplicationContext()
    ↓
BeanPostProcessor.postProcessBeforeInitialization()
    ↓
@PostConstruct
    ↓
InitializingBean.afterPropertiesSet()
    ↓
Custom init-method
    ↓
BeanPostProcessor.postProcessAfterInitialization()
    ↓
🟢 BEAN READY FOR USE
    ↓
Container Shutdown Signal
    ↓
@PreDestroy
    ↓
DisposableBean.destroy()
    ↓
Custom destroy-method
    ↓
Bean Destroyed
```

### 4.1 Initialization

#### Method 1: @PostConstruct (Recommended)

```java
@Service
public class DataService {
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void init() {
        System.out.println("Initializing data service");
        // Load initial data
        // Open connections
        // Validate configuration
    }
}
```

#### Method 2: InitializingBean Interface

```java
@Service
public class CacheService implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Cache service initialized");
        // Initialize cache
        // Warm up cache
    }
}
```

#### Method 3: Custom Init Method

```java
@Configuration
public class AppConfig {
    
    @Bean(initMethod = "initialize")
    public ConnectionPool connectionPool() {
        return new ConnectionPool();
    }
}

public class ConnectionPool {
    public void initialize() {
        System.out.println("Connection pool initialized");
        // Create connections
    }
}
```

### 4.2 Destruction

#### Method 1: @PreDestroy (Recommended)

```java
@Service
public class DatabaseService {
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Cleaning up database connections");
        // Close connections
        // Save state
        // Release resources
    }
}
```

#### Method 2: DisposableBean Interface

```java
@Service
public class FileService implements DisposableBean {
    
    @Override
    public void destroy() throws Exception {
        System.out.println("Closing file handles");
        // Close files
        // Flush buffers
    }
}
```

#### Method 3: Custom Destroy Method

```java
@Configuration
public class AppConfig {
    
    @Bean(destroyMethod = "shutdown")
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }
}
```

### 4.3 Aware Interfaces

```java
@Component
public class ApplicationContextBean implements 
        ApplicationContextAware,
        BeanNameAware,
        BeanFactoryAware,
        EnvironmentAware {
    
    private ApplicationContext applicationContext;
    private String beanName;
    private BeanFactory beanFactory;
    private Environment environment;
    
    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        this.applicationContext = ctx;
        System.out.println("ApplicationContext injected");
    }
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("Bean name: " + name);
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        System.out.println("BeanFactory injected");
    }
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        String profile = environment.getActiveProfiles()[0];
        System.out.println("Active profile: " + profile);
    }
}
```

### 4.4 BeanPostProcessor

```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) 
            throws BeansException {
        System.out.println("Before initialization: " + beanName);
        // Modify bean before init methods
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) 
            throws BeansException {
        System.out.println("After initialization: " + beanName);
        // Wrap bean in proxy if needed
        return bean;
    }
}

// Example: Auto-inject custom dependencies
@Component
public class InjectablePostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        
        // Find fields with custom annotation
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectCustom.class)) {
                field.setAccessible(true);
                try {
                    field.set(bean, createCustomDependency());
                } catch (IllegalAccessException e) {
                    throw new BeanCreationException("Failed to inject", e);
                }
            }
        }
        return bean;
    }
    
    private Object createCustomDependency() {
        return new CustomDependency();
    }
}
```

---

## 5. Dependency Resolution

### 5.1 @Qualifier - Select Specific Bean

```java
// Multiple implementations
public interface MessageSender {
    void send(String message);
}

@Component("emailSender")
public class EmailSender implements MessageSender {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

@Component("smsSender")
public class SmsSender implements MessageSender {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

// Use specific implementation
@Service
public class NotificationService {
    private final MessageSender sender;
    
    @Autowired
    public NotificationService(@Qualifier("emailSender") MessageSender sender) {
        this.sender = sender;
    }
}
```

### 5.2 @Primary - Default Bean

```java
@Component
@Primary  // Use this by default
public class EmailSender implements MessageSender {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

@Component
public class SmsSender implements MessageSender {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

// EmailSender injected by default
@Service
public class NotificationService {
    @Autowired
    private MessageSender sender;  // Gets EmailSender (Primary)
}
```

### 5.3 Inject All Implementations

```java
@Service
public class BroadcastService {
    
    // Inject all MessageSender implementations
    @Autowired
    private List<MessageSender> allSenders;
    
    @Autowired
    private Map<String, MessageSender> senderMap;  // Bean name -> Implementation
    
    public void broadcast(String message) {
        allSenders.forEach(sender -> sender.send(message));
    }
    
    public void sendVia(String senderName, String message) {
        MessageSender sender = senderMap.get(senderName);
        if (sender != null) {
            sender.send(message);
        }
    }
}
```

### 5.4 Optional Dependencies

```java
@Service
public class OptionalDependencyService {
    
    // Constructor with Optional
    private final NotificationService notificationService;
    
    @Autowired
    public OptionalDependencyService(
            @Autowired(required = false) NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    // Or use Optional<T>
    @Autowired
    private Optional<CacheService> cacheService;
    
    public void doSomething() {
        cacheService.ifPresent(cache -> cache.put("key", "value"));
    }
    
    // Or use @Nullable
    @Autowired(required = false)
    private @Nullable LoggingService loggingService;
}
```

### 5.5 @Lazy - Lazy Initialization

```java
@Service
@Lazy  // Bean created only when first accessed
public class HeavyService {
    
    public HeavyService() {
        System.out.println("Heavy service created");
        // Expensive initialization
    }
}

// Inject lazily
@Service
public class MyService {
    
    @Autowired
    @Lazy  // Proxy injected, real bean created on first use
    private HeavyService heavyService;
    
    public void useHeavyService() {
        heavyService.doSomething();  // HeavyService created here
    }
}
```

### 5.6 Circular Dependencies

```java
// ❌ BAD - Circular dependency with constructor injection
@Service
public class ServiceA {
    private final ServiceB serviceB;
    
    @Autowired
    public ServiceA(ServiceB serviceB) {  // BeanCurrentlyInCreationException!
        this.serviceB = serviceB;
    }
}

@Service
public class ServiceB {
    private final ServiceA serviceA;
    
    @Autowired
    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }
}

// ✅ SOLUTION 1 - Use setter injection
@Service
public class ServiceA {
    private ServiceB serviceB;
    
    @Autowired
    public void setServiceB(ServiceB serviceB) {
        this.serviceB = serviceB;
    }
}

// ✅ SOLUTION 2 - Use @Lazy
@Service
public class ServiceA {
    private final ServiceB serviceB;
    
    @Autowired
    public ServiceA(@Lazy ServiceB serviceB) {  // Injects proxy
        this.serviceB = serviceB;
    }
}

// ✅ SOLUTION 3 - Refactor (BEST)
// Extract shared logic to a third service
@Service
public class SharedService {
    // Common logic
}

@Service
public class ServiceA {
    @Autowired
    private SharedService sharedService;
}

@Service
public class ServiceB {
    @Autowired
    private SharedService sharedService;
}
```

---

## 6. Configuration Metadata

### 6.1 @PropertySource

```java
@Configuration
@PropertySource("classpath:application.properties")
@PropertySource("classpath:database.properties")
public class AppConfig {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version}")
    private String version;
    
    @Value("${database.url}")
    private String dbUrl;
}
```

**application.properties:**
```properties
app.name=MyApplication
app.version=1.0.0
database.url=jdbc:postgresql://localhost:5432/mydb
database.username=user
database.password=secret
```

### 6.2 @Value Injection

```java
@Component
public class ConfigBean {
    
    // Simple value
    @Value("${server.port}")
    private int port;
    
    // With default value
    @Value("${app.name:DefaultApp}")
    private String appName;
    
    // SpEL expression
    @Value("#{systemProperties['user.home']}")
    private String userHome;
    
    // List
    @Value("${app.allowed.origins}")
    private List<String> allowedOrigins;
    
    // Map
    @Value("#{${app.config.map}}")
    private Map<String, String> configMap;
}
```

**application.properties:**
```properties
server.port=8080
app.allowed.origins=http://localhost:3000,http://example.com
app.config.map={'key1':'value1','key2':'value2'}
```

### 6.3 Environment

```java
@Service
public class EnvironmentService {
    
    @Autowired
    private Environment environment;
    
    public void printConfig() {
        // Get property
        String appName = environment.getProperty("app.name");
        
        // With default
        String version = environment.getProperty("app.version", "1.0.0");
        
        // With type conversion
        Integer port = environment.getProperty("server.port", Integer.class);
        
        // Check if property exists
        boolean hasProperty = environment.containsProperty("app.name");
        
        // Get required property (throws if missing)
        String required = environment.getRequiredProperty("database.url");
        
        // Get active profiles
        String[] profiles = environment.getActiveProfiles();
        System.out.println("Active profiles: " + Arrays.toString(profiles));
        
        // Get default profiles
        String[] defaultProfiles = environment.getDefaultProfiles();
    }
}
```

### 6.4 @ConfigurationProperties

```java
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String name;
    private String version;
    private Security security = new Security();
    private List<String> allowedOrigins;
    
    // Getters and setters
    
    public static class Security {
        private String jwtSecret;
        private long jwtExpiration;
        
        // Getters and setters
    }
}
```

**application.yml:**
```yaml
app:
  name: MyApplication
  version: 1.0.0
  security:
    jwt-secret: mySecretKey
    jwt-expiration: 3600000
  allowed-origins:
    - http://localhost:3000
    - http://example.com
```

**Usage:**
```java
@Service
public class AuthService {
    
    @Autowired
    private AppProperties appProperties;
    
    public String generateToken() {
        String secret = appProperties.getSecurity().getJwtSecret();
        long expiration = appProperties.getSecurity().getJwtExpiration();
        // Generate token
    }
}
```

---

## 7. ApplicationContext

### 7.1 Types of ApplicationContext

```java
// 1. Annotation-based
ApplicationContext context = 
    new AnnotationConfigApplicationContext(AppConfig.class);

// 2. XML-based
ApplicationContext context = 
    new ClassPathXmlApplicationContext("applicationContext.xml");

// 3. File system XML
ApplicationContext context = 
    new FileSystemXmlApplicationContext("/path/to/beans.xml");

// 4. Web application (Servlet)
WebApplicationContext webContext = 
    WebApplicationContextUtils.getWebApplicationContext(servletContext);
```

### 7.2 ApplicationContext Features

```java
@Service
public class ContextAwareService implements ApplicationContextAware {
    
    private ApplicationContext context;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }
    
    public void demonstrateFeatures() {
        // 1. Bean retrieval
        UserService service = context.getBean(UserService.class);
        UserService byName = context.getBean("userService", UserService.class);
        
        // 2. Check bean existence
        boolean exists = context.containsBean("userService");
        
        // 3. Get all beans of type
        Map<String, UserService> allServices = 
            context.getBeansOfType(UserService.class);
        
        // 4. Get bean names
        String[] names = context.getBeanDefinitionNames();
        
        // 5. Get bean definition count
        int count = context.getBeanDefinitionCount();
        
        // 6. Publish events
        context.publishEvent(new UserCreatedEvent(this, new User()));
        
        // 7. Access environment
        Environment env = context.getEnvironment();
        String profile = env.getActiveProfiles()[0];
        
        // 8. Access resources
        Resource resource = context.getResource("classpath:data.txt");
        
        // 9. Get parent context (if exists)
        ApplicationContext parent = context.getParent();
        
        // 10. Get display name
        String displayName = context.getDisplayName();
    }
}
```

### 7.3 Hierarchical Contexts

```java
// Parent context
ApplicationContext parentContext = 
    new AnnotationConfigApplicationContext(ParentConfig.class);

// Child context
AnnotationConfigApplicationContext childContext = 
    new AnnotationConfigApplicationContext();
childContext.setParent(parentContext);  // Set parent
childContext.register(ChildConfig.class);
childContext.refresh();

// Child can access parent beans, but not vice versa
```

---

## 8. Resource Management

### 8.1 Resource Types

```java
@Service
public class ResourceService {
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    public void loadResources() throws IOException {
        // 1. Classpath resource
        Resource classpathResource = resourceLoader.getResource(
            "classpath:config/application.properties"
        );
        
        // 2. File system resource
        Resource fileResource = resourceLoader.getResource(
            "file:/home/user/data.txt"
        );
        
        // 3. URL resource
        Resource urlResource = resourceLoader.getResource(
            "https://example.com/api/data.json"
        );
        
        // Read resource
        if (classpathResource.exists() && classpathResource.isReadable()) {
            try (InputStream is = classpathResource.getInputStream()) {
                String content = new String(is.readAllBytes());
                System.out.println(content);
            }
        }
        
        // Get file info
        System.out.println("Filename: " + classpathResource.getFilename());
        System.out.println("Description: " + classpathResource.getDescription());
        System.out.println("URI: " + classpathResource.getURI());
    }
}
```

### 8.2 Inject Resources

```java
@Service
public class ConfigService {
    
    // Inject single resource
    @Value("classpath:application.properties")
    private Resource configFile;
    
    // Inject multiple resources
    @Value("classpath*:config/*.properties")
    private Resource[] configFiles;
    
    @PostConstruct
    public void loadConfig() throws IOException {
        // Load main config
        Properties props = new Properties();
        try (InputStream is = configFile.getInputStream()) {
            props.load(is);
        }
        
        // Load all configs
        for (Resource resource : configFiles) {
            System.out.println("Loading: " + resource.getFilename());
            try (InputStream is = resource.getInputStream()) {
                props.load(is);
            }
        }
    }
}
```

### 8.3 Resource Patterns

```java
@Service
public class PatternResourceService {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    public void loadMatchingResources() throws IOException {
        // Load all .properties files
        Resource[] resources = resolver.getResources(
            "classpath*:config/*.properties"
        );
        
        // Recursive search
        Resource[] xmlFiles = resolver.getResources(
            "classpath:config/**/*.xml"
        );
        
        // Search in all JARs
        Resource[] = metaInf = resolver.getResources("classpath*:META-INF/spring.factories");

        for (Resource resource : resources) {
           System.out.println("Found: " + resource.getURI());
        }
    }
}
```

---

## 9. Environment & Profiles

### 9.1 Profiles
```java
// Define profile-specific beans
@Configuration
@Profile("dev")
public class DevConfig {
    
    @Bean
    public DataSource dataSource() {
        // H2 in-memory database for development
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }
}

@Configuration
@Profile("prod")
public class ProdConfig {
    
    @Bean
    public DataSource dataSource() {
        // Production database
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://prod-db:5432/mydb");
        return ds;
    }
}

// Multiple profiles
@Configuration
@Profile({"dev", "test"})
public class NonProdConfig {
}

// NOT profile
@Configuration
@Profile("!prod")
public class NonProductionConfig {
}
```

**Activate Profiles:**
```java
// 1. Programmatically
AnnotationConfigApplicationContext context = 
    new AnnotationConfigApplicationContext();
context.getEnvironment().setActiveProfiles("dev");
context.register(AppConfig.class);
context.refresh();

// 2. Via system property
-Dspring.profiles.active=dev,debug

// 3. Via application.properties
spring.profiles.active=dev

// 4. Via environment variable
export SPRING_PROFILES_ACTIVE=dev
```

### 9.2 @Conditional
```java
// Custom condition
public class WindowsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, 
                          AnnotatedTypeMetadata metadata) {
        return context.getEnvironment()
            .getProperty("os.name")
            .toLowerCase()
            .contains("windows");
    }
}

@Configuration
public class ConditionalConfig {
    
    @Bean
    @Conditional(WindowsCondition.class)
    public FileService windowsFileService() {
        return new WindowsFileService();
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.feature.enabled", havingValue = "true")
    public FeatureService featureService() {
        return new FeatureService();
    }
    
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource defaultDataSource() {
        return new EmbeddedDatabaseBuilder().build();
    }
    
    @Bean
    @ConditionalOnClass(name = "com.example.SomeClass")
    public SomeService someService() {
        return new SomeService();
    }
}
```

---

## 10. Validation & Data Binding

### 10.1 JSR-303/380 Validation
```java
public class User {
    
    @NotNull(message = "ID cannot be null")
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @Email(message = "Invalid email format")
    @NotBlank
    private String email;
    
    @Min(value = 18, message = "Must be at least 18 years old")
    @Max(value = 150, message = "Age cannot exceed 150")
    private Integer age;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String phoneNumber;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    @Future(message = "Appointment must be in the future")
    private LocalDateTime appointmentTime;
    
    @Valid  // Cascade validation
    @NotNull
    private Address address;
}

public class Address {
    @NotBlank
    private String street;
    
    @NotBlank
    @Size(min = 2, max = 100)
    private String city;
    
    @NotBlank
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$")
    private String zipCode;
}
```

### 10.2 Custom Validator
```java
// Custom annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator implementation
@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;  // Use @NotNull for null checking
        }
        return !userRepository.existsByEmail(email);
    }
}

// Usage
public class CreateUserRequest {
    @UniqueEmail
    @Email
    private String email;
}
```

### 10.3 Programmatic Validation
```java
@Service
public class ValidationService {
    
    @Autowired
    private Validator validator;
    
    public void validateUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        if (!violations.isEmpty()) {
            violations.forEach(violation -> 
                System.out.println(violation.getPropertyPath() + ": " + 
                                 violation.getMessage())
            );
            throw new ValidationException("User validation failed");
        }
    }
    
    // Validate specific property
    public void validateProperty(User user, String propertyName) {
        Set<ConstraintViolation<User>> violations = 
            validator.validateProperty(user, propertyName);
        
        if (!violations.isEmpty()) {
            throw new ValidationException("Validation failed for: " + propertyName);
        }
    }
    
    // Validate with groups
    public void validateForCreation(User user) {
        Set<ConstraintViolation<User>> violations = 
            validator.validate(user, CreateGroup.class);
    }
}
```

### 10.4 Validation Groups
```java
public interface CreateGroup {}
public interface UpdateGroup {}

public class User {
    @NotNull(groups = UpdateGroup.class)
    private Long id;
    
    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    @NotNull(groups = CreateGroup.class)
    @Null(groups = UpdateGroup.class)
    private String password;
}

// Usage
@RestController
public class UserController {
    
    @PostMapping("/users")
    public User create(@Validated(CreateGroup.class) @RequestBody User user) {
        // id not required, password required
        return userService.save(user);
    }
    
    @PutMapping("/users/{id}")
    public User update(@Validated(UpdateGroup.class) @RequestBody User user) {
        // id required, password should be null
        return userService.update(user);
    }
}
```

### 10.5 Data Binding & Type Conversion
```java
@Configuration
public class ConversionConfig {
    
    // Custom Converter
    @Bean
    public ConversionService conversionService() {
        DefaultConversionService service = new DefaultConversionService();
        service.addConverter(new StringToDateConverter());
        service.addConverter(new StringToEnumConverter());
        return service;
    }
}

// String to Date converter
public class StringToDateConverter implements Converter<String, Date> {
    
    @Override
    public Date convert(String source) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(source);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }
}

// Formatter (for display)
public class DateFormatter implements Formatter<Date> {
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    @Override
    public Date parse(String text, Locale locale) throws ParseException {
        return sdf.parse(text);
    }
    
    @Override
    public String print(Date date, Locale locale) {
        return sdf.format(date);
    }
}

// Usage in controller
@Controller
public class UserController {
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Register custom editor
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
            new SimpleDateFormat("yyyy-MM-dd"), true
        ));
    }
    
    @GetMapping("/users/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "userDetails";
    }
}
```

---

## 11. SpEL (Spring Expression Language)

### 11.1 Basic Syntax
```java
@Component
public class SpELExamples {
    
    // Literal values
    @Value("#{100}")
    private int literalInt;
    
    @Value("#{'Hello World'}")
    private String literalString;
    
    @Value("#{true}")
    private boolean literalBoolean;
    
    // Arithmetic operations
    @Value("#{10 + 20}")
    private int sum;
    
    @Value("#{10 * 5}")
    private int product;
    
    @Value("#{100 / 4}")
    private int division;
    
    // Relational operators
    @Value("#{10 > 5}")
    private boolean greater;
    
    @Value("#{10 == 10}")
    private boolean equal;
    
    // Logical operators
    @Value("#{true and false}")
    private boolean andOperation;
    
    @Value("#{true or false}")
    private boolean orOperation;
    
    @Value("#{not false}")
    private boolean notOperation;
}
```

### 11.2 Property References
```java
@Component
public class PropertyReferences {
    
    // System properties
    @Value("#{systemProperties['java.home']}")
    private String javaHome;
    
    @Value("#{systemProperties['user.dir']}")
    private String userDir;
    
    // Environment variables
    @Value("#{systemEnvironment['PATH']}")
    private String path;
    
    // Application properties
    @Value("${app.name}")
    private String appName;
    
    @Value("#{environment.getProperty('app.version')}")
    private String version;
}
```

### 11.3 Bean References
```java
@Component
public class BeanReferences {
    
    // Reference another bean
    @Value("#{userService}")
    private UserService userService;
    
    // Call bean method
    @Value("#{userService.getUserCount()}")
    private int userCount;
    
    // Access bean property
    @Value("#{configService.databaseUrl}")
    private String dbUrl;
    
    // Chain method calls
    @Value("#{userService.getCurrentUser().getName()}")
    private String currentUserName;
}
```

### 11.4 Collections
```java
@Component
public class CollectionOperations {
    
    // List
    @Value("#{{'A', 'B', 'C'}}")
    private List<String> list;
    
    // Map
    @Value("#{{key1: 'value1', key2: 'value2'}}")
    private Map<String, String> map;
    
    // Array
    @Value("#{new int[]{1,2,3,4,5}}")
    private int[] array;
    
    // Collection selection (filter)
    @Value("#{userService.getUsers().?[age > 18]}")
    private List<User> adults;
    
    // Collection projection (map)
    @Value("#{userService.getUsers().![name]}")
    private List<String> names;
    
    // First match
    @Value("#{userService.getUsers().^[age > 18]}")
    private User firstAdult;
    
    // Last match
    @Value("#{userService.getUsers().$[age > 18]}")
    private User lastAdult;
}
```

### 11.5 Conditional Expressions
```java
@Component
public class ConditionalExpressions {
    
    // Ternary operator
    @Value("#{userService.isLoggedIn() ? 'Welcome' : 'Please login'}")
    private String greeting;
    
    // Elvis operator (null-safe)
    @Value("#{userService.getCurrentUser()?.name ?: 'Guest'}")
    private String username;
    
    // Safe navigation
    @Value("#{userService.getCurrentUser()?.address?.city}")
    private String city;
    
    // Complex condition
    @Value("#{userService.getUserCount() > 100 ? 'Many users' : 'Few users'}")
    private String userCountStatus;
}
```

### 11.6 Programmatic SpEL
```java
@Service
public class SpELService {
    
    private final SpelExpressionParser parser = new SpelExpressionParser();
    
    public void demonstrateSpEL() {
        // Simple expression
        Expression exp = parser.parseExpression("'Hello World'");
        String message = (String) exp.getValue();
        System.out.println(message);  // Hello World
        
        // Expression with context
        User user = new User("John", 30);
        exp = parser.parseExpression("name");
        String name = (String) exp.getValue(user);
        System.out.println(name);  // John
        
        // Method invocation
        exp = parser.parseExpression("name.toUpperCase()");
        String upper = (String) exp.getValue(user);
        System.out.println(upper);  // JOHN
        
        // With StandardEvaluationContext
        StandardEvaluationContext context = new StandardEvaluationContext(user);
        context.setVariable("bonus", 100);
        
        exp = parser.parseExpression("age > 25 ? #bonus : 0");
        Integer bonus = exp.getValue(context, Integer.class);
        System.out.println(bonus);  // 100
    }
    
    public void collectionOperations() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("numbers", numbers);
        
        // Filter
        Expression exp = parser.parseExpression("#numbers.?[#this > 5]");
        List<Integer> filtered = (List<Integer>) exp.getValue(context);
        System.out.println(filtered);  // [6, 7, 8, 9, 10]
        
        // Map/Project
        exp = parser.parseExpression("#numbers.![#this * 2]");
        List<Integer> doubled = (List<Integer>) exp.getValue(context);
        System.out.println(doubled);  // [2, 4, 6, 8, 10, 12, 14, 16, 18, 20]
    }
}
```

---

## 12. AOP (Aspect Oriented Programming)

### 12.1 Enable AOP
```java
@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
}
```

### 12.2 Basic Aspect
```java
@Aspect
@Component
public class LoggingAspect {
    
    // Before advice
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Before: " + joinPoint.getSignature().getName());
        System.out.println("Arguments: " + Arrays.toString(joinPoint.getArgs()));
    }
    
    // After returning advice
    @AfterReturning(
        pointcut = "execution(* com.example.service.*.*(..))",
        returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("After returning: " + joinPoint.getSignature().getName());
        System.out.println("Result: " + result);
    }
    
    // After throwing advice
    @AfterThrowing(
        pointcut = "execution(* com.example.service.*.*(..))",
        throwing = "error"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        System.out.println("Exception in: " + joinPoint.getSignature().getName());
        System.out.println("Exception: " + error.getMessage());
    }
    
    // After (finally) advice
    @After("execution(* com.example.service.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("After: " + joinPoint.getSignature().getName());
    }
    
    // Around advice
    @Around("execution(* com.example.service.*.*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("Before method: " + pjp.getSignature().getName());
        
        long start = System.currentTimeMillis();
        
        Object result = pjp.proceed();  // Execute the method
        
        long time = System.currentTimeMillis() - start;
        System.out.println("After method: " + pjp.getSignature().getName());
        System.out.println("Execution time: " + time + "ms");
        
        return result;
    }
}
```

### 12.3 Pointcut Expressions
```java
@Aspect
@Component
public class PointcutExamples {
    
    // Named pointcut
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() {}
    
    // Use named pointcut
    @Before("serviceMethods()")
    public void beforeServiceMethod() {
        System.out.println("Before service method");
    }
    
    // Specific method
    @Pointcut("execution(* com.example.service.UserService.createUser(..))")
    public void createUserMethod() {}
    
    // Any method in package
    @Pointcut("execution(* com.example.service..*.*(..))")
    public void anyServiceMethod() {}
    
    // Methods with specific annotation
    @Pointcut("@annotation(com.example.annotation.Loggable)")
    public void loggableMethods() {}
    
    // Classes with specific annotation
    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceClasses() {}
    
    // Methods with specific parameter
    @Pointcut("execution(* *(com.example.model.User))")
    public void methodsWithUserParameter() {}
    
    // Combine pointcuts
    @Pointcut("serviceMethods() && loggableMethods()")
    public void loggableServiceMethods() {}
    
    // Bean name pattern
    @Pointcut("bean(*Service)")
    public void serviceBeans() {}
}
```

### 12.4 Custom Annotation for AOP
```java
// Define custom annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {
}

// Aspect for custom annotation
@Aspect
@Component
public class TimingAspect {
    
    @Around("@annotation(Timed)")
    public Object measureTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        
        Object result = pjp.proceed();
        
        long time = System.currentTimeMillis() - start;
        System.out.println(pjp.getSignature().getName() + " took " + time + "ms");
        
        return result;
    }
}

// Usage
@Service
public class UserService {
    
    @Timed
    public User createUser(User user) {
        // Method logic
        return user;
    }
}
```

### 12.5 Real-World AOP Examples
```java
// Security/Authorization Aspect
@Aspect
@Component
public class SecurityAspect {
    
    @Before("@annotation(requiresRole)")
    public void checkRole(JoinPoint joinPoint, RequiresRole requiresRole) {
        String requiredRole = requiresRole.value();
        User currentUser = SecurityContextHolder.getCurrentUser();
        
        if (!currentUser.hasRole(requiredRole)) {
            throw new AccessDeniedException("Insufficient permissions");
        }
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    String value();
}

// Transaction Management Aspect (similar to @Transactional)
@Aspect
@Component
public class TransactionAspect {
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object manageTransaction(ProceedingJoinPoint pjp) throws Throwable {
        TransactionStatus status = transactionManager.getTransaction(
            new DefaultTransactionDefinition()
        );
        
        try {
            Object result = pjp.proceed();
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
}

// Caching Aspect
@Aspect
@Component
public class CachingAspect {
    
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    @Around("@annotation(Cacheable)")
    public Object cache(ProceedingJoinPoint pjp) throws Throwable {
        String key = pjp.getSignature().toString() + 
                    Arrays.toString(pjp.getArgs());
        
        if (cache.containsKey(key)) {
            System.out.println("Cache hit: " + key);
            return cache.get(key);
        }
        
        Object result = pjp.proceed();
        cache.put(key, result);
        System.out.println("Cache miss: " + key);
        
        return result;
    }
}

// Retry Aspect
@Aspect
@Component
public class RetryAspect {
    
    @Around("@annotation(retryable)")
    public Object retry(ProceedingJoinPoint pjp, Retryable retryable) throws Throwable {
        int maxAttempts = retryable.maxAttempts();
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            try {
                return pjp.proceed();
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw e;
                }
                System.out.println("Retry attempt " + attempt + " of " + maxAttempts);
                Thread.sleep(retryable.delay());
            }
        }
        
        throw new RuntimeException("Max retry attempts exceeded");
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {
    int maxAttempts() default 3;
    long delay() default 1000;
}
```

---

## 13. Events

### 13.1 Built-in Events
```java
@Component
public class ApplicationEventListener implements ApplicationListener<ContextRefreshedEvent> {
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("Context refreshed!");
        // Perform startup tasks
    }
}

// Or use @EventListener
@Component
public class EventListeners {
    
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        System.out.println("Context refreshed");
    }
    
    @EventListener
    public void handleContextStarted(ContextStartedEvent event) {
        System.out.println("Context started");
    }
    
    @EventListener
    public void handleContextStopped(ContextStoppedEvent event) {
        System.out.println("Context stopped");
    }
    
    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        System.out.println("Context closed");
    }
}
```

### 13.2 Custom Events
```java
// Define custom event
public class UserCreatedEvent extends ApplicationEvent {
    private final User user;
    
    public UserCreatedEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
}

// Publish event
@Service
public class UserService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public User createUser(User user) {
        // Save user
        User savedUser = userRepository.save(user);
        
        // Publish event
        eventPublisher.publishEvent(new UserCreatedEvent(this, savedUser));
        
        return savedUser;
    }
}

// Listen to event
@Component
public class UserEventListener {
    
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        User user = event.getUser();
        System.out.println("User created: " + user.getName());
        
        // Send welcome email
        // Create audit log
        // Trigger notifications
    }
    
    // Async event handling
    @EventListener
    @Async
    public void handleUserCreatedAsync(UserCreatedEvent event) {
        // Handle asynchronously
    }
    
    // Conditional event handling
    @EventListener(condition = "#event.user.age > 18")
    public void handleAdultUserCreated(UserCreatedEvent event) {
        // Only for adult users
    }
}
```

### 13.3 Transaction-Bound Events
```java
@Component
public class TransactionalEventListener {
    
    // Execute after transaction commits
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(UserCreatedEvent event) {
        // Send email only if transaction succeeds
        emailService.sendWelcomeEmail(event.getUser());
    }
    
    // Execute after transaction rollback
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleAfterRollback(UserCreatedEvent event) {
        // Clean up if transaction fails
    }
    
    // Execute before transaction commit
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleBeforeCommit(UserCreatedEvent event) {
        // Validation before commit
    }
    
    // Execute after transaction completion (commit or rollback)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleAfterCompletion(UserCreatedEvent event) {
        // Cleanup regardless of result
    }
}
```

---

## 14. Interview Questions

### Q1: What is Dependency Injection?
**Answer:** DI is a design pattern where object dependencies are provided by an external entity (Spring container) rather than the object creating them itself. Benefits: loose coupling, easier testing, better maintainability.

### Q2: Constructor vs Setter vs Field Injection?
**Answer:**
- **Constructor (Recommended):** Immutable, required dependencies clear, easy to test
- **Setter:** Optional dependencies, can change at runtime
- **Field:** Not recommended, hard to test, cannot be final

### Q3: What are bean scopes?
**Answer:**
- **Singleton:** One instance per container (default)
- **Prototype:** New instance per request
- **Request:** One per HTTP request (web)
- **Session:** One per HTTP session (web)
- **Application:** One per ServletContext (web)

### Q4: Explain bean lifecycle
**Answer:** Instantiation → Populate Properties → BeanNameAware → BeanFactoryAware → ApplicationContextAware → BeanPostProcessor (before) → @PostConstruct → InitializingBean → init-method → BeanPostProcessor (after) → Ready → @PreDestroy → DisposableBean → destroy-method

### Q5: How to resolve circular dependencies?
**Answer:**
1. Use setter injection (Spring exposes partially initialized beans)
2. Use @Lazy on one dependency
3. Refactor code (best solution - extract shared logic)

### Q6: @Qualifier vs @Primary?
**Answer:**
- **@Qualifier:** Specify exact bean to inject when multiple candidates
- **@Primary:** Mark default bean when multiple candidates
- Use @Primary for default, @Qualifier for exceptions

### Q7: What is ApplicationContext?
**Answer:** Advanced Spring container that extends BeanFactory. Provides: bean lifecycle management, AOP, event publication, i18n, resource loading, and more.

### Q8: Explain @Value annotation
**Answer:** Injects values from properties files, system properties, or SpEL expressions into fields/methods. Supports default values: `@Value("${property:default}")`

### Q9: What is AOP? Explain advice types
**Answer:** AOP separates cross-cutting concerns (logging, security, transactions) from business logic.
- **@Before:** Before method execution
- **@After:** After method execution (finally)
- **@AfterReturning:** After successful execution
- **@AfterThrowing:** After exception
- **@Around:** Wrap method execution (most powerful)

### Q10: Explain Spring profiles
**Answer:** Profiles allow different configurations for different environments (dev, test, prod). Activate via: spring.profiles.active property, -Dspring.profiles.active=dev, or programmatically.

---

## 📝 Notes Section

_Add your observations, questions, and additional examples here:_

---

---

---

**Good luck with your Spring Core assessment! 🚀**

*Remember: Practice coding these examples. Understanding comes from doing!*</parameter>

