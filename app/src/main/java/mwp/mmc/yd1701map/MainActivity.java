package mwp.mmc.yd1701map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    MapView mapView;
    CheckBox cb_pt,cb_wx,cb_rlt,cb_jt;
    BaiduMap baiduMap;
    LocationClient mLocationClient;
    public BDLocationListener myListener = new MyLocationListener();
    private LatLng latLng;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isFirstLocate=true;
    
    SuggestionSearch mSuggestionSearch = null;
    String mcity="";
    private PoiSearch poiSearch;
    AutoCompleteTextView search_edt;



    public void showContacts(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"没有权限,请手动开启定位权限",Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 100);
        }else{
            initViews();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    initViews();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //判断6.0以上
        if (Build.VERSION.SDK_INT>=23){
            showContacts();
        }else{
            initViews();//init为定位方法
        }
        //定位初始化
        SDKInitializer.initialize(getApplicationContext());

    }
    private void initViews() {
        mapView=this.findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        cb_pt=this.findViewById(R.id.cb_pt);
        cb_wx=this.findViewById(R.id.cb_wx);
        cb_rlt=this.findViewById(R.id.cb_rlt);
        cb_jt=this.findViewById(R.id.cb_jt);

        cb_pt.setOnCheckedChangeListener(this);
        cb_wx.setOnCheckedChangeListener(this);
        cb_rlt.setOnCheckedChangeListener(this);
        cb_jt.setOnCheckedChangeListener(this);

        search_edt = findViewById(R.id.search_edt);

        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        //配置定位SDK参数
        initLocation();
        mLocationClient.registerLocationListener(myListener);    //注册监听函数

        //开启定位
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();
        poiSearch = PoiSearch.newInstance();

//        mSuggestionSearch = SuggestionSearch.newInstance();
//        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        search_edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!mcity.equals("") && !search_edt.getText().toString().equals("")){
                    if (actionId == EditorInfo.IME_ACTION_SEARCH){
                        baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,null));
                        String keyword = search_edt.getText().toString();
                        search_edt.dismissDropDown();

                        poiSearch.searchInCity((new PoiCitySearchOption())
                                .city(mcity).keyword(keyword).pageNum(0));
                        Log.e("city+key", "onEditorAction: ========="+mcity+"----"+keyword );
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                        return true;
                    }else{
                        return false;
                    }
                }else{

                    return false;
                }
            }
        });
        search_edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
//                mSuggestionSearch
//                        .requestSuggestion((new SuggestionSearchOption())
//                                .keyword(cs.toString()).city(city));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult result) {
                Log.e( "onGetPoiResult: ==",String.valueOf(result.error)+"\n"+ String.valueOf(result) );
                if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    baiduMap.clear();
                    //创建PoiOverlay对象
                    PoiOverlay overlay = new MyPoiOverlay(baiduMap);
                    baiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(result);
                    overlay.addToMap();
                    overlay.zoomToSpan();

                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });

    }

    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override

        public boolean onPoiClick(int index) {
            super.onPoiClick(index);

            PoiInfo poi = getPoiResult().getAllPoi().get(index);
//            selectPoi = poi;
//            // if (poi.hasCaterDetails) {
//            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
//                    .poiUid(poi.uid));
////            Log.e( "点击Poi详情: ","Address:"+poi.address+"\n"+"Area:"+poi.area+"\n"
////                    +"A:"+poi.city+"|"+poi.name+"|"+poi.phoneNum+"|"+poi.postCode+"|"+poi.province+"|"+poi.street_id
////                    +"\n"+poi.detail+"|"+poi.location+"|"+poi.type+"|"+poi.poiDetailInfo);
////            Log.e("是否为公交Poi", String.valueOf(poi.detail == 0) );0
//            TextView name,addr;
//
//            name = findViewById(R.id.poi_name);
//            addr = findViewById(R.id.poi_address);
//            if (poi.detail==0){
//                name.setText(poi.name+"(公交站)");
//            }else{
//                name.setText(poi.name);
//            }
//
//            addr.setText(poi.address);
//            poi_info.setVisibility(View.VISIBLE);
            LatLng latLng = new LatLng(poi.location.latitude,poi.location.longitude);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().target(latLng).build()));
            return true;
        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 0;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        option.setOpenGps(true); // 打开gps

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mcity=location.getCity();
            Log.e("city", "onReceiveLocation: ==="+mcity );
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            baiduMap.setMyLocationData(locData);
            // 当不需要定位图层时关闭定位图层
            //
            //BaiduMap.setMyLocationEnabled(false);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
               baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    // GPS定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    // 网络定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();

                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                    // 离线定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();

                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(MainActivity.this, "服务器错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(MainActivity.this, "网络错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Log.e("222", "onReceiveLocation: "+location.getLocType());
                    Toast.makeText(MainActivity.this, "手机模式错误，请检查是否飞行", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton cb, boolean b) {
        switch (cb.getId()){
            case R.id.cb_pt:
                if (b) {
                    baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }else {
                    baiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
                }
            break;
            case R.id.cb_wx:
                if (b) {
                    baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }else {
                    baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
                break;
            case R.id.cb_jt:
                if (b) {
                    baiduMap.setTrafficEnabled(true);
                    baiduMap.setCustomTrafficColor("#ffba0101","#fff33131","#ffff9e19","#00000000");
                    MapStatusUpdate mapStatusUpdate=MapStatusUpdateFactory.zoomTo(18);
                    baiduMap.animateMapStatus(mapStatusUpdate);
                }else {
                    baiduMap.setTrafficEnabled(false);
                }
                break;
            case R.id.cb_rlt:
                if (b) {

                    baiduMap.setBaiduHeatMapEnabled(true);
                }else {
                    baiduMap.setTrafficEnabled(false);
                }
                break;
        }
    }


    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        super.onDestroy();
    }

    }









