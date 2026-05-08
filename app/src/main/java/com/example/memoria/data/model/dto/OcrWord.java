package com.example.memoria.data.model.dto;

import android.graphics.RectF;

/**
 * A Data Transfer Object representing a single detected word from the OCR engine.
 * Fields are kept public and final for fast direct access during Canvas rendering in the custom view.
 */
public class OcrWord {

    /**
     * The text content of the detected word.
     */
    public final String text;

    /**
     * The bounding box representing the word's position and size on the image.
     */
    public final RectF rect;

    /**
     * The index of the line this word belongs to. Used for locking selection to a single line.
     */
    public final int lineIndex;

    /**
     * The center X coordinate of the word's bounding box. Pre-calculated for optimization.
     */
    public final float cx;

    /**
     * Constructs a new OcrWord.
     *
     * @param text      The detected text string.
     * @param rect      The bounding box of the word.
     * @param lineIndex The line number grouping.
     */
    public OcrWord(String text, RectF rect, int lineIndex) {
        this.text = text;
        this.rect = rect;
        this.lineIndex = lineIndex;
        this.cx = rect.centerX();
    }
}