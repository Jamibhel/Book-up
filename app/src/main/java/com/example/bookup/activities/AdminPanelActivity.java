package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookup.databinding.ActivityAdminPanelBinding;

public class AdminPanelActivity extends AppCompatActivity {
    private ActivityAdminPanelBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnManageNews.setOnClickListener(v -> startActivity(new Intent(this, ManageNewsActivity.class)));
        binding.btnManageMaterials.setOnClickListener(v -> startActivity(new Intent(this, ManageMaterialsActivity.class)));
        binding.btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, ManageUsersActivity.class)));
        binding.btnAppSettings.setOnClickListener(v -> startActivity(new Intent(this, AppSettingsActivity.class)));
        
        binding.toolbarAdminPanel.setNavigationOnClickListener(v -> onBackPressed());
    }
}
