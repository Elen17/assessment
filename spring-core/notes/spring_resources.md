# Spring Resources - Complete Guide

## Overview

Spring's **Resource** abstraction provides a unified way to access low-level resources (files, URLs, classpath resources) regardless of their location.

---

## Table of Contents
- [Resource Interface](#resource-interface)
- [Resource Implementations](#resource-implementations)
- [ResourceLoader](#resourceloader)
- [Resource Injection](#resource-injection)
- [Resource Patterns](#resource-patterns)
- [Practical Examples](#practical-examples)
- [Interview Questions](#interview-questions)

---

## Resource Interface

The `Resource` interface is Spring's abstraction for resource access.

### Core Methods

```java
public interface Resource extends InputStreamSource {
    boolean exists();
    boolean isReadable();
    boolean isOpen();
    boolean isFile();
    
    URL getURL() throws IOException;
    URI getURI() throws IOException;
    File getFile() throws IOException;
    
    long contentLength() throws IOException;
    long lastModified() throws IOException;
    
    Resource createRelative(String relativePath) throws IOException;
    String getFilename();
    String getDescription();
    
    InputStream getInputStream() throws IOException;
}
```

### Basic Usage

```java
import org.springframework.core.io.Resource;

@Service
public class FileService {
    
    public void readResource(Resource resource) throws IOException {
        // Check if resource exists
        if (resource.exists() && resource.isReadable()) {
            
            // Get InputStream
            try (InputStream is = resource.getInputStream()) {
                // Read content
                String content = new String(is.readAllBytes());
                System.out.println(content);
            }
            
            // Get file info
            System.out.println("Filename: " + resource.getFilename());
            System.out.println("Description: " + resource.getDescription());
            
            // Get as File (if possible)
            if (resource.isFile()) {
                File file = resource.getFile();
                System.out.println("Absolute path: " + file.getAbsolutePath());
            }
        }
    }
}
```

---

## Resource Implementations

Spring provides several Resource implementations:

### 1. UrlResource

Access resources via URL (HTTP, FTP, file://)

```java
Resource resource = new UrlResource("https://example.com/data.json");
Resource fileResource = new UrlResource("file:///home/user/data.txt");

// Read HTTP resource
try (InputStream is = resource.getInputStream()) {
    String content = new String(is.readAllBytes());
}
```

### 2. ClassPathResource

Access resources from classpath

```java
// From classpath root
Resource resource = new ClassPathResource("application.properties");

// From specific package
Resource resource = new ClassPathResource("config/database.properties");

// With class loader
Resource resource = new ClassPathResource("data.xml", MyClass.class.getClassLoader());

// Reading
try (InputStream is = resource.getInputStream()) {
    Properties props = new Properties();
    props.load(is);
}
```

### 3. FileSystemResource

Access resources from file system

```java
Resource resource = new FileSystemResource("/home/user/data.txt");
Resource resource = new FileSystemResource("C:\\Users\\data.txt");

// Get as File
File file = resource.getFile();

// Check exists
if (resource.exists()) {
    String content = Files.readString(file.toPath());
}
```

### 4. ServletContextResource

Access resources in web application context

```java
// In web application
Resource resource = new ServletContextResource(servletContext, "/WEB-INF/data.xml");
```

### 5. InputStreamResource

Wrap an InputStream as Resource

```java
InputStream is = new ByteArrayInputStream("Hello".getBytes());
Resource resource = new InputStreamResource(is);

// Note: Use only when other implementations don't apply
```

### 6. ByteArrayResource

In-memory byte array as Resource

```java
byte[] data = "Test data".getBytes();
Resource resource = new ByteArrayResource(data);

// Multiple reads supported
try (InputStream is = resource.getInputStream()) {
    // Read data
}
```

---

## ResourceLoader

The `ResourceLoader` interface loads resources using location strings.

### Using ResourceLoader

```java
@Service
public class DocumentService {
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    public Resource loadResource(String location) {
        return resourceLoader.getResource(location);
    }
    
    public void processDocument(String path) throws IOException {
        // Load with prefix
        Resource resource = resourceLoader.getResource(path);
        
        if (resource.exists()) {
            try (InputStream is = resource.getInputStream()) {
                // Process resource
            }
        }
    }
}
```

### Location Prefixes

```java
@Service
public class MultiSourceLoader {
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    public void demonstratePrefixes() throws IOException {
        // Classpath
        Resource cp = resourceLoader.getResource("classpath:config/app.properties");
        
        // File system
        Resource fs = resourceLoader.getResource("file:/home/user/data.txt");
        
        // URL
        Resource url = resourceLoader.getResource("https://example.com/api/data");
        
        // Relative (ServletContext in web app)
        Resource rel = resourceLoader.getResource("data.xml");
    }
}
```

### ResourcePatternResolver

Load multiple resources matching a pattern:

```java
@Service
public class BulkLoader {
    
    @Autowired
    private ResourcePatternResolver resourcePatternResolver;
    
    public void loadAllConfigs() throws IOException {
        // Load all .properties files
        Resource[] resources = resourcePatternResolver.getResources(
            "classpath*:config/*.properties"
        );
        
        for (Resource resource : resources) {
            System.out.println("Found: " + resource.getFilename());
            // Process each resource
        }
    }
    
    public void loadFromAllJars() throws IOException {
        // Search in all JARs on classpath
        Resource[] resources = resourcePatternResolver.getResources(
            "classpath*:META-INF/spring/*.xml"
        );
    }
}
```

---

## Resource Injection

Spring allows direct injection of resources:

### 1. @Value Injection

```java
@Service
public class ConfigService {
    
    // Inject single resource
    @Value("classpath:application.properties")
    private Resource configFile;
    
    // Inject file system resource
    @Value("file:/etc/myapp/config.yml")
    private Resource externalConfig;
    
    // Inject URL resource
    @Value("https://api.example.com/config")
    private Resource remoteConfig;
    
    public void loadConfig() throws IOException {
        Properties props = new Properties();
        try (InputStream is = configFile.getInputStream()) {
            props.load(is);
        }
    }
}
```

### 2. Constructor Injection

```java
@Service
public class TemplateService {
    
    private final Resource emailTemplate;
    
    public TemplateService(
        @Value("classpath:templates/email.html") Resource emailTemplate
    ) {
        this.emailTemplate = emailTemplate;
    }
    
    public String loadTemplate() throws IOException {
        try (InputStream is = emailTemplate.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
```

### 3. Array/List Injection

```java
@Service
public class MultiConfigService {
    
    // Inject multiple resources
    @Value("classpath*:config/*.properties")
    private Resource[] configFiles;
    
    @PostConstruct
    public void loadAllConfigs() throws IOException {
        for (Resource resource : configFiles) {
            System.out.println("Loading: " + resource.getFilename());
            Properties props = new Properties();
            try (InputStream is = resource.getInputStream()) {
                props.load(is);
            }
        }
    }
}
```

---

## Resource Patterns

### Classpath Patterns

```java
@Service
public class PatternExamples {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    public void examples() throws IOException {
        // Single file
        Resource[] r1 = resolver.getResources("classpath:config/app.properties");
        
        // All files in directory
        Resource[] r2 = resolver.getResources("classpath:config/*.properties");
        
        // Recursive search
        Resource[] r3 = resolver.getResources("classpath:config/**/*.xml");
        
        // Search in all JARs (note classpath*)
        Resource[] r4 = resolver.getResources("classpath*:META-INF/*.xml");
        
        // Multiple extensions
        Resource[] r5 = resolver.getResources("classpath:data/*.{json,xml}");
    }
}
```

### Ant-Style Patterns

```java
// ? - matches one character
"file:/home/user/data?.txt"  // data1.txt, dataA.txt

// * - matches zero or more characters (one directory level)
"classpath:config/*.properties"  // All .properties in config/

// ** - matches zero or more directories
"classpath:config/**/*.xml"  // All .xml files recursively

// Examples
"classpath:com/example/**/config/*.xml"
"file:/var/log/**/*.log"
"classpath*:META-INF/spring.factories"
```

---

## Practical Examples

### Example 1: Configuration File Loader

```java
@Service
public class AppConfigLoader {
    
    @Value("classpath:application.properties")
    private Resource defaultConfig;
    
    @Value("file:${user.home}/.myapp/config.properties")
    private Resource userConfig;
    
    public Properties loadConfiguration() throws IOException {
        Properties props = new Properties();
        
        // Load default config
        try (InputStream is = defaultConfig.getInputStream()) {
            props.load(is);
        }
        
        // Override with user config if exists
        if (userConfig.exists()) {
            try (InputStream is = userConfig.getInputStream()) {
                props.load(is);  // Overrides defaults
            }
        }
        
        return props;
    }
}
```

### Example 2: Template Engine

```java
@Service
public class EmailTemplateService {
    
    @Value("classpath:templates/email/*.html")
    private Resource[] templates;
    
    private Map<String, String> templateCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void loadTemplates() throws IOException {
        for (Resource template : templates) {
            String name = template.getFilename();
            try (InputStream is = template.getInputStream()) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                templateCache.put(name, content);
            }
        }
    }
    
    public String renderTemplate(String templateName, Map<String, Object> vars) {
        String template = templateCache.get(templateName);
        // Process template with variables
        return processTemplate(template, vars);
    }
    
    private String processTemplate(String template, Map<String, Object> vars) {
        // Simple variable replacement
        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            template = template.replace("${" + entry.getKey() + "}", 
                                       String.valueOf(entry.getValue()));
        }
        return template;
    }
}
```

### Example 3: Static Resource Serving

```java
@RestController
@RequestMapping("/files")
public class FileDownloadController {
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Resource resource = resourceLoader.getResource(
                "classpath:static/downloads/" + filename
            );
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentLength(resource.contentLength())
                .body(resource);
                
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
```

### Example 4: Data Import Service

```java
@Service
public class DataImportService {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    public void importAllData() throws IOException {
        // Find all CSV files
        Resource[] csvFiles = resolver.getResources("classpath:data/**/*.csv");
        
        for (Resource csv : csvFiles) {
            System.out.println("Importing: " + csv.getFilename());
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(csv.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    // Process CSV line
                    processLine(line);
                }
            }
        }
    }
    
    private void processLine(String line) {
        // Parse and import data
    }
}
```

### Example 5: Multi-Environment Configuration

```java
@Configuration
public class EnvironmentConfig {
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Bean
    public Properties environmentProperties(ResourceLoader resourceLoader) 
            throws IOException {
        
        String location = String.format(
            "classpath:config/application-%s.properties", 
            activeProfile
        );
        
        Resource resource = resourceLoader.getResource(location);
        
        Properties props = new Properties();
        if (resource.exists()) {
            try (InputStream is = resource.getInputStream()) {
                props.load(is);
            }
        }
        
        return props;
    }
}
```

---

## Interview Questions

### Q1: What is the Resource abstraction in Spring?
**Answer:** Resource is Spring's abstraction for accessing low-level resources (files, URLs, classpath resources). It provides a unified interface regardless of resource location, with implementations like ClassPathResource, FileSystemResource, and UrlResource.

### Q2: What's the difference between Resource and File?
**Answer:**
- **Resource:** Spring abstraction, works with any location (classpath, URL, file system)
- **File:** Java abstraction, only for file system access
- Resource provides additional methods and can represent resources inside JARs

### Q3: Explain classpath vs classpath* prefix
**Answer:**
- **classpath:** Searches only the first matching resource
- **classpath*:** Searches all matching resources in all JARs on classpath

```java
// Returns first match only
"classpath:config/app.properties"

// Returns all matches from all JARs
"classpath*:META-INF/spring.factories"
```

### Q4: How to read a file from classpath?
**Answer:**
```java
@Value("classpath:data.txt")
private Resource resource;

// Or
Resource resource = new ClassPathResource("data.txt");

try (InputStream is = resource.getInputStream()) {
    String content = new String(is.readAllBytes());
}
```

### Q5: What is ResourceLoader?
**Answer:** ResourceLoader is an interface for loading resources using location strings with prefixes (classpath:, file:, http:). ApplicationContext implements ResourceLoader.

### Q6: How to load multiple resources matching a pattern?
**Answer:** Use ResourcePatternResolver:
```java
@Autowired
private ResourcePatternResolver resolver;

Resource[] resources = resolver.getResources("classpath*:config/*.xml");
```

### Q7: Can you inject a Resource in Spring?
**Answer:** Yes, using @Value:
```java
@Value("classpath:config.properties")
private Resource configFile;
```

### Q8: What Ant-style patterns are supported?
**Answer:**
- `?` - one character
- `*` - zero or more characters (one level)
- `**` - zero or more directories

### Q9: How to access resources in a WAR file?
**Answer:** Use ServletContextResource or "classpath:" prefix:
```java
Resource resource = new ServletContextResource(servletContext, "/WEB-INF/data.xml");
// Or
@Value("classpath:data.xml")
private Resource resource;
```

### Q10: What's the difference between getResource() and getResources()?
**Answer:**
- **getResource():** Returns single Resource
- **getResources():** Returns array of Resources matching pattern

---

## Best Practices

✅ **DO:**
- Use Resource abstraction instead of File when possible
- Use classpath: for packaged resources
- Use file: for external configuration
- Close InputStreams properly (try-with-resources)
- Check resource.exists() before reading

❌ **DON'T:**
- Don't use File directly for classpath resources
- Don't forget to close streams
- Don't assume resources are always files (may be in JARs)
- Don't hard-code absolute paths

---

## Notes Section

_Add your notes, observations, and examples here:_

---

---

---
