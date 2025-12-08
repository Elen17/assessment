# Spring Framework Assessment - Complete Topics Index

## How to Use This Guide

Each topic has been prepared as a comprehensive README. I've provided detailed guides for the first two topics above. Below is an index of all topics with key points for quick review.

---

## ✅ COMPLETED DETAILED GUIDES

1. **IoC Container** - Complete with examples, lifecycle, DI types, scopes
2. **Resources** - Complete with Resource types, patterns, practical examples

---

## 📋 CORE TOPICS

### 3. Validation, Data Binding, and Type Conversion

**Key Concepts:**
- JSR-303/380 Bean Validation (@NotNull, @Size, @Email)
- Spring Validator interface
- DataBinder and PropertyEditor
- ConversionService and Converter
- Formatter for display formatting

**Quick Example:**
```java
public class User {
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;
    
    @Email
    private String email;
    
    @Min(18)
    private int age;
}

@RestController
public class UserController {
    @PostMapping("/users")
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        // Validation happens automatically
    }
}
```

**Interview Points:**
- @Valid vs @Validated
- BindingResult
- Custom validators
- PropertyEditor vs Converter
- @InitBinder

---

### 4. Spring Expression Language (SpEL)

**Key Concepts:**
- Expression syntax: `#{expression}`
- Accessing properties: `@Value("#{systemProperties['user.home']}")`
- Bean references: `#{beanName.property}`
- Collection selection/projection
- Ternary operator and Elvis operator

**Quick Example:**
```java
@Value("#{systemProperties['java.home']}")
private String javaHome;

@Value("#{'Hello ' + environment.getProperty('user.name')}")
private String greeting;

@Value("#{userService.getUserCount()}")
private int userCount;

@Value("#{config.enabled ? 'ON' : 'OFF'}")
private String status;
```

**Interview Points:**
- SpEL syntax
- Bean method invocation
- Safe navigation (?.)
- Collection filtering
- Template expressions

---

### 5. Aspect Oriented Programming (AOP)

**Key Concepts:**
- Cross-cutting concerns (logging, security, transactions)
- Join points, Pointcuts, Advice
- @Aspect, @Before, @After, @Around, @AfterReturning, @AfterThrowing
- Pointcut expressions

**Quick Example:**
```java
@Aspect
@Component
public class LoggingAspect {
    
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Executing: " + joinPoint.getSignature());
    }
    
    @Around("@annotation(com.example.Timed)")
    public Object measureTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long time = System.currentTimeMillis() - start;
        System.out.println("Execution time: " + time + "ms");
        return result;
    }
}
```

**Interview Points:**
- AOP concepts (Aspect, Advice, Pointcut, Join Point)
- Advice types
- Pointcut expressions
- @EnableAspectJAutoProxy
- Proxy patterns (JDK vs CGLIB)

---

### 6. Spring AOP APIs

**Key Concepts:**
- ProxyFactory
- Advisor and PointcutAdvisor
- MethodInterceptor
- Programmatic proxy creation
- IntroductionAdvisor

**Quick Example:**
```java
ProxyFactory factory = new ProxyFactory(targetObject);
factory.addAdvice(new MethodInterceptor() {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Before method");
        Object result = invocation.proceed();
        System.out.println("After method");
        return result;
    }
});
MyService proxy = (MyService) factory.getProxy();
```

---

### 7. Null-safety

**Key Concepts:**
- @NonNull, @Nullable annotations
- @NonNullApi, @NonNullFields (package-level)
- IDE integration
- Kotlin null-safety support

**Quick Example:**
```java
@NonNullApi
package com.example.service;

public class UserService {
    public User findById(@NonNull Long id) {
        // id is guaranteed non-null
    }
    
    @Nullable
    public String getOptionalField() {
        // May return null
    }
}
```

---

### 8. Data Buffers and Codecs

**Key Concepts:**
- DataBuffer abstraction
- DataBufferFactory
- Encoder and Decoder
- WebFlux reactive streams
- Jackson2JsonEncoder/Decoder

