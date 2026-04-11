package com.example.memoria.data.model.dto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * OCR selection overlay:
 * - Create initial selection via rubber band (drag) or tap.
 * - Lock selection to a single line (lockedLineIndex).
 * - Show 2 handles (start/end) that can be dragged.
 * - IMPORTANT: Dragging one handle does NOT move the other; if it crosses, it clamps instead of swapping.
 */
public class OcrSelectOverlayView extends View {

    public interface Callback {
        void onSelectionChanged(String joinedText);
    }

    private final Paint wordBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rubberBandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<OcrWord> words = new ArrayList<>();

    // Rubber band selection
    private RectF rubberBand;
    private float startX, startY;

    // Selection range in flattened words list
    private int selStart = -1;
    private int selEnd = -1;

    // Lock to a single line
    private int lockedLineIndex = -1; // -1 = not locked

    // Handle drag state
    private static final int DRAG_NONE = 0;
    private static final int DRAG_START = 1;
    private static final int DRAG_END = 2;
    private int dragMode = DRAG_NONE;

    // Handle UI
    private final RectF startHandle = new RectF();
    private final RectF endHandle = new RectF();
    private float handleRadiusPx = 18f;

    private Callback callback;

    public OcrSelectOverlayView(Context context) {
        super(context);
        init();
    }

    public OcrSelectOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        handleRadiusPx = 12f * d;

        wordBoxPaint.setStyle(Paint.Style.STROKE);
        wordBoxPaint.setStrokeWidth(2f);
        wordBoxPaint.setColor(0x3300FF00);

        selectedPaint.setStyle(Paint.Style.FILL);
        selectedPaint.setColor(0x55FFCC00);

