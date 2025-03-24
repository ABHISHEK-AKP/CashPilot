package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LogIn extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    Button btnLogIn, googleSignInBtn;
    ProgressBar progressBar;
    TextView signUpNow;
    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;

    @Override
    public void onStart() {
        super.onStart();
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error on Start: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            FirebaseApp.initializeApp(this);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_log_in);

            mAuth = FirebaseAuth.getInstance();
            editTextEmail = findViewById(R.id.email);
            editTextPassword = findViewById(R.id.password);
            btnLogIn = findViewById(R.id.btn_login);
            progressBar = findViewById(R.id.progressBar);
            signUpNow = findViewById(R.id.signUpNow);
            googleSignInBtn = findViewById(R.id.btn_google_login);

            GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.client_id))
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(LogIn.this, options);

            googleSignInBtn.setOnClickListener(view -> {
                try {
                    Intent googleSignInIntent = googleSignInClient.getSignInIntent();
                    activityResultLauncher.launch(googleSignInIntent);
                } catch (Exception e) {
                    Toast.makeText(LogIn.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });

            signUpNow.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(getApplicationContext(), SignUp.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "Navigation Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });

            btnLogIn.setOnClickListener(v -> {
                try {
                    String email = String.valueOf(editTextEmail.getText());
                    String password = String.valueOf(editTextPassword.getText());

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(LogIn.this, "Enter Email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(LogIn.this, "Enter Password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Sign In Successful!!", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(mainActivityIntent);
                                    finish();
                                } else {
                                    Toast.makeText(LogIn.this, "Authentication failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    Toast.makeText(LogIn.this, "Login Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

        } catch (Exception e) {
            Toast.makeText(this, "Initialization Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            Log.e("LogInActivity", "Initialization Error: " + e.getMessage());
        }
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    try {
                        if (result.getResultCode() == RESULT_OK) {
                            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                            mAuth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LogIn.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                                    Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(mainActivityIntent);
                                    finish();
                                } else {
                                    Toast.makeText(LogIn.this, "Failed Log in: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (ApiException e) {
                        Toast.makeText(LogIn.this, "Google Sign-In Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } catch (Exception e) {
                        Toast.makeText(LogIn.this, "Unexpected Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
    );
}
