package com.example.memoria.data.repository;

import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository {
    @Inject
    public UserRepository() {

    }

    // Tạo callback để báo cho UI biết khi nào xong
    public interface UpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    public void updateUserProfile(String newName, Uri newImageUri, UpdateCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("Người dùng chưa đăng nhập!");
            return;
        }

        // KỊCH BẢN 1: USER CHỈ ĐỔI TÊN (KHÔNG ĐỔI ẢNH)
        if (newImageUri == null) {
            updateAuthProfile(user, newName, null, callback);
            return;
        }

        // KỊCH BẢN 2: USER CÓ CHỌN ẢNH -> Đẩy file ảnh lên Cloudinary
        String userUid = user.getUid();

        MediaManager.get().upload(newImageUri)
            .option("folder", "avatars")       // 1. Chỉ định thư mục
            .option("public_id", userUid)            // 2. Đặt tên file là UID của user
            .option("overwrite", true)         // 3. Cho phép ghi đè ảnh cũ
            .callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    // Quá trình up ảnh bắt đầu (UI đang xoay vòng loading rồi nên cứ kệ nó)
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    // Hàm này nháy liên tục báo % up ảnh, tạm thời bỏ qua cho nhẹ máy
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    String secureUrl = (String) resultData.get("secure_url");
                    Uri cloudUri = Uri.parse(secureUrl);

                    updateAuthProfile(user, newName, cloudUri, callback);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    callback.onError("Lỗi up ảnh: " + error.getDescription());
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    // Dùng khi mất mạng up lại, tạm thời bỏ qua
                }
            }).dispatch();
    }

    // Hàm phụ trợ: Cập nhật thông tin vào Firebase Auth
    private void updateAuthProfile(FirebaseUser user, String newName, Uri photoUrl, UpdateCallback callback) {
        UserProfileChangeRequest.Builder profileBuilder = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName);

        // Nếu có truyền link ảnh vào thì mới set, không thì thôi giữ nguyên ảnh cũ
        if (photoUrl != null) {
            profileBuilder.setPhotoUri(photoUrl);
        }

        user.updateProfile(profileBuilder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(); // Báo về ViewModel là XONG TẤT CẢ!
                    } else {
                        callback.onError("Lỗi cập nhật hồ sơ Firebase!");
                    }
                });
    }
}
