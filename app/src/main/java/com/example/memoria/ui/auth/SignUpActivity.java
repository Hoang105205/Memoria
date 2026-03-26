package com.example.memoria.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_signup);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etEmail = findViewById(R.id.auth_signup_et_email);
        etPassword = findViewById(R.id.auth_signup_et_password);
        etConfirmPassword = findViewById(R.id.auth_signup_et_confirm_password);
        btnSignUp = findViewById(R.id.auth_signup_btn_signup);
        TextView tvTabLogin = findViewById(R.id.auth_signup_tv_tab_login);
        progressBar = findViewById(R.id.auth_signup_progressBar);

        setupObservers();

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (checkValidInput(email, password, confirmPass)) {
                viewModel.signUp(email, password);
            }
        });

        tvTabLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, this::setLoading);

        viewModel.getSnackbarMessage().observe(this, message -> {
            if (message != null) showSnackbar(message);
        });

        viewModel.getNavigateToMain().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                viewModel.resetNavigation();
                goToMainActivity();
            }
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

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!isLoading);
    }
}
