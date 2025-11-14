package com.example.bookup.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookup.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadMaterialActivity extends AppCompatActivity {
    private static final String TAG = "UploadMaterialActivity";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private TextInputEditText textInputTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextSubject;
    private AutoCompleteTextView autoCompleteMaterialType;
    private TextView textSelectedFileName;
    private ImageView imageFileIcon;
    private MaterialButton btnSelectFile;
    private ShapeableImageView imageThumbnailPreview;
    private TextView textSelectedThumbnailName;
    private MaterialButton btnSelectThumbnail;
    private MaterialButton btnUploadMaterialFinal;
    private ProgressBar progressBarUpload;

    private Uri selectedFileUri;
    private Uri selectedThumbnailUri;
    private String selectedFileName;
    private boolean fileUploaded = false;

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    updateSelectedFileName(uri);
                }
            });

    private final ActivityResultLauncher<String> thumbnailPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedThumbnailUri = uri;
                    displaySelectedThumbnail(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_material);

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.upload_material);
        }

        // Initialize views
        textInputTitle = findViewById(R.id.text_input_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextSubject = findViewById(R.id.edit_text_subject);
        autoCompleteMaterialType = findViewById(R.id.auto_complete_material_type);
        textSelectedFileName = findViewById(R.id.text_selected_file_name);
        imageFileIcon = findViewById(R.id.image_file_icon);
        btnSelectFile = findViewById(R.id.btn_select_file);
        imageThumbnailPreview = findViewById(R.id.image_thumbnail_preview);
        textSelectedThumbnailName = findViewById(R.id.text_selected_thumbnail_name);
        btnSelectThumbnail = findViewById(R.id.btn_select_thumbnail);
        btnUploadMaterialFinal = findViewById(R.id.btn_upload_material_final);
        progressBarUpload = findViewById(R.id.progress_bar_upload);

        // Set up material type dropdown
        String[] materialTypes = getResources().getStringArray(R.array.material_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, materialTypes);
        autoCompleteMaterialType.setAdapter(adapter);

        // Set up click listeners
        btnSelectFile.setOnClickListener(v -> selectFile());
        btnSelectThumbnail.setOnClickListener(v -> selectThumbnail());
        btnUploadMaterialFinal.setOnClickListener(v -> uploadMaterial());
    }

    private void selectFile() {
        filePickerLauncher.launch("application/pdf");
    }

    private void selectThumbnail() {
        thumbnailPickerLauncher.launch("image/*");
    }

    private void updateSelectedFileName(Uri uri) {
        try {
            String fileName = getFileName(uri);
            if (fileName != null) {
                selectedFileName = fileName;
                textSelectedFileName.setText(fileName);
                textSelectedFileName.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_file_selection), Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySelectedThumbnail(Uri uri) {
        imageThumbnailPreview.setImageURI(uri);
        imageThumbnailPreview.setVisibility(View.VISIBLE);
        textSelectedThumbnailName.setVisibility(View.VISIBLE);
        // Get and display the filename
        try {
            String fileName = getFileName(uri);
            if (fileName != null) {
                textSelectedThumbnailName.setText(fileName);
            }
        } catch (FileNotFoundException e) {
            textSelectedThumbnailName.setText(uri.getLastPathSegment());
        }
    }

    private String getFileName(Uri uri) throws FileNotFoundException {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf("/");
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadMaterial() {
        String title = textInputTitle.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        String materialType = autoCompleteMaterialType.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            textInputTitle.setError(getString(R.string.error_title_required));
            return;
        }

        if (TextUtils.isEmpty(subject)) {
            editTextSubject.setError(getString(R.string.error_subject_required));
            return;
        }

        if (TextUtils.isEmpty(materialType)) {
            autoCompleteMaterialType.setError(getString(R.string.error_material_type_required));
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, getString(R.string.error_file_required), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarUpload.setVisibility(View.VISIBLE);
        btnUploadMaterialFinal.setEnabled(false);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.error_auth_required), Toast.LENGTH_SHORT).show();
            return;
        }

        String fileId = UUID.randomUUID().toString();
        String thumbnailId = UUID.randomUUID().toString();
        
        StorageReference fileRef = storageRef.child("materials/" + fileId + ".pdf");
        UploadTask uploadTask = fileRef.putFile(selectedFileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(fileUrl -> {
                if (selectedThumbnailUri != null) {
                    StorageReference thumbnailRef = storageRef.child("thumbnails/" + thumbnailId + ".jpg");
                    thumbnailRef.putFile(selectedThumbnailUri)
                            .addOnSuccessListener(thumbnailSnapshot -> {
                                thumbnailRef.getDownloadUrl().addOnSuccessListener(thumbnailUrl -> {
                                    saveMaterialToFirestore(title, subject, materialType, description, fileUrl.toString(), thumbnailUrl.toString(), currentUser.getUid());
                                });
                            })
                            .addOnFailureListener(this::handleUploadError);
                } else {
                    saveMaterialToFirestore(title, subject, materialType, description, fileUrl.toString(), null, currentUser.getUid());
                }
            });
        }).addOnFailureListener(this::handleUploadError)
          .addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            progressBarUpload.setProgress((int) progress);
        });
    }

    private void saveMaterialToFirestore(String title, String subject, String materialType, 
            String description, String fileUrl, String thumbnailUrl, String userId) {
        Map<String, Object> material = new HashMap<>();
        material.put("title", title);
        material.put("subject", subject);
        material.put("type", materialType);
        material.put("description", description);
        material.put("fileUrl", fileUrl);
        if (thumbnailUrl != null) {
            material.put("thumbnailUrl", thumbnailUrl);
        }
        material.put("uploadedBy", userId);
        material.put("uploadedAt", FieldValue.serverTimestamp());

        db.collection("materials")
                .add(material)
                .addOnSuccessListener(documentReference -> {
                    progressBarUpload.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(this::handleUploadError);
    }

    private void handleUploadError(Exception e) {
        progressBarUpload.setVisibility(View.GONE);
        btnUploadMaterialFinal.setEnabled(true);
        Toast.makeText(this, getString(R.string.upload_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
