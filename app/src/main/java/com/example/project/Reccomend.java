package com.example.project;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.*;
import java.util.*;

public class Reccomend extends AppCompatActivity {

    EditText editSalary, editRent, editUtilities, editGroceries, editOther;
    Button btnSubmit;
    TextView textRecommendation;

    // Replace with your actual Together API Key
    private static final String TOGETHER_API_KEY = "Bearer 3e1098274c44435facf8613000c684fa9f77865c52fea11958e2f59391a4407b";
    private static final String TOGETHER_API_URL = "https://api.together.xyz/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reccomend);
        editRent = findViewById(R.id.edit_rent);
        editUtilities = findViewById(R.id.edit_utilities);
        editGroceries = findViewById(R.id.edit_groceries);
        editOther = findViewById(R.id.edit_other);
        btnSubmit = findViewById(R.id.btn_submit);
        textRecommendation = findViewById(R.id.text_recommendation);

        btnSubmit.setOnClickListener(v -> {
            String rent = editRent.getText().toString().trim();
            String utilities = editUtilities.getText().toString().trim();
            String groceries = editGroceries.getText().toString().trim();
            String other = editOther.getText().toString().trim();

            if (rent.isEmpty() || utilities.isEmpty() || groceries.isEmpty() || other.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchRecommendation(rent, utilities, groceries, other);
        });
    }

    private void fetchRecommendation(String rent, String utilities, String groceries, String other) {
        try {
            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "meta-llama/Llama-3.3-70B-Instruct-Turbo");

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");

            String prompt = String.format(
                    "My monthly rent is %s, utilities cost %s, groceries cost %s, and other expenses are %s. Based on this, give me a detailed budgeting recommendation.",
                    rent, utilities, groceries, other
            );

            userMessage.put("content", prompt);
            messages.put(userMessage);
            jsonBody.put("messages", messages);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    TOGETHER_API_URL,
                    jsonBody,
                    response -> {
                        try {
                            JSONArray choices = response.getJSONArray("choices");
                            JSONObject firstChoice = choices.getJSONObject(0);
                            JSONObject message = firstChoice.getJSONObject("message");
                            String reply = message.getString("content");
                            textRecommendation.setText(reply);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            textRecommendation.setText("Error parsing response.");
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        textRecommendation.setText("API call failed: " + error.getMessage());
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", TOGETHER_API_KEY);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create JSON body", Toast.LENGTH_SHORT).show();
        }
    }
}
