package com.example.memoria.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoria.R;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private EditText etEmail;
    private Button btnReset;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_forgot_password);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etEmail = findViewById(R.id.auth_forgot_et_email);
        btnReset = findViewById(R.id.auth_forgot_btn_reset);
        progressBar = findViewById(R.id.auth_forgot_progressBar);
        TextView tvBackToLogin = findViewById(R.id.auth_forgot_tv_back_to_login);

        setupObservers();

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (checkValidateEmail(email)) {
                viewModel.resetPassword(email);
            }
        });

        tvBackToLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, this::setLoading);

        viewModel.getSnackbarMessage().observe(this, message -> {
            if (message != null) showSnackbar(message);
        });

        viewModel.getShowResetSuccessDialog().observe(this, showDialog -> {
            if (showDialog) {
                viewModel.resetDialogState();
                showSuccessDialog();
            }
        });
    }

    private boolean checkValidateEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.err_email_empty));
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_email_invalid));
            return false;
        }

        return true;
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_reset_password))
                .setMessage(getString(R.string.msg_reset_password))
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnReset.setEnabled(!isLoading);
    }
}
