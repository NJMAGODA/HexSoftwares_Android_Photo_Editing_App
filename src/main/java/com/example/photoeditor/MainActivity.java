package com.example.photoeditor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private ImageView imageView;
    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    private Slider brightnessSlider;
    private Slider contrastSlider;
    private Slider saturationSlider;
    private Button selectButton;
    private Button saveButton;
    private Button rotateButton;
    private Button flipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        brightnessSlider = findViewById(R.id.brightnessSlider);
        contrastSlider = findViewById(R.id.contrastSlider);
        saturationSlider = findViewById(R.id.saturationSlider);
        selectButton = findViewById(R.id.selectButton);
        saveButton = findViewById(R.id.saveButton);
        rotateButton = findViewById(R.id.rotateButton);
        flipButton = findViewById(R.id.flipButton);

        // Initialize sliders
        brightnessSlider.setValue(0f);
        contrastSlider.setValue(1f);
        saturationSlider.setValue(1f);
    }

    private void setupListeners() {
        selectButton.setOnClickListener(v -> checkPermissionAndPickImage());

        saveButton.setOnClickListener(v -> saveImage());

        rotateButton.setOnClickListener(v -> rotateImage());

        flipButton.setOnClickListener(v -> flipImage());

        brightnessSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (currentBitmap != null) {
                applyImageEffects();
            }
        });

        contrastSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (currentBitmap != null) {
                applyImageEffects();
            }
        });

        saturationSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (currentBitmap != null) {
                applyImageEffects();
            }
        });
    }

    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            pickImage();
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                currentBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                imageView.setImageBitmap(currentBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void applyImageEffects() {
        if (originalBitmap == null) return;

        float brightness = brightnessSlider.getValue();
        float contrast = contrastSlider.getValue();
        float saturation = saturationSlider.getValue();

        currentBitmap = ImageProcessor.adjustImage(originalBitmap, brightness, contrast, saturation);
        imageView.setImageBitmap(currentBitmap);
    }

    private void rotateImage() {
        if (currentBitmap == null) return;

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        currentBitmap = Bitmap.createBitmap(currentBitmap, 0, 0,
                currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(currentBitmap);
    }

    private void flipImage() {
        if (currentBitmap == null) return;

        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        currentBitmap = Bitmap.createBitmap(currentBitmap, 0, 0,
                currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(currentBitmap);
    }

    private void saveImage() {
        if (currentBitmap == null) return;

        try {
            String fileName = "edited_" + System.currentTimeMillis() + ".jpg";
            MediaStore.Images.Media.insertImage(getContentResolver(), currentBitmap, fileName, "Edited image");
            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }
}
