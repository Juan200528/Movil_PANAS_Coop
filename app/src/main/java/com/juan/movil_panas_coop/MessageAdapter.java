package com.juan.movil_panas_coop;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth; // Para saber si el mensaje es del usuario actual
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private Context context;
    // Opcional: para diferenciar mensajes enviados/recibidos en el layout
    // private static final int VIEW_TYPE_SENT = 1;
    // private static final int VIEW_TYPE_RECEIVED = 2;


    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Aquí podrías inflar diferentes layouts si quieres diferenciar mensajes enviados/recibidos
        // if (viewType == VIEW_TYPE_SENT) { ... } else { ... }
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.textViewSender.setText(message.getSenderName() != null ? message.getSenderName() : "Anónimo");
        holder.textViewMessage.setText(message.getText());

        if (message.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.textViewTimestamp.setText(sdf.format(message.getTimestamp()));
        } else {
            holder.textViewTimestamp.setText("");
        }

        // --- Lógica para alinear mensajes (opcional, necesitarías modificar item_message.xml o tener dos) ---
        // FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // if (currentUser != null && message.getSenderId() != null && message.getSenderId().equals(currentUser.getUid())) {
        //     // Mensaje enviado por el usuario actual (alinear a la derecha, cambiar color, etc.)
        //     ((LinearLayout.LayoutParams) holder.cardView.getLayoutParams()).gravity = Gravity.END;
        //     holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_message_sent_background));
        // } else {
        //     // Mensaje recibido (alinear a la izquierda, cambiar color, etc.)
        //     ((LinearLayout.LayoutParams) holder.cardView.getLayoutParams()).gravity = Gravity.START;
        //     holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_message_received_background));
        // }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // Opcional: para diferenciar vistas de mensajes enviados/recibidos
    // @Override
    // public int getItemViewType(int position) {
    //    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    //    Message message = messageList.get(position);
    //    if (currentUser != null && message.getSenderId() != null && message.getSenderId().equals(currentUser.getUid())) {
    //        return VIEW_TYPE_SENT;
    //    } else {
    //        return VIEW_TYPE_RECEIVED;
    //    }
    // }


    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSender;
        TextView textViewMessage;
        TextView textViewTimestamp;
        // androidx.cardview.widget.CardView cardView; // Si necesitas acceder a la CardView para estilo

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSender = itemView.findViewById(R.id.textViewSender);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            // cardView = itemView.findViewById(R.id.your_card_view_id_if_needed); // Asegúrate de tener un ID en el CardView de item_message.xml
        }
    }

    public void updateMessages(List<Message> newMessages) {
        this.messageList.clear();
        this.messageList.addAll(newMessages);
        notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
    }

    public void addMessage(Message message) {
        this.messageList.add(message);
        notifyItemInserted(messageList.size() - 1); // Notificar inserción para animación
    }
}