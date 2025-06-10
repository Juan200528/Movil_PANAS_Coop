package com.juan.movil_panas_coop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private Button buttonSendMessage;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String activityMongoDbId;
    private CollectionReference messagesRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado. Redirigiendo al inicio de sesión.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, InicioSesion.class));
            finish();
            return;
        }

        // Initialize Firestore and UI components
        db = FirebaseFirestore.getInstance();
        messagesRef = db.collection("chats").document(activityMongoDbId).collection("messages");

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        buttonSendMessage.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "El mensaje no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderId = currentUser.getUid();
        String senderName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuario";

        Message message = new Message(senderId, senderName, messageText);

        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    editTextMessage.setText("");
                    Log.d(TAG, "Mensaje enviado con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al enviar mensaje", e);
                    Toast.makeText(ChatActivity.this, "Error al enviar mensaje.", Toast.LENGTH_SHORT).show();
                });
    }

    private void listenForMessages() {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Toast.makeText(ChatActivity.this, "Error al cargar mensajes.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Message message = dc.getDocument().toObject(Message.class);
                                messageList.add(message);
                                messageAdapter.notifyItemInserted(messageList.size() - 1);
                                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                                break;
                        }
                    }
                });
    }
}