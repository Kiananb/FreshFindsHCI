package com.example.freshfinds;

import android.util.Log;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageStorage {
    private static final String TAG = "MessageStorage";
    private static final String REVIEWS_DATA_TYPE = "product_reviews";
    private static final String CONVERSATIONS_DATA_TYPE = "conversations";
    
    private static MessageStorage instance;
    private Map<String, List<ReviewItem>> productReviews;
    private Map<String, Conversation> conversations; // Store conversations by farmer name
    
    private MessageStorage() {
        loadData();
        
        // If there are no reviews after loading (first run), initialize with mock data
        if (productReviews.isEmpty()) {
            initializeReviews();
            saveReviewsData();
        }
    }
    
    public static synchronized MessageStorage getInstance() {
        if (instance == null) {
            instance = new MessageStorage();
        }
        return instance;
    }
    
    private void initializeReviews() {
        // Add some mock reviews for products
        
        // Reviews for Organic Apples
        List<ReviewItem> appleReviews = new ArrayList<>();
        appleReviews.add(new ReviewItem("f1", "Sarah J.", 5, "These apples are amazing! So crisp and sweet."));
        appleReviews.add(new ReviewItem("f1", "Mike T.", 4, "Good quality, but a bit pricey."));
        appleReviews.add(new ReviewItem("f1", "Linda R.", 5, "Best apples I've ever had. Will buy again!"));
        productReviews.put("f1", appleReviews);
        
        // Reviews for Fresh Spinach
        List<ReviewItem> spinachReviews = new ArrayList<>();
        spinachReviews.add(new ReviewItem("v2", "David K.", 4, "Fresh and lasted all week."));
        spinachReviews.add(new ReviewItem("v2", "Jenny L.", 3, "Decent quality but some leaves were wilted."));
        productReviews.put("v2", spinachReviews);
        
        // Reviews for Raw Wildflower Honey
        List<ReviewItem> honeyReviews = new ArrayList<>();
        honeyReviews.add(new ReviewItem("h1", "Robert S.", 5, "Delicious honey, amazing flavor!"));
        honeyReviews.add(new ReviewItem("h1", "Tina P.", 5, "Best honey I've ever tasted, truly wonderful."));
        honeyReviews.add(new ReviewItem("h1", "Daniel W.", 4, "Great taste, nice consistency."));
        productReviews.put("h1", honeyReviews);
    }
    
    // Load data from storage
    private void loadData() {
        loadReviewsData();
        loadConversationsData();
    }
    
    // Load reviews from SharedPreferences
    @SuppressWarnings("unchecked")
    private void loadReviewsData() {
        try {
            Type mapType = new TypeToken<HashMap<String, ArrayList<ReviewItem>>>(){}.getType();
            Map<String, List<ReviewItem>> loadedReviews = DataPersistenceManager.loadData(
                    REVIEWS_DATA_TYPE, mapType, new HashMap<>());
            
            if (loadedReviews != null) {
                productReviews = loadedReviews;
            } else {
                productReviews = new HashMap<>();
            }
            Log.d(TAG, "Reviews data loaded with " + productReviews.size() + " products");
        } catch (Exception e) {
            Log.e(TAG, "Error loading reviews data: " + e.getMessage());
            productReviews = new HashMap<>();
        }
    }
    
    // Save reviews to SharedPreferences
    private void saveReviewsData() {
        try {
            DataPersistenceManager.saveData(REVIEWS_DATA_TYPE, productReviews);
            Log.d(TAG, "Reviews data saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving reviews data: " + e.getMessage());
        }
    }
    
    // Load conversations from SharedPreferences
    @SuppressWarnings("unchecked")
    private void loadConversationsData() {
        try {
            Type mapType = new TypeToken<HashMap<String, Conversation>>(){}.getType();
            Map<String, Conversation> loadedConversations = DataPersistenceManager.loadData(
                    CONVERSATIONS_DATA_TYPE, mapType, new HashMap<>());
            
            if (loadedConversations != null) {
                conversations = loadedConversations;
            } else {
                conversations = new HashMap<>();
            }
            Log.d(TAG, "Conversations data loaded with " + conversations.size() + " conversations");
        } catch (Exception e) {
            Log.e(TAG, "Error loading conversations data: " + e.getMessage());
            conversations = new HashMap<>();
        }
    }
    
    // Save conversations to SharedPreferences
    private void saveConversationsData() {
        try {
            DataPersistenceManager.saveData(CONVERSATIONS_DATA_TYPE, conversations);
            Log.d(TAG, "Conversations data saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving conversations data: " + e.getMessage());
        }
    }
    
    // Reload all data from storage
    public void reloadData() {
        loadData();
    }
    
    public List<ReviewItem> getReviewsForProduct(String productId) {
        List<ReviewItem> reviews = productReviews.get(productId);
        return reviews != null ? reviews : new ArrayList<>();
    }
    
    public void addReview(String productId, ReviewItem review) {
        List<ReviewItem> reviews = productReviews.get(productId);
        if (reviews == null) {
            reviews = new ArrayList<>();
            productReviews.put(productId, reviews);
        }
        reviews.add(review);
        saveReviewsData();
    }
    
    public float getAverageRating(String productId) {
        List<ReviewItem> reviews = getReviewsForProduct(productId);
        if (reviews.isEmpty()) return 0;
        
        float sum = 0;
        for (ReviewItem review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }
    
    public int getReviewCount(String productId) {
        List<ReviewItem> reviews = getReviewsForProduct(productId);
        return reviews.size();
    }
    
    // Add a message to a conversation
    public void addMessage(String farmerName, String messageText, boolean fromFarmer) {
        Date timestamp = new Date();
        Message message = new Message(messageText, timestamp, fromFarmer);
        
        if (!conversations.containsKey(farmerName)) {
            conversations.put(farmerName, new Conversation(farmerName));
        }
        
        conversations.get(farmerName).addMessage(message);
        saveConversationsData();
    }
    
    // Get all conversations
    public List<Conversation> getAllConversations() {
        return new ArrayList<>(conversations.values());
    }
    
    // Get a specific conversation by farmer name
    public Conversation getConversation(String farmerName) {
        return conversations.get(farmerName);
    }
    
    // Check if any conversations exist
    public boolean hasConversations() {
        return !conversations.isEmpty();
    }
    
    // ReviewItem class
    public static class ReviewItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String productId;
        private String userName;
        private int rating;
        private String comment;
        
        public ReviewItem() {
            // Empty constructor for serialization
        }
        
        public ReviewItem(String productId, String userName, int rating, String comment) {
            this.productId = productId;
            this.userName = userName;
            this.rating = rating;
            this.comment = comment;
        }
        
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
    
    // Conversation class to hold a thread of messages with one farmer
    public static class Conversation implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String farmerName;
        private List<Message> messages;
        private Date lastMessageTime;
        
        public Conversation() {
            // Empty constructor for serialization
            this.messages = new ArrayList<>();
            this.lastMessageTime = new Date(0);
        }
        
        public Conversation(String farmerName) {
            this.farmerName = farmerName;
            this.messages = new ArrayList<>();
            this.lastMessageTime = new Date(0); // Initialize to "epoch" (oldest possible date)
        }
        
        public void addMessage(Message message) {
            messages.add(message);
            lastMessageTime = message.getTimestamp(); // Update last message time
        }
        
        public String getFarmerName() { return farmerName; }
        public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
        
        public List<Message> getMessages() { return messages; }
        public void setMessages(List<Message> messages) { this.messages = messages; }
        
        public Date getLastMessageTime() { return lastMessageTime; }
        public void setLastMessageTime(Date lastMessageTime) { this.lastMessageTime = lastMessageTime; }
        
        public int getMessageCount() { return messages.size(); }
        
        public Message getLastMessage() {
            if (messages.isEmpty()) return null;
            return messages.get(messages.size() - 1);
        }
    }
    
    // Individual message in a conversation
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String content;
        private Date timestamp;
        private boolean fromFarmer; // true if sent by farmer, false if sent by user
        
        public Message() {
            // Empty constructor for serialization
        }
        
        public Message(String content, Date timestamp, boolean fromFarmer) {
            this.content = content;
            this.timestamp = timestamp;
            this.fromFarmer = fromFarmer;
        }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        
        public boolean isFromFarmer() { return fromFarmer; }
        public void setFromFarmer(boolean fromFarmer) { this.fromFarmer = fromFarmer; }
    }
}
