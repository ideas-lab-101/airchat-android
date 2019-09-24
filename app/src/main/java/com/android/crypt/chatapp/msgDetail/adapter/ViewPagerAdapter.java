package com.android.crypt.chatapp.msgDetail.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.wingsofts.dragphotoview.DragPhotoView;

import java.util.List;

/**
 * Created by White on 2019/6/3.
 */

public class ViewPagerAdapter extends PagerAdapter{

    private List<View> viewList;
    public ViewPagerAdapter(List<View> viewList)
    {
        this.viewList = viewList;
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v=viewList.get(position);
        ViewGroup parent = (ViewGroup) v.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        container.addView(viewList.get(position));

        return viewList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
        container.removeView(viewList.get(position));
        //container.removeView(viewList.get(position));
    }
}