**Quick Example:**
```java
@Configuration
public class CodecConfig {
    
    @Bean
    public CodecConfigurer codecConfigurer() {
        CodecConfigurer configurer = CodecConfigurer.create();
        configurer.defaultCodecs().jackson2JsonEncoder(
            new Jackson2JsonEncoder(objectMapper())
        );
        return configurer;
    }
}
```

---

## 🧪 TESTING TOPICS

### 9. Unit Testing

**Key Concepts:**
- @MockBean, @SpyBean
- MockMvc for controller testing
- @WebMvcTest, @DataJpaTest, @JsonTest
- Mockito integration
- TestRestTemplate

**Quick Example:**
```java
@SpringBootTest
class UserServiceTest {
    
    @MockBean
    private UserRepository repository;
    
    @Autowired
    private UserService service;
    
    @Test
    void testFindUser() {
        User user = new User("John");
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        
        User found = service.findById(1L);
        assertEquals("John", found.getName());
    }
}
```

---

### 10. Integration Testing

**Key Concepts:**
- @SpringBootTest
- Test slices (@WebMvcTest, @DataJpaTest)
- @TestConfiguration
- TestContainers
- @Sql for database setup
- @DirtiesContext

**Quick Example:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateUser() throws Exception {
        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"John\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John"));
    }
}
```

---

## 💾 DATA ACCESS TOPICS

### 11. Transaction Management

**Key Concepts:**
- @Transactional
- Propagation levels (REQUIRED, REQUIRES_NEW, NESTED)
- Isolation levels
- Rollback rules
- PlatformTransactionManager
- Programmatic transactions

**Quick Example:**
```java
@Service
public class OrderService {
    
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class
    )
    public void createOrder(Order order) {
        orderRepository.save(order);
        paymentService.processPayment(order);
        // Rolls back if any exception
    }
}
```

---

### 12. DAO Support

**Key Concepts:**
- @Repository
- DataAccessException hierarchy
- PersistenceExceptionTranslationPostProcessor
- Template classes (JdbcTemplate, HibernateTemplate)

**Quick Example:**
```java
@Repository
public class UserDaoImpl implements UserDao {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public User findById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new UserRowMapper(),
            id
        );
    }
}
```

---

### 13. Data Access with JDBC

**Key Concepts:**
- JdbcTemplate
- NamedParameterJdbcTemplate
- SimpleJdbcInsert
- RowMapper, ResultSetExtractor
- Batch operations

**Quick Example:**
```java
@Repository
public class ProductRepository {
    
    @Autowired
    private JdbcTemplate jdbc;
    
    public List<Product> findAll() {
        return jdbc.query(
            "SELECT * FROM products",
            (rs, rowNum) -> new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getBigDecimal("price")
            )
        );
    }
    
    public void batchInsert(List<Product> products) {
        jdbc.batchUpdate(
            "INSERT INTO products (name, price) VALUES (?, ?)",
            new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) {
                    Product p = products.get(i);
                    ps.setString(1, p.getName());
                    ps.setBigDecimal(2, p.getPrice());
                }
                public int getBatchSize() {
                    return products.size();
                }
            }
        );
    }
}
```

---

### 14. Object Relational Mapping (ORM)

**Key Concepts:**
- Spring Data JPA
- @Entity, @Table, @Column
- EntityManager
- JpaRepository
- Query methods, @Query
- Specifications

**Quick Example:**
```java
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    
    @OneToMany(mappedBy = "user")
    private List<Order> orders;
}

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContaining(String name);
    
    @Query("SELECT u FROM User u WHERE u.age > :age")
    List<User> findAdults(@Param("age") int age);
}
```

---

### 15. Marshalling XML

**Key Concepts:**
- JAXB (Java Architecture for XML Binding)
- @XmlRootElement, @XmlElement
- Jaxb2Marshaller
- MarshallingHttpMessageConverter
- XStream integration

**Quick Example:**
```java
@XmlRootElement
public class User {
    @XmlElement
    private String name;
    
