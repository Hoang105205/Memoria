package com.example.memoria.ui.profile;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.google.android.material.imageview.ShapeableImageView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    private UserProfileViewModel viewModel;
    private ShapeableImageView ivAvatar;
    private EditText etUsername;
    private Button btnSave, btnBack;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // Tự lấy Glide vẽ ảnh nháp lên màn hình Edit
                    Glide.with(this).load(uri).circleCrop().into(ivAvatar);

                    // Lưu ngầm Uri vào ViewModel để chờ bấm Save
                    viewModel.updateTemporaryAvatar(uri);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);

        ivAvatar = view.findViewById(R.id.edit_profile_iv_avatar);
        etUsername = view.findViewById(R.id.edit_profile_et_username);
        btnSave = view.findViewById(R.id.edit_profile_btn_save);
        btnBack = view.findViewById(R.id.edit_profile_btn_back);

        setupObservers();

        // Nút sửa ảnh (FAB)
        view.findViewById(R.id.edit_profile_fab_change_avatar).setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Nút Lưu
        btnSave.setOnClickListener(v -> {
            String newName = etUsername.getText().toString().trim();
            if (newName.isEmpty()) {
                etUsername.setError("Name cannot be empty");
                return;
            }

            viewModel.saveProfile(newName);
        });

        // Nút Back
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Xóa sạch ảnh nháp nếu user thoát ra mà chưa bấm Save
        if (viewModel != null) {
            viewModel.clearTemporaryData();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupObservers() {
        // Đổ tên ra UI
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> etUsername.setText(name));

        // Vẽ ảnh ra UI
        viewModel.getUserAvatar().observe(getViewLifecycleOwner(), uri -> {
            Glide.with(this)
                    .load(uri != null ? uri : R.drawable.ic_default_avatar)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(ivAvatar);
        });

        // Hiện Toast thông báo từ ViewModel
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && message.equals("Success")) {
                Toast.makeText(requireContext(), getString(R.string.successfully_edit_your_profile), Toast.LENGTH_SHORT).show();
            }
        });

        // Bật tắt nút Save lúc đang Up ảnh (Đợi bạn rảnh thì thêm cái ProgressBar vô XML nha)
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnSave.setEnabled(!isLoading);
            if (isLoading) {
                btnSave.setText(getString(R.string.saving) + "...");
            } else {
                btnSave.setText(getString(R.string.action_save));
            }
        });

        // NHẬN LỆNH THOÁT MÀN HÌNH TỪ VIEWMODEL KHI UP XONG
        viewModel.getNavigateBack().observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate) {
                viewModel.resetNavigateBack();

                Navigation.findNavController(requireView()).popBackStack();
            }
        });
    }

    private void saveProfile(String newName) {
        viewModel.saveProfile(newName);
    }
}
