package com.example.memoria.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import com.example.memoria.data.repository.SyncRepository;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpActivity extends AppCompatActivity {

    @Inject
    SyncRepository syncRepository;
    private EditText etEmail, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;
    private Button btnSignUp;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_signup);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.auth_signup_et_email);
        etPassword = findViewById(R.id.auth_signup_et_password);
        etConfirmPassword = findViewById(R.id.auth_signup_et_confirm_password);
        btnSignUp = findViewById(R.id.auth_signup_btn_signup);
        TextView tvTabLogin = findViewById(R.id.auth_signup_tv_tab_login);
        progressBar = findViewById(R.id.auth_signup_progressBar);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (checkValidInput(email, password, confirmPass)) {
                registerUser(email, password);
            }
        });

        tvTabLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }

    private boolean checkValidInput(String email, String password, String confirmPass) {
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.err_email_empty));
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.err_password_empty));
            etPassword.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_email_invalid));
            etEmail.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.err_password_short));
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPass)) {
            etConfirmPassword.setError(getString(R.string.err_password_notMatch));
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }
    private void registerUser(String email, String password) {
        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showSnackbar(getString(R.string.success_signup));

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Tạo account xong, chạy pull data (sẽ rỗng) cho chắc cú
                            syncRepository.pullAllDataFromCloud(user.getUid(), isSuccess -> {
                                runOnUiThread(() -> {
                                    setLoading(false);
                                    goToMainActivity();
                                });
                            });
                        }
                    } else {
                        setLoading(false);
                        showSnackbar(task.getException() != null ? task.getException().getMessage() : getString(R.string.err_signup));
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSignUp.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSignUp.setEnabled(true);
        }
    }
}
