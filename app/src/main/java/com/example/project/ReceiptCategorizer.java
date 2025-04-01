package com.example.project;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Local receipt categorization utility class.
 * Uses keyword matching and pattern recognition to categorize receipts
 * without requiring external API calls.
 */
public class ReceiptCategorizer {
    // Define categories
    public static final String CATEGORY_GROCERIES = "Groceries";
    public static final String CATEGORY_FOOD = "Food";
    public static final String CATEGORY_UTILITIES = "Utilities";
    public static final String CATEGORY_SHOPPING = "Shopping";
    public static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String CATEGORY_HEALTHCARE = "Healthcare";
    public static final String CATEGORY_TRAVEL = "Travel";
    public static final String CATEGORY_UNKNOWN = "Other";

    // Maps to hold keywords for each category
    private final Map<String, List<String>> categoryKeywords;

    // Maps to hold merchant names for each category
    private final Map<String, List<String>> merchantCategories;

    // Confidence threshold for categorization
    private static final double CONFIDENCE_THRESHOLD = 0.6;

    // Constructor - initialize the keyword maps
    public ReceiptCategorizer() {
        categoryKeywords = new HashMap<>();
        merchantCategories = new HashMap<>();
        initializeCategoryKeywords();
        initializeMerchantCategories();
    }

