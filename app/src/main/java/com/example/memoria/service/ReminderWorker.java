package com.example.memoria.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.hilt.work.HiltWorker;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.example.memoria.data.repository.CardRepository;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class ReminderWorker extends Worker {
    private final CardRepository repository;

    @AssistedInject
    public ReminderWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            CardRepository repository // Hilt sẽ tự động inject vào đây
    ) {
        super(context, workerParams);
        this.repository = repository;
    }

    @NonNull
    @Override
    public Result doWork() {
        // 1. Kiểm tra số thẻ đến hạn
        int dueCount = repository.getDueCardsCountSync(System.currentTimeMillis());

        // 2. Nếu có thẻ thì bắn notification
        if (dueCount > 0) {
            sendNotification(dueCount);
        }

        return Result.success();
    }

    private void sendNotification(int dueCount) {
        String channelId = "daily_reminder_channel";
        Context context = getApplicationContext();

        // 1. Tạo Intent trỏ thẳng đến MainActivity (giống hệt Widget)
        Intent intent = new Intent(context, MainActivity.class);

        // Nếu bạn muốn báo cho MainActivity biết là mở từ Notification để làm gì đó (tùy chọn)
        intent.putExtra("FROM_NOTIFICATION", true);

        // Flags quan trọng: Nếu app đang mở thì không tạo thêm instance mới,
        // mà chỉ đưa instance cũ lên trên (Clear Top / Single Top)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // 2. Tạo PendingIntent (Sử dụng getActivity thay vì NavDeepLinkBuilder)
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1, // Request code khác với Widget (ví dụ: 1) để tránh bị đè
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    context.getString(R.string.study_reminder), NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.time_for_reviewing) +  " \uD83D\uDCD6")
                .setContentText(dueCount + " " + context.getString(R.string.noti_note))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)// <--- Gắn PendingIntent vào đây
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }
}
