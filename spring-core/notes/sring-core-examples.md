// ============================================
// 1. DEPENDENCY INJECTION - CONSTRUCTOR INJECTION
// ============================================

// Service interface
interface EmailService {
void sendEmail(String to, String message);
}

// Implementation
class EmailServiceImpl implements EmailService {
@Override
public void sendEmail(String to, String message) {
System.out.println("Sending email to: " + to);
System.out.println("Message: " + message);
}
}

// User service with constructor injection
@Service
class UserService {
private final EmailService emailService;

    @Autowired
    public UserService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public void registerUser(String email) {
        System.out.println("Registering user: " + email);
        emailService.sendEmail(email, "Welcome to our platform!");
    }
}

// ============================================
// 2. DEPENDENCY INJECTION - SETTER INJECTION
// ============================================

@Service
class NotificationService {
private EmailService emailService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public void notifyUser(String email, String notification) {
        emailService.sendEmail(email, notification);
    }
}

// ============================================
// 3. BEAN CONFIGURATION - Java Config
// ============================================

@Configuration
class AppConfig {

    @Bean
    public EmailService emailService() {
        return new EmailServiceImpl();
    }
    
    @Bean
    public UserService userService() {
        return new UserService(emailService());
    }
    
    @Bean
    @Scope("prototype")
    public OrderProcessor orderProcessor() {
        return new OrderProcessor();
    }
}

// ============================================
// 4. COMPONENT SCANNING
// ============================================

@Component
class ProductRepository {
public void save(String product) {
System.out.println("Saving product: " + product);
}

    public String findById(Long id) {
        return "Product-" + id;
    }
}

@Service
class ProductService {
private final ProductRepository repository;

    @Autowired
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
    
    public void createProduct(String name) {
        repository.save(name);
    }
}

@Controller
class ProductController {
private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    public void addProduct(String name) {
        productService.createProduct(name);
    }
}

// ============================================
// 5. BEAN SCOPES
// ============================================

@Component
@Scope("singleton") // Default scope
class SingletonBean {
private int counter = 0;

    public void increment() {
        counter++;
    }
    
    public int getCounter() {
        return counter;
    }
}

@Component
@Scope("prototype")
class PrototypeBean {
private String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}

// ============================================
// 6. BEAN LIFECYCLE - @PostConstruct & @PreDestroy
// ============================================

@Component
class DatabaseConnection {

    @PostConstruct
    public void init() {
        System.out.println("Database connection initialized");
        // Connect to database
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Closing database connection");
        // Close connection
    }
    
    public void executeQuery(String sql) {
        System.out.println("Executing: " + sql);
    }
}

// ============================================
// 7. BEAN LIFECYCLE - InitializingBean & DisposableBean
// ============================================

@Component
class CacheManager implements InitializingBean, DisposableBean {
private Map<String, Object> cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing cache...");
        cache = new HashMap<>();
    }
    
    @Override
    public void destroy() throws Exception {
        System.out.println("Clearing cache...");
        cache.clear();
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
}

// ============================================
// 8. PROPERTY INJECTION with @Value
// ============================================

@Component
class ApplicationProperties {

    @Value("${app.name:MyApp}")
    private String appName;
    
    @Value("${app.version:1.0}")
    private String version;
    
    @Value("${app.max.connections:100}")
    private int maxConnections;
    
    public void printConfig() {
        System.out.println("App Name: " + appName);
        System.out.println("Version: " + version);
        System.out.println("Max Connections: " + maxConnections);
    }
}

// ============================================
// 9. QUALIFIER - Multiple Bean Implementations
// ============================================

interface PaymentService {
void processPayment(double amount);
}

@Component
@Qualifier("creditCard")
class CreditCardPayment implements PaymentService {
@Override
public void processPayment(double amount) {
System.out.println("Processing credit card payment: $" + amount);
}
}

@Component
@Qualifier("paypal")
class PayPalPayment implements PaymentService {
@Override
public void processPayment(double amount) {
System.out.println("Processing PayPal payment: $" + amount);
}
}

