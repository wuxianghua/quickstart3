package com.palmaplus.nagrand.quickstart;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ViewGroup;

import com.palmaplus.nagrand.core.Engine;
import com.palmaplus.nagrand.data.BasicElement;
import com.palmaplus.nagrand.data.DataSource;
import com.palmaplus.nagrand.data.Feature;
import com.palmaplus.nagrand.data.MapElement;
import com.palmaplus.nagrand.data.PlanarGraph;
import com.palmaplus.nagrand.geos.Coordinate;
import com.palmaplus.nagrand.geos.GeometryFactory;
import com.palmaplus.nagrand.geos.Point;
import com.palmaplus.nagrand.position.PositioningManager;
import com.palmaplus.nagrand.position.atlas.AtlasLoaction;
import com.palmaplus.nagrand.position.atlas.AtlasLocationManager;
import com.palmaplus.nagrand.position.util.PositioningUtil;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.layer.FeatureLayer;

import java.util.HashMap;

/**
 * Created by Overu on 2015/5/14.
 */
public class PositioningDemo extends Activity implements SensorEventListener {

  private LocationMark locationMark;
  MapView mapView;

  AtlasLocationManager positioningManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Engine engine = Engine.getInstance();
    engine.startWithLicense(Constant.APP_KEY, this);
    // 获取放置Overlay的ViewGroup
    ViewGroup container =  findViewById(R.id.overlay_container);
    mapView = findViewById(R.id.mapView);
    // 设置这个ViewGroup用于放置Overlay
    mapView.setOverlayContainer(container);
    locationMark = new LocationMark(mapView.getContext(), mapView);
    mapView.start();
    //新建一个定位对象，并指定一个mac地址
//    positioningManager =
//            new SinglePositioningManager("58:44:98:DD:7C:4F");

    //新建一个蓝牙定位
    SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    sm.registerListener(this,
            sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_FASTEST);
    HashMap<String,Long> hashMap = new HashMap<>();
    hashMap.put(Constant.FLOOR_PLAN_ID,(long)1849233);
    positioningManager = new AtlasLocationManager( // 蓝牙定位管理对象
            PositioningDemo.this,
            Constant.API_ID_KEY,
            Constant.API_KEY_SECRET,
            Constant.LOCATION_ID,
            hashMap

    );
    /*positioningManager.setLocationChangeListener(new AtlasLocationManager.LocationChangeListener() {
      @Override
      public void onLocationChanged(PositioningManager.LocationStatus locationStatus, AtlasLoaction atlasLoaction, AtlasLoaction atlasLoaction1, float v) {
        switch (locationStatus){
          case MOVE:
            break;
        }

      }
    });*/

    final DataSource dataSource = new DataSource("http://api.ipalmap.com/");

    dataSource.requestPlanarGraph(
            1849233, //请先确认你所需要的地图是否包含了定位的权限
            new DataSource.OnRequestDataEventListener<PlanarGraph>() {
      @Override
      public void onRequestDataEvent(DataSource.ResourceState state, PlanarGraph data) {
        if (state == DataSource.ResourceState.OK) {
          mapView.drawPlanarGraph(data);

          final FeatureLayer positioningLayer = new FeatureLayer("positioning"); //新建一个放置定位点的图层
          mapView.addLayer(positioningLayer);  //把这个图层添加至MapView中
          mapView.setLayerOffset(positioningLayer); //让这个图层获取到当前地图的坐标偏移
          //添加一个Feature，用于展现定位点
          Point point = GeometryFactory.createPoint(new Coordinate(0, 0));//添加一个定位点
          MapElement mapElement = new MapElement(); //设置它的属性
          mapElement.addElement("id", new BasicElement(1L)); //设置id
          Feature feature = new Feature(point, mapElement); //创建Feature
          positioningLayer.addFeature(feature); //把这个Feature添加到FeatureLayer中
          /*positioningManager.setOnLocationChangeListener(new PositioningManager.OnLocationChangeListener<Location>() { //定位监听的事件，如果得到了新的位置数据，就会调用这个方法
            @Override
            public void onLocationChange(PositioningManager.LocationStatus status, Location oldLocation, Location newLocation) {  //分别代表着上一个位置点和新位置点
              switch (status) {
                case MOVE:
                PositioningUtil.positionLocation(1, positioningLayer, newLocation); //这里的id就是上面设置的id，这个接口的意思是把id为1的定位点移动到newLocation的位置上
                  Point point = newLocation.getPoint();
                  Coordinate coordinate = point.getCoordinate();
                  coordinate.getX();
                  coordinate.getY();
                  coordinate.getZ();


                   break;
              }
            }
          });*/
          positioningManager.setLocationChangeListener(new AtlasLocationManager.LocationChangeListener() {
            @Override
            public void onLocationChanged(PositioningManager.LocationStatus locationStatus, AtlasLoaction atlasLoaction, AtlasLoaction atlasLoaction1, float v) {
              switch (locationStatus) {
                case MOVE:
                  PositioningUtil.positionLocation(1, positioningLayer, atlasLoaction1);
                  refreshLocation(atlasLoaction1.getPoint().getCoordinate());
              }
            }
          });
          positioningManager.start(); //开启定位
        } else {
          //error
        }

      }
    });
  }

  public void refreshLocation(Coordinate coordinate) {
    double x = coordinate.getX();
    double y = coordinate.getY();
    locationMark.init(new double[]{x, y});
    locationMark.setFloorId(1849233);
    mapView.addOverlay(locationMark);
    mapView.getOverlayController().refresh();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.drop();
    positioningManager.stop();
    positioningManager.close();
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
      float value = sensorEvent.values[0];
      locationMark.setRotation(value - (float) mapView.getRotate());
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {

  }
}