    /**
     * Initialize keywords for each category
     */
    private void initializeCategoryKeywords() {
        // Groceries keywords
        List<String> groceriesKeywords = new ArrayList<>();
        groceriesKeywords.add("grocery");
        groceriesKeywords.add("groceries");
        groceriesKeywords.add("supermarket");
        groceriesKeywords.add("market");
        groceriesKeywords.add("fruit");
        groceriesKeywords.add("vegetable");
        groceriesKeywords.add("produce");
        groceriesKeywords.add("dairy");
        groceriesKeywords.add("meat");
        groceriesKeywords.add("bakery");
        groceriesKeywords.add("deli");
        categoryKeywords.put(CATEGORY_GROCERIES, groceriesKeywords);

        // Food/Dining keywords
        List<String> foodKeywords = new ArrayList<>();
        foodKeywords.add("restaurant");
        foodKeywords.add("cafe");
        foodKeywords.add("coffee");
        foodKeywords.add("diner");
        foodKeywords.add("bistro");
        foodKeywords.add("eatery");
        foodKeywords.add("bar");
        foodKeywords.add("grill");
        foodKeywords.add("kitchen");
        foodKeywords.add("burger");
        foodKeywords.add("pizza");
        foodKeywords.add("sandwich");
        foodKeywords.add("taco");
        foodKeywords.add("sushi");
        foodKeywords.add("thai");
        foodKeywords.add("italian");
        foodKeywords.add("chinese");
        foodKeywords.add("mexican");
        foodKeywords.add("indian");
        foodKeywords.add("takeout");
        foodKeywords.add("delivery");
        foodKeywords.add("doordash");
        foodKeywords.add("ubereats");
        foodKeywords.add("grubhub");
        categoryKeywords.put(CATEGORY_FOOD, foodKeywords);

        // Utilities keywords
        List<String> utilitiesKeywords = new ArrayList<>();
        utilitiesKeywords.add("electric");
        utilitiesKeywords.add("electricity");
        utilitiesKeywords.add("water");
        utilitiesKeywords.add("gas");
        utilitiesKeywords.add("power");
        utilitiesKeywords.add("energy");
        utilitiesKeywords.add("utility");
        utilitiesKeywords.add("utilities");
        utilitiesKeywords.add("internet");
        utilitiesKeywords.add("phone");
        utilitiesKeywords.add("mobile");
        utilitiesKeywords.add("cellular");
        utilitiesKeywords.add("bill");
        utilitiesKeywords.add("service");
        utilitiesKeywords.add("broadband");
        utilitiesKeywords.add("cable");
        utilitiesKeywords.add("satellite");
        categoryKeywords.put(CATEGORY_UTILITIES, utilitiesKeywords);

        // Shopping keywords
        List<String> shoppingKeywords = new ArrayList<>();
        shoppingKeywords.add("store");
        shoppingKeywords.add("shop");
        shoppingKeywords.add("mall");
        shoppingKeywords.add("retail");
        shoppingKeywords.add("outlet");
        shoppingKeywords.add("boutique");
        shoppingKeywords.add("clothing");
        shoppingKeywords.add("apparel");
        shoppingKeywords.add("fashion");
        shoppingKeywords.add("shoes");
        shoppingKeywords.add("accessories");
        shoppingKeywords.add("jewelry");
        shoppingKeywords.add("electronics");
        shoppingKeywords.add("furniture");
        shoppingKeywords.add("home");
        shoppingKeywords.add("hardware");
        shoppingKeywords.add("appliance");
        shoppingKeywords.add("department");
        shoppingKeywords.add("amazon");
        shoppingKeywords.add("walmart");
        shoppingKeywords.add("target");
        shoppingKeywords.add("best buy");
        shoppingKeywords.add("costco");
        categoryKeywords.put(CATEGORY_SHOPPING, shoppingKeywords);

        // Entertainment keywords
        List<String> entertainmentKeywords = new ArrayList<>();
        entertainmentKeywords.add("cinema");
        entertainmentKeywords.add("movie");
        entertainmentKeywords.add("theater");
        entertainmentKeywords.add("theatre");
        entertainmentKeywords.add("concert");
        entertainmentKeywords.add("show");
        entertainmentKeywords.add("performance");
        entertainmentKeywords.add("event");
        entertainmentKeywords.add("ticket");
        entertainmentKeywords.add("admission");
        entertainmentKeywords.add("museum");
        entertainmentKeywords.add("gallery");
        entertainmentKeywords.add("park");
        entertainmentKeywords.add("zoo");
        entertainmentKeywords.add("aquarium");
        entertainmentKeywords.add("game");
        entertainmentKeywords.add("arcade");
        entertainmentKeywords.add("entertainment");
        entertainmentKeywords.add("streaming");
        entertainmentKeywords.add("netflix");
        entertainmentKeywords.add("hulu");
        entertainmentKeywords.add("disney");
        entertainmentKeywords.add("spotify");
        entertainmentKeywords.add("apple music");
        categoryKeywords.put(CATEGORY_ENTERTAINMENT, entertainmentKeywords);

        // Healthcare keywords
        List<String> healthcareKeywords = new ArrayList<>();
        healthcareKeywords.add("doctor");
        healthcareKeywords.add("physician");
        healthcareKeywords.add("medical");
        healthcareKeywords.add("health");
        healthcareKeywords.add("healthcare");
        healthcareKeywords.add("hospital");
        healthcareKeywords.add("clinic");
        healthcareKeywords.add("pharmacy");
        healthcareKeywords.add("drug");
        healthcareKeywords.add("prescription");
        healthcareKeywords.add("medicine");
        healthcareKeywords.add("dental");
        healthcareKeywords.add("dentist");
        healthcareKeywords.add("vision");
        healthcareKeywords.add("optical");
        healthcareKeywords.add("therapy");
        healthcareKeywords.add("therapist");
        healthcareKeywords.add("specialist");
        healthcareKeywords.add("wellness");
        healthcareKeywords.add("fitness");
        healthcareKeywords.add("gym");
        categoryKeywords.put(CATEGORY_HEALTHCARE, healthcareKeywords);

        // Travel keywords
        List<String> travelKeywords = new ArrayList<>();
        travelKeywords.add("travel");
        travelKeywords.add("trip");
        travelKeywords.add("vacation");
        travelKeywords.add("flight");
        travelKeywords.add("airline");
        travelKeywords.add("airfare");
        travelKeywords.add("hotel");
        travelKeywords.add("motel");
        travelKeywords.add("inn");
        travelKeywords.add("accommodation");
        travelKeywords.add("lodging");
        travelKeywords.add("resort");
        travelKeywords.add("car rental");
        travelKeywords.add("taxi");
        travelKeywords.add("cab");
        travelKeywords.add("uber");
        travelKeywords.add("lyft");
        travelKeywords.add("transport");
        travelKeywords.add("transportation");
        travelKeywords.add("train");
        travelKeywords.add("bus");
        travelKeywords.add("subway");
        travelKeywords.add("metro");
        travelKeywords.add("fare");
        travelKeywords.add("ticket");
        travelKeywords.add("booking");
        travelKeywords.add("reservation");
        categoryKeywords.put(CATEGORY_TRAVEL, travelKeywords);
    }

