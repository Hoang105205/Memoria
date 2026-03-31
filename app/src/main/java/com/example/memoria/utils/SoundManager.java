package com.example.memoria.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.memoria.R;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SoundManager {

    public enum SoundEvent {
        CORRECT_ANSWER,
        WRONG_ANSWER,
        COMPLETE_QUIZ
    }

    private final SoundPool soundPool;
    private final Map<SoundEvent, Integer> soundMap = new HashMap<>();

    private int loadedSoundsCount = 0;
    private final int TOTAL_SOUNDS = 3; // Nho cap nhap

    @Inject
    public SoundManager(@ApplicationContext Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                loadedSoundsCount++;
            }
        });

        soundMap.put(SoundEvent.CORRECT_ANSWER, soundPool.load(context, R.raw.quiz_correct_sfx, 1));
        soundMap.put(SoundEvent.WRONG_ANSWER, soundPool.load(context, R.raw.quiz_wrong_sfx, 1));
        soundMap.put(SoundEvent.COMPLETE_QUIZ, soundPool.load(context, R.raw.quiz_complete_sfx, 1));
    }

    public void playSound(SoundEvent event) {
        if (soundPool != null && loadedSoundsCount > 0 && soundMap.containsKey(event)) {
            Integer soundId = soundMap.get(event);
            if (soundId != null && soundId != 0) {
                soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }
    }
}