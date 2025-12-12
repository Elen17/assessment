package org.example.springcore.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
@SessionScope
public class MovieRecommender {
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private final String catalog;
    private final Integer version;
    private final Date releaseDate;
    private final String runtimeExpression;
    private final Map<String, Integer> countOfMoviesPerCatalog;

    /*
     * @Value("${catalog.name}") - looks for catalog.name in application.properties, if not found uses ${catalog.name}
     * @Value("${catalog.version:defaultVersion}") - looks for catalog.version in application.properties, if not found uses 1
     */
    public MovieRecommender(@Value("${catalog.name}") String catalog,
                            @Value("${catalog.version:1}") Integer version,
                            @Value("${catalog.releaseDate}") Date releaseDate,
                            @Value("#{systemProperties['user.catalog'] + 'Catalog' }") String runtimeExpression, /*if user.catalog is not set, then it will be null => nullCatalog*/
                            @Value("#{{'Thriller': 100, 'Comedy': 300}}") Map<String, Integer> countOfMoviesPerCatalog) {
        this.catalog = catalog;
        this.version = version;
        this.releaseDate = releaseDate;
        this.runtimeExpression = runtimeExpression;
        this.countOfMoviesPerCatalog = countOfMoviesPerCatalog;
    }

    public void getRecommendations() {
        System.out.printf("Catalog: %s, Version: %d; Release Date: %s, Runtime Expression: %s, Count of movies per catalog: %s%n",
                catalog, version, FORMATTER.format(releaseDate), runtimeExpression, countOfMoviesPerCatalog);
        /*
         * Output:
         * Catalog: ${catalog.name}, Version: 21
         */
    }
}
