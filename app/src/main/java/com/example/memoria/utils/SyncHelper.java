package com.example.memoria.utils;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.memoria.service.SyncWorker;
import java.util.concurrent.TimeUnit;

public class SyncHelper {
//    public static void triggerDataSync(Context context, String userId) {
//        // Điều kiện chạy là phải kết nối mạng
//        Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build();
//
//        Data inputData = new Data.Builder()
//                .putString("USER_ID", userId)
//                .build();
//
//        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
//                .setConstraints(constraints)
//                .setInputData(inputData)
//                .build();
//
//        // Dùng enqueueUniqueWork để chống gọi trùng lặp tiến trình
//        WorkManager.getInstance(context).enqueueUniqueWork(
//                "SYNC_FAV_DATA",
//                ExistingWorkPolicy.KEEP,
//                syncRequest
//        );
//    }

    // 1. Chạy ngay lập tức (Dùng cho lúc mở App và lúc CUD)
    public static void triggerImmediateSync(Context context, String userId) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data inputData = new Data.Builder().putString("USER_ID", userId).build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();

        // Chống spam: Nếu đang chạy rồi thì bỏ qua (KEEP)
        WorkManager.getInstance(context).enqueueUniqueWork(
                "IMMEDIATE_SYNC", ExistingWorkPolicy.KEEP, syncRequest
        );
    }

    // 2. Chạy định kỳ (15 phút/lần - Gọi 1 lần duy nhất lúc đăng nhập thành công)
    public static void startPeriodicSync(Context context, String userId) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data inputData = new Data.Builder().putString("USER_ID", userId).build();

        PeriodicWorkRequest periodicRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 15, TimeUnit.MINUTES) // Tối thiểu 15 phút
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();

        // KEEP: Đảm bảo chỉ có 1 tiến trình đếm ngược 15 phút tồn tại
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "PERIODIC_SYNC", ExistingPeriodicWorkPolicy.KEEP, periodicRequest
        );
    }
}
