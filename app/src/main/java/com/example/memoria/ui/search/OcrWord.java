package com.example.memoria.ui.search;

import android.graphics.RectF;

public class OcrWord {
    public final String text;
    public final RectF rect;
    public final int lineIndex;
    public final float cx;

    public OcrWord(String text, RectF rect, int lineIndex) {
        this.text = text;
        this.rect = rect;
        this.lineIndex = lineIndex;
        this.cx = rect.centerX();
    }
}