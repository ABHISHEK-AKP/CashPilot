package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.adapters.SpendingAdapter;
import com.example.project.models.Spending;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView menuIcon, closeDrawerIcon;
    TextView logout, home, report, scan_rcpt, profile, user_name, user_email;
    RecyclerView recyclerView;
    TextView emptyMessage;

    SpendingAdapter adapter;
    List<Spending> spendingList = new ArrayList<>();
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.orange));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        closeDrawerIcon = findViewById(R.id.close_drawer);
        logout = findViewById(R.id.logout);
        home = findViewById(R.id.home_nav);
        report = findViewById(R.id.reports_nav);
        scan_rcpt = findViewById(R.id.scan_receipts_nav);
        profile = findViewById(R.id.profile_nav);
        user_name = findViewById(R.id.profile_name);
        user_email = findViewById(R.id.profile_email);

        // User info from intent
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String userEmail = intent.getStringExtra("userEmail");

        user_name.setText(userName != null ? userName : "No Name");
        user_email.setText(userEmail != null ? userEmail : "No Email");

        // Drawer open/close
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeDrawerIcon.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        // Logout
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LogIn.class));
            finish();
        });
        // Home (optional, if you want to reload or show a toast)
        home.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(MainActivity.this, "You're already on Home", Toast.LENGTH_SHORT).show();
        });

        // Reports
        report.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, Reports.class));
        });

        // Scan Receipts
        scan_rcpt.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, ScanReciptsActivity.class));
        });

        // Budgeting Recommendations
        profile.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, Reccomend.class));
        });

        // Layout insets (status bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent1 = new Intent(MainActivity.this, AddExpense.class);
            startActivity(intent1);
        });

        // Firestore: setup RecyclerView + empty message
        recyclerView = findViewById(R.id.spendingList);
        emptyMessage = findViewById(R.id.empty_message);

        adapter = new SpendingAdapter(spendingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchConsolidatedExpenses();
    }

    private void fetchConsolidatedExpenses() {
        CollectionReference expensesRef = db.collection("expenses");

        expensesRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null) {
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Temporary list to hold all expenses
            List<Spending> allExpenses = new ArrayList<>();

            // First, extract all expenses from the snapshot
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String amount = doc.getString("amount");
                String purpose = doc.getString("purpose");
                String date = doc.getString("date");
                String type = doc.getString("type");

                if (amount != null && purpose != null && date != null && type != null) {
                    allExpenses.add(new Spending(amount, purpose, date, type));
                }
            }

            // Now consolidate expenses by purpose
            Map<String, Spending> consolidatedMap = new HashMap<>();

            for (Spending expense : allExpenses) {
                String purpose = expense.getPurpose();

                if (consolidatedMap.containsKey(purpose)) {
                    // Purpose exists, add to the total
                    Spending existing = consolidatedMap.get(purpose);

                    try {
                        double currentAmount = Double.parseDouble(existing.getAmount());
                        double newAmount = Double.parseDouble(expense.getAmount());
                        double totalAmount = currentAmount + newAmount;

                        // Create an updated spending with the new total
                        Spending updated = new Spending(
                                String.format("%.2f", totalAmount),  // Formatted amount with 2 decimal places
                                purpose,                             // Same purpose
                                expense.getDate(),                   // Keep the date (could be most recent)
                                expense.getType()                    // Same type
                        );

                        consolidatedMap.put(purpose, updated);
                    } catch (NumberFormatException ex) {
                        // Handle case where amount is not a valid number
                        // Just keep the existing entry
                    }
                } else {
                    // New purpose, add to the map
                    consolidatedMap.put(purpose, expense);
                }
            }

            // Clear existing list and add consolidated expenses
            spendingList.clear();
            spendingList.addAll(consolidatedMap.values());

            // Update UI
            if (spendingList.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyMessage.setVisibility(View.VISIBLE);
                emptyMessage.setText("Go on, add some expenses and let us do the rest");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyMessage.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        });
    }
}