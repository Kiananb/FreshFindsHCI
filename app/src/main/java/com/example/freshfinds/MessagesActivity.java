package com.example.freshfinds;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesActivity extends BaseActivity {

    private static final String TAG = "MessagesActivity";
    private RecyclerView recyclerView;
    private TextView placeholderText;
    private BottomNavigationView bottomNavigationView;
    private MessageStorage messageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        
        messageStorage = MessageStorage.getInstance();
        
        recyclerView = findViewById(R.id.recyclerViewMessages);
        placeholderText = findViewById(R.id.textViewPlaceholder);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        updateConversationList();
        
        // Setup bottom navigation
        setupBottomNavigation();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateConversationList();
    }
    
    private void updateConversationList() {
        // Get conversations
        List<MessageStorage.Conversation> conversations = messageStorage.getAllConversations();
        
        // Display conversations or show placeholder
        if (conversations.isEmpty()) {
            placeholderText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            placeholderText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            // Setup RecyclerView
            ConversationsAdapter adapter = new ConversationsAdapter(conversations);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.messages); // Set messages as selected
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(MessagesActivity.this, HomePage.class));
                        overridePendingTransition(0, 0); // Disable animation
                        finish();
                        return true;
                    } else if (itemId == R.id.cart) {
                        startActivity(new Intent(MessagesActivity.this, CartActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    } else if (itemId == R.id.messages) {
                        // Already on messages screen
                        return true;
                    } else if (itemId == R.id.profile) {
                        startActivity(new Intent(MessagesActivity.this, ProfileActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(MessagesActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    
    private class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {
        
        private List<MessageStorage.Conversation> conversations;
        private SimpleDateFormat dateFormat;
        
        public ConversationsAdapter(List<MessageStorage.Conversation> conversations) {
            this.conversations = conversations;
            this.dateFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        }
        
        @NonNull
        @Override
        public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MessagesActivity.this).inflate(
                    R.layout.item_conversation_preview, parent, false);
            return new ConversationViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
            MessageStorage.Conversation conversation = conversations.get(position);
            
            holder.farmerNameText.setText(conversation.getFarmerName());
            
            MessageStorage.Message lastMessage = conversation.getLastMessage();
            if (lastMessage != null) {
                holder.lastMessageText.setText(lastMessage.getContent());
                holder.timestampText.setText(dateFormat.format(lastMessage.getTimestamp()));
            } else {
                holder.lastMessageText.setText("No messages");
                holder.timestampText.setText("");
            }
            
            // Set click listener to open conversation detail activity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MessagesActivity.this, ConversationDetailActivity.class);
                intent.putExtra("farmer_name", conversation.getFarmerName());
                startActivity(intent);
            });
        }
        
        @Override
        public int getItemCount() {
            return conversations.size();
        }
        
        class ConversationViewHolder extends RecyclerView.ViewHolder {
            TextView farmerNameText, lastMessageText, timestampText;
            
            public ConversationViewHolder(@NonNull View itemView) {
                super(itemView);
                
                farmerNameText = itemView.findViewById(R.id.textViewFarmerName);
                lastMessageText = itemView.findViewById(R.id.textViewLastMessage);
                timestampText = itemView.findViewById(R.id.textViewTimestamp);
            }
        }
    }
}
