package com.example.labwork4;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


class DateWidgetUpdateService extends Service {
    private static String LOG_TAG = "DWUpdateService";
    public DateWidgetUpdateService() {}

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        updateInfoWidget();
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateInfoWidget(){   //Обновление виджета
        Log.d(LOG_TAG, "updateInfoWidget_Start");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(this.getApplicationContext().getPackageName(), DateWidget.class.getName()));
        for (int i = 0; i < ids.length; i++)
            DateWidget.updateWidget(this.getApplicationContext(), appWidgetManager, ids[i]);
        Log.d(LOG_TAG, "updateInfoWidget_End");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}