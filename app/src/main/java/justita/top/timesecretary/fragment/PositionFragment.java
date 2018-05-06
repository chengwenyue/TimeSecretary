package justita.top.timesecretary.fragment;


import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;

import justita.top.timesecretary.R;
import justita.top.timesecretary.activity.SearchActivity;


public class PositionFragment extends BaseAttrFragment implements LocationSource, View.OnClickListener {
    String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast";
    final int REQUEST_CODE = 1;

    private double lat;
    private double lon;

    private MapView mMapView;
    private AMap aMap;
    private UiSettings mUiSettings;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private PendingIntent mPendingIntent = null;
    private TextView mSetLocation;
    private TextView mGetLocation;
    private TextView mLeftLocation;
    private TextView mLocationSerEt;
    private LinearLayout mPositionTextView;

    private String mCurrentLocation;
    private String mCurrentCity;
    private boolean flag = true;

    private Marker locationMarker;
    public AMapLocationListener aMapLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
//            amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//            amapLocation.getLatitude();//获取纬度
//            amapLocation.getLongitude();//获取经度
//            amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//            amapLocation.getCountry();//国家信息
//            amapLocation.getProvince();//省信息
//            amapLocation.getCity();//城市信息
//            amapLocation.getDistrict();//城区信息
//            amapLocation.getStreet();//街道信息
//            amapLocation.getStreetNum();//街道门牌号信息

            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    lat = aMapLocation.getLatitude();
                    lon = aMapLocation.getLongitude();
                    if(mPositionResult != null){
                        mPositionResult.setLatitude(aMapLocation.getLatitude());
                        mPositionResult.setLongitude(aMapLocation.getLongitude());
                        mPositionResult.setAddress(aMapLocation.getAddress());
                    }
                    mCurrentLocation = aMapLocation.getAddress();
                    mCurrentCity = aMapLocation.getCity();
                    LatLng latLng = new LatLng(lat, lon);//取出经纬度
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("当前位置");
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.position_marker));
                    markerOptions.visible(true);
                    locationMarker = aMap.addMarker(markerOptions);
                    locationMarker.showInfoWindow();//主动显示indowindow
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    Log.e("AmapError", "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
                    Toast.makeText(getContext(), aMapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_position, container, false);
        initView(view, savedInstanceState);
        return view;
    }

    private void initView(View view, Bundle savedInstanceState) {

        mSetLocation = (TextView) view.findViewById(R.id.set_location);
        mSetLocation.setOnClickListener(this);
        mGetLocation = (TextView) view.findViewById(R.id.get_location);
        mGetLocation.setOnClickListener(this);
        mLeftLocation = (TextView) view.findViewById(R.id.left_location);
        mLeftLocation.setOnClickListener(this);

        mPositionTextView = (LinearLayout) view.findViewById(R.id.position_text_view);

        mLocationSerEt = (TextView) view.findViewById(R.id.text_position);
        mLocationSerEt.setHint("设置地理位置");
        mLocationSerEt.setFocusable(false);
        mLocationSerEt.setKeyListener(null);
        mLocationSerEt.setOnClickListener(this);


        mMapView = (MapView) view.findViewById(R.id.affair_map);
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        mUiSettings = aMap.getUiSettings();
        // 关闭缩放按钮
        mUiSettings.setZoomControlsEnabled(false);
        //定位按钮
        aMap.setLocationSource(this);
        mUiSettings.setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        // 关闭手势
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);

        mLocationOption = new AMapLocationClientOption();
        mLocationOption = mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setNeedAddress(true);

        //初始化定位
        mLocationClient = new AMapLocationClient(view.getContext());
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.setLocationListener(aMapLocationListener);
        //启动定位
        mLocationClient.startLocation();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case REQUEST_CODE:
                final String name = data.getStringExtra("name");
                mLocationSerEt.setHint(name);
                GeocodeSearch geocoderSearch = new GeocodeSearch(getActivity());
                GeocodeQuery query = new GeocodeQuery(name, "");
                geocoderSearch.getFromLocationNameAsyn(query);
                geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                    @Override
                    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

                    }

                    @Override
                    public void onGeocodeSearched(GeocodeResult result, int rCode) {
                        if (rCode == 1000) {
                            if (result != null && result.getGeocodeAddressList() != null
                                    && result.getGeocodeAddressList().size() > 0) {
                                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(convertToLatLng(address.getLatLonPoint()), 15));
                                if (mPositionResult != null){
                                    mPositionResult.setLatitude(address.getLatLonPoint().getLatitude());
                                    mPositionResult.setLongitude(address.getLatLonPoint().getLongitude());
                                    mPositionResult.setAddress(address.getFormatAddress());
                                }
                                if (locationMarker == null) {
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(convertToLatLng(address.getLatLonPoint()));
                                    markerOptions.title(name);
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.position_marker));
                                    markerOptions.visible(true);
                                    locationMarker = aMap.addMarker(markerOptions);
                                } else {
                                    locationMarker.setPosition(convertToLatLng(address.getLatLonPoint()));
                                    locationMarker.setTitle(name);
                                }
                                locationMarker.showInfoWindow();
                            } else {
                                Toast.makeText(getActivity(), "无结果...", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "出错了...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        LatLng latLng = new LatLng(lat, lon);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    public void deactivate() {

    }


    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    private positionResult mPositionResult;

    public void getPositionResult(positionResult positionResult){
        this.mPositionResult = positionResult;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_location:
                mLocationSerEt.setHint("设置地理位置");
                for (int i = 0; i < mPositionTextView.getChildCount(); i++) {
                    TextView tv = (TextView) mPositionTextView.getChildAt(i);
                    if (tv.getId() == R.id.set_location){
                        tv.setBackgroundColor(getResources().getColor(R.color.fragment2));
                        tv.setTextColor(getResources().getColor(R.color.white));
                    } else{
                        tv.setBackgroundColor(getResources().getColor(R.color.white));
                        tv.setTextColor(getResources().getColor(R.color.black));
                    }
                }
                break;
            case R.id.get_location:
                mLocationSerEt.setHint("设置到达区域");
                for (int i = 0; i < mPositionTextView.getChildCount(); i++) {
                    TextView tv = (TextView) mPositionTextView.getChildAt(i);
                    if (tv.getId() == R.id.get_location){
                        tv.setBackgroundColor(getResources().getColor(R.color.fragment2));
                        tv.setTextColor(getResources().getColor(R.color.white));
                    } else{
                        tv.setBackgroundColor(getResources().getColor(R.color.white));
                        tv.setTextColor(getResources().getColor(R.color.black));
                    }
                }
                break;
            case R.id.left_location:
                mLocationSerEt.setHint("设置离开区域");
                for (int i = 0; i < mPositionTextView.getChildCount(); i++) {
                    TextView tv = (TextView) mPositionTextView.getChildAt(i);
                    if (tv.getId() == R.id.left_location){
                        tv.setBackgroundColor(getResources().getColor(R.color.fragment2));
                        tv.setTextColor(getResources().getColor(R.color.white));
                    } else{
                        tv.setBackgroundColor(getResources().getColor(R.color.white));
                        tv.setTextColor(getResources().getColor(R.color.black));
                    }
                }
                break;
            case R.id.text_position:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("CurrentLocation", mCurrentLocation);
                intent.putExtra("CurrentCity", mCurrentCity);
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }
    }

    public interface positionResult {
        void setLatitude(double latitude);
        void setLongitude(double longitude);
        void setAddress(String address);
    }

}
