package com.example.memoria.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_signup);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.auth_signup_et_email);
        etPassword = findViewById(R.id.auth_signup_et_password);
        Button btnSignUp = findViewById(R.id.auth_signup_btn_signup);
        TextView tvTabLogin = findViewById(R.id.auth_signup_tv_tab_login);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if(email.isEmpty() || pass.isEmpty()) return;

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvTabLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }
}
