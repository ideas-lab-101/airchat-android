package com.android.crypt.chatapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by J!nl!n on 2015/1/26.
 */
public class MySearchView extends AppCompatEditText implements View.OnFocusChangeListener, View.OnKeyListener {
    private static final String TAG = MySearchView.class.getSimpleName();
    /**
     * 是否是默认图标再左边的样式
     */
    private boolean isLeft = false;
    private Drawable dLeft;
    private Drawable dRight;
    private Rect rBounds;
    /**
     * 是否点击软键盘搜索
     */
    private boolean pressSearch = false;
    /**
     * 软键盘搜索键监听
     */
    private OnSearchClickListener listener;

    public void setOnSearchClickListener(OnSearchClickListener listener) {
        this.listener = listener;
    }

    public interface OnSearchClickListener {
        void onSearchClick(View view, String searchText);
    }

    public MySearchView(Context context) {
        this(context, null);
        init();
    }

    public MySearchView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
        init();
    }

    public MySearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setEditTextDrawable();
        this.setOnFocusChangeListener(this);
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setEditTextDrawable();
            }
        });
        this.setOnKeyListener(this);
    }

    public void setEditTextDrawable() {
        if (getText().toString().length() == 0) {
            setCompoundDrawables(this.dLeft, null, null, null);
        } else {
            setCompoundDrawables(this.dLeft, null, this.dRight, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        if ((this.dRight != null) && (paramMotionEvent.getAction() == 1)) {
            this.rBounds = this.dRight.getBounds();
            int i = (int) paramMotionEvent.getRawX();// 距离屏幕的距离
            //int i = (int) paramMotionEvent.getX();//距离边框的距离
            if (i > getRight() - this.rBounds.width()) { //点击了x按钮
                setText("");
                paramMotionEvent.setAction(MotionEvent.ACTION_CANCEL);
            }
        }
        return super.onTouchEvent(paramMotionEvent);
    }

    @Override
    public void setCompoundDrawables(Drawable paramDrawable1, Drawable paramDrawable2, Drawable paramDrawable3, Drawable paramDrawable4) {
        if (paramDrawable1 != null)
            this.dLeft = paramDrawable1;
        if (paramDrawable3 != null)
            this.dRight = paramDrawable3;
        super.setCompoundDrawables(paramDrawable1, paramDrawable2, paramDrawable3, paramDrawable4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isLeft) { // 如果是默认样式，则直接绘制
            super.onDraw(canvas);
        } else { // 如果不是默认样式，需要将图标绘制在中间
            Drawable[] drawables = getCompoundDrawables();
            Drawable drawableLeft = drawables[0];
            Drawable drawableRight = drawables[2];
            translate(drawableLeft, canvas);
            translate(drawableRight, canvas);
            super.onDraw(canvas);
        }

    }

    public void translate(Drawable drawable, Canvas canvas) {
        if (drawable != null) {
            float textWidth = getPaint().measureText(getHint().toString());
            int drawablePadding = getCompoundDrawablePadding();
            int drawableWidth = drawable.getIntrinsicWidth();
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            if (drawable == getCompoundDrawables()[0]) {
                canvas.translate((getWidth() - bodyWidth - getPaddingLeft() - getPaddingRight()) / 2, 0);
            } else {
                setPadding(getPaddingLeft(), getPaddingTop(), (int)(getWidth() - bodyWidth - getPaddingLeft()), getPaddingBottom());
                canvas.translate((getWidth() - bodyWidth - getPaddingLeft()) / 2, 0);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.d(TAG, "onFocusChange execute");
        // 恢复EditText默认的样式
        if (!pressSearch && TextUtils.isEmpty(getText().toString())) {
            isLeft = hasFocus;
        }
        setEditTextDrawable();
    }

    public void setLeft(boolean blnLeft){
        this.isLeft = blnLeft;
        setEditTextDrawable();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        pressSearch = (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);
        if (pressSearch && listener != null) {
            /*隐藏软键盘*/
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }
            listener.onSearchClick(v, getText().toString());
        }
        return false;
    }

}