    @XmlElement
    private int age;
}

@Configuration
public class XmlConfig {
    
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(User.class);
        return marshaller;
    }
}
```

---

## 🌐 WEB SERVLET TOPICS

### 16. Spring Web MVC

**Key Concepts:**
- DispatcherServlet
- @Controller, @RestController
- @RequestMapping, @GetMapping, @PostMapping
- @PathVariable, @RequestParam, @RequestBody
- @ResponseBody, ResponseEntity
- ViewResolver
- Exception handling (@ExceptionHandler, @ControllerAdvice)

**Quick Example:**

```java

@RestController
@RequestMapping("/org/example/springcore/service/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User created = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
```

---

### 17. REST Clients

**Key Concepts:**
- RestTemplate (legacy)
- WebClient (reactive)
- RestClient (Spring 6+)
- HttpInterface clients
- Error handling

**Quick Example:**
```java
// RestTemplate
@Service
public class ApiClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUser(Long id) {
        return restTemplate.getForObject(
            "https://api.example.com/users/{id}",
            User.class,
            id
        );
    }
}

// WebClient (Reactive)
@Service
public class ReactiveApiClient {
    
    private final WebClient webClient;
    
    public ReactiveApiClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.example.com").build();
    }
    
    public Mono<User> getUser(Long id) {
        return webClient.get()
            .uri("/users/{id}", id)
            .retrieve()
            .bodyToMono(User.class);
    }
}
```

---

### 18. Testing (Web)

**Key Concepts:**
- MockMvc
- @WebMvcTest
- @AutoConfigureMockMvc
- TestRestTemplate
- WebTestClient (for WebFlux)

**Quick Example:**

```java

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @Test
    void testGetUser() throws Exception {
        User user = new User(1L, "John");
        when(service.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/org/example/springcore/service/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }
}
```

---

### 19. WebSockets

**Key Concepts:**
- @EnableWebSocketMessageBroker
- STOMP protocol
- @MessageMapping
- SimpMessagingTemplate
- @SubscribeMapping, @SendTo

**Quick Example:**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}

@Controller
public class ChatController {
    
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        return message;
    }
}
```

---

## 📝 Quick Reference: Common Annotations

### Core
- `@Component`, `@Service`, `@Repository`, `@Controller`
- `@Autowired`, `@Qualifier`, `@Primary`
- `@Configuration`, `@Bean`
- `@Value`, `@PropertySource`
- `@Scope`, `@Lazy`
- `@PostConstruct`, `@PreDestroy`

### Web
- `@RestController`, `@RequestMapping`
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- `@PathVariable`, `@RequestParam`, `@RequestBody`
- `@ResponseStatus`, `@ExceptionHandler`
- `@CrossOrigin`, `@ControllerAdvice`

### Data
- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- `@Transactional`, `@EnableTransactionManagement`
- `@Query`, `@Modifying`
- `@Repository`, `@EntityGraph`

### Testing
- `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`
- `@MockBean`, `@SpyBean`
- `@Test`, `@BeforeEach`, `@AfterEach`
- `@Sql`, `@DirtiesContext`

### AOP
- `@Aspect`, `@EnableAspectJAutoProxy`
- `@Before`, `@After`, `@Around`
- `@AfterReturning`, `@AfterThrowing`

### Validation
- `@Valid`, `@Validated`
- `@NotNull`, `@NotBlank`, `@Size`
- `@Email`, `@Pattern`, `@Min`, `@Max`

---

## 🎯 Study Strategy

1. **IoC Container** - Master first (foundation)
2. **Data Access** - JDBC → JPA → Transactions
3. **Web MVC** - Controllers → REST → Testing
4. **AOP** - Cross-cutting concerns
5. **Advanced** - SpEL, Resources, WebSockets

---

## 📚 Additional Resources

- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Baeldung Spring Tutorials](https://www.baeldung.com/spring-tutorial)

---

## Notes Section

_Would you like me to create detailed guides for specific topics? Just let me know which ones!_

---
