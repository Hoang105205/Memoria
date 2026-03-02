package com.example.memoria.ui.permission;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.memoria.R;

public class PermissionHelper {
    public interface PermissionCallback {
        void onAccept();  // Chạy khi bấm "Tiếp tục"
        void onDecline(); // Chạy khi bấm "Để sau"
    }
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    public static void showPermissionDialog(Activity activity, String title, String message, PermissionCallback callback){
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.next, (dialog, which) -> {
                    if (callback != null) callback.onAccept();
                })
                .setNegativeButton(R.string.later, (dialog, which) -> {
                    if (callback != null) callback.onDecline();
                })
                .setCancelable(false)
                .show();
    }

}
