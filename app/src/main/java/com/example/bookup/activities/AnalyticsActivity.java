package com.example.bookup.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookup.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {
    private static final String TAG = "AnalyticsActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Components
    private PieChart pieChartMaterialTypes;
    private LineChart lineChartUserActivity;
    private BarChart barChartSubjectDistribution;
    private CircularProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_analytics);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.analytics_title);
        }

        // Check authentication and admin status
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to view analytics.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkAdminStatus();

        initViews();
        setupCharts();
        loadAnalytics();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        pieChartMaterialTypes = findViewById(R.id.pie_chart_material_types);
        lineChartUserActivity = findViewById(R.id.line_chart_user_activity);
        barChartSubjectDistribution = findViewById(R.id.bar_chart_subject_distribution);
        progressIndicator = findViewById(R.id.progress_indicator);
    }

    private void setupCharts() {
        // Setup Pie Chart
        pieChartMaterialTypes.setUsePercentValues(true);
        pieChartMaterialTypes.getDescription().setEnabled(false);
        pieChartMaterialTypes.setDrawHoleEnabled(true);
        pieChartMaterialTypes.setHoleColor(android.R.color.transparent);
        pieChartMaterialTypes.setHoleRadius(58f);
        pieChartMaterialTypes.setTransparentCircleRadius(61f);
        pieChartMaterialTypes.setCenterText("Material Types");
        pieChartMaterialTypes.setCenterTextSize(16f);

        // Setup Line Chart
        lineChartUserActivity.getDescription().setEnabled(false);
        lineChartUserActivity.setTouchEnabled(true);
        lineChartUserActivity.setDragEnabled(true);
        lineChartUserActivity.setScaleEnabled(true);
        lineChartUserActivity.setPinchZoom(true);
        lineChartUserActivity.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // Setup Bar Chart
        barChartSubjectDistribution.getDescription().setEnabled(false);
        barChartSubjectDistribution.setFitBars(true);
        barChartSubjectDistribution.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartSubjectDistribution.getXAxis().setGranularity(1f);
        barChartSubjectDistribution.getAxisLeft().setGranularity(1f);
        barChartSubjectDistribution.getAxisRight().setEnabled(false);
    }

    private void checkAdminStatus() {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                    if (isAdmin == null || !isAdmin) {
                        Toast.makeText(this, "Access denied: Not an admin.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "User profile not found. Access denied.", Toast.LENGTH_LONG).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to check admin status: " + e.getMessage(), e);
                Toast.makeText(this, "Error checking admin status.", Toast.LENGTH_LONG).show();
                finish();
            });
    }

    private void loadAnalytics() {
        setLoading(true);

        // Load material type distribution
        loadMaterialTypeDistribution();

        // Load user activity over time
        loadUserActivityOverTime();

        // Load subject distribution
        loadSubjectDistribution();
    }

    private void loadMaterialTypeDistribution() {
        db.collection("studyMaterials")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Map<String, Integer> typeCount = new HashMap<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String type = document.getString("type");
                    if (type != null) {
                        typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
                    }
                }

                List<PieEntry> entries = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }

                PieDataSet dataSet = new PieDataSet(entries, "Material Types");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setSliceSpace(3f);

                PieData data = new PieData(dataSet);
                data.setValueTextSize(12f);
                pieChartMaterialTypes.setData(data);
                pieChartMaterialTypes.invalidate();

                setLoading(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading material type distribution", e);
                Toast.makeText(this, "Error loading analytics", Toast.LENGTH_SHORT).show();
                setLoading(false);
            });
    }

    private void loadUserActivityOverTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // Last 30 days
        
        db.collection("userActivity")
            .whereGreaterThan("timestamp", cal.getTime())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Map<String, Integer> dailyActivity = new HashMap<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String date = document.getDate("timestamp").toString();
                    dailyActivity.put(date, dailyActivity.getOrDefault(date, 0) + 1);
                }

                List<Entry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                int index = 0;
                for (Map.Entry<String, Integer> entry : dailyActivity.entrySet()) {
                    entries.add(new Entry(index, entry.getValue()));
                    labels.add(entry.getKey());
                    index++;
                }

                LineDataSet dataSet = new LineDataSet(entries, "Daily User Activity");
                dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
                dataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);
                dataSet.setLineWidth(2f);
                dataSet.setCircleRadius(4f);
                dataSet.setDrawCircleHole(false);

                LineData lineData = new LineData(dataSet);
                lineChartUserActivity.setData(lineData);
                lineChartUserActivity.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                lineChartUserActivity.invalidate();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading user activity", e);
                Toast.makeText(this, "Error loading user activity", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadSubjectDistribution() {
        db.collection("studyMaterials")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Map<String, Integer> subjectCount = new HashMap<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String subject = document.getString("subject");
                    if (subject != null) {
                        subjectCount.put(subject, subjectCount.getOrDefault(subject, 0) + 1);
                    }
                }

                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                int index = 0;
                for (Map.Entry<String, Integer> entry : subjectCount.entrySet()) {
                    entries.add(new BarEntry(index, entry.getValue()));
                    labels.add(entry.getKey());
                    index++;
                }

                BarDataSet dataSet = new BarDataSet(entries, "Materials per Subject");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.9f);

                barChartSubjectDistribution.setData(barData);
                barChartSubjectDistribution.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                barChartSubjectDistribution.getXAxis().setLabelRotationAngle(45);
                barChartSubjectDistribution.invalidate();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading subject distribution", e);
                Toast.makeText(this, "Error loading subject distribution", Toast.LENGTH_SHORT).show();
            });
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        pieChartMaterialTypes.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        lineChartUserActivity.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        barChartSubjectDistribution.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}
