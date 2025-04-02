package com.example.project;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.project.models.Spending;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to consolidate expenses by purpose when fetching from Firestore
 */
public class ExpenseConsolidationManager {
    private static final String TAG = "ExpenseConsolidation";

    public interface OnExpensesLoadedListener {
        void onExpensesLoaded(List<Spending> consolidatedExpenses);
        void onError(Exception e);
    }

    /**
     * Fetch expenses from Firestore and consolidate them by purpose
     *
     * @param listener Callback to notify when expenses are loaded
     */
    public static void fetchConsolidatedExpenses(OnExpensesLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("expenses")
                .whereEqualTo("type", "out")  // Only get expenses (not income)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Process the results
                            List<Spending> rawExpenses = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Spending spending = document.toObject(Spending.class);
                                rawExpenses.add(spending);
                            }

                            // Consolidate expenses by purpose
                            List<Spending> consolidatedExpenses = consolidateByPurpose(rawExpenses);

                            // Return the consolidated list
                            listener.onExpensesLoaded(consolidatedExpenses);
                        } else {
                            Log.e(TAG, "Error getting expenses", task.getException());
                            listener.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Group expenses by purpose and sum their amounts
     */
    private static List<Spending> consolidateByPurpose(List<Spending> expenses) {
        // Use a map to group expenses by purpose
        Map<String, Spending> consolidatedMap = new HashMap<>();

        for (Spending expense : expenses) {
            String purpose = expense.getPurpose();

            if (consolidatedMap.containsKey(purpose)) {
                // Purpose exists, add to the total
                Spending existing = consolidatedMap.get(purpose);
                double currentAmount = Double.parseDouble(existing.getAmount());
                double newAmount = Double.parseDouble(expense.getAmount());
                double totalAmount = currentAmount + newAmount;

                // Create an updated spending with the new total
                Spending updated = new Spending(
                        String.valueOf(totalAmount),  // Summed amount
                        purpose,                      // Same purpose
                        expense.getDate(),            // Keep the most recent date
                        expense.getType()             // Same type
                );

                consolidatedMap.put(purpose, updated);
            } else {
                // New purpose, add to the map
                consolidatedMap.put(purpose, expense);
            }
        }

        // Convert map back to a list
        return new ArrayList<>(consolidatedMap.values());
    }
}