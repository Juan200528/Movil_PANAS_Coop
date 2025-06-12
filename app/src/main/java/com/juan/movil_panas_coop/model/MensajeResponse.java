package com.juan.movil_panas_coop.model;

import java.util.ArrayList;
import java.util.List;

public class MensajeResponse {
    private List<Mensaje> messages = new ArrayList<>(); // Inicializada por defecto

    public List<Mensaje> getMessages() {
        return messages;
    }

    public void setMessages(List<Mensaje> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
    }
}