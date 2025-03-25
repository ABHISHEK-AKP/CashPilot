package com.example.project.models;

public class Spending {
    private String amount;
    private String purpose;
    private String date;
    private String type;

    public Spending() {}

    public Spending(String amount, String purpose, String date, String type) {
        this.amount = amount;
        this.purpose = purpose;
        this.date = date;
        this.type = type;
    }


    public String getAmount() {
        return amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getDate() {
        return date;
    }
    public String getType() {
        return type;
    }
}