@Service
class CheckoutService {
private final PaymentService paymentService;

    @Autowired
    public CheckoutService(@Qualifier("creditCard") PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    public void checkout(double amount) {
        paymentService.processPayment(amount);
    }
}

// ============================================
// 10. PRIMARY BEAN
// ============================================

interface Logger {
void log(String message);
}

@Component
@Primary
class ConsoleLogger implements Logger {
@Override
public void log(String message) {
System.out.println("[CONSOLE] " + message);
}
}

@Component
class FileLogger implements Logger {
@Override
public void log(String message) {
System.out.println("[FILE] Writing to file: " + message);
}
}

@Service
class LoggingService {
private final Logger logger;

    @Autowired
    public LoggingService(Logger logger) {
        this.logger = logger; // Will inject ConsoleLogger (Primary)
    }
    
    public void logMessage(String msg) {
        logger.log(msg);
    }
}

// ============================================
// 11. LAZY INITIALIZATION
// ============================================

@Component
@Lazy
class HeavyService {

    public HeavyService() {
        System.out.println("HeavyService instantiated (expensive operation)");
    }
    
    public void performTask() {
        System.out.println("Performing heavy task...");
    }
}

// ============================================
// 12. PROFILE-BASED CONFIGURATION
// ============================================

@Configuration
@Profile("dev")
class DevConfig {

    @Bean
    public DataSource dataSource() {
        System.out.println("Creating DEV DataSource");
        return new DataSource("localhost", 5432);
    }
}

@Configuration
@Profile("prod")
class ProdConfig {

    @Bean
    public DataSource dataSource() {
        System.out.println("Creating PROD DataSource");
        return new DataSource("prod-server.com", 5432);
    }
}

class DataSource {
private String host;
private int port;

    public DataSource(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public void connect() {
        System.out.println("Connecting to " + host + ":" + port);
    }
}

// ============================================
// 13. APPLICATION CONTEXT USAGE
// ============================================

@Component
class OrderProcessor {
private String orderId = UUID.randomUUID().toString();

    public void processOrder(String order) {
        System.out.println("Processing order: " + order + " (ID: " + orderId + ")");
    }
}

// Main application demonstrating ApplicationContext
class SpringCoreDemo {
public static void main(String[] args) {
// Create application context
ApplicationContext context =
new AnnotationConfigApplicationContext(AppConfig.class);

        // Get beans
        UserService userService = context.getBean(UserService.class);
        userService.registerUser("user@example.com");
        
        // Demonstrate singleton scope
        SingletonBean singleton1 = context.getBean(SingletonBean.class);
        SingletonBean singleton2 = context.getBean(SingletonBean.class);
        System.out.println("Singleton same instance: " + (singleton1 == singleton2));
        
        // Demonstrate prototype scope
        PrototypeBean proto1 = context.getBean(PrototypeBean.class);
        PrototypeBean proto2 = context.getBean(PrototypeBean.class);
        System.out.println("Prototype different instances: " + (proto1 != proto2));
        
        // Close context (triggers @PreDestroy)
        ((ConfigurableApplicationContext) context).close();
    }
}

// ============================================
// 14. AUTOWIRING MODES
// ============================================

@Component
class InventoryService {

    @Autowired
    private ProductRepository repository; // Field injection
    
    private LoggingService logger;
    
    @Autowired
    public void setLogger(LoggingService logger) { // Setter injection
        this.logger = logger;
    }
    
    public void checkStock(Long productId) {
        String product = repository.findById(productId);
        logger.logMessage("Checking stock for: " + product);
    }
}

// ============================================
// 15. BEAN FACTORY POST PROCESSOR (Advanced)
// ============================================

@Component
class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        System.out.println("Customizing bean factory before bean creation");
        // Modify bean definitions here
    }
}

// ============================================
// 16. BEAN POST PROCESSOR (Advanced)
// ============================================

@Component
class CustomBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("Before initialization of: " + beanName);
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("After initialization of: " + beanName);
        return bean;
    }
}