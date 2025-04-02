package com.example.project.models;

public class ExpenseDetail {
    private String documentId;
    private String amount;
    private String purpose;
    private String date;
    private String type;

    public ExpenseDetail() {
        // Required empty constructor for Firestore
    }

    public ExpenseDetail(String documentId, String amount, String purpose, String date, String type) {
        this.documentId = documentId;
        this.amount = amount;
        this.purpose = purpose;
        this.date = date;
        this.type = type;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}