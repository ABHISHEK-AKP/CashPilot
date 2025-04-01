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

public class ScanReciptsActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView previewView;
    private Button btnCapture, btnNavigate;
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private FirebaseFirestore db;
    private String selectedDate = "";

    // Create an instance of our receipt categorizer
    private ReceiptCategorizer categorizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        try {
            // Initialize our receipt categorizer
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
                    captureImage();
                } catch (Exception e) {
                    Log.e("CAPTURE_ERROR", "Error capturing image: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Navigate to another activity
            btnNavigate.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, AddExpense.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("NAVIGATE_ERROR", "Error navigating to activity: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e("ONCREATE_ERROR", "Error initializing activity: " + e.getMessage());
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
            Log.e("PERMISSION_ERROR", "Error handling permissions: " + e.getMessage());
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
                    Log.e("CAMERA_ERROR", "Failed to start camera: " + e.getMessage());
                    Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
                }
            }, ContextCompat.getMainExecutor(this));

        } catch (Exception e) {
            Log.e("CAMERA_INIT_ERROR", "Error initializing camera: " + e.getMessage());
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
                            Log.e("CAPTURE_ERROR", "Capture failed: " + exception.getMessage());
                            Toast.makeText(ScanReciptsActivity.this, "Capture failed", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Log.e("CAPTURE_EXCEPTION", "Failed to capture image: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    }

    private void analyzeImage(File photoFile) {
        try {
            InputImage image = InputImage.fromFilePath(this, Uri.fromFile(photoFile));

            TextRecognizer recognizer = TextRecognition.getClient(
                    new com.google.mlkit.vision.text.latin.TextRecognizerOptions.Builder().build()
            );

            recognizer.process(image)
                    .addOnSuccessListener(this::processExtractedText)
                    .addOnFailureListener(e -> {
                        Log.e("ML_KIT_ERROR", "Failed to analyze image: " + e.getMessage());
                        Toast.makeText(this, "Failed to analyze image", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e("ANALYZE_ERROR", "Exception in analyzeImage: " + e.getMessage());
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
            Log.d("RECEIPT_TEXT", "Extracted text: " + extractedText);

            // Use our local categorizer to determine the expense category
            String category = categorizer.categorizeReceipt(extractedText);

            // Extract the amount from the receipt
            double amount = categorizer.extractAmount(extractedText);

            // Try to extract date from receipt
            String extractedDate = categorizer.extractDate(extractedText);
            if (!extractedDate.isEmpty()) {
                selectedDate = extractedDate;
            } else {
                // If no date found, use current date
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                selectedDate = sdf.format(new Date());
            }

            // Show a toast with the detected category and amount
            Toast.makeText(this, "Category: " + category + ", Amount: $" + String.format("%.2f", amount),
                    Toast.LENGTH_LONG).show();

            // Save the expense to Firestore
            saveToFirestore(category, amount);

        } catch (Exception e) {
            Log.e("PROCESS_ERROR", "Failed to process text: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveToFirestore(String category, double amount) {
        try {
            // Verify we have valid data
            if (category == null || category.isEmpty() || amount <= 0) {
                Log.w("FIRESTORE_SAVE", "Invalid data - category: " + category + ", amount: " + amount);
                Toast.makeText(this, "Could not save: Invalid expense data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a Spending object
            Spending spending = new Spending(
                    String.valueOf(amount),  // Amount
                    category,                // Category
                    selectedDate,            // Date
                    "out"                    // Type (expenditure)
            );

            // Save to Firestore
            db.collection("expenses")
                    .add(spending)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Saved: " + category + " - $" + String.format("%.2f", amount),
                                Toast.LENGTH_SHORT).show();

                        // Optionally, navigate to expense list or dashboard after saving
                        // Intent intent = new Intent(this, ExpenseListActivity.class);
                        // startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIRESTORE_ERROR", "Failed to save: " + e.getMessage());
                        Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e("SAVE_ERROR", "Exception while saving to Firestore: " + e.getMessage());
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            cameraExecutor.shutdown();
        } catch (Exception e) {
            Log.e("DESTROY_ERROR", "Failed to shut down executor: " + e.getMessage());
        }
    }
}