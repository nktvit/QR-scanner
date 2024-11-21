package com.nktvitdev.qrcode;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.Manifest;

import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private TextView resultText;
    private MaterialButton browserButton;
    private String currentWebsite = ""; // Store the website URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        MaterialButton scanButton = findViewById(R.id.scanButton);
        browserButton = findViewById(R.id.browserButton);
        MaterialButton createQrButton = findViewById(R.id.createQrButton);
        createQrButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateQRActivity.class);
            startActivity(intent);
        });

        resultText = findViewById(R.id.resultText);

        // Initially hide the browser button
        browserButton.setVisibility(View.GONE);



        // Request camera permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }

        // Set up scan button
        scanButton.setOnClickListener(v -> startQRScan());

        // Set up browser button
        browserButton.setOnClickListener(v -> openInBrowser());

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void startQRScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Align QR code within the frame");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.initiateScan();
    }

    private void openInBrowser() {
        if (currentWebsite.isEmpty()) {
            Toast.makeText(this, "No website URL available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(currentWebsite));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open website: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            try {
                // Parse the JSON content
                JSONObject jsonResult = new JSONObject(result.getContents());
                String title = jsonResult.getString("title");
                String website = jsonResult.getString("website");

                // Store the website URL
                currentWebsite = website;

                // Display formatted result
                String displayText = String.format("Title: %s\nWebsite: %s", title, website);
                resultText.setText(displayText);

                // Show browser button only if we have a valid website URL
                browserButton.setVisibility(website != null && !website.isEmpty() ? View.VISIBLE : View.GONE);

                Toast.makeText(this, "Scan successful!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                resultText.setText("Invalid QR code format");
                browserButton.setVisibility(View.GONE);
                currentWebsite = "";
                Toast.makeText(this, "Error parsing QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            resultText.setText("Scan cancelled");
            browserButton.setVisibility(View.GONE);
            currentWebsite = "";
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}