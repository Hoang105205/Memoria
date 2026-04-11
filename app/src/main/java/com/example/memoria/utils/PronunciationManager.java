package com.example.memoria.utils;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;

public class PronunciationManager {
    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private boolean isTtsReady = false;

    // Khởi tạo và chuẩn bị sẵn TTS
    public PronunciationManager(Context context) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US); // Giọng Mỹ chuẩn
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true;
                }
            }
        });
    }

    // Hàm public duy nhất để UI gọi
    public void playSound(String word, String audioUrl, float speed) {
        // 1. Nếu có link mp3 từ API -> Ưu tiên phát bằng MediaPlayer
        if (audioUrl != null && !audioUrl.isEmpty()) {
            playFromUrl(word, audioUrl, speed);
        }
        // 2. Nếu API không trả về link mp3 -> Dùng ngay con robot TTS
        else {
            playFromTts(word, speed);
        }
    }

    private void playFromUrl(String word, String url, float speed) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                // Chỉnh tốc độ mp3 (Chỉ chạy trên Android 6.0+)
                PlaybackParams params = new PlaybackParams();
                params.setSpeed(speed);
                mp.setPlaybackParams(params);
                mp.start();
            });

            // Nếu link mp3 bị lỗi (VD: Die link, mất mạng), lập tức chữa cháy bằng TTS
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("Audio", "Lỗi phát mp3, chuyển sang TTS dự phòng");
                playFromTts(word, speed);
                return true;
            });

        } catch (IOException e) {
            playFromTts(word, speed);
        }
    }

    private void playFromTts(String word, float speed) {
        if (isTtsReady && word != null) {
            tts.setSpeechRate(speed);
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // ⚠️ HÀM QUAN TRỌNG: Phải gọi khi tắt màn hình để dọn rác bộ nhớ
    public void releaseResources() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
