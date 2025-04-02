package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.ExpenseDetailAdapter;
import com.example.project.models.ExpenseDetail;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDetailActivity extends AppCompatActivity implements ExpenseDetailAdapter.OnDeleteClickListener {
    private static final String TAG = "ExpenseDetailActivity";

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ExpenseDetailAdapter adapter;
    private List<ExpenseDetail> expenseList;
    private TextView categoryTitle, totalAmount, emptyMessage;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        recyclerView = findViewById(R.id.detail_recycler_view);
        categoryTitle = findViewById(R.id.detail_category_name);
        totalAmount = findViewById(R.id.detail_total_amount);
        emptyMessage = findViewById(R.id.detail_empty_message);
        ImageView backButton = findViewById(R.id.back_button);

        // Get the category name from intent
        categoryName = getIntent().getStringExtra("categoryName");
        if (categoryName == null) {
            categoryName = "Unknown Category";
        }
        categoryTitle.setText(categoryName);

        // Setup RecyclerView
        expenseList = new ArrayList<>();
        adapter = new ExpenseDetailAdapter(expenseList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Load expenses for this category
        fetchExpensesForCategory(categoryName);
    }

    private void fetchExpensesForCategory(String category) {
        db.collection("expenses")
                .whereEqualTo("purpose", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    expenseList.clear();
                    double total = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();
                        String amount = document.getString("amount");
                        String purpose = document.getString("purpose");
                        String date = document.getString("date");
                        String type = document.getString("type");

                        if (amount != null && purpose != null && date != null && type != null) {
                            expenseList.add(new ExpenseDetail(documentId, amount, purpose, date, type));

                            // Add to total
                            try {
                                total += Double.parseDouble(amount);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing amount: " + amount, e);
                            }
                        }
                    }

                    // Update UI
                    updateUI(total);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching expenses", e);
                    Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
                    updateUI(0);
                });
    }

    private void updateUI(double total) {
        totalAmount.setText("Total: CAD " + String.format("%.2f", total));

        if (expenseList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeleteClick(ExpenseDetail expense, int position) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> deleteExpense(expense, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExpense(ExpenseDetail expense, int position) {
        String documentId = expense.getDocumentId();

        db.collection("expenses").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from UI
                    adapter.removeItem(position);
                    Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();

                    // Recalculate total
                    calculateNewTotal();

                    // Check if list is now empty
                    if (expenseList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyMessage.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting expense", e);
                    Toast.makeText(this, "Failed to delete expense", Toast.LENGTH_SHORT).show();
                });
    }

    private void calculateNewTotal() {
        double total = 0;
        for (ExpenseDetail expense : expenseList) {
            try {
                total += Double.parseDouble(expense.getAmount());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing amount: " + expense.getAmount(), e);
            }
        }
        totalAmount.setText("Total: CAD " + String.format("%.2f", total));
    }
}