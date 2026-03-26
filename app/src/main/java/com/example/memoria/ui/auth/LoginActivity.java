package com.example.memoria.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.memoria.data.repository.SyncRepository;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Inject
    SyncRepository syncRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.auth_login_et_email);
        etPassword = findViewById(R.id.auth_login_et_password);
        btnLogin = findViewById(R.id.auth_login_btn_login);
        TextView tvTabSignup = findViewById(R.id.auth_login_tv_tab_signup);
        TextView tvForgotPassword = findViewById(R.id.auth_login_tv_forgot_password);
        progressBar = findViewById(R.id.auth_login_progressBar);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (checkValidInput(email, pass)) {
                login(email, pass);
            }
        });

        tvTabSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            overridePendingTransition(0, 0);
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        }
    }

    private boolean checkValidInput(String email, String password) {
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

        return true;
    }

    private void login(String email, String password) {
        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showSnackbar(getString(R.string.success_login));

                        // Pull data về trước khi chuyển màn hình
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            syncRepository.pullAllDataFromCloud(user.getUid(), isSuccess -> {
                                // Luôn tắt loading ở callback dù thành công hay thất bại
                                runOnUiThread(() -> {
                                    setLoading(false);
                                    if (!isSuccess) {
                                        showSnackbar(getString(R.string.err_get_data_from_firestore));
                                    }
                                    goToMainActivity(); // Kéo xong mới cho vào App
                                });
                            });
                        }
                    } else {
                        setLoading(false);
                        showSnackbar(getString(R.string.err_login));
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Đóng LoginActivity để không quay lại được bằng nút Back
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }
}