    /**
     * Initialize known merchant names for each category
     */
    private void initializeMerchantCategories() {
        // Grocery stores
        List<String> groceryStores = new ArrayList<>();
        groceryStores.add("kroger");
        groceryStores.add("albertsons");
        groceryStores.add("safeway");
        groceryStores.add("trader joe");
        groceryStores.add("whole foods");
        groceryStores.add("aldi");
        groceryStores.add("publix");
        groceryStores.add("heb");
        groceryStores.add("giant");
        groceryStores.add("food lion");
        groceryStores.add("meijer");
        groceryStores.add("stop & shop");
        groceryStores.add("wegmans");
        groceryStores.add("sprouts");
        groceryStores.add("harris teeter");
        groceryStores.add("winco");
        groceryStores.add("shoprite");
        groceryStores.add("save mart");
        groceryStores.add("vons");
        groceryStores.add("ralphs");
        groceryStores.add("piggly wiggly");
        merchantCategories.put(CATEGORY_GROCERIES, groceryStores);

        // Restaurants and food services
        List<String> restaurants = new ArrayList<>();
        restaurants.add("mcdonald");
        restaurants.add("burger king");
        restaurants.add("wendy");
        restaurants.add("subway");
        restaurants.add("taco bell");
        restaurants.add("chipotle");
        restaurants.add("starbucks");
        restaurants.add("dunkin");
        restaurants.add("pizza hut");
        restaurants.add("domino");
        restaurants.add("panera");
        restaurants.add("kfc");
        restaurants.add("olive garden");
        restaurants.add("applebee");
        restaurants.add("chili");
        restaurants.add("outback");
        restaurants.add("red lobster");
        restaurants.add("ihop");
        restaurants.add("denny");
        restaurants.add("cheesecake factory");
        restaurants.add("panda express");
        restaurants.add("sonic");
        restaurants.add("five guys");
        restaurants.add("popeyes");
        restaurants.add("chick-fil-a");
        merchantCategories.put(CATEGORY_FOOD, restaurants);

        // Utility companies
        List<String> utilities = new ArrayList<>();
        utilities.add("at&t");
        utilities.add("verizon");
        utilities.add("t-mobile");
        utilities.add("sprint");
        utilities.add("xfinity");
        utilities.add("comcast");
        utilities.add("spectrum");
        utilities.add("cox");
        utilities.add("dish");
        utilities.add("directv");
        utilities.add("pg&e");
        utilities.add("duke energy");
        utilities.add("edison");
        utilities.add("dominion");
        utilities.add("national grid");
        utilities.add("centurylink");
        utilities.add("frontier");
        merchantCategories.put(CATEGORY_UTILITIES, utilities);

        // Retail stores (non-grocery)
        List<String> retailStores = new ArrayList<>();
        retailStores.add("amazon");
        retailStores.add("walmart");
        retailStores.add("target");
        retailStores.add("costco");
        retailStores.add("best buy");
        retailStores.add("home depot");
        retailStores.add("lowes");
        retailStores.add("ikea");
        retailStores.add("macys");
        retailStores.add("kohls");
        retailStores.add("nordstrom");
        retailStores.add("sephora");
        retailStores.add("ulta");
        retailStores.add("gamestop");
        retailStores.add("apple store");
        retailStores.add("microsoft");
        retailStores.add("old navy");
        retailStores.add("gap");
        retailStores.add("tj maxx");
        retailStores.add("marshalls");
        retailStores.add("ross");
        retailStores.add("dollar tree");
        retailStores.add("dollar general");
        retailStores.add("walgreens");
        retailStores.add("cvs");
        merchantCategories.put(CATEGORY_SHOPPING, retailStores);

        // Entertainment venues and services
        List<String> entertainment = new ArrayList<>();
        entertainment.add("amc");
        entertainment.add("regal");
        entertainment.add("cinemark");
        entertainment.add("netflix");
        entertainment.add("hulu");
        entertainment.add("disney+");
        entertainment.add("spotify");
        entertainment.add("apple music");
        entertainment.add("hbo");
        entertainment.add("youtube");
        entertainment.add("prime video");
        entertainment.add("paramount");
        entertainment.add("live nation");
        entertainment.add("ticketmaster");
        entertainment.add("stubhub");
        entertainment.add("spotify");
        entertainment.add("dave & buster");
        merchantCategories.put(CATEGORY_ENTERTAINMENT, entertainment);

        // Healthcare providers and pharmacies
        List<String> healthcare = new ArrayList<>();
        healthcare.add("cvs pharmacy");
        healthcare.add("walgreens pharmacy");
        healthcare.add("rite aid");
        healthcare.add("kaiser");
        healthcare.add("blue cross");
        healthcare.add("aetna");
        healthcare.add("humana");
        healthcare.add("cigna");
        healthcare.add("unitedhealth");
        healthcare.add("walgreens");
        healthcare.add("cvs");
        healthcare.add("express scripts");
        healthcare.add("optum");
        healthcare.add("labcorp");
        healthcare.add("quest diagnostics");
        merchantCategories.put(CATEGORY_HEALTHCARE, healthcare);

        // Travel companies
        List<String> travel = new ArrayList<>();
        travel.add("american airlines");
        travel.add("delta");
        travel.add("united");
        travel.add("southwest");
        travel.add("jetblue");
        travel.add("frontier");
        travel.add("spirit");
        travel.add("alaska air");
        travel.add("marriott");
        travel.add("hilton");
        travel.add("hyatt");
        travel.add("holiday inn");
        travel.add("expedia");
        travel.add("booking.com");
        travel.add("airbnb");
        travel.add("hertz");
        travel.add("avis");
        travel.add("enterprise");
        travel.add("budget");
        travel.add("uber");
        travel.add("lyft");
        travel.add("amtrak");
        travel.add("greyhound");
        merchantCategories.put(CATEGORY_TRAVEL, travel);
    }

