package org.example;


import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Sale;
import org.example.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
public class GuavaCollectionsExamples {

    public static void main(String[] args) {
        immutableListExamples();
        immutableSetExamples();
        immutableMapExamples();

        multisetExamples();
        multimapExamples();
        tableExamples();
        biMapExamples();

        rangeSetExamples();
        rangeMapExamples();

        collectionUtilitiesExamples();
        orderingExamples();
        advancedPatterns();

    }

    public static void immutableListExamples() {
        log.info("\n=== IMMUTABLE LIST ===");

        ImmutableList<String> list1 = ImmutableList.of("A", "B", "C");
        log.info("of(): {}", list1);

        List<String> mutable = Arrays.asList("X", "Y", "Z");
        ImmutableList<String> list2 = ImmutableList.copyOf(mutable);
        log.info("copyOf(): {}", list2);

        ImmutableList<String> list3 = ImmutableList.<String>builder()
                .add("First")
                .add("Second", "Third")
                .addAll(Arrays.asList("Fourth", "Fifth"))
                .build();
        log.info("Builder: {}", list3);

        // Defensive copy protection
        List<String> original = new ArrayList<>(Arrays.asList("A", "B"));
        ImmutableList<String> immutable = ImmutableList.copyOf(original);
        original.clear(); // Doesn't affect immutable
        log.info("After clearing original: {}", immutable); // [A, B]
    }

    public static void immutableSetExamples() {
        log.info("\n=== IMMUTABLE SET ===");

        ImmutableSet<String> set1 = ImmutableSet.of("Red", "Green", "Blue");
        log.info("of(): {}", set1);

        ImmutableSet<Integer> set3 = ImmutableSet.<Integer>builder()
                .add(1)
                .add(2, 3)
                .addAll(Arrays.asList(4, 5, 5)) // Duplicate 5 ignored
                .build();
        log.info("Builder: {}", set3);


        // Set operations (returns views)
        ImmutableSet<String> setA = ImmutableSet.of("A", "B", "C");
        ImmutableSet<String> setB = ImmutableSet.of("B", "C", "D");

        Sets.SetView<String> union = Sets.union(setA, setB);
        log.info("Union: {}", union); // [A, B, C, D]

        Sets.SetView<String> intersection = Sets.intersection(setA, setB);
        log.info("Intersection: {}", intersection); // [B, C]

        Sets.SetView<String> difference = Sets.difference(setA, setB);
        log.info("Difference (A - B): {}", difference); // [A]
    }

    public static void immutableMapExamples() {
        log.info("\n=== IMMUTABLE MAP ===");

        ImmutableMap<String, Integer> map1 = ImmutableMap.of(
                "one", 1,
                "two", 2,
                "three", 3
        );
        log.info("of(): {}", map1);

        ImmutableMap<String, String> map2 = ImmutableMap.<String, String>builder()
                .put("USA", "Washington DC")
                .put("UK", "London")
                .put("France", "Paris")
                .put("Germany", "Berlin")
                .put("Japan", "Tokyo")
                .put("India", "New Delhi")
                .build();
        log.info("Builder (6+ entries): {}", map2);

        // copyOf()
        Map<String, Integer> mutable = new HashMap<>();
        mutable.put("A", 1);
        mutable.put("B", 2);
        ImmutableMap<String, Integer> map3 = ImmutableMap.copyOf(mutable);
        log.info("copyOf(): {}", map3);

        // Example 5: Iterate entries
        log.info("Entries:");
        map1.forEach((key, value) ->
                log.info("{} -> {}", key, value));
    }

