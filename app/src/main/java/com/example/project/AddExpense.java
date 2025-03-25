package com.example.project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddExpense extends AppCompatActivity {

    EditText editAmount, editPurpose;
    TextView textDate;
    Button btnAdd;

    FirebaseFirestore db;
    String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        db = FirebaseFirestore.getInstance();

        editAmount = findViewById(R.id.editAmount);
        editPurpose = findViewById(R.id.editPurpose);
        textDate = findViewById(R.id.textDate);
        btnAdd = findViewById(R.id.btnAddExpense);

        // Open date picker
        textDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year1, month1, dayOfMonth) -> {
                        selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        textDate.setText(selectedDate);
                    }, year, month, day);
            dialog.show();
        });
        Spinner typeSpinner;
        final String[] selectedType = new String[1]; // We'll store one string
        selectedType[0] = "Out"; // default

        typeSpinner = findViewById(R.id.typeSpinner);

// Populate dropdown
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"In", "Out"}
        );
        typeSpinner.setAdapter(adapterSpinner);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType[0] = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Add to Firestore
        btnAdd.setOnClickListener(v -> {
            String amount = editAmount.getText().toString().trim();
            String purpose = editPurpose.getText().toString().trim();

            if (amount.isEmpty() || purpose.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> expense = new HashMap<>();
            expense.put("amount", amount);
            expense.put("purpose", purpose);
            expense.put("date", selectedDate);
            expense.put("type", selectedType[0]);

            db.collection("expenses")
                    .add(expense)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();  // 👈 This will give the real reason in Logcat
                    });
        });
    }
}
