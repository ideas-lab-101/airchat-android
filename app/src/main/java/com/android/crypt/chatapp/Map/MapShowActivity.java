package com.android.crypt.chatapp.Map;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.poisearch.PoiSearch;
import com.android.crypt.chatapp.Map.utils.DatasKey;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.Map.utils.SPUtils;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageBody;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapShowActivity extends BaseActivity implements SensorEventListener {
    @BindView(R.id.m_map_view)
    MapView mMapView;
    @BindView(R.id.m_iv_location)
    ImageView mIvLocation;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.im_position_main)
    TextView imPositionMain;
    @BindView(R.id.im_position_detail)
    TextView imPositionDetail;

    private AMap mAMap;
    private Marker mMarker, mLocationGpsMarker;
    private UiSettings mUiSettings;
    private PoiSearch mPoiSearch;

    private float zoom = 14;//地图缩放级别
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = new AMapLocationClientOption();
    private AMapLocation location;
    private AMapLocationListener mAMapLocationListener;
    private View.OnClickListener mOnClickListener;
    private Gson gson;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SendMessageBody mapBody;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_show);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_position);
        setSupportActionBar(toolbar);
        initView(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理ActionBar的菜单
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
        mMapView.onDestroy();
        if (null != mPoiSearch) {
            mPoiSearch = null;
        }
        if (null != gson) {
            gson = null;
        }
        if (null != locationClient) {
            locationClient.onDestroy();
        }
    }

    private void initView(Bundle savedInstanceState) {
        initDatas(savedInstanceState);
        initListener();
        startLocation();
        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        Intent intent = getIntent();
        this.mapBody = (SendMessageBody) intent.getSerializableExtra("body");

        Logger.d("mapBody = " + mapBody.getExcessInfo());
        if (this.mapBody != null) {
            String excessInfo = this.mapBody.getExcessInfo();
            String[] info_arr = excessInfo.split("&");
            String title = "";
            String subTitle = "";
            String latLong = "";
            double lat = 0.0;
            double lon = 0.0;
            try {
                title = info_arr[0];
                subTitle = info_arr[1];
                imPositionMain.setText(title);
                imPositionDetail.setText(subTitle);

                latLong = info_arr[2];
                Logger.d("latLong = " + latLong);
                String[] lat_lon_arr = latLong.split("_");
                Logger.d("lat_lon_arr size = " + lat_lon_arr.length);

                if (lat_lon_arr.length == 2) {
                    lat = Double.valueOf(lat_lon_arr[0].toString());
                    lon = Double.valueOf(lat_lon_arr[1].toString());
                    refleshMark(lat, lon);
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private long lastTime;
    private long TIME_SENSOR = 1;
    private float mAngle = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (System.currentTimeMillis() - lastTime < TIME_SENSOR) {
            return;
        }
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION: {
                float x = event.values[0];
                x += 0;
                x %= 360.0F;
                if (x > 180.0F)
                    x -= 360.0F;
                else if (x < -180.0F)
                    x += 360.0F;

                if (Math.abs(mAngle - x) < 3.0f) {
                    break;
                }
                mAngle = Float.isNaN(x) ? 0 : x;
                if (mLocationGpsMarker != null) {
                    mLocationGpsMarker.setRotateAngle(360 - mAngle);
                }
                lastTime = System.currentTimeMillis();
            }
        }
    }

    private void initListener() {
        //监测地图画面的移动
        mAMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
            }

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
            }
        });

        //设置触摸地图监听器
        mAMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });

        //gps定位监听器
        mAMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation loc) {
                try {
                    if (null != loc) {
                        stopLocation();
                        if (loc.getErrorCode() == 0) {//可在其中解析amapLocation获取相应内容。
                            location = loc;
                            SPUtils.putString(MapShowActivity.this, DatasKey.LOCATION_INFO, gson.toJson(location));
                            doWhenLocationSucess();
                        } else {
                            //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                            Log.e("AmapError", "location Error, ErrCode:" + loc.getErrorCode() + ", errInfo:" + loc.getErrorInfo());
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };


        //控件点击监听器
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.m_iv_location:
                        mIvLocation.setImageResource(R.mipmap.location_gps_black);
                        if (null == location) {
                            startLocation();
                        } else {
                            doWhenLocationSucess();
                        }
                        break;
                }
            }
        };
        mIvLocation.setOnClickListener(mOnClickListener);
    }

    private void initDatas(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        mAMap = mMapView.getMap();

        mUiSettings = mAMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);//是否显示地图中放大缩小按钮
        mUiSettings.setMyLocationButtonEnabled(false); // 是否显示默认的定位按钮
        mUiSettings.setScaleControlsEnabled(true);//是否显示缩放级别
        mAMap.setMyLocationEnabled(false);// 是否可触发定位并显示定位层

        gson = new Gson();
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        if (null == locationClient) {
            //初始化client
            locationClient = new AMapLocationClient(this.getApplicationContext());
            //设置定位参数
            locationClient.setLocationOption(getDefaultOption());
            // 设置定位监听
            locationClient.setLocationListener(mAMapLocationListener);
        }
    }

    /**
     * 默认的定位参数
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setMockEnable(true);//如果您希望位置被模拟，请通过setMockEnable(true);方法开启允许位置模拟
        return mOption;
    }

    /**
     * 开始定位
     */
    public void startLocation() {
        initLocation();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     */
    private void stopLocation() {
        if (null != locationClient) {
            locationClient.stopLocation();
        }
    }

    /**
     * 当定位成功需要做的事情
     */
    private void doWhenLocationSucess() {
        moveMapCamera(location.getLatitude(), location.getLongitude());
        refleshLocationMark(location.getLatitude(), location.getLongitude());
    }

    /**
     * 把地图画面移动到定位地点(使用moveCamera方法没有动画效果)
     *
     * @param latitude
     * @param longitude
     */
    private void moveMapCamera(double latitude, double longitude) {
        if (null != mAMap) {
            mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
        }
    }

    /**
     * 刷新地图标志物gps定位位置
     *
     * @param latitude
     * @param longitude
     */
    private void refleshLocationMark(double latitude, double longitude) {
        if (mLocationGpsMarker == null) {
            mLocationGpsMarker = mAMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.mipmap.location_blue)))
                    .draggable(true));
        }
        mLocationGpsMarker.setPosition(new LatLng(latitude, longitude));
        mAMap.invalidate();

    }

    private void refleshMark(double latitude, double longitude) {
        if (mMarker == null) {
            mMarker = mAMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.mipmap.location_show)))
                    .draggable(true));
        }
        mMarker.setPosition(new LatLng(latitude, longitude));
        if (!mMarker.isVisible()) {
            mMarker.setVisible(true);
        }
        mAMap.invalidate();
    }


}
