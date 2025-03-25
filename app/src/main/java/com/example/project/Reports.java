package com.example.project;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class Reports extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);

        // Set up system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Back button functionality
        ImageView backBtn = findViewById(R.id.btn_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        } else {
            Toast.makeText(this, "Back button not found in layout!", Toast.LENGTH_SHORT).show();
        }

        // Chart references
        BarChart barChart = findViewById(R.id.bar_chart);
        PieChart pieChartPurpose = findViewById(R.id.pie_chart_purpose); // Add this in layout
        PieChart pieChartInOut = findViewById(R.id.pie_chart_inout);     // Add this in layout

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("expenses").get().addOnSuccessListener(snapshot -> {
            Map<String, Float> purposeMap = new HashMap<>();
            float lastMonthTotal = 0f, thisMonthTotal = 0f;
            float inTotal = 0f, outTotal = 0f;
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1; // Jan = 0
            int lastMonth = currentMonth == 1 ? 12 : currentMonth - 1;

            for (QueryDocumentSnapshot doc : snapshot) {
                String purpose = doc.getString("purpose");
                String amountStr = doc.getString("amount");
                String dateStr = doc.getString("date");
                String type = doc.getString("type");

                if (purpose == null || amountStr == null || dateStr == null || type == null) continue;

                float amount = Float.parseFloat(amountStr);
                purposeMap.put(purpose, purposeMap.getOrDefault(purpose, 0f) + amount);

                // Date handling
                try {
                    Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
                    Calendar entryCal = Calendar.getInstance();
                    entryCal.setTime(date);
                    int entryMonth = entryCal.get(Calendar.MONTH) + 1;

                    if (entryMonth == currentMonth) thisMonthTotal += amount;
                    else if (entryMonth == lastMonth) lastMonthTotal += amount;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // In vs Out
                if (type.equalsIgnoreCase("In")) inTotal += amount;
                else outTotal += amount;
            }

            // BarChart - Last vs This Month
            if (barChart != null) {
                List<BarEntry> entries = new ArrayList<>();
                entries.add(new BarEntry(0, lastMonthTotal));
                entries.add(new BarEntry(1, thisMonthTotal));
                BarDataSet dataSet = new BarDataSet(entries, "Monthly Spend");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                BarData barData = new BarData(dataSet);
                barChart.setData(barData);
                barChart.getDescription().setEnabled(false);

                final String[] labels = new String[]{"Last", "This"};
                barChart.getXAxis().setGranularity(1f);
                barChart.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        int index = (int) value;
                        return index >= 0 && index < labels.length ? labels[index] : "";
                    }
                });

                barChart.animateY(1000);
                barChart.invalidate();
            }

            // PieChart - Purpose based spending
            if (pieChartPurpose != null) {
                List<PieEntry> purposeEntries = new ArrayList<>();
                for (Map.Entry<String, Float> entry : purposeMap.entrySet()) {
                    purposeEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
                PieDataSet pieDataSet = new PieDataSet(purposeEntries, "Spending by Purpose");
                pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                PieData pieData = new PieData(pieDataSet);
                pieChartPurpose.setData(pieData);
                pieChartPurpose.getDescription().setEnabled(false);
                pieChartPurpose.animateY(1000);
                pieChartPurpose.invalidate();
            }

            // PieChart - In vs Out Spending
            if (pieChartInOut != null) {
                List<PieEntry> inOutEntries = new ArrayList<>();
                inOutEntries.add(new PieEntry(inTotal, "In"));
                inOutEntries.add(new PieEntry(outTotal, "Out"));
                PieDataSet ioDataSet = new PieDataSet(inOutEntries, "In vs Out");
                ioDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                PieData ioData = new PieData(ioDataSet);
                pieChartInOut.setData(ioData);
                pieChartInOut.getDescription().setEnabled(false);
                pieChartInOut.animateY(1000);
                pieChartInOut.invalidate();
            }
        });
    }
}
