package com.nktvitdev.qrcode;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import org.json.JSONObject;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateQRActivity extends AppCompatActivity {
    private ImageView qrImageView;
    private TextInputEditText titleInput;
    private TextInputEditText websiteInput;
    private MaterialButton generateButton;
    private MaterialButton saveButton;
    private Bitmap qrBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qr);

        // Initialize views
        qrImageView = findViewById(R.id.qrImageView);
        titleInput = findViewById(R.id.titleInput);
        websiteInput = findViewById(R.id.websiteInput);
        generateButton = findViewById(R.id.generateButton);
        saveButton = findViewById(R.id.saveButton);

        // Set up generate button
        generateButton.setOnClickListener(v -> generateQRCode());

        // Set up save button
        saveButton.setOnClickListener(v -> saveQRCode());
    }

    private void generateQRCode() {
        String title = titleInput.getText().toString().trim();
        String website = websiteInput.getText().toString().trim();

        if (title.isEmpty() || website.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create JSON object
            JSONObject jsonData = new JSONObject();
            jsonData.put("title", title);
            jsonData.put("website", website);
            String jsonString = jsonData.toString();

            // Generate QR code
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(jsonString, BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrBitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrImageView.setImageBitmap(qrBitmap);

            // Enable save button
            saveButton.setEnabled(true);

            Toast.makeText(this, "QR Code generated successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error generating QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Please generate a QR code first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create filename with timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "QR_" + timeStamp + ".png";

            // Create content values for the image
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QRCodes");

            // Get content resolver and insert image
            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(imageUri)) {
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    Toast.makeText(this, "QR Code saved successfully!", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}