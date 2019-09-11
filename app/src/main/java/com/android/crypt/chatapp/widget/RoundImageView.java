package com.android.crypt.chatapp.widget;

/**
 * Created by mulaliu on 15/11/5.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.android.crypt.chatapp.R;


public class RoundImageView extends AppCompatImageView {

    private boolean isCircle = true;
    private Paint paint;
    private float borderRadius = 5;
    private float borderWidth = 0f;
    private int borderColor = 0x44000000;

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundImageView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {

        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
            isCircle = a.getBoolean(R.styleable.RoundImageView_isCircle, isCircle);
            borderRadius = a.getDimension(R.styleable.RoundImageView_borderRadius, borderRadius);
            borderWidth= a.getDimension(R.styleable.RoundImageView_borderWidth, borderWidth);
            borderColor= a.getColor(R.styleable.RoundImageView_borderColor, borderColor);
            a.recycle();
        }else {
            float density = context.getResources().getDisplayMetrics().density;
            borderRadius = (int) (borderRadius*density);
        }

        //初始化画笔
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        if(isCircle){ //画圆
            borderRadius = Math.min(bitmap.getWidth(), bitmap.getWidth())/2;
        }
        super.onDraw(canvas2);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        drawLeftUp(canvas2);
        drawLeftDown(canvas2);
        drawRightDown(canvas2);
        drawRightUp(canvas2);
        paint.reset();
        canvas.drawBitmap(bitmap, rect, rectF, paint);
        bitmap.recycle();
    }

    private void drawLeftUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, borderRadius);
        path.lineTo(0, 0);
        path.lineTo(borderRadius, 0);
        path.arcTo(new RectF(
                        0,
                        0,
                        borderRadius*2,
                        borderRadius*2),
                -90,
                -90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawLeftDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, getHeight()-borderRadius);
        path.lineTo(0, getHeight());
        path.lineTo(borderRadius, getHeight());
        path.arcTo(new RectF(
                        0,
                        getHeight()-borderRadius*2,
                        0+borderRadius*2,
                        getHeight()),
                90,
                90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth()-borderRadius, getHeight());
        path.lineTo(getWidth(), getHeight());
        path.lineTo(getWidth(), getHeight()-borderRadius);
        path.arcTo(new RectF(
                getWidth()-borderRadius*2,
                getHeight()-borderRadius*2,
                getWidth(),
                getHeight()), 0, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth(), borderRadius);
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth()-borderRadius, 0);
        path.arcTo(new RectF(
                        getWidth()-borderRadius*2,
                        0,
                        getWidth(),
                        0+borderRadius*2),
                -90,
                90);
        path.close();
        canvas.drawPath(path, paint);
    }

}
