package com.android.crypt.chatapp.utility.Cache.CacheClass;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by White on 2019/4/26.
 */

@Entity
public class ObjectCacheBody {

    @Id(autoincrement = false)
    private Long id;
    private String value;

    @Generated(hash = 1536696101)
    public ObjectCacheBody() {
    }

    @Generated(hash = 1426277556)
    public ObjectCacheBody(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }





}
