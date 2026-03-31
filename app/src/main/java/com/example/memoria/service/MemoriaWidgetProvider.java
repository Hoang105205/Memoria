package com.example.memoria.service;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.example.memoria.data.database.AppDatabase;
import com.example.memoria.data.database.dao.CardDao;

import java.util.Calendar;
import java.util.List;

import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

public class MemoriaWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_WIDGET_CLICK = "com.example.memoria.ACTION_WIDGET_CLICK";

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface WidgetEntryPoint {
        CardDao cardDao();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_WIDGET_CLICK.equals(intent.getAction())) {
            // 1. Mở MainActivity ngay lập tức để người dùng cảm thấy nhạy
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);

            // 2. Ép Widget cập nhật lại dữ liệu mới nhất
            forceUpdateWidget(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.memoria_widget);

        // THAY ĐỔI: Gửi Broadcast tới chính Class này thay vì mở trực tiếp Activity
        Intent intent = new Intent(context, MemoriaWidgetProvider.class);
        intent.setAction(ACTION_WIDGET_CLICK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        // --- Giữ nguyên phần Thread lấy dữ liệu bên dưới ---
        new Thread(() -> {
            try {
                WidgetEntryPoint entryPoint = EntryPoints.get(context.getApplicationContext(), WidgetEntryPoint.class);
                CardDao cardDao = entryPoint.cardDao();

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                int learnedCount = cardDao.countCardsReviewedTodaySync(cal.getTimeInMillis());
                List<Long> studyDays = cardDao.getDistinctStudyDaysSync();
                int streak = calculateStreak(studyDays);

                views.setTextViewText(R.id.widget_tv_words, String.valueOf(learnedCount));
                views.setTextViewText(R.id.widget_tv_streak, String.valueOf(streak));

                appWidgetManager.updateAppWidget(appWidgetId, views);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Cập nhật phát đầu để nhận Click
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    private static int calculateStreak(List<Long> dates) {
        if (dates == null || dates.isEmpty()) return 0;
        int currentStreak = 0;
        long oneDay = 86400000L;
        long today = (System.currentTimeMillis() / oneDay) * oneDay;
        long yesterday = today - oneDay;

        if (dates.get(0) < yesterday) return 0;

        long expectedDate = dates.get(0);
        for (Long date : dates) {
            if (date.equals(expectedDate)) {
                currentStreak++;
                expectedDate -= oneDay;
            } else { break; }
        }
        return currentStreak;
    }

    public static void forceUpdateWidget(Context context) {
        Intent intent = new Intent(context, MemoriaWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new android.content.ComponentName(context, MemoriaWidgetProvider.class));

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
