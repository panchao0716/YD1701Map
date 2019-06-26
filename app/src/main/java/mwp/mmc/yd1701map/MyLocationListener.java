package mwp.mmc.yd1701map;

import android.location.Location;
import android.widget.CheckBox;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MyLocationListener extends BDAbstractLocationListener {
    MapView mapView;
    BaiduMap baiduMap;
    LocationClient locationClient;

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
            //mapView 销毁后不在处理新接收的位置
            if (bdLocation == null || mapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            baiduMap.setMyLocationData(locData);

    }


}
