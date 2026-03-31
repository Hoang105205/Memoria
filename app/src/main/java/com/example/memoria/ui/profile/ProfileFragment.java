package com.example.memoria.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.example.memoria.ui.auth.LoginActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private UserProfileViewModel viewModel;
    private ShapeableImageView ivAvatar;
    private TextView tvUsername;
    private Button btnSignOut, btnChangePassword;
    private TextView tvEditProfile;
    private MaterialAutoCompleteTextView actvLanguage;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);

        ivAvatar = view.findViewById(R.id.profile_iv_avatar);
        tvUsername = view.findViewById(R.id.profile_tv_username);
        btnSignOut = view.findViewById(R.id.profile_btn_signout);
        btnChangePassword = view.findViewById(R.id.profile_btn_change_password);
        tvEditProfile = view.findViewById(R.id.profile_tv_edit); // Đã kết nối đúng ID
        actvLanguage = view.findViewById(R.id.profile_actv_language);

        setupObservers();
        setupLanguageSelection();

        // Gắn sự kiện Click cho TextView "Edit profile"
        tvEditProfile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment);
        });

        // Gắn sự kiện Click cho nút "Change Password"
        btnChangePassword.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_changePasswordFragment);
        });

        btnSignOut.setOnClickListener(v -> {
            viewModel.signOut();
        });
        
        view.findViewById(R.id.profile_cv_progress).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_progressFragment);
        });
    }

    private void setupObservers() {
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            tvUsername.setText(name);
        });

        viewModel.getUserAvatar().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        });

        viewModel.getNavigateBack().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        
        viewModel.getLearnedToday().observe(getViewLifecycleOwner(), count -> {
            if (getView() != null) {
                TextView tvLearned = getView().findViewById(R.id.profile_tv_words_count);
                if (tvLearned != null) tvLearned.setText(String.valueOf(count));
            }
        });

        viewModel.getStreakLiveData().observe(getViewLifecycleOwner(), streak -> {
            if (getView() != null) {
                TextView tvStreak = getView().findViewById(R.id.profile_tv_streak_count);
                if (tvStreak != null) tvStreak.setText(String.valueOf(streak));
            }
        });
    }

    private void setupLanguageSelection() {
        List<String> languageOptions = new ArrayList<>();
        languageOptions.add(getString(R.string.lang_en));
        languageOptions.add(getString(R.string.lang_vi));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, languageOptions);

        actvLanguage.setAdapter(adapter);

        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        String currentLangCode = appLocales.isEmpty() ? Locale.getDefault().getLanguage() : Objects.requireNonNull(appLocales.get(0)).getLanguage();

        if (currentLangCode.equals("vi")) {
            actvLanguage.setText(getString(R.string.lang_vi), false);
        } else {
            actvLanguage.setText(getString(R.string.lang_en), false);
        }

        actvLanguage.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLang = (String) parent.getItemAtPosition(position);
            if (selectedLang.equals(getString(R.string.lang_en))) {
                changeAppLanguage("en");
            } else if (selectedLang.equals(getString(R.string.lang_vi))) {
                changeAppLanguage("vi");
            }
        });
    }

    private void changeAppLanguage(String languageCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}
