package com.juan.movil_panas_coop;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    private String senderId;
    private String senderName; // Opcional, puedes obtenerlo de otra forma
    private String text;
    private @ServerTimestamp Date timestamp; // Firestore usará esto para el timestamp del servidor

    // Constructor vacío requerido para Firestore
    public Message() {}

    public Message(String senderId, String senderName, String text) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        // El timestamp será puesto por Firestore usando @ServerTimestamp
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
