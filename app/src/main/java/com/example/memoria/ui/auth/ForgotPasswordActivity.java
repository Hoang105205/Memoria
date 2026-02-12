package com.example.memoria.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memoria.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnReset;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.auth_forgot_et_email);
        btnReset = findViewById(R.id.auth_forgot_btn_reset);
        progressBar = findViewById(R.id.auth_forgot_progressBar);
        TextView tvBackToLogin = findViewById(R.id.auth_forgot_tv_back_to_login);

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (checkValidateEmail(email)) {
                resetPassword(email);
            }
        });

        tvBackToLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
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

    private void resetPassword(String email) {
        setLoading(true);
        // Firebase password reset logic
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(this, getString(R.string.success_forgot_password), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.err_forgot_password), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnReset.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnReset.setEnabled(true);
        }
    }
}
