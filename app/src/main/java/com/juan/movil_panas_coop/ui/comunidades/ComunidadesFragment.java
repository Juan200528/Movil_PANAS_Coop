package com.juan.movil_panas_coop.ui.comunidades;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.juan.movil_panas_coop.ChatActivity;
import com.juan.movil_panas_coop.CrearComunidadActivity;
import com.juan.movil_panas_coop.InicioSesion;
import com.juan.movil_panas_coop.Message;
import com.juan.movil_panas_coop.MessageAdapter;
import com.juan.movil_panas_coop.R;
import com.juan.movil_panas_coop.model.Comunidad;
import com.juan.movil_panas_coop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ComunidadesFragment extends Fragment implements ComunidadesAdapter.OnComunidadClickListener {

    private static final String TAG = "ComunidadesFragment";

    private RecyclerView recyclerViewComunidades;
    private ComunidadesAdapter comunidadesAdapter;
    private List<Comunidad> comunidadList;
    private TextView textViewNoComunidades;
    private LinearLayout containerChatView; // Fixed type
    private EditText editTextChatMessage;
    private Button buttonSendChatMessage;
    private RecyclerView recyclerViewChatMessages;
    private MessageAdapter chatMessagesAdapter;
    private List<Message> chatMessageList;

    private FirebaseFirestore db;
    private CollectionReference currentChatMessagesRef;
    private ListenerRegistration chatListenerRegistration;

    private Comunidad comunidadSeleccionadaActual = null;

    public ComunidadesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comunidades, container, false);

        recyclerViewComunidades = view.findViewById(R.id.recyclerViewComunidades);
        textViewNoComunidades = view.findViewById(R.id.textViewNoComunidades);
        containerChatView = view.findViewById(R.id.container_chat_view); // Fixed cast
        editTextChatMessage = view.findViewById(R.id.editTextChatMessage);
        buttonSendChatMessage = view.findViewById(R.id.buttonSendChatMessage);
        recyclerViewChatMessages = view.findViewById(R.id.recyclerViewChatMessages);

        recyclerViewComunidades.setLayoutManager(new LinearLayoutManager(getContext()));
        comunidadList = new ArrayList<>();
        comunidadesAdapter = new ComunidadesAdapter(getContext(), comunidadList, this);
        recyclerViewComunidades.setAdapter(comunidadesAdapter);

        chatMessageList = new ArrayList<>();
        chatMessagesAdapter = new MessageAdapter(getContext(), chatMessageList);
        recyclerViewChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChatMessages.setAdapter(chatMessagesAdapter);

        buttonSendChatMessage.setOnClickListener(v -> sendMessage());

        containerChatView.setVisibility(View.GONE);
        Button buttonCrearNuevaComunidad = view.findViewById(R.id.buttonCrearNuevaComunidad);
        buttonCrearNuevaComunidad.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CrearComunidadActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();

        if (TextUtils.isEmpty(userId)) {
            // Mostrar mensaje de usuario no autenticado
            Toast.makeText(getContext(), "Usuario no autenticado. Redirigiendo al inicio de sesión.", Toast.LENGTH_LONG).show();

            // Redirigir al inicio de sesión
            startActivity(new Intent(getContext(), InicioSesion.class));
            requireActivity().finish();
            return;
        }

        // Cargar comunidades si el usuario está autenticado
        cargarComunidades();
    }

    private void cargarComunidades() {
        db.collection("comunidades")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    comunidadList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Comunidad comunidad = document.toObject(Comunidad.class);
                        comunidadList.add(comunidad);
                    }
                    comunidadesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar comunidades", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onComunidadClick(Comunidad comunidad) {
        Log.d(TAG, "Comunidad seleccionada: " + comunidad.getNombre());

        // Crear un Intent para navegar a ChatActivity
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("ACTIVITY_MONGO_ID", comunidad.getIdMongo()); // Pasar el ID de MongoDB
        intent.putExtra("ACTIVITY_NAME", comunidad.getNombre());     // Pasar el nombre de la comunidad
        startActivity(intent);
    }


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

        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();
        String username = sessionManager.getUsername();

        Message message = new Message(userId, username, messageText);

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

    private void listenForChatMessages() {
        if (currentChatMessagesRef == null) return;

        chatListenerRegistration = currentChatMessagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Toast.makeText(getContext(), "Error al cargar mensajes.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Message message = dc.getDocument().toObject(Message.class);
                                chatMessageList.add(message);
                                chatMessagesAdapter.notifyItemInserted(chatMessageList.size() - 1);
                                recyclerViewChatMessages.scrollToPosition(chatMessageList.size() - 1);
                                break;
                            case MODIFIED:
                                Log.d(TAG, "Mensaje modificado: " + dc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.d(TAG, "Mensaje eliminado: " + dc.getDocument().getData());
                                break;
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
        }
    }
}