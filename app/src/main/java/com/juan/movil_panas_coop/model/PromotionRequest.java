package com.juan.movil_panas_coop.model;

public class PromotionRequest {
    private String activityId; // Nuevo campo para el ID de la actividad
    private boolean isPromoted;
    private Promotion promotion;

    public PromotionRequest(String activityId, boolean isPromoted, String startDate, String endDate) {
        this.activityId = activityId;
        this.isPromoted = isPromoted;
        this.promotion = new Promotion(startDate, endDate);
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public boolean isPromoted() {
        return isPromoted;
    }

    public void setPromoted(boolean promoted) {
        isPromoted = promoted;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    // Inner class for Promotion
    public static class Promotion {
        private String startDate;
        private String endDate;

        public Promotion(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
}