    public static void multisetExamples() {
        log.info("\n=== MULTISET ===");

        Multiset<String> basket = HashMultiset.create();
        basket.add("apple");
        basket.add("banana");
        basket.add("apple");
        basket.add("apple");
        basket.add("orange");

        log.info("Basket: {}", basket);
        log.info("Apple count: {}", basket.count("apple")); // 3
        log.info("Unique items: {}", basket.elementSet()); // [apple, banana, orange]

        Multiset<String> votes = HashMultiset.create();
        votes.add("Alice", 5);  // Alice gets 5 votes
        votes.add("Bob", 3);    // Bob gets 3 votes
        votes.add("Alice", 2);  // Alice gets 2 more votes

        log.info("\nVoting results:");
        votes.entrySet().forEach(entry ->
                log.info("{}: {} votes", entry.getElement(), +entry.getCount())
        );
        // Alice: 7 votes
        // Bob: 3 votes

        // Word frequency analysis
        String text = "the quick brown fox jumps over the lazy dog the fox";
        Multiset<String> wordFreq = HashMultiset.create();
        wordFreq.addAll(Arrays.asList(text.split("\\s+")));

        log.info("\nWord frequencies:");
        wordFreq.entrySet().stream()
                .sorted((a, b) -> b.getCount() - a.getCount()) // Sort by count desc
                .forEach(entry ->
                        log.info("{}: {}", entry.getElement(), +entry.getCount())
                );

        // LinkedHashMultiset - maintains insertion order
        Multiset<String> ordered = LinkedHashMultiset.create();
        ordered.add("z");
        ordered.add("a");
        ordered.add("m");
        log.info("\nInsertion order: {}", ordered.elementSet()); // [z, a, m]

        // TreeMultiset - sorted order
        Multiset<String> sorted = TreeMultiset.create();
        sorted.add("z");
        sorted.add("a");
        sorted.add("m");
        log.info("Sorted order: {}", sorted.elementSet()); // [a, m, z]

        // Inventory management
        Multiset<String> inventory = HashMultiset.create();
        inventory.add("Widget-A", 50);
        inventory.add("Widget-B", 30);

        // Sell 10 Widget-A
        inventory.remove("Widget-A", 10);
        log.info("\nInventory after sale:");
        log.info("Widget-A stock: {}", inventory.count("Widget-A")); // 40
    }

    public static void multimapExamples() {
        log.info("\n=== MULTIMAP ===");

        // HashMultimap - Set values (no duplicates)
        Multimap<String, String> userRoles = HashMultimap.create();
        userRoles.put("john", "ADMIN");
        userRoles.put("john", "USER");
        userRoles.put("john", "ADMIN"); // Duplicate - not added!
        userRoles.put("jane", "USER");

        log.info("HashMultimap (Set values):");
        log.info("John's roles: {}", userRoles.get("john")); // [ADMIN, USER]

        // ArrayListMultimap - List values (allows duplicates)
        Multimap<String, String> tags = ArrayListMultimap.create();
        tags.put("article1", "java");
        tags.put("article1", "guava");
        tags.put("article1", "java"); // Duplicate - IS added!

        log.info("\nArrayListMultimap (List values):");
        log.info("Article1 tags: {}", tags.get("article1")); // [java, guava, java]

        // Student enrollment (one student, multiple courses)
        Multimap<String, String> enrollment = HashMultimap.create();
        enrollment.put("Alice", "Math");
        enrollment.put("Alice", "Physics");
        enrollment.put("Alice", "Chemistry");
        enrollment.put("Bob", "Math");
        enrollment.put("Bob", "English");

        log.info("\nStudent enrollments:");
        enrollment.asMap().forEach((student, courses) ->
                log.info("{}: {}", student, courses));

        // Tag-based article search
        Multimap<String, String> articleIndex = HashMultimap.create();
        articleIndex.put("java", "Article-1");
        articleIndex.put("java", "Article-2");
        articleIndex.put("python", "Article-2");
        articleIndex.put("python", "Article-3");

        boolean hasRole = userRoles.containsEntry("john", "ADMIN");
        log.info("\nDoes john have ADMIN? {}", hasRole); // true

        userRoles.remove("john", "ADMIN");
        log.info("After removing ADMIN: {}", userRoles.get("john"));
    }

