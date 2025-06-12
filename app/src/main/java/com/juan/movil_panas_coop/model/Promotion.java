package com.juan.movil_panas_coop.model;

import com.google.gson.annotations.SerializedName;

public class Promotion {
    private String startDate;
    private String endDate;

    public Promotion(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

