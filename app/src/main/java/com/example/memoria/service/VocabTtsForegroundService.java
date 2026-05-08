package com.example.memoria.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.CardAudioItem;
import com.example.memoria.data.repository.VocabularyRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VocabTtsForegroundService extends Service {

    public static final String ACTION_START  = "tts.action.START";
    public static final String ACTION_TOGGLE = "tts.action.TOGGLE";
    public static final String ACTION_STOP   = "tts.action.STOP";

    private static final String TAG = "VocabTTS";
    private static final String CHANNEL_ID = "vocab_tts";
    private static final int NOTIF_ID = 2001;

    private TextToSpeech tts;
    private boolean ttsReady = false;

    private boolean isPlaying = false;
    private boolean isStopping = false;

    private final List<CardAudioItem> playlist = new ArrayList<>();
    private int cardIndex = 0;
    private int meaningIndex = -1; // -1 = frontText, >=0 = backMeaning index

    @Inject VocabularyRepository vocabularyRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        initTts();
        Log.d(TAG, "Service onCreate");
    }

    private void initTts() {
        tts = new TextToSpeech(getApplicationContext(), status -> {
            Log.d(TAG, "TTS init status=" + status);

            if (status != TextToSpeech.SUCCESS) {
                ttsReady = false;
                return;
            }

            int res = tts.setLanguage(new Locale("en", "US"));
            ttsReady = (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED);

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) { }

                @Override
                public void onDone(String utteranceId) {
                    if (!isPlaying || isStopping) return;

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isPlaying || isStopping) return;
                        playNextSegment();
                    }, 1500);
                }

                @Override public void onError(String utteranceId) {
                    if (!isPlaying || isStopping) return;
                    playNextSegment();
                }
            });

            Log.d(TAG, "TTS ready=" + ttsReady);

            // nếu user đã bấm play trước khi ttsReady:
            if (isPlaying && ttsReady) {
                playCurrentSegment();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        Log.d(TAG, "onStartCommand action=" + action);

        if (ACTION_START.equals(action)) {
            handleStart(intent);
        } else if (ACTION_TOGGLE.equals(action)) {
            toggle();
        } else if (ACTION_STOP.equals(action)) {
            stopEverything();
        } else {
            // nếu system restart service với intent null
            Log.d(TAG, "Unknown/Null action");
        }

        return START_STICKY;
    }

    private void handleStart(Intent intent) {
        // hiện notification ngay
        startForeground(NOTIF_ID, buildNotification());

        ArrayList<String> deckIds = intent.getStringArrayListExtra(VocabTtsServiceStarter.EXTRA_DECK_IDS);
        boolean selectAll = intent.getBooleanExtra(VocabTtsServiceStarter.EXTRA_SELECT_ALL, false);

        isStopping = false;
        isPlaying = true;
        updateNotification();

        vocabularyRepository.getAllCardsOfAllDecksFiltered(deckIds, selectAll, items -> {
            playlist.clear();
            if (items != null) playlist.addAll(items);

            cardIndex = 0;
            meaningIndex = -1;

            isPlaying = true;
            updateNotification();

            if (ttsReady) playCurrentSegment();
        });
    }

    private void toggle() {
        if (!isPlaying) {
            // resume
            isStopping = false;
            isPlaying = true;

            updateNotification();

            if (ttsReady) playCurrentSegment();
            return;
        }

        // pause
        isPlaying = false;
        if (tts != null) tts.stop();

        updateNotification();
    }

    private void stopEverything() {
        isStopping = true;
        isPlaying = false;
        updateNotification();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }

        stopForeground(true);
        stopSelf();
    }

    private void playCurrentSegment() {
        if (!isPlaying || !ttsReady || isStopping) return;

        if (playlist.isEmpty()) {
            speak("Bạn chưa có từ vựng để luyện nghe.", new Locale("vi", "VN"));
            return;
        }

        if (cardIndex < 0 || cardIndex >= playlist.size()) {
            // LOOP: quay lại đầu
            cardIndex = 0;
            meaningIndex = -1;
        }

        CardAudioItem item = playlist.get(cardIndex);

        String text;
        Locale locale;

        if (meaningIndex == -1) {
            text = safe(item.frontText);
            locale = new Locale("en", "US");
        } else {
            if (item.backMeanings == null || meaningIndex >= item.backMeanings.size()) {
                playNextCard();
                return;
            }
            text = safe(item.backMeanings.get(meaningIndex));
            locale = new Locale("vi", "VN");
        }

        if (text.isEmpty()) {
            playNextSegment();
            return;
        }

        updateNotification();
        speak(text, locale);
    }

    private void playNextSegment() {
        if (!isPlaying || isStopping) return;

        if (playlist.isEmpty()) return;

        CardAudioItem item = playlist.get(cardIndex);

        if (meaningIndex == -1) {
            if (item.backMeanings != null && !item.backMeanings.isEmpty()) {
                meaningIndex = 0;
                playCurrentSegment();
            } else {
                playNextCard();
            }
            return;
        }

        if (item.backMeanings != null && meaningIndex + 1 < item.backMeanings.size()) {
            meaningIndex++;
            playCurrentSegment();
        } else {
            playNextCard();
        }
    }

    private void playNextCard() {
        meaningIndex = -1;
        cardIndex++;

        // LOOP
        if (cardIndex >= playlist.size()) {
            cardIndex = 0;
        }

        playCurrentSegment();
    }

    @SuppressWarnings("deprecation")
    private void speak(String text, Locale locale) {
        if (tts == null) return;

        tts.setLanguage(locale);

        String utteranceId = "utt_" + System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private Notification buildNotification() {
        PendingIntent toggle = PendingIntent.getService(
                this,
                1,
                new Intent(this, VocabTtsForegroundService.class).setAction(ACTION_TOGGLE),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int playPauseIcon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
        String playPauseTitle = isPlaying ? "Pause" : "Play";

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Passive Listening")
                .setContentText(currentLabel())
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(playPauseIcon, playPauseTitle, toggle) // index 0
                .setStyle(new MediaStyle().setShowActionsInCompactView(0));

        return b.build();
    }

    private String currentLabel() {
        if (playlist.isEmpty()) return "Đang chuẩn bị danh sách...";
        if (cardIndex < 0 || cardIndex >= playlist.size()) return "Đang phát...";

        CardAudioItem item = playlist.get(cardIndex);
        if (meaningIndex == -1) return "Front: " + safe(item.frontText);

        if (item.backMeanings != null && meaningIndex < item.backMeanings.size()) {
            return "Meaning: " + safe(item.backMeanings.get(meaningIndex));
        }
        return "Meaning...";
    }

    private void updateNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, buildNotification());
    }

    private void createChannel() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Vocabulary Passive Listening",
                    NotificationManager.IMPORTANCE_LOW
            );
            nm.createNotificationChannel(ch);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        isPlaying = false;

        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}