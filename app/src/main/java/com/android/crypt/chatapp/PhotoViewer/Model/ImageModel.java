package com.android.crypt.chatapp.PhotoViewer.Model;

import java.io.Serializable;

/**
 * Created by White on 2019/5/30.
 */

public class ImageModel implements  Serializable {
    public String image_url = "";   //图片url
    public String de_key = "";      //图片解密密钥，为空就是未加密图片

    public ImageModel(String image_url, String de_key){
        this.image_url = image_url;
        this.de_key = de_key;
    }
}
