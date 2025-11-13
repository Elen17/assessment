// JavaGsonSimpleExample.java
package org.example.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.example.gson.adapter.LocalDateAdapter;
import org.example.gson.adapter.PersonCreatorWithDefaultDate;
import org.example.gson.config.GsonConfig;
import org.example.gson.model.Person;
import org.example.gson.service.PersonService;
import org.example.gson.util.JsonUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;

@Slf4j
public class JavaGsonSimpleExample {
    private static final String FILE_NAME = "person.json";
    private static final PersonService personService = new PersonService(GsonConfig.createDefaultGson());

    public static void main(String[] args) throws IOException {
        log.info("Starting GSON examples");

        // Basic serialization/deserialization
        demonstrateBasicOperations();

        // custom type adapter
        demonstrateCustomTypeAdapterExample();

        // custom exclusion strategy
        demonstrateExclusionStrategy();

        // JSON Reader
        demonstrateJsonReader();

        // JSON Writer
        demonstrateJsonWriter();

        log.info("All examples completed successfully");
    }

    private static void demonstrateBasicOperations() {
        log.info("--- Basic Operations ---");

        // Deserialize from JSON string
        Person person = personService.deserializePerson(JSON_NAME);
        log.info("Deserialized person: {} {}", person.getFirstName(), person.getLastName());

        // Serialize person to JSON
        Person samplePerson = personService.createSamplePerson();
        String json = personService.serializePerson(samplePerson);
        log.info("Serialized person: {}", JsonUtils.toPrettyJson(json));
    }

    private static void demonstrateCustomTypeAdapterExample() {
        log.info("--- Advanced Features ---");

        // registering custom type adapter for Person
        Gson customGson = new GsonBuilder()
                .registerTypeAdapter(Person.class, new PersonCreatorWithDefaultDate())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        // during deserialization with custom type adapter default date will be set
        Person person = customGson.fromJson(JSON_NAME_WITHOUT_DATE, Person.class);
        log.info("Person with custom creator: {}", person);

    }

    private static void demonstrateExclusionStrategy() {
        // serialization with custom exclusion strategy
        Gson gsonWithStrategy = GsonConfig.createGsonWithExclusionStrategy();
        log.info("Person with exclusion strategy: {}",
                gsonWithStrategy.toJson(personService.createSamplePerson()));
    }

    private static void demonstrateJsonReader() throws IOException {
        log.info("--- JSON Streaming ---");

        try (JsonReader reader = new JsonReader(new StringReader(CAR_JSON))) {
            log.info("Reading JSON with JsonReader:");
            StringBuilder json = new StringBuilder();
            readJson(reader, 0, json);
            log.info("Result:\n{}", json);
        }
    }

    private static void readJson(JsonReader reader, int depth, StringBuilder json) throws IOException {
        String indent = "  ".repeat(depth);

        JsonToken token = reader.peek();
        switch (token) {
            case BEGIN_OBJECT:
                json.append(indent);
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    json.append(indent).append(" : Field ").append(name);
                    readJson(reader, depth + 1, json);
                }
                reader.endObject();
                break;

            case BEGIN_ARRAY:
                json.append(indent).append("Array [");
                reader.beginArray();
                int index = 0;
                while (reader.hasNext()) {
                    json.append(indent).append("  [").append(index).append("]");
                    readJson(reader, depth + 1, json);
                }
                reader.endArray();
                json.append(indent).append("]");
                break;

            case STRING:
                json.append(indent).append("String: ").append(reader.nextString());
                break;

            case NUMBER:
                String number = reader.nextString();
                json.append(indent).append("Number: ").append(number);
                break;

            case BOOLEAN:
                json.append(indent).append("Boolean: ").append(reader.nextBoolean());
                break;

            case NULL:
                reader.nextNull();
                json.append(indent).append("null");
                break;

            default:
                log.warn("{}  Unhandled token: {}", indent, token);
                reader.skipValue();
                break;
        }
    }

    private static void demonstrateJsonWriter() throws IOException {

        // JsonWriter example
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter writer = new JsonWriter(stringWriter)) {
            writer.beginObject()
                    .name("name").value("Alice")
                    .name("age").value(28)
                    .name("languages")
                    .beginArray()
                    .value("Java")
                    .value("Python")
                    .value("Kotlin")
                    .endArray()
                    .name("address")
                    .beginObject()
                    .name("address").value("123 Main St")
                    .name("city").value("Anytown")
                    .name("state").value("CA")
                    .name("zip").value("12345")
                    .endObject()
                    .endObject();
        }
        log.info("Generated JSON: {}", stringWriter);
    }


    // Constants
    private static final String JSON_NAME = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "dateOfBirth": "1990-01-01"
            }""";

    private static final String JSON_NAME_WITHOUT_DATE = """
            {
                "firstName": "John",
                "lastName": "Doe"
            }""";

    private static final String CAR_JSON = """
            {
                "make": "Toyota",
                "model": "Camry",
                "year": 2023,
                "price": 25999.99,
                "colors": ["Pearl White", "Midnight Black", "Ruby Red"],
                "specs": {
                    "engine": "2.5L 4-cylinder",
                    "horsepower": 203,
                    "mpg": {
                        "city": 28,
                        "highway": 39,
                        "combined": 32
                    },
                    "transmission": "8-speed automatic",
                    "driveType": "Front-Wheel Drive"
                }
            }""";
}
