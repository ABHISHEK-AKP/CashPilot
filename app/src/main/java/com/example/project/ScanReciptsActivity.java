package com.example.project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.project.models.Spending;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanReciptsActivity extends AppCompatActivity {
    private static final String TAG = "ScanReciptsActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView previewView;
    private Button btnCapture, btnNavigate;
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private FirebaseFirestore db;
    private String selectedDate = "";

    // Receipt analysis components
    private ReceiptCategorizer categorizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        try {
            // Initialize receipt analysis tools
            categorizer = new ReceiptCategorizer();

            previewView = findViewById(R.id.previewView);
            btnCapture = findViewById(R.id.btnCapture);
            btnNavigate = findViewById(R.id.btnNavigate);
            db = FirebaseFirestore.getInstance();

            // Check and request camera permission
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_CODE);
            } else {
                startCamera();
            }

            cameraExecutor = Executors.newSingleThreadExecutor();

            // Capture image and analyze it
            btnCapture.setOnClickListener(v -> {
                try {
                    btnCapture.setEnabled(false);
                    btnNavigate.setEnabled(false);
                    captureImage();
                } catch (Exception e) {
                    Log.e(TAG, "Error capturing image: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Navigate to another activity
            btnNavigate.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, AddExpense.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to activity: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error initializing activity: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize activity", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == CAMERA_PERMISSION_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permissions: " + e.getMessage());
        }
    }

    private void startCamera() {
        try {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    imageCapture = new ImageCapture.Builder().build();

                    androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder()
                            .build();

                    cameraProvider.bindToLifecycle(this, androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                } catch (Exception e) {
                    Log.e(TAG, "Failed to start camera: " + e.getMessage());
                    Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
                }
            }, ContextCompat.getMainExecutor(this));

        } catch (Exception e) {
            Log.e(TAG, "Error initializing camera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void captureImage() {
        try {
            File photoFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");

            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(ScanReciptsActivity.this, "Photo Captured!", Toast.LENGTH_SHORT).show();
                            analyzeImage(photoFile);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e(TAG, "Capture failed: " + exception.getMessage());
                            Toast.makeText(ScanReciptsActivity.this, "Capture failed", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Failed to capture image: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    }

    private void analyzeImage(File photoFile) {
        try {
            Toast.makeText(this, "Analyzing receipt...", Toast.LENGTH_SHORT).show();

            InputImage image = InputImage.fromFilePath(this, Uri.fromFile(photoFile));

            TextRecognizer recognizer = TextRecognition.getClient(
                    new com.google.mlkit.vision.text.latin.TextRecognizerOptions.Builder().build()
            );

            recognizer.process(image)
                    .addOnSuccessListener(this::processExtractedText)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to analyze image: " + e.getMessage());
                        Toast.makeText(this, "Failed to analyze image", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception in analyzeImage: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to analyze image", Toast.LENGTH_SHORT).show();
        }
    }

    private void processExtractedText(Text text) {
        try {
            String extractedText = text.getText();

            if (extractedText.isEmpty()) {
                Toast.makeText(this, "No text detected in image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log the extracted text for debugging
            Log.d(TAG, "Extracted text: " + extractedText);

            // Try to extract date from receipt
            String extractedDate = extractDate(extractedText);
            if (!extractedDate.isEmpty()) {
                selectedDate = extractedDate;
            } else {
                // If no date found, use current date
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                selectedDate = sdf.format(new Date());
            }

            // Categorize the receipt - this will be used as the "purpose" in your model
            String purpose = categorizer.categorizeReceipt(extractedText);

            // Extract ONLY the total amount
            double totalAmount = extractTotalAmount(extractedText);

            if (totalAmount <= 0) {
                Toast.makeText(this, "Could not find valid total amount on receipt", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show success toast
            Toast.makeText(this, "Found receipt total: $" + String.format("%.2f", totalAmount), Toast.LENGTH_SHORT).show();

            // Save to Firestore using your existing model structure
            saveToFirestore(purpose, totalAmount);

        } catch (Exception e) {
            Log.e(TAG, "Failed to process text: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error processing receipt", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Extract the total amount from the receipt text
     * Focuses on finding the final amount with tax included
     */
    private double extractTotalAmount(String receiptText) {
        double totalAmount = 0.0;

        try {
            // First try to find lines with "total" keyword
            Pattern totalPattern = Pattern.compile("(?i)\\b(total|amount due|grand total|amount|balance|due|pay)\\s*:?\\s*\\$?\\s*(\\d+\\.\\d{2})\\b");
            Matcher totalMatcher = totalPattern.matcher(receiptText);

            // Find all matches and get the last one (usually the final total)
            double lastTotal = 0.0;
            while (totalMatcher.find()) {
                try {
                    lastTotal = Double.parseDouble(totalMatcher.group(2));
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            if (lastTotal > 0.0) {
                return lastTotal;
            }

            // If no "total" keyword was found, look for the last dollar amount in the receipt
            // (often the total is the last amount mentioned)
            Pattern amountPattern = Pattern.compile("\\$?\\s*(\\d+\\.\\d{2})");
            Matcher amountMatcher = amountPattern.matcher(receiptText);

            double lastAmount = 0.0;
            while (amountMatcher.find()) {
                try {
                    lastAmount = Double.parseDouble(amountMatcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            if (lastAmount > 0.0) {
                return lastAmount;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error extracting total amount: " + e.getMessage());
        }

        return totalAmount;
    }

    /**
     * Extract date from receipt text
     */
    private String extractDate(String text) {
        try {
            // Look for common date formats: MM/DD/YYYY, MM-DD-YYYY, etc.
            Pattern datePattern = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b");
            Matcher dateMatcher = datePattern.matcher(text);

            if (dateMatcher.find()) {
                return dateMatcher.group(1);
            }

            // Try another format: Month name DD, YYYY
            Pattern textDatePattern = Pattern.compile("\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]* \\d{1,2},? \\d{2,4}\\b", Pattern.CASE_INSENSITIVE);
            Matcher textDateMatcher = textDatePattern.matcher(text);

            if (textDateMatcher.find()) {
                return textDateMatcher.group(0);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error extracting date: " + e.getMessage());
        }

        return "";
    }

    private void saveToFirestore(String purpose, double amount) {
        try {
            // Verify we have valid data
            if (purpose == null || purpose.isEmpty() || amount <= 0) {
                Log.w(TAG, "Invalid data - purpose: " + purpose + ", amount: " + amount);
                Toast.makeText(this, "Could not save: Invalid expense data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a Spending object using your existing model
            Spending spending = new Spending(
                    String.valueOf(amount),  // Amount as string
                    purpose,                 // Purpose (what was category before)
                    selectedDate,            // Date
                    "out"                    // Type (expenditure)
            );

            // Save to Firestore
            db.collection("expenses")
                    .add(spending)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Saved: " + purpose + " - $" + String.format("%.2f", amount),
                                Toast.LENGTH_SHORT).show();
                        Intent mainActivityintent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(mainActivityintent);
                        finish();

                        // Optionally, navigate to expense list or dashboard after saving
                        // Intent intent = new Intent(this, ExpenseListActivity.class);
                        // startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save: " + e.getMessage());
                        Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception while saving to Firestore: " + e.getMessage());
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            cameraExecutor.shutdown();
        } catch (Exception e) {
            Log.e(TAG, "Failed to shut down executor: " + e.getMessage());
        }
    }
}