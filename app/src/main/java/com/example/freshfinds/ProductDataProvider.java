package com.example.freshfinds;

import java.util.ArrayList;
import java.util.List;

public class ProductDataProvider {
    private static List<Product> productList;
    
    public static List<Product> getProductList() {
        if (productList == null) {
            productList = new ArrayList<>();
            initializeProducts();
        }
        return productList;
    }
    
    private static void initializeProducts() {
        // For real product images, replace placeholder with actual drawable resources
        
        // Vegetables
        productList.add(new Product("v1", "Organic Carrots", 
            "Fresh organic carrots from local farms. Perfect for salads and cooking.", 
            2.99, "Greenfield Farm", R.drawable.carrots, "vegetables", true));

        productList.add(new Product("v2", "Fresh Spinach",
            "Nutrient-rich spinach leaves, perfect for salads or cooking.", 
            3.49, "Green Valley Farm", R.drawable.spinach, "vegetables", true));
            
        productList.add(new Product("v3", "Bell Peppers", 
            "Sweet and crunchy red bell peppers. Great for stir-fries or raw in salads.", 
            1.99, "Sunny Acres", R.drawable.red_bell_peppers, "vegetables", false));
            
        productList.add(new Product("v4", "Organic Broccoli", 
            "Fresh broccoli heads. Rich in vitamins and nutrients.", 
            2.79, "Greenfield Farm", R.drawable.broccoli, "vegetables", true));
            
        productList.add(new Product("v5", "Zucchini", 
            "Fresh zucchini squash. Versatile for cooking and grilling.", 
            1.49, "Sunny Acres", R.drawable.zucchini, "vegetables", false));
        
        // Fruits
        productList.add(new Product("f1", "Organic Apples", 
            "Sweet and crisp apples grown without pesticides.", 
            4.99, "Orchard Hills", R.drawable.apples, "fruits", true));
            
        productList.add(new Product("f2", "Strawberries", 
            "Sweet, juicy strawberries picked at peak ripeness.", 
            3.99, "Berry Lane Farm", R.drawable.strawberries, "fruits", false));
            
        productList.add(new Product("f3", "Organic Blueberries", 
            "Plump and sweet organic blueberries. Great for snacking or baking.", 
            5.49, "Berry Lane Farm", R.drawable.blueberries, "fruits", true));
            
        productList.add(new Product("f4", "Bananas", 
            "Ripe, sweet bananas. Perfect for eating fresh or in smoothies.", 
            1.29, "Tropical Harvest", R.drawable.bananas, "fruits", false));
            
        productList.add(new Product("f5", "Peaches", 
            "Juicy, sweet peaches at peak ripeness.", 
            2.99, "Orchard Hills", R.drawable.peaches, "fruits", false));
        
        // Dairy & Eggs
        productList.add(new Product("d1", "Farm Fresh Eggs", 
            "Free-range, organic eggs from pasture-raised hens.", 
            5.99, "Happy Hen Farm", R.drawable.eggs, "dairy", true));
            
        productList.add(new Product("d2", "Fresh Milk", 
            "Fresh milk from grass-fed cows. Non-homogenized and rich in flavor.", 
            4.49, "Green Pastures Dairy", R.drawable.milk, "dairy", true));
        
        // Honey & Preserves
        productList.add(new Product("h1", "Raw Wildflower Honey", 
            "Unfiltered, raw honey collected from wildflower fields. Rich in flavor and health benefits.", 
            8.99, "Busy Bee Apiary", R.drawable.honey, "honey", true));
            
        productList.add(new Product("h2", "Strawberry Jam", 
            "Homemade strawberry jam made with fresh berries and pure cane sugar.", 
            6.49, "Berry Lane Farm", R.drawable.strawberry_jam, "honey", false));
        
        // Herbs
        productList.add(new Product("hr1", "Fresh Basil", 
            "Aromatic fresh basil. Perfect for Italian dishes and pesto.", 
            2.49, "Greenfield Farm", R.drawable.basil, "herbs", true));
            
        productList.add(new Product("hr2", "Mint", 
            "Fresh mint leaves. Great for teas, cocktails, and cooking.", 
            1.99, "Herb Haven", R.drawable.mint, "herbs", false));
    }
    
    public static List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getProductList();
        }
        
        String lowercaseQuery = query.toLowerCase().trim();
        List<Product> searchResults = new ArrayList<>();
        
        for (Product product : getProductList()) {
            // First check if the query exactly matches the start of words in the name
            if (product.getName().toLowerCase().startsWith(lowercaseQuery) || 
                containsWordStartingWith(product.getName().toLowerCase(), lowercaseQuery)) {
                searchResults.add(product);
                continue;
            }
            
            // Then check category (exact match only)
            if (product.getCategory().toLowerCase().equals(lowercaseQuery)) {
                searchResults.add(product);
                continue;
            }
            
            // Only if we have longer queries (3+ chars) do we search description and farm
            if (lowercaseQuery.length() >= 3) {
                if (containsWordStartingWith(product.getDescription().toLowerCase(), lowercaseQuery) ||
                    containsWordStartingWith(product.getFarm().toLowerCase(), lowercaseQuery)) {
                    searchResults.add(product);
                }
            }
        }
        
        return searchResults;
    }
    
    // Helper to check if a string contains any word starting with the given prefix
    private static boolean containsWordStartingWith(String text, String prefix) {
        // Split by spaces, dashes, commas, etc.
        String[] words = text.split("[\\s,.-]+");
        for (String word : words) {
            if (word.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
    
    public static Product getProductById(String id) {
        for (Product product : getProductList()) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }
}