        rubberBandPaint.setStyle(Paint.Style.STROKE);
        rubberBandPaint.setStrokeWidth(3f);
        rubberBandPaint.setColor(0xAA2196F3);

        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setColor(0xFF1A73E8);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setWords(List<OcrWord> newWords) {
        words.clear();
        if (newWords != null) words.addAll(newWords);

        selStart = -1;
        selEnd = -1;
        lockedLineIndex = -1;

        dragMode = DRAG_NONE;
        rubberBand = null;

        invalidate();
        emitSelection();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw all words bbox (optional but useful)
        for (OcrWord w : words) {
            canvas.drawRect(w.rect, wordBoxPaint);
        }

        // Draw selected range and handles
        if (hasSelection()) {
            for (int i = selStart; i <= selEnd; i++) {
                canvas.drawRect(words.get(i).rect, selectedPaint);
            }

            computeHandles();
            canvas.drawOval(startHandle, handlePaint);
            canvas.drawOval(endHandle, handlePaint);
        }

        // Draw rubber band while creating selection
        if (rubberBand != null) {
            canvas.drawRect(rubberBand, rubberBandPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (words.isEmpty()) return true;

        float x = e.getX();
        float y = e.getY();

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // If touch near handles -> drag them
                if (hasSelection()) {
                    computeHandles();
                    if (startHandle.contains(x, y)) {
                        dragMode = DRAG_START;
                        return true;
                    } else if (endHandle.contains(x, y)) {
                        dragMode = DRAG_END;
                        return true;
                    }
                }

                // Else start rubber band selection
                dragMode = DRAG_NONE;
                startX = x;
                startY = y;
                rubberBand = new RectF(startX, startY, startX, startY);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                // Drag handle (do NOT swap the other handle; clamp instead)
                if (dragMode == DRAG_START || dragMode == DRAG_END) {
                    int idx = findNearestWordIndex(x, y);
                    if (idx != -1) {
                        if (dragMode == DRAG_START) {
                            selStart = idx;
                            clampStartToEnd(); // start can't pass end
                        } else {
                            selEnd = idx;
                            clampEndToStart(); // end can't pass start
                        }

                        // Ensure selection stays in locked line without swapping
                        clampSelectionToLockedLineFixed();

                        invalidate();
                        emitSelection();
                    }
                    return true;
                }

                // Rubber band update
                if (rubberBand != null) {
                    rubberBand.set(
                            Math.min(startX, x),
                            Math.min(startY, y),
                            Math.max(startX, x),
                            Math.max(startY, y)
                    );
                    updateSelectionByRubberBand(rubberBand);
                    invalidate();
                    emitSelection();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragMode == DRAG_START || dragMode == DRAG_END) {
                    dragMode = DRAG_NONE;
                    invalidate();
                    return true;
                }

                if (rubberBand != null) {
                    // Treat as tap if tiny band
                    if (rubberBand.width() < 12 && rubberBand.height() < 12) {
                        int hit = findHitWordIndexAnyLine(x, y);
                        if (hit != -1) {
                            selStart = hit;
                            selEnd = hit;
                        }
                    }

                    rubberBand = null;

                    // Normalize initial selection (allowed to swap here)
                    normalizeSelection();

                    // Lock line after initial selection created
                    lockLineFromSelection();

                    // Keep selection in that line
                    clampSelectionToLockedLineFixed();

                    invalidate();
                    emitSelection();
                }
                return true;
        }

        return super.onTouchEvent(e);
    }

    private boolean hasSelection() {
        return selStart >= 0
                && selEnd >= 0
                && selStart < words.size()
                && selEnd < words.size()
                && selStart <= selEnd;
    }

    /**
     * Normalize selection for INITIAL selection creation only (rubber band / tap).
     * This may swap start/end if needed.
     */
    private void normalizeSelection() {
        if (selStart == -1 || selEnd == -1) return;

        if (selStart > selEnd) {
            int t = selStart;
            selStart = selEnd;
            selEnd = t;
        }

        if (selStart < 0) selStart = 0;
        if (selEnd >= words.size()) selEnd = words.size() - 1;
    }

    private void updateSelectionByRubberBand(RectF band) {
        int first = -1;
        int last = -1;
        for (int i = 0; i < words.size(); i++) {
            if (RectF.intersects(band, words.get(i).rect)) {
                if (first == -1) first = i;
                last = i;
            }
        }
        selStart = first;
        selEnd = last;

        // We don't lock during move; lock on ACTION_UP so user can drag first.
    }

    private void lockLineFromSelection() {
        if (!hasSelection()) {
            lockedLineIndex = -1;
            return;
        }

        lockedLineIndex = words.get(selStart).lineIndex;

        // If selection spans multiple lines, clamp selEnd backward into same line
        int newEnd = selEnd;
        while (newEnd >= selStart && words.get(newEnd).lineIndex != lockedLineIndex) {
            newEnd--;
        }
        selEnd = Math.max(selStart, newEnd);

        // Also clamp selStart forward (defensive)
        int newStart = selStart;
        while (newStart <= selEnd && words.get(newStart).lineIndex != lockedLineIndex) {
            newStart++;
        }
        selStart = Math.min(newStart, selEnd);
    }

    /**
     * Clamp start index into bounds, and ensure it never passes end (NO swapping).
     */
    private void clampStartToEnd() {
        if (selStart < 0) selStart = 0;
        if (selStart >= words.size()) selStart = words.size() - 1;

        if (selEnd != -1 && selStart > selEnd) selStart = selEnd;
    }

    /**
     * Clamp end index into bounds, and ensure it never goes before start (NO swapping).
     */
    private void clampEndToStart() {
        if (selEnd < 0) selEnd = 0;
        if (selEnd >= words.size()) selEnd = words.size() - 1;

        if (selStart != -1 && selEnd < selStart) selEnd = selStart;
    }

    /**
     * Clamp selection to lockedLineIndex but DO NOT swap start/end.
     * If dragged outside line, it clamps to nearest valid index within the line.
     */
    private void clampSelectionToLockedLineFixed() {
        if (lockedLineIndex == -1) return;
        if (!hasSelection()) return;

        // Clamp start forward until it is in locked line
        while (selStart <= selEnd && words.get(selStart).lineIndex != lockedLineIndex) {
            selStart++;
        }
        if (selStart > selEnd) {
            // Keep other handle fixed; start cannot move beyond end
            selStart = selEnd;
        }

        // Clamp end backward until it is in locked line
        while (selEnd >= selStart && words.get(selEnd).lineIndex != lockedLineIndex) {
            selEnd--;
        }
        if (selEnd < selStart) {
            // Keep other handle fixed; end cannot move before start
            selEnd = selStart;
        }

        // Final safety bounds
        if (selStart < 0) selStart = 0;
        if (selEnd >= words.size()) selEnd = words.size() - 1;

        // Still keep invariants without swapping
        if (selStart > selEnd) selStart = selEnd;
        if (selEnd < selStart) selEnd = selStart;
    }

    private int findHitWordIndexAnyLine(float x, float y) {
        // Choose smallest-area box that contains point (any line)
        int hit = -1;
        float bestArea = Float.MAX_VALUE;

        for (int i = 0; i < words.size(); i++) {
            RectF r = words.get(i).rect;
            if (r.contains(x, y)) {
                float area = r.width() * r.height();
                if (area < bestArea) {
                    bestArea = area;
                    hit = i;
                }
            }
        }
        return hit;
    }

    private int findHitWordIndexLockedLine(float x, float y) {
        // Choose smallest-area box that contains point, but only in locked line if locked
        int hit = -1;
        float bestArea = Float.MAX_VALUE;

        for (int i = 0; i < words.size(); i++) {
            if (lockedLineIndex != -1 && words.get(i).lineIndex != lockedLineIndex) continue;

            RectF r = words.get(i).rect;
            if (r.contains(x, y)) {
                float area = r.width() * r.height();
                if (area < bestArea) {
                    bestArea = area;
                    hit = i;
                }
            }
        }
        return hit;
    }

    private int findNearestWordIndex(float x, float y) {
        // Prefer hit first, within locked line
        int hit = findHitWordIndexLockedLine(x, y);
        if (hit != -1) return hit;

        float threshold = handleRadiusPx * 4f;
        float best = Float.MAX_VALUE;
        int bestIdx = -1;

        for (int i = 0; i < words.size(); i++) {
            if (lockedLineIndex != -1 && words.get(i).lineIndex != lockedLineIndex) continue;

            RectF r = words.get(i).rect;
            float cx = r.centerX();
            float cy = r.centerY();
            float dx = cx - x;
            float dy = cy - y;
            float dist = (dx * dx) + (dy * dy);
            if (dist < best) {
                best = dist;
                bestIdx = i;
            }
        }

        if (bestIdx != -1 && best <= threshold * threshold) return bestIdx;
        return -1;
    }

    private void computeHandles() {
        if (!hasSelection()) return;

        RectF startRect = words.get(selStart).rect;
        RectF endRect = words.get(selEnd).rect;

        // Handles at left-top of start and right-bottom of end
        float sx = startRect.left;
        float sy = startRect.top;
        float ex = endRect.right;
        float ey = endRect.bottom;

        startHandle.set(
                sx - handleRadiusPx, sy - handleRadiusPx,
                sx + handleRadiusPx, sy + handleRadiusPx
        );
        endHandle.set(
                ex - handleRadiusPx, ey - handleRadiusPx,
                ex + handleRadiusPx, ey + handleRadiusPx
        );
    }

    private void emitSelection() {
        if (callback == null) return;

        if (!hasSelection()) {
            lockedLineIndex = -1;
            callback.onSelectionChanged("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = selStart; i <= selEnd; i++) {
            if (i > selStart) sb.append(" ");
            sb.append(words.get(i).text);
        }
        callback.onSelectionChanged(sb.toString().trim());
    }
}