    /**
     * Categorize receipt text locally without using external APIs
     *
     * @param receiptText The full text extracted from the receipt
     * @return The best matching category
     */
    public String categorizeReceipt(String receiptText) {
        if (receiptText == null || receiptText.trim().isEmpty()) {
            return CATEGORY_UNKNOWN;
        }

        // Convert to lowercase for case-insensitive matching
        String text = receiptText.toLowerCase();

        // Step 1: Try to identify store/merchant name from the first few lines
        String merchantName = extractMerchantName(text);
        if (merchantName != null && !merchantName.isEmpty()) {
            String merchantCategory = categoryFromMerchant(merchantName);
            if (!merchantCategory.equals(CATEGORY_UNKNOWN)) {
                Log.d("ReceiptCategorizer", "Categorized by merchant name: " + merchantName + " -> " + merchantCategory);
                return merchantCategory;
            }
        }

        // Step 2: Score each category based on keyword matches
        Map<String, Double> categoryScores = new HashMap<>();

        // Initialize scores
        categoryScores.put(CATEGORY_GROCERIES, 0.0);
        categoryScores.put(CATEGORY_FOOD, 0.0);
        categoryScores.put(CATEGORY_UTILITIES, 0.0);
        categoryScores.put(CATEGORY_SHOPPING, 0.0);
        categoryScores.put(CATEGORY_ENTERTAINMENT, 0.0);
        categoryScores.put(CATEGORY_HEALTHCARE, 0.0);
        categoryScores.put(CATEGORY_TRAVEL, 0.0);

        // Calculate scores for each category
        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            String category = entry.getKey();
            List<String> keywords = entry.getValue();

            double score = calculateCategoryScore(text, keywords);
            categoryScores.put(category, score);
        }

        // Find category with highest score
        String bestCategory = CATEGORY_SHOPPING; // Default to Shopping if no clear winner
        double bestScore = 0.0;

        for (Map.Entry<String, Double> entry : categoryScores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        // Only return the category if score is above confidence threshold
        if (bestScore >= CONFIDENCE_THRESHOLD) {
            Log.d("ReceiptCategorizer", "Categorized by keywords with score " + bestScore + ": " + bestCategory);
            return bestCategory;
        } else {
            // If confidence is low, default to Shopping as most common category
            Log.d("ReceiptCategorizer", "Low confidence categorization (score: " + bestScore + "), defaulting to Shopping");
            return CATEGORY_SHOPPING;
        }
    }

