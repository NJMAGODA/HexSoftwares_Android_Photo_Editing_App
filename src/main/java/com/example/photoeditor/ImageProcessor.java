package com.example.photoeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageProcessor {
    public static Bitmap adjustImage(Bitmap original, float brightness, float contrast, float saturation) {
        Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        ColorMatrix colorMatrix = new ColorMatrix();

        // Apply brightness
        colorMatrix.set(new float[] {
                1, 0, 0, 0, brightness * 255,
                0, 1, 0, 0, brightness * 255,
                0, 0, 1, 0, brightness * 255,
                0, 0, 0, 1, 0
        });

        // Apply contrast
        float scale = contrast + 1.f;
        float translate = (-.5f * scale + .5f) * 255.f;
        ColorMatrix contrastMatrix = new ColorMatrix(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });
        colorMatrix.postConcat(contrastMatrix);

        // Apply saturation
        float[] matrix = {
                (1 - saturation) * 0.3086f + saturation, (1 - saturation) * 0.6094f, (1 - saturation) * 0.0820f, 0, 0,
                (1 - saturation) * 0.3086f, (1 - saturation) * 0.6094f + saturation, (1 - saturation) * 0.0820f, 0, 0,
                (1 - saturation) * 0.3086f, (1 - saturation) * 0.6094f, (1 - saturation) * 0.0820f + saturation, 0, 0,
                0, 0, 0, 1, 0
        };
        ColorMatrix saturationMatrix = new ColorMatrix(matrix);
        colorMatrix.postConcat(saturationMatrix);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(original, 0, 0, paint);

        return result;
    }
}