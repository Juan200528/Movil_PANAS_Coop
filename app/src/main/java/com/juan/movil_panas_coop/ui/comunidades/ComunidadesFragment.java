package com.juan.movil_panas_coop.ui.comunidades;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout; // Para el container_chat_view
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.juan.movil_panas_coop.Message;      // Importa tu clase Message
import com.juan.movil_panas_coop.MessageAdapter; // Importa tu MessageAdapter para el chat
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.models.Comunidad;

import java.util.ArrayList;
import java.util.List;

public class ComunidadesFragment extends Fragment implements ComunidadesAdapter.OnComunidadClickListener {

    private static final String TAG = "ComunidadesFragment";

    // UI para la lista de comunidades
    private RecyclerView recyclerViewComunidades;
    private ComunidadesAdapter comunidadesAdapter;
    private List<Comunidad> comunidadList;
    private TextView textViewNoComunidades;
    private TextView textViewSelectComunidadPrompt;

    // UI para el Chat
    private RelativeLayout containerChatView;
    private TextView textViewChatTitle;
    private RecyclerView recyclerViewChatMessages;
    private EditText editTextChatMessage;
    private Button buttonSendChatMessage;
    private com.juan.movil_panas_coop.MessageAdapter chatMessagesAdapter; // Usar el MessageAdapter existente
    private List<Message> chatMessageList;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference currentChatMessagesRef;
    private ListenerRegistration chatListenerRegistration; // Para remover el listener anterior

    private Comunidad comunidadSeleccionadaActual = null;


