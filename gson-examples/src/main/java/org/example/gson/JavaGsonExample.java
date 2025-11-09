package org.example.gson;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.example.gson.adapter.LocalDateAdapter;
import org.example.gson.adapter.PersonCreator;
import org.example.gson.model.Address;
import org.example.gson.model.Person;
import org.example.gson.serializer.CustomDateSerializer;
import org.example.gson.strategy.PersonLastNameFieldExclusionStrategy;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.lang.System.out;

public class JavaGsonExample {

    public static void main() throws URISyntaxException, IOException {
    /*
        you can create an instance of the Gson class with 2 approaches:
            Gson gson = new Gson();
            // Using a GsonBuilder allows you to set configuration options on the GsonBuilder before creating the Gson object.
            Gson gson = new GsonBuilder().create();
    */

        LocalDateAdapter localDateAdapter = new LocalDateAdapter();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateAdapter)
                .create();
        //  you should make sure that Person class has a no-arg constructor, or GSON cannot use it.
        Person person = gson.fromJson(JSON_NAME, Person.class);

        out.printf(FIRST_NAME_S_LAST_NAME_S_N, person.getFirstName(), person.getLastName());

        // generate json from object
        Person person2 = new Person();
        person2.setFirstName("Jane");
        person2.setLastName("Doe");
        Address address = new Address();
        address.setCity("New York City");
        address.setStreet("Times Square");
        person2.setAddress(address);

        String json = gson.toJson(person2);
        out.println(json);

        // adding a custom config for pretty printing

        Gson gsonPretty = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateAdapter)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setPrettyPrinting()
                .create();
        out.println(gsonPretty.toJson(person2));

        // we can exclude fields from serialization

        // Transient Fields; no additional configuration needed
        person2.setDateOfBirth(LocalDate.of(2001, 1, 1));
        Gson gsonTransientExcluded = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateAdapter)
                .create();
        out.println(gsonTransientExcluded.toJson(person2));
        out.println(STR."Age: \{person2.getAge()}");

        Person person3 = gson.fromJson(JSON_NAME, Person.class);
        // setting ignoreMe to 0 (default) excludes the field
        out.println(person3.getAge());

        // you can also exclude fields with @Expose annotation; configuring for both serialization and deserialization
        // Note, that this configuration makes GSON ignore all fields that do not have an @Expose annotation.
        // To have a field included in serialization or deserialization it must have an @Expose annotation above it.
        Gson gsonExposeExcluded = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateAdapter)
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        // any field of Person without @Expose annotation will be excluded
        out.println(gsonExposeExcluded.toJson(person3));

        // setting more readable date format
        Gson gsonWithDateFormat = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateAdapter)
                .setPrettyPrinting().setDateFormat("yyyy-MM-dd")
                .create();
        out.println(gsonWithDateFormat.toJson(person2));


        // you can apply ExcludeStrategy to exclude fields from serialization and deserialization
        // this is useful for security reasons
        Gson gsonExcludeStrategy = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateAdapter)
                .setExclusionStrategies(new PersonLastNameFieldExclusionStrategy())
                .setPrettyPrinting()
                .create();
        out.println(STR."Last name excluded: " + gsonExcludeStrategy.toJson(person2));

        Person personWithoutLastName = gsonExcludeStrategy.fromJson(JSON_NAME, Person.class);
        out.println(STR."Last name: \{personWithoutLastName.getLastName()}");

        Person emptyPerson = new Person();
        emptyPerson.setFirstName("John");
        emptyPerson.setAddress(address);
        Gson gsonIncludeNulls = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateAdapter)
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd")
                .serializeNulls()
                .create();
        Path filePath = Paths.get(ClassLoader.getSystemResource(FILE_NAME).toURI());

        Files.writeString(filePath, gsonIncludeNulls.toJson(emptyPerson));


        // creating custom instance creator
        // to do some default configuration of the instance

        Gson gsonWithCustomInstanceCreator = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateAdapter)
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd")
                .serializeNulls()
                .registerTypeAdapter(Person.class, new PersonCreator())
                .create();

        out.println(gsonWithCustomInstanceCreator.fromJson(JSON_NAME_WITHOUT_DATE, Person.class));


        // you can create custom serializers and deserializers
        // to do some default configuration of the instance
        CustomDateSerializer customDateSerializer = new CustomDateSerializer();
        Gson gsonWithCustomSerializer = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDate.class, customDateSerializer)
                .create();

        out.println(gsonWithCustomSerializer.toJson(person2));
        // will throw an exception
        // .InaccessibleObjectException: Unable to make field private final int java.time.LocalDate.year accessible
        // LocalDate should have adapter, or custom deserializer to make it accessible

