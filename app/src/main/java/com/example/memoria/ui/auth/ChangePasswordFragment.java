package com.example.memoria.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.memoria.R;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChangePasswordFragment extends Fragment {

    private AuthViewModel authViewModel;
    private EditText etCurrent, etNew, etConfirm;
    private Button btnChange, btnBack;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etCurrent = view.findViewById(R.id.change_pass_et_current);
        etNew = view.findViewById(R.id.change_pass_et_new);
        etConfirm = view.findViewById(R.id.change_pass_et_confirm);
        btnChange = view.findViewById(R.id.change_pass_btn_action);
        btnBack = view.findViewById(R.id.change_pass_btn_back);
        progressBar = view.findViewById(R.id.change_pass_progressBar);

        setupObservers();

        btnChange.setOnClickListener(v -> {
            String currentPass = etCurrent.getText().toString().trim();
            String newPass = etNew.getText().toString().trim();
            String confirmPass = etConfirm.getText().toString().trim();

            if (validateInput(currentPass, newPass, confirmPass)) {
                authViewModel.changePassword(currentPass, newPass);
            }
        });

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    private void setupObservers() {
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnChange.setEnabled(!isLoading);
        });

        authViewModel.getSnackbarMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
            }
        });

        authViewModel.getSnackbarMessageRes().observe(getViewLifecycleOwner(), resId -> {
            if (resId != null) {
                Snackbar.make(requireView(), getString(resId), Snackbar.LENGTH_LONG).show();
            }
        });

        authViewModel.getNavigateBack().observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate) {
                authViewModel.resetNavigateBack();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });
    }

    private boolean validateInput(String currentPass, String newPass, String confirmPass) {
        if (currentPass.isEmpty()) {
            etCurrent.setError(getString(R.string.err_password_empty));
            return false;
        }
        if (newPass.length() < 6) {
            etNew.setError(getString(R.string.err_password_short));
            return false;
        }
        if (!newPass.equals(confirmPass)) {
            etConfirm.setError(getString(R.string.err_password_notMatch));
            return false;
        }
        return true;
    }
}