    public ComunidadesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comunidades, container, false);

        // --- Inicializar UI de Lista de Comunidades ---
        recyclerViewComunidades = view.findViewById(R.id.recyclerViewComunidades);
        textViewNoComunidades = view.findViewById(R.id.textViewNoComunidades);
        textViewSelectComunidadPrompt = view.findViewById(R.id.textViewSelectComunidadPrompt);
        recyclerViewComunidades.setLayoutManager(new LinearLayoutManager(getContext()));
        comunidadList = new ArrayList<>();
        // Pasamos 'this' porque ComunidadesFragment ahora implementa OnComunidadClickListener
        comunidadesAdapter = new ComunidadesAdapter(getContext(), comunidadList, this);
        recyclerViewComunidades.setAdapter(comunidadesAdapter);

        // --- Inicializar UI del Chat ---
        containerChatView = view.findViewById(R.id.container_chat_view);
        textViewChatTitle = view.findViewById(R.id.textViewChatTitle);
        recyclerViewChatMessages = view.findViewById(R.id.recyclerViewChatMessages);
        editTextChatMessage = view.findViewById(R.id.editTextChatMessage);
        buttonSendChatMessage = view.findViewById(R.id.buttonSendChatMessage);

        chatMessageList = new ArrayList<>();
        // Asegúrate que tu MessageAdapter esté en el paquete correcto
        chatMessagesAdapter = new com.juan.movil_panas_coop.MessageAdapter(getContext(), chatMessageList);
        LinearLayoutManager chatLayoutManager = new LinearLayoutManager(getContext());
        chatLayoutManager.setStackFromEnd(true);
        recyclerViewChatMessages.setLayoutManager(chatLayoutManager);
        recyclerViewChatMessages.setAdapter(chatMessagesAdapter);

        buttonSendChatMessage.setOnClickListener(v -> sendMessage());

        // Inicialmente, la vista de chat está oculta
        containerChatView.setVisibility(View.GONE);
        textViewSelectComunidadPrompt.setVisibility(View.VISIBLE);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (currentUser == null) {
            Toast.makeText(getContext(), "Usuario no autenticado. Por favor, inicia sesión.", Toast.LENGTH_LONG).show();
            // Aquí podrías redirigir al login o deshabilitar la funcionalidad.
            return;
        }
        cargarComunidades();
    }

    private void cargarComunidades() {
        Log.d(TAG, "Cargando comunidades (simulado)...");
        List<Comunidad> datosDeEjemplo = new ArrayList<>();
        datosDeEjemplo.add(new Comunidad("chat_general_panascoop", "Chat General PanasCoop", "Discusiones generales."));
        datosDeEjemplo.add(new Comunidad("actividad_voluntariado_xyz", "Voluntariado Parque", "Coordinación del voluntariado."));

        if (datosDeEjemplo.isEmpty()) {
            textViewNoComunidades.setVisibility(View.VISIBLE);
            recyclerViewComunidades.setVisibility(View.GONE);
        } else {
            textViewNoComunidades.setVisibility(View.GONE);
            recyclerViewComunidades.setVisibility(View.VISIBLE);
            comunidadList.clear();
            comunidadList.addAll(datosDeEjemplo);
            comunidadesAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "Comunidades cargadas (simulado): " + comunidadList.size());

        // Aquí iría tu lógica para cargar desde la API con Retrofit
    }

    // --- Implementación de OnComunidadClickListener ---
    @Override
    public void onComunidadClick(Comunidad comunidad) {
        Log.d(TAG, "Comunidad seleccionada: " + comunidad.getNombre() + " ID: " + comunidad.getIdMongo());
        comunidadSeleccionadaActual = comunidad;

        if (comunidad.getIdMongo() == null || comunidad.getIdMongo().isEmpty()) {
            Toast.makeText(getContext(), "ID de comunidad no válido.", Toast.LENGTH_SHORT).show();
            containerChatView.setVisibility(View.GONE);
            textViewSelectComunidadPrompt.setVisibility(View.VISIBLE);
            return;
        }

        textViewChatTitle.setText("Chat: " + comunidad.getNombre());
        containerChatView.setVisibility(View.VISIBLE);
        textViewSelectComunidadPrompt.setVisibility(View.GONE);


        // Remover listener anterior si existe para evitar múltiples listeners al mismo chat o a chats antiguos
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
        }
        chatMessageList.clear(); // Limpiar mensajes del chat anterior
        chatMessagesAdapter.notifyDataSetChanged();


        currentChatMessagesRef = db.collection("chats").document(comunidad.getIdMongo()).collection("messages");
        listenForChatMessages();
    }

    // --- Lógica del Chat (Movida desde ChatActivity) ---
    private void sendMessage() {
        if (comunidadSeleccionadaActual == null || currentChatMessagesRef == null) {
            Toast.makeText(getContext(), "Selecciona una comunidad primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageText = editTextChatMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(getContext(), "El mensaje no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            String senderId = currentUser.getUid();
            String senderName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuario";

            Message message = new Message(senderId, senderName, messageText);

            currentChatMessagesRef.add(message)
                    .addOnSuccessListener(documentReference -> {
                        editTextChatMessage.setText("");
                        Log.d(TAG, "Mensaje enviado con ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al enviar mensaje", e);
                        Toast.makeText(getContext(), "Error al enviar mensaje.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void listenForChatMessages() {
        if (currentChatMessagesRef == null) return;

        chatListenerRegistration = currentChatMessagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        if (getContext() != null) Toast.makeText(getContext(), "Error al cargar mensajes.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean scrolled = false;
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Message message = dc.getDocument().toObject(Message.class);
                                chatMessageList.add(message);
                                chatMessagesAdapter.notifyItemInserted(chatMessageList.size() - 1);
                                if (!scrolled) { // Solo hacer scroll una vez por batch de nuevos mensajes
                                    recyclerViewChatMessages.scrollToPosition(chatMessageList.size() - 1);
                                    scrolled = true;
                                }
                                Log.d(TAG, "Nuevo mensaje: " + dc.getDocument().getData());
                                break;
                            case MODIFIED:
                                Log.d(TAG, "Mensaje modificado: " + dc.getDocument().getData());
                                // Implementar lógica de actualización si es necesario
                                break;
                            case REMOVED:
                                Log.d(TAG, "Mensaje eliminado: " + dc.getDocument().getData());
                                // Implementar lógica de eliminación si es necesario
                                break;
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Es muy importante remover el listener para evitar memory leaks y consumo innecesario
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
        }
    }
}