    public static void biMapExamples() {
        log.info("\n=== BIMAP ===");

        // User ID to Username
        BiMap<Long, String> userMap = HashBiMap.create();
        userMap.put(1L, "alice");
        userMap.put(2L, "bob");
        userMap.put(3L, "charlie");

        log.info("Forward lookup - ID to username:");
        log.info("User 1: {}", userMap.get(1L)); // alice

        log.info("\nReverse lookup - Username to ID:");
        log.info("alice's ID: {}", userMap.inverse().get("alice")); // 1

        // Enforces bidirectional uniqueness
        try {
            userMap.put(4L, "alice"); // alice already exists as value!
        } catch (IllegalArgumentException e) {
            log.info("\nDuplicate value detected: {}", e.getMessage());
        }

        // forcePut() - overwrites existing mappings
        userMap.forcePut(4L, "alice"); // Removes (1L, alice) and adds (4L, alice)
        log.info("\nAfter forcePut(4, 'alice'):");
        log.info("Forward map: {}", userMap);
        log.info("User 1 exists? {}", userMap.containsKey(1L)); // false

        // Session token management
        BiMap<String, Long> sessions = HashBiMap.create();
        sessions.put("token-abc-123", 100L); // User 100
        sessions.put("token-def-456", 200L); // User 200

        Long userId = sessions.get("token-abc-123");
        log.info("\nUser for token: {}", userId);

        String token = sessions.inverse().get(100L);
        log.info("Token for user 100: {}", token);

        // ImmutableBiMap
        ImmutableBiMap<String, Integer> statusCodes = ImmutableBiMap.of(
                "OK", 200,
                "NOT_FOUND", 404,
                "INTERNAL_ERROR", 500
        );

        log.info("\nHTTP status code: {}", statusCodes.get("OK"));
        log.info("Status for 404: {}", statusCodes.inverse().get(404));
    }

