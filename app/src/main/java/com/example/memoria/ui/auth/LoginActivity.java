package com.example.memoria.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private Button btnLogin;
    private ProgressBar progressBar;

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
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            goToMainActivity();
//        }
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
                    setLoading(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, getString(R.string.success_login), Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.err_login), Toast.LENGTH_SHORT).show();
                    }
                });
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
