package com.palmaplus.nagrand.quickstart;

import android.app.Activity;
import android.os.Bundle;

import com.palmaplus.nagrand.core.Engine;
import com.palmaplus.nagrand.data.DataList;
import com.palmaplus.nagrand.data.DataSource;
import com.palmaplus.nagrand.data.FeatureCollection;
import com.palmaplus.nagrand.data.LocationList;
import com.palmaplus.nagrand.data.MapModel;
import com.palmaplus.nagrand.data.PlanarGraph;
import com.palmaplus.nagrand.geos.Coordinate;
import com.palmaplus.nagrand.navigate.NavigateManager;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.layer.FeatureLayer;

/**
 * Created by Overu on 2016/6/29.
 */
public class NavigateDemo extends Activity {

  MapView mapView;
  FeatureLayer featureLayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Engine engine = Engine.getInstance(); //初始化引擎
    engine.startWithLicense(Constant.APP_KEY, this); //设置验证lincense，可以通过开发者平台去查找自己的lincense

    final DataSource dataSource = new DataSource("http://api.ipalmap.com/"); //填写服务器的URL
    mapView = new MapView("default", this);
    setContentView(mapView);
    mapView.start(); //开始绘制地图
    dataSource.requestMaps(new DataSource.OnRequestDataEventListener<DataList<MapModel>>() {
      @Override
      public void onRequestDataEvent(DataSource.ResourceState state, DataList<MapModel> data) {
        if (state != DataSource.ResourceState.OK)
          return;
        if (data.getSize() == 0) //如果列表中的地图数量是0，请去开发者平台添加一些地图
          return;
        dataSource.requestPOIChildren(MapModel.POI.get(data.getPOI(0)), new DataSource.OnRequestDataEventListener<LocationList>() {
          @Override
          public void onRequestDataEvent(DataSource.ResourceState state, LocationList data) {
            if (state != DataSource.ResourceState.OK)
              return;
            if (data.getSize() == 0) //如果是0说明这套图没有楼层，请反馈给我们
              return;
            dataSource.requestPlanarGraph(
                    1849233,
                    new DataSource.OnRequestDataEventListener<PlanarGraph>() { //发起获取一个平面图的请求
                      @Override
                      public void onRequestDataEvent(DataSource.ResourceState state, PlanarGraph data) {
                        if (state == DataSource.ResourceState.OK) {

                          mapView.drawPlanarGraph(data);  //加载平面图

                          //添加导航层
                          featureLayer = new FeatureLayer("navigate");
                          mapView.setLayerOffset(featureLayer);
                          mapView.addLayer(featureLayer);

                          final NavigateManager nm = new NavigateManager();
                          nm.setOnNavigateComplete(new NavigateManager.OnNavigateComplete() {
                            @Override
                            public void onNavigateComplete(NavigateManager.NavigateState state, FeatureCollection featureCollection) {
                              featureLayer.addFeatures(featureCollection); //获取导航线
                              featureLayer.addFeature(nm.getTransitFeature(1003497)); //获取经停点
                            }
                          });
                          Coordinate[] coordinates = new Coordinate[]{new Coordinate(13526590.66, 3663426.49), new Coordinate(13526597.03, 3663427.70)};
                          long[] ids = new long[]{1003497, 1003497};
                          nm.navigation(new Coordinate(13526582.000000, 3663422.750000), 1003497, new Coordinate(13526605.000000, 3663424.250000), 1003497, coordinates, ids);
                        } else {
                          //error
                        }
                      }
                    });
          }
        });
      }
    });


  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.drop();
  }
}