    public static void tableExamples() {
        log.info("\n=== TABLE ===");

        Table<String, String, BigDecimal> pricing = HashBasedTable.create();
        pricing.put("Laptop", "USA", new BigDecimal("999.99"));
        pricing.put("Laptop", "EU", new BigDecimal("1099.99"));
        pricing.put("Mouse", "USA", new BigDecimal("29.99"));
        pricing.put("Mouse", "EU", new BigDecimal("34.99"));

        log.info("Price of Laptop in USA: {}", pricing.get("Laptop", "USA"));
        log.info("Price of Mouse in EU: {}", pricing.get("Mouse", "EU"));

        // Get all prices for a product (row)
        Map<String, BigDecimal> laptopPrices = pricing.row("Laptop");
        log.info("\nLaptop prices in all regions:");
        laptopPrices.forEach((region, price) ->
                log.info("{}: ${}", region, price));

        // Get all products in a region (column)
        Map<String, BigDecimal> usaPrices = pricing.column("USA");
        log.info("\nAll products in USA:");
        usaPrices.forEach((product, price) ->
                log.info("{}: ${}", product, price));

        // User × Feature → Enabled
        Table<String, String, Boolean> features = HashBasedTable.create();
        features.put("alice", "dark_mode", true);
        features.put("alice", "beta_features", true);
        features.put("bob", "dark_mode", false);
        features.put("bob", "beta_features", false);

        log.info("\nAlice's features:");
        features.row("alice").forEach((feature, enabled) ->
                log.info("  {}: {}", feature, enabled));

        // Iterate all cells
        log.info("\nAll pricing entries:");
        for (Table.Cell<String, String, BigDecimal> cell : pricing.cellSet()) {
            log.info("{} in {} = ${}", cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }

        // contains()
        boolean hasPrice = pricing.contains("Laptop", "USA");
        log.info("\nHas Laptop price for USA? {}", hasPrice);

        // TreeBasedTable - sorted
        Table<Integer, Integer, String> sorted = TreeBasedTable.create();
        sorted.put(3, 2, "C");
        sorted.put(1, 1, "A");
        sorted.put(2, 3, "B");

        log.info("\nTreeBasedTable (sorted keys):");
        log.info("Row keys: {}", sorted.rowKeySet()); // [1, 2, 3]
        log.info("Column keys: {}", sorted.columnKeySet()); // [1, 2, 3]
    }

    public static void rangeSetExamples() {
        log.info("\n=== RANGESET ===");

        // Business hours
        RangeSet<Integer> businessHours = TreeRangeSet.create();
        businessHours.add(Range.closed(9, 12));  // 9 AM to 12 PM
        businessHours.add(Range.closed(13, 17)); // 1 PM to 5 PM

        log.info("Business hours: {}", businessHours);
        log.info("Is 10 AM business hours? {}", businessHours.contains(10)); // true
        log.info("Is 12:30 PM business hours? {}", businessHours.contains(12)); // false

        // Merging overlapping ranges
        RangeSet<Integer> numbers = TreeRangeSet.create();
        numbers.add(Range.closed(1, 5));
        numbers.add(Range.closed(3, 8));  // Overlaps with [1,5]
        numbers.add(Range.closed(10, 15));

        log.info("\nMerged ranges: {}", numbers.asRanges());
        // Result: [[1..8], [10..15]] - overlapping ranges merged

        // Date range availability
        RangeSet<LocalDate> availability = TreeRangeSet.create();
        LocalDate today = LocalDate.of(2024, 1, 1);
        availability.add(Range.closed(today, today.plusDays(5)));
        availability.add(Range.closed(today.plusDays(10), today.plusDays(15)));

        LocalDate checkDate = today.plusDays(3);
        log.info("\nIs {} available? {}", checkDate, availability.contains(checkDate));

        // Remove a range (e.g., book a time slot)
        RangeSet<Integer> timeSlots = TreeRangeSet.create();
        timeSlots.add(Range.closed(9, 17)); // 9 AM to 5 PM available

        // Book 2 PM to 3 PM
        timeSlots.remove(Range.closed(14, 15));

        log.info("\nAvailable time slots after booking: {}", timeSlots.asRanges()); // [[9..13], [16..17]]

        // Complement (inverse)
        RangeSet<Integer> restricted = TreeRangeSet.create();
        restricted.add(Range.closed(5, 10));

        RangeSet<Integer> allowed = restricted.complement();
        log.info("\nRestricted: {}", restricted.asRanges());
        log.info("Allowed (complement): {}", allowed.subRangeSet(Range.closed(0, 20)).asRanges());
    }

    public static void rangeMapExamples() {
        log.info("\n=== RANGEMAP ===");

        // Age-based ticket pricing
        RangeMap<Integer, BigDecimal> ticketPrices = TreeRangeMap.create();
        ticketPrices.put(Range.closedOpen(0, 5), BigDecimal.ZERO);           // Free
        ticketPrices.put(Range.closedOpen(5, 18), new BigDecimal("10.00")); // Child
        ticketPrices.put(Range.closedOpen(18, 65), new BigDecimal("20.00")); // Adult
        ticketPrices.put(Range.atLeast(65), new BigDecimal("15.00"));       // Senior

        log.info("Ticket price for age 4: ${}", ticketPrices.get(4));
        log.info("Ticket price for age 10: ${}", ticketPrices.get(10));
        log.info("Ticket price for age 30: ${}", ticketPrices.get(30));
        log.info("Ticket price for age 70: ${}", ticketPrices.get(70));

        // Shipping costs by order total
        RangeMap<Double, BigDecimal> shipping = TreeRangeMap.create();
        shipping.put(Range.lessThan(25.0), new BigDecimal("9.99"));
        shipping.put(Range.closedOpen(25.0, 50.0), new BigDecimal("4.99"));
        shipping.put(Range.closedOpen(50.0, 100.0), new BigDecimal("2.99"));
        shipping.put(Range.atLeast(100.0), BigDecimal.ZERO); // Free shipping

        log.info("\nShipping cost for $20 order: ${}", shipping.get(20.0));
        log.info("Shipping cost for $45 order: ${}", shipping.get(45.0));
        log.info("Shipping cost for $150 order: ${}", shipping.get(150.0));

        // Tax brackets
        RangeMap<Integer, Double> taxRates = TreeRangeMap.create();
        taxRates.put(Range.lessThan(10000), 0.10);      // 10%
        taxRates.put(Range.closedOpen(10000, 50000), 0.20); // 20%
        taxRates.put(Range.atLeast(50000), 0.30);       // 30%

        int income = 35000;
        Double rate = taxRates.get(income);
        log.info("\nTax rate for income ${}: {}%", income, rate * 100);

        // Example 4: Grade mapping
        RangeMap<Integer, String> grades = TreeRangeMap.create();
        grades.put(Range.closedOpen(0, 60), "F");
        grades.put(Range.closedOpen(60, 70), "D");
        grades.put(Range.closedOpen(70, 80), "C");
        grades.put(Range.closedOpen(80, 90), "B");
        grades.put(Range.closed(90, 100), "A");

        log.info("\nScore 85 = Grade {}", grades.get(85));
        log.info("Score 92 = Grade {}", grades.get(92));
        log.info("Score 55 = Grade {}", grades.get(55));

        // Get subRangeMap
        RangeMap<Integer, String> subMap = grades.subRangeMap(Range.closed(70, 90));
        log.info("\nGrades in 70-90 range:");
        subMap.asMapOfRanges().forEach((range, grade) ->
                log.info("  {} = {}", range, grade));
    }

    public static void collectionUtilitiesExamples() {
        log.info("\n=== COLLECTION UTILITIES ===");

        // Lists.partition() - Split list into batches
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> batches = Lists.partition(numbers, 3);
        log.info("Batches of 3: {}", batches);
        // [[1, 2, 3], [4, 5, 6], [7, 8, 9], [10]]

        // Lists.reverse() - Reverse a list
        List<String> items = Arrays.asList("A", "B", "C", "D");
        List<String> reversed = Lists.reverse(items);
        log.info("Reversed: {}", reversed); // [D, C, B, A]

        // Sets.union() - Combine two sets
        Set<String> set1 = Sets.newHashSet("A", "B", "C");
        Set<String> set2 = Sets.newHashSet("C", "D", "E");
        Sets.SetView<String> union = Sets.union(set1, set2);
        log.info("\nUnion: {}", union); // [A, B, C, D, E]

        // Sets.intersection() - Common elements
        Sets.SetView<String> intersection = Sets.intersection(set1, set2);
        log.info("Intersection: {}", intersection); // [C]

        // Sets.difference() - Elements in set1 but not set2
        Sets.SetView<String> difference = Sets.difference(set1, set2);
        log.info("Difference (set1 - set2): {}", difference); // [A, B]

        // Maps.difference() - Compare two maps
        Map<String, Integer> map1 = ImmutableMap.of("A", 1, "B", 2, "C", 3);
        Map<String, Integer> map2 = ImmutableMap.of("B", 2, "C", 4, "D", 5);

        MapDifference<String, Integer> diff = Maps.difference(map1, map2);
        log.info("\nMap differences:");
        log.info("  Only in map1: {}", diff.entriesOnlyOnLeft()); // {A=1}
        log.info("  Only in map2: {}", diff.entriesOnlyOnRight()); // {D=5}
        log.info("  Different values: {}", diff.entriesDiffering()); // {C=(3, 4)}
        log.info("  Common: {}", diff.entriesInCommon()); // {B=2}

        // Maps.uniqueIndex() - Create index from list
        List<User> users = Arrays.asList(
                new User("alice@example.com", "Alice"),
                new User("bob@example.com", "Bob")
        );
        Map<String, User> byEmail = Maps.uniqueIndex(users, User::email);
        log.info("\nUser by email:");
        log.info("  alice@example.com -> {}", byEmail.get("alice@example.com").name());

        // Multimaps.index() - Group items by a function
        List<String> words = Arrays.asList("apple", "banana", "apricot", "blueberry");
        Multimap<Character, String> byFirstLetter = Multimaps.index(words,
                word -> word.charAt(0));
        log.info("\nWords grouped by first letter:");
        byFirstLetter.asMap().forEach((letter, wordList) ->
                log.info("  {}: {}", letter, wordList));

        // Collections2.filter() - Lazy filtering
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Collection<Integer> evens = Collections2.filter(nums, n -> n % 2 == 0);
        log.info("\nEven numbers: {}", evens); // [2, 4, 6, 8, 10]

        // Collections2.transform() - Lazy transformation
        Collection<String> strings = Collections2.transform(nums, n -> "Number: " + n);
        log.info("Transformed: {}", strings.stream().limit(3).toList());
    }

    public static void orderingExamples() {
        log.info("\n=== ORDERING ===");

        List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9);
        List<Integer> sorted = Ordering.natural().sortedCopy(numbers);
        log.info("Natural order: {}", sorted);

        List<Integer> reversed = Ordering.natural().reverse().sortedCopy(numbers);
        log.info("Reversed: {}", reversed);

        List<String> withNulls = Arrays.asList("B", null, "A", "C", null);
        List<String> sortedNulls = Ordering.natural().nullsFirst().sortedCopy(withNulls);
        log.info("Nulls first: {}", sortedNulls);

        // ordering with length then alphabetically, when length is the same
        List<String> words = Arrays.asList("banana", "apple", "kiwi", "pear");
        Ordering<String> byLength = Ordering.natural().onResultOf(String::length);
        Ordering<String> compound = byLength.compound(Ordering.natural());
        log.info("\nCompound ordering (length, then alpha): {}", compound.sortedCopy(words));

        List<Integer> topThree = Ordering.natural().greatestOf(numbers, 3);
        log.info("\nTop 3 numbers: {}", topThree);

        // Check if sorted
        boolean isSorted = Ordering.natural().isOrdered(sorted);
        log.info("\nIs list sorted? {}", isSorted);
    }

    // ========================================================================
    // 12. ADVANCED PATTERNS
    // ========================================================================

    public static void advancedPatterns() {
        log.info("\n=== ADVANCED PATTERNS ===");

        // Pattern 1: Counting with Multiset
        log.info("--- Word Frequency Counter ---");
        String text = "to be or not to be that is the question";
        Multiset<String> wordCount = HashMultiset.create(
                Arrays.asList(text.split("\\s+")));

        // Get top 3 words
        wordCount.entrySet().stream()
                .sorted((a, b) -> b.getCount() - a.getCount())
                .limit(3)
                .forEach(e -> log.info("{}: {}", e.getElement(), e.getCount()));

        // Pattern 2: Tag-based search with Multimap
        log.info("\n--- Tag-based Article Index ---");
        Multimap<String, String> articleTags = HashMultimap.create();
        articleTags.put("java", "Article-1");
        articleTags.put("java", "Article-2");
        articleTags.put("guava", "Article-2");
        articleTags.put("guava", "Article-3");

        // Find articles with both java AND guava
        Set<String> javaArticles = new HashSet<>(articleTags.get("java"));
        Set<String> guavaArticles = new HashSet<>(articleTags.get("guava"));
        javaArticles.retainAll(guavaArticles); // Intersection
        log.info("Articles with both tags: {}", javaArticles);

        // Pattern 3: User session management with BiMap
        log.info("\n--- Session Management ---");
        BiMap<String, Long> sessions = HashBiMap.create();
        sessions.put("session-abc", 100L);
        sessions.put("session-def", 200L);

        // Create new session for user 100 (invalidates old one)
        sessions.inverse().remove(100L);
        sessions.put("session-xyz", 100L);
        log.info("New session for user 100: {}", sessions.inverse().get(100L));

        // Pattern 4: Matrix operations with Table
        log.info("\n--- Multiplication Table ---");
        Table<Integer, Integer, Integer> multiplicationTable = HashBasedTable.create();
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 5; j++) {
                multiplicationTable.put(i, j, i * j);
            }
        }

        // Get row (all multiples of 3)
        log.info("3 times table: {}", multiplicationTable.row(3));

        // Pattern 5: Time slot booking with RangeSet
        log.info("\n--- Time Slot Booking ---");
        RangeSet<Integer> available = TreeRangeSet.create();
        available.add(Range.closed(9, 17)); // 9 AM to 5 PM

        // Book 2-3 PM
        available.remove(Range.closed(14, 15));
        log.info("Available slots: {}", available.asRanges());

        // Check if 2:30 PM is available
        boolean isAvailable = available.contains(14);
        log.info("Is 2:30 PM available? {}", isAvailable);

        // Pattern 6: Dynamic pricing with RangeMap
        log.info("\n--- Dynamic Pricing ---");
        RangeMap<Integer, String> pricing = TreeRangeMap.create();
        pricing.put(Range.lessThan(10), "Peak hours - $50/hr");
        pricing.put(Range.closed(10, 16), "Off-peak - $30/hr");
        pricing.put(Range.greaterThan(16), "Peak hours - $50/hr");

        log.info("Price at 8 AM: {}", pricing.get(8));
        log.info("Price at 12 PM: {}", pricing.get(12));
        log.info("Price at 6 PM: {}", pricing.get(18));

        // Pattern 7: Batch processing with Lists.partition()
        log.info("\n--- Batch Processing ---");
        List<Integer> allIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int batchSize = 3;

        for (List<Integer> batch : Lists.partition(allIds, batchSize)) {
            log.info("Processing batch: {}", batch);
            // Simulate batch database operation
        }

        // Pattern 8: Data grouping and aggregation
        log.info("\n--- Sales by Category ---");
        List<Sale> sales = Arrays.asList(
                new Sale("Electronics", 100),
                new Sale("Books", 50),
                new Sale("Electronics", 200),
                new Sale("Books", 75)
        );

        // Group by category
        Multimap<String, Sale> byCategory = Multimaps.index(sales, Sale::category);

        // Calculate total per category
        byCategory.asMap().forEach((category, salesList) -> {
            int total = salesList.stream()
                    .mapToInt(Sale::amount)
                    .sum();
            log.info("{}: ${}", category, total);
        });
    }
}