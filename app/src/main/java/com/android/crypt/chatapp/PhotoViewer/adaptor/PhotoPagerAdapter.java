package com.android.crypt.chatapp.PhotoViewer.adaptor;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.crypt.chatapp.PhotoViewer.Model.ImageModel;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.msgDetail.IMTools.DownLoadImage;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.wingsofts.dragphotoview.DragPhotoView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by White on 2019/6/3.
 */

public class PhotoPagerAdapter extends PagerAdapter{

    private ArrayList<ImageModel> imageUrlList;
    private Context context;
    private LayoutInflater inflater;
    private LinkedList<View> mViewCache = new LinkedList<>();


    public AddLongTapCallback callbacks;
    public interface AddLongTapCallback{
        void addLongTapMethod(DragPhotoView photo, int position);
    }


    public PhotoPagerAdapter( ArrayList<ImageModel>imageUrlList, Context context, AddLongTapCallback callbacks) {
        this.imageUrlList = imageUrlList;
        this.context = context;
        this.callbacks = callbacks;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imageUrlList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }



    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View convertView = null;
        if (mViewCache.size() == 0) {
            convertView = inflater.inflate(R.layout.activity_photo_viewer, null);;
        } else {
            convertView = mViewCache.removeFirst();
        }
        container.addView(convertView);
        freshItem(convertView, position);
        return convertView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View contentView = (View) object;
        container.removeView(contentView);
        mViewCache.add(contentView);
    }





    private void freshItem(View view, int position) {
        String deKey = imageUrlList.get(position).de_key;
        String image_url = imageUrlList.get(position).image_url;

        TextView textView = (TextView)view.findViewById(R.id.text_se_tips);
        DragPhotoView photo = (DragPhotoView)view.findViewById(R.id.big_image_viewer);

        if (deKey == null || deKey.equals("")) {
            RequestOptions requestOptions = new RequestOptions().fitCenter().placeholder(R.mipmap.default_image);
            Glide.with(context).load(image_url).apply(requestOptions).into(photo);
            textView.setVisibility(View.GONE);

        } else {
            textView.setVisibility(View.VISIBLE);
            //加密图片
            DownLoadImage loadTool = new DownLoadImage(context);
            loadTool.loadFile(photo, image_url, deKey, R.mipmap.default_image);
            textView.setVisibility(View.GONE);
        }

        if (callbacks != null){
            callbacks.addLongTapMethod(photo, position);
        }
    }


}
