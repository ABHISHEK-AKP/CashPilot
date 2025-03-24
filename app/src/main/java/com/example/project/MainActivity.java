package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView menuIcon, closeDrawerIcon;
    TextView logout,home,report,scan_rcpt,profile,user_name,user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
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
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String userEmail = intent.getStringExtra("userEmail");
        String userPhotoUrl = intent.getStringExtra("userPhotoUrl");
        user_name.setText(userName != null ? userName : "No Name");
        user_email.setText(userEmail!=null?userEmail:"No Email");

        // Open drawer on hamburger icon click
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Close drawer on X icon click
        closeDrawerIcon.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        // Handle logout
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LogIn.class));
            finish();
        });


        // Handle padding for notch/status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