//        out.println(gsonWithCustomSerializer.fromJson(JSON_NAME, Person.class));


        // JsonReader

        JsonReader jsonReader = new JsonReader(new StringReader(CAR_JSON));

        try {
            out.println('{');
            while(jsonReader.hasNext()) {
                JsonToken nextToken = jsonReader.peek();
//                System.out.println(nextToken);

                if(JsonToken.BEGIN_OBJECT.equals(nextToken)){

                    jsonReader.beginObject();

                } else if(JsonToken.NAME.equals(nextToken)){

                    String name  =  jsonReader.nextName();
                    System.out.print(STR."\t\{name}");

                } else if(JsonToken.STRING.equals(nextToken)){

                    String value =  jsonReader.nextString();
                    System.out.println(STR.": \{value}");

                } else if(JsonToken.NUMBER.equals(nextToken)){

                    long value =  jsonReader.nextLong();
                    System.out.println(STR.": \{value}");

                }
            }

            out.println('}');

        } catch (IOException e) {
            e.printStackTrace();
        }


         // JsonParser

        JsonParser parser = new JsonParser();

        JsonElement jsonTree = parser.parse(DUMMY_JSON);

        if(jsonTree.isJsonObject()){
            out.println('{');

            JsonObject jsonObject = jsonTree.getAsJsonObject();

            JsonElement f1 = jsonObject.get("f1");
            out.println(STR."\t\"f1\": \"\{f1.getAsString()}\"");

            JsonElement f2 = jsonObject.get("f2");

            if(f2.isJsonObject()){
                JsonObject f2Obj = f2.getAsJsonObject();

                JsonElement f3 = f2Obj.get("f3");

                out.println(STR."\t\"f2\": {\n\t\t\"f3\": \"\{f3.getAsString()}\"\n\t}");
            }
            out.println('}');
        }

         // JsonWriter

        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);

        writer.beginObject();          // {
        writer.name("name").value("Alice");
        writer.name("age").value(28);

        writer.name("languages");
        writer.beginArray();           // "languages": [
        writer.value("Java");
        writer.value("Python");
        writer.value("Kotlin");
        writer.endArray();             // ]

        writer.endObject();            // }
        writer.close();

        System.out.println(stringWriter);
    }


    public static final String FIRST_NAME_S_LAST_NAME_S_N = """
            First name: %s
            Last name: %s%n
            """.stripIndent();

    public static final String JSON_NAME = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "dateOfBirth": "1990-01-01"
            }
            """.stripIndent();

    public static final String JSON_NAME_WITHOUT_DATE = """
            {
                "firstName": "John",
                "lastName": "Doe"
            }
            """.stripIndent();

    public static final String JSON_ARRAY = "[{\"firstName\": \"James\", \"lastName\": \"Mayer\"}, {\"firstName\": \"Audrey\", \"lastName\": \"Bolton\"}]";

    public static final String CAR_JSON = "{\"brand\" : \"Toyota\", \"doors\" : 5}";

    public static final String DUMMY_JSON = "{ \"f1\":\"Hello\",\"f2\":{\"f3\":\"World\"}}";



    public static final String FILE_NAME = "person.json";
    public static final String NAME_LIST_FILE_NAME = "nameList.json";
    public static final String INVALID_FILE = "invalidJson.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

}
