package com.example.memoria.service;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderManager {
    public static void scheduleDailyReminder(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);

        // 1. GỬI THÔNG BÁO DEMO (Chạy 1 lần duy nhất sau 5 giây)
        OneTimeWorkRequest demoRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
        workManager.enqueue(demoRequest);

        // 2. LẬP LỊCH CHẠY ĐỊNH KỲ (Lúc 8 giờ tối mỗi ngày)
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 20);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        if (target.before(Calendar.getInstance())) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelayForPeriodic = target.getTimeInMillis() - now;

        PeriodicWorkRequest dailyRequest = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayForPeriodic, TimeUnit.MILLISECONDS)
                .build();

        // Sử dụng KEEP để tránh việc mỗi lần mở app lại tính toán lại delay và reset lịch 20h
        workManager.enqueueUniquePeriodicWork(
                "MemoriaDailyReminder",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
        );
    }

}
