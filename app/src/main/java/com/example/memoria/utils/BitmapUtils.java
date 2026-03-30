package com.example.memoria.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

public class BitmapUtils {

    /**
     * Decode bitmap từ file và xoay đúng theo EXIF (để không bị ngang/dọc sai).
     */
    public static @Nullable Bitmap decodeAndFixRotation(String filePath) {
        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        if (bmp == null) return null;

        int rotationDegrees = 0;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            rotationDegrees = exifToDegrees(orientation);
        } catch (IOException e) {
            // Nếu lỗi EXIF thì cứ trả nguyên bitmap
            rotationDegrees = 0;
        }

        if (rotationDegrees == 0) return bmp;

        Matrix m = new Matrix();
        m.postRotate(rotationDegrees);

        Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        if (rotated != bmp) {
            bmp.recycle();
        }
        return rotated;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) return 90;
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) return 180;
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) return 270;
        return 0;
    }
}