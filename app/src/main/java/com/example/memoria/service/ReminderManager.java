package com.example.memoria.service;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderManager {
    public static void scheduleDailyReminder(Context context) {
        // Tính toán khoảng thời gian từ bây giờ đến 20h
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 20); // 8 Giờ tối
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        // Nếu hiện tại đã quá 20h, thì hẹn cho 20h ngày mai
        if (target.before(Calendar.getInstance())) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        //long initialDelay = target.getTimeInMillis() - now;
        long initialDelay = 5000;
        // Tạo request chạy định kỳ mỗi 24 tiếng
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        // Đưa vào WorkManager (Sử dụng KEEP để không reset lại delay mỗi khi app mở)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "MemoriaDailyReminder",
                //ExistingPeriodicWorkPolicy.KEEP,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }

}
