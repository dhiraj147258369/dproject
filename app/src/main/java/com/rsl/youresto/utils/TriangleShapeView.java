package com.rsl.youresto.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class TriangleShapeView extends View {
    public TriangleShapeView(Context context) {
        super(context);
    }

    public TriangleShapeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TriangleShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth() / 2;

        Path path = new Path();
//        path.moveTo( w, 0);
//        path.lineTo( 2 * w , 0);
//        path.lineTo( 2 * w , w);
//        path.lineTo( w , 0);
//        path.close();

        path.moveTo(0, 0);
        //top line
        path.lineTo(w, 0);
        path.lineTo(0, w);
        path.lineTo(0, w);
        path.close();

        @SuppressLint("DrawAllocation") Paint p = new Paint();
        p.setColor(Color.YELLOW);

        canvas.drawPath(path, p);
    }
}
