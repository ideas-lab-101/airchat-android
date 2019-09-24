package com.android.crypt.chatapp.msgDetail.IMTools;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.android.crypt.chatapp.ChatAppApplication;
import com.android.crypt.chatapp.msgDetail.Interface.MsgTouchEvent;
import com.android.crypt.chatapp.utility.Common.DensityUtil;

/**
 * Created by White on 2019/4/23.
 */

public class MsgBgView extends HorizontalScrollView {
    public MsgTouchEvent callback;
    private int mBaseScrollX;//滑动基线。也就是点击并滑动之前的x值，以此值计算相对滑动距离。
    private int mScreenWidth;

    private LinearLayout mContainer;
    private boolean flag;
    private int mPageCount;//页面数量

    private int mScrollX = 200;//滑动多长距离翻页
    private int gapValue = 500;
    public boolean pageFlag = false;

    private GestureDetector mGestureDetector;
    View.OnTouchListener mGestureListener;

    public MsgBgView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics dm = context.getApplicationContext().getResources()
                .getDisplayMetrics();
        mScreenWidth = dm.widthPixels;

        gapValue = DensityUtil.dip2px(context, 200);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);
    }


    /**
     * 添加一个页面到最后。
     * @param page
     */
    public void addPage(View page) {
        addPage(page, -1);
    }

    /**
     * 添加一个页面。
     * @param page
     */
    public void addPage(View page, int index) {
        if(!flag) {
            mContainer = (LinearLayout) getChildAt(0);
            flag = true;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mScreenWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        if(index == -1) {
            mContainer.addView(page, params);
        } else {
            mContainer.addView(page, index, params);
        }
        mPageCount++;
    }

    /**
     * 移除一个页面。
     * @param index
     */
    public void removePage(int index) {
        if(mPageCount < 1) {
            return;
        }
        if(index<0 || index>mPageCount-1) {
            return;
        }
        mContainer.removeViewAt(index);
        mPageCount--;
    }

    /**
     * 移除所有的页面
     */
    public void removeAllPages() {
        if(mPageCount > 0) {
            mContainer.removeAllViews();
        }
    }

    /**
     * 获取页面数量
     * @return
     */
    public int getPageCount() {
        return mPageCount;
    }

    /**
     * 获取相对滑动位置。由右向左滑动，返回正值；由左向右滑动，返回负值。
     * @return
     */
    private int getBaseScrollX() {
        return getScrollX() - mBaseScrollX;
    }

    /**
     * 使相对于基线移动x距离。
     * @param x x为正值时右移；为负值时左移。
     */
    private void baseSmoothScrollTo(int x) {
        smoothScrollTo(x + mBaseScrollX, 0);
    }

    public void toFirstPage(){
        smoothScrollTo(0, 0);
        pageFlag = false;
    }

    public void toSecondPage(){
        smoothScrollTo(gapValue, 0);
        pageFlag = true;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    /**
     * 如果竖向滑动距离<横向距离，执行横向滑动，否则竖向。如果是ScrollView，则'<'换成'>'
     */
    class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceY) < Math.abs(distanceX)) {
                return true;
            }
            return false;
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                int scrollX = getBaseScrollX();
                //左滑，大于一半，移到下一页
                if (scrollX > mScrollX) {
                    baseSmoothScrollTo(gapValue);
                    mBaseScrollX = 0;
                    pageFlag = true;
                } else {
                    baseSmoothScrollTo(0);
                    pageFlag = false;
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }


}

