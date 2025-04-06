package com.example.freshfinds;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationDetailActivity extends BaseActivity {

    private static final String TAG = "ConversationDetail";
    
    private String farmerName;
    private MessageStorage messageStorage;
    private MessageStorage.Conversation conversation;
    
    private TextView farmerNameTitle;
    private RecyclerView messageRecyclerView;
    private EditText messageInputField;
    private Button sendButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);
        
        // Get farmer name from intent
        farmerName = getIntent().getStringExtra("farmer_name");
        if (farmerName == null) {
            Log.e(TAG, "Farmer name is null, finishing activity");
            finish();
            return;
        }
        
        // Initialize MessageStorage
        messageStorage = MessageStorage.getInstance();
        conversation = messageStorage.getConversation(farmerName);
        if (conversation == null) {
            Log.e(TAG, "Conversation not found for farmer: " + farmerName);
            finish();
            return;
        }
        
        // Initialize views
        farmerNameTitle = findViewById(R.id.textViewFarmerNameTitle);
        messageRecyclerView = findViewById(R.id.recyclerViewMessages);
        messageInputField = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
        backButton = findViewById(R.id.buttonBack);
        
        // Set farmer name in title
        farmerNameTitle.setText(farmerName);
        
        // Setup RecyclerView
        MessagesAdapter adapter = new MessagesAdapter(conversation.getMessages());
        messageRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Stack from bottom (most recent at bottom)
        messageRecyclerView.setLayoutManager(layoutManager);
        
        // Setup send button
        sendButton.setOnClickListener(v -> {
            String messageText = messageInputField.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                // Add message to conversation
                messageStorage.addMessage(farmerName, messageText, false);
                messageInputField.setText("");
                
                // Refresh the messages list
                adapter.updateMessages(conversation.getMessages());
                messageRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        
        // Setup back button
        backButton.setOnClickListener(v -> finish());
    }
    
    private class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
        
        private List<MessageStorage.Message> messages;
        private final int VIEW_TYPE_USER = 0;
        private final int VIEW_TYPE_FARMER = 1;
        private SimpleDateFormat dateFormat;
        
        public MessagesAdapter(List<MessageStorage.Message> messages) {
            this.messages = messages;
            this.dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        }
        
        public void updateMessages(List<MessageStorage.Message> newMessages) {
            this.messages = newMessages;
            notifyDataSetChanged();
        }
        
        @Override
        public int getItemViewType(int position) {
            // Return different view types for user and farmer messages
            return messages.get(position).isFromFarmer() ? VIEW_TYPE_FARMER : VIEW_TYPE_USER;
        }
        
        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_USER) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_user, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_farmer, parent, false);
            }
            return new MessageViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            MessageStorage.Message message = messages.get(position);
            holder.messageText.setText(message.getContent());
            holder.timeText.setText(dateFormat.format(message.getTimestamp()));
        }
        
        @Override
        public int getItemCount() {
            return messages.size();
        }
        
        class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;
            TextView timeText;
            
            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.textViewMessage);
                timeText = itemView.findViewById(R.id.textViewMessageTime);
            }
        }
    }
}
