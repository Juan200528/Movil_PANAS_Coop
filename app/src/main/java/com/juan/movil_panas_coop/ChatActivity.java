package com.juan.movil_panas_coop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FieldValue; // Para ServerTimestamp si no usas la anotación

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String activityMongoDbId; // Este ID vendrá de la actividad anterior
    private CollectionReference messagesRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Obtener el ID de la actividad de los extras del Intent
        // Necesitarás pasar esto cuando inicies ChatActivity
        activityMongoDbId = getIntent().getStringExtra("ACTIVITY_MONGO_ID");
        String activityName = getIntent().getStringExtra("ACTIVITY_NAME"); // Opcional

        if (activityMongoDbId == null) {
            Toast.makeText(this, "ID de actividad no encontrado.", Toast.LENGTH_LONG).show();
            finish(); // Cerrar si no hay ID
            return;
        }

        if (getSupportActionBar() != null && activityName != null) {
            getSupportActionBar().setTitle(activityName); // Poner el nombre de la actividad como título
        }


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // El usuario no está logueado, manejar esto (e.g., redirigir a login)
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_LONG).show();
            // Por ahora, simplemente cerramos, pero deberías tener un flujo de login
            finish();
            return;
        }

        // Referencia a la subcolección de mensajes para este chat específico
        messagesRef = db.collection("chats").document(activityMongoDbId).collection("messages");

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Para que los nuevos mensajes aparezcan abajo y se haga scroll
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        listenForMessages();
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "El mensaje no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            String senderId = currentUser.getUid();
            String senderName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuario"; // O tu lógica para nombres

            // Usamos la clase Message con el constructor que no toma el timestamp,
            // ya que @ServerTimestamp se encargará de ello.
            Message message = new Message(senderId, senderName, messageText);

            messagesRef.add(message) // add() crea un ID de documento automático para el mensaje
                    .addOnSuccessListener(documentReference -> {
                        editTextMessage.setText(""); // Limpiar el campo de texto
                        Log.d(TAG, "Mensaje enviado con ID: " + documentReference.getId());
                        // El RecyclerView se actualizará por el listener
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al enviar mensaje", e);
                        Toast.makeText(ChatActivity.this, "Error al enviar mensaje.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void listenForMessages() {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING) // Ordenar por timestamp
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            Toast.makeText(ChatActivity.this, "Error al cargar mensajes.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message message = dc.getDocument().toObject(Message.class);
                                    // Podrías añadir lógica aquí para evitar duplicados si recargas la actividad,
                                    // pero con addSnapshotListener y limpiando/re-añadiendo no debería ser un gran problema.
                                    // O mejor, solo añade el nuevo.
                                    messageList.add(message);
                                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                                    recyclerViewMessages.scrollToPosition(messageList.size() - 1); // Scroll al último mensaje
                                    Log.d(TAG, "Nuevo mensaje: " + dc.getDocument().getData());
                                    break;
                                case MODIFIED:
                                    // Si permites editar mensajes (no es común en chats simples)
                                    Log.d(TAG, "Mensaje modificado: " + dc.getDocument().getData());
                                    // Necesitarías encontrar y actualizar el mensaje en tu messageList
                                    break;
                                case REMOVED:
                                    // Si permites borrar mensajes
                                    Log.d(TAG, "Mensaje eliminado: " + dc.getDocument().getData());
                                    // Necesitarías encontrar y eliminar el mensaje en tu messageList
                                    break;
                            }
                        }
                        // Si prefieres recargar toda la lista cada vez (más simple pero menos eficiente para cambios pequeños):
                        // messageList.clear();
                        // for (QueryDocumentSnapshot doc : snapshots) {
                        //    Message message = doc.toObject(Message.class);
                        //    messageList.add(message);
                        // }
                        // messageAdapter.notifyDataSetChanged();
                        // if (!messageList.isEmpty()) {
                        //    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                        // }
                    }
                });
    }

    // Cómo iniciar esta actividad desde otra:
    // Intent intent = new Intent(this, ChatActivity.class);
    // intent.putExtra("ACTIVITY_MONGO_ID", "el_id_de_mongo_de_la_actividad");
    // intent.putExtra("ACTIVITY_NAME", "Nombre de la Actividad para el Título"); // Opcional
    // startActivity(intent);
}