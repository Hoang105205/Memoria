package com.example.memoria.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.example.memoria.ui.auth.LoginActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FirebaseAuth mAuth;
    private ShapeableImageView ivAvatar;
    private TextView tvUsername;
    private Button btnSignOut;
    private MaterialAutoCompleteTextView actvLanguage;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivAvatar = view.findViewById(R.id.profile_iv_avatar);
        tvUsername = view.findViewById(R.id.profile_tv_username);
        btnSignOut = view.findViewById(R.id.profile_btn_signout);
        actvLanguage = view.findViewById(R.id.profile_actv_language);

        FirebaseUser user = mAuth.getCurrentUser();
        initAccount(user);

        setupLanguageSelection();

        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupLanguageSelection() {
        List<String> languageOptions = new ArrayList<>();

        languageOptions.add(getString(R.string.lang_en));
        languageOptions.add(getString(R.string.lang_vi));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, languageOptions);

        actvLanguage.setAdapter(adapter);

        // Lấy ngôn ngữ app đang dùng hiện tại (nếu chưa set thì lấy theo hệ thống)
        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        String currentLangCode = appLocales.isEmpty() ? Locale.getDefault().getLanguage() : Objects.requireNonNull(appLocales.get(0)).getLanguage();

        // Set ngôn ngữ mặc định cho AutoCompleteTextView")
        // Set text mặc định hiển thị trên ô Dropdown
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

    public void initAccount(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                tvUsername.setText(name);
            } else {
                tvUsername.setText(user.getEmail());
            }

            Uri avatarUrl = user.getPhotoUrl();

            if (avatarUrl != null) {
                // Dùng Glide để load ảnh từ mạng
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }
    }

    // Hàm xử lý khi user bấm chọn ngôn ngữ
    private void changeAppLanguage(String languageCode) {
        // languageCode truyền vào sẽ là "en" (English) hoặc "vi" (Vietnamese)
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    // Hàm để reset về mặc private void resetToSystemLanguage() {
    ////        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
    ////    }định của hệ thống điện thoại (Nếu bạn cần dùng)
//
}