    /**
     * Try to extract the merchant/store name from receipt text
     * Usually at the top of the receipt
     */
    private String extractMerchantName(String text) {
        // Split text into lines
        String[] lines = text.split("\\r?\\n");

        // Check first 3 lines (usually where merchant name appears)
        for (int i = 0; i < Math.min(3, lines.length); i++) {
            String line = lines[i].trim();

            // Skip very short lines or lines with just numbers/special chars
            if (line.length() > 3 && line.matches(".*[a-zA-Z].*")) {
                // Remove common prefixes like "Welcome to" etc.
                line = line.replaceAll("(?i)welcome to |thank you for shopping at |receipt from ", "").trim();

                // Further clean the line
                line = line.replaceAll("[^a-zA-Z0-9\\s]", "").trim();

                if (!line.isEmpty()) {
                    return line;
                }
            }
        }

        return "";
    }

    /**
     * Try to determine category from merchant name
     */
    private String categoryFromMerchant(String merchantName) {
        merchantName = merchantName.toLowerCase();

        // Check each category's merchant list
        for (Map.Entry<String, List<String>> entry : merchantCategories.entrySet()) {
            for (String merchant : entry.getValue()) {
                // Check if merchant name contains this known merchant
                if (merchantName.contains(merchant) || merchantLevDistance(merchantName, merchant) <= 2) {
                    return entry.getKey();
                }
            }
        }

        return CATEGORY_UNKNOWN;
    }

    /**
     * Calculate category score based on keyword matches
     */
    private double calculateCategoryScore(String text, List<String> keywords) {
        int matches = 0;
        int totalWords = text.split("\\s+").length;

        for (String keyword : keywords) {
            // Count how many times this keyword appears
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                matches++;
            }
        }

        // Normalize score based on text length
        return (double) matches / Math.min(20, totalWords);
    }

    /**
     * Levenshtein distance for fuzzy merchant name matching
     * Helps with typos and OCR errors
     */
    private int merchantLevDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];

        for (int j = 0; j <= s2.length(); j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;

            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }

            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[s2.length()];
    }

    /**
     * Extract amount from receipt text
     */
    public double extractAmount(String text) {
        try {
            // Priority 1: Look for "Total" or "Amount" followed by price
            Pattern totalPattern = Pattern.compile("(?i)\\b(total|amount|sum|due|pay|balance)\\s*:?\\s*\\$?\\s*(\\d+\\.\\d{2})");
            Matcher totalMatcher = totalPattern.matcher(text);
            if (totalMatcher.find()) {
                return Double.parseDouble(totalMatcher.group(2));
            }

            // Priority 2: Find the largest dollar amount in the text (often the total)
            Pattern amountPattern = Pattern.compile("\\$?\\s*(\\d+\\.\\d{2})");
            Matcher amountMatcher = amountPattern.matcher(text);

            double largestAmount = 0.0;

            while (amountMatcher.find()) {
                double amount = Double.parseDouble(amountMatcher.group(1));
                if (amount > largestAmount) {
                    largestAmount = amount;
                }
            }

            if (largestAmount > 0.0) {
                return largestAmount;
            }

            // Priority 3: Look for any number with 2 decimal places
            Pattern numberPattern = Pattern.compile("(\\d+\\.\\d{2})");
            Matcher numberMatcher = numberPattern.matcher(text);

            if (numberMatcher.find()) {
                return Double.parseDouble(numberMatcher.group(1));
            }

        } catch (NumberFormatException e) {
            Log.e("ReceiptCategorizer", "Error parsing amount: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Extract date from receipt text
     */
    public String extractDate(String text) {
        try {
            // Look for common date formats: MM/DD/YYYY, MM-DD-YYYY, etc.
            Pattern datePattern = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b");
            Matcher dateMatcher = datePattern.matcher(text);

            if (dateMatcher.find()) {
                return dateMatcher.group(1);
            }

            // Try another format: Month name DD, YYYY
            Pattern textDatePattern = Pattern.compile("\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]* \\d{1,2},? \\d{2,4}\\b", Pattern.CASE_INSENSITIVE);
            Matcher textDateMatcher = textDatePattern.matcher(text);

            if (textDateMatcher.find()) {
                return textDateMatcher.group(0);
            }

        } catch (Exception e) {
            Log.e("ReceiptCategorizer", "Error extracting date: " + e.getMessage());
        }

        return "";
    }
}