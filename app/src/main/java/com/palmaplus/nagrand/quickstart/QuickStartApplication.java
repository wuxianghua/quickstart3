package com.palmaplus.nagrand.quickstart;

import android.app.Application;
import android.widget.Toast;

/**
 * Created by Overu on 2015/11/30.
 */
public class QuickStartApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    if (FileUtilsTools.checkoutSDCard()) {
      FileUtilsTools.copyDirToSDCardFromAsserts(this, Constant.LUA_NAME, "font");
      FileUtilsTools.copyDirToSDCardFromAsserts(this, Constant.LUA_NAME, "Nagrand/lua");
    } else {
      Toast.makeText(this, "未找到SDCard", Toast.LENGTH_LONG).show();
    }
  }




}
