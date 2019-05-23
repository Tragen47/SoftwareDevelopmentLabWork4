package com.example.labwork4;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class DateWidget extends AppWidgetProvider {
    final static String LOG_TAG = "myLogs_DateWidget";
    static String curDate = "";
    static int year, month, dayOfMonth, started;
    static long days;
    static int counter = 0;
    private PendingIntent service = null;
    private Timer myTimer = new Timer(); //Создание локального экземляра таймера для обновления
    //Context contextActivity;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        Log.d(LOG_TAG, "onEnabled");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //context.deleteDatabase("DateDB");
       // contextActivity = context;
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
        for (int id : appWidgetIds) {
            startTimer(context, appWidgetManager, appWidgetIds); //Начало работы таймера, внутри метода устанавливается период обновления
            updateWidget(context, appWidgetManager, id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
        // Удаляем Preferences
        DBHelper dbHelper = new DBHelper(context);
        stopTimer(); //Остановка таймера
        SharedPreferences.Editor editor = context.getSharedPreferences(
                ChooseDateActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            dbHelper.deleteDate(widgetID);
            editor.remove(ChooseDateActivity.WIDGET_TEXT + widgetID);
            editor.remove(ChooseDateActivity.WIDGET_COLOR + widgetID);
        }
        editor.commit();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
        context.deleteDatabase("DateDB");
        stopTimer(); //Остановка таймера
    }

    private void startTimer(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < appWidgetIds.length; i++)
                    updateWidget(context, appWidgetManager, appWidgetIds[i]);
            }
        }, 0, 60000);
    }

    private void stopTimer() {
        myTimer.cancel();
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {
        Log.d(LOG_TAG, "updateWidget " + widgetID);

        SharedPreferences sp = context.getSharedPreferences(
                ChooseDateActivity.WIDGET_PREF, Context.MODE_PRIVATE);

        // Читаем параметры Preferences
        String widgetText = sp.getString( ChooseDateActivity.WIDGET_TEXT + widgetID, null);
        if (widgetText == null) return;
        // Настраиваем внешний вид виджета
        RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                R.layout.widget);
        getDate(context, widgetID);
        long days = getDaysDiff(context, dayOfMonth, month, year);
        if (days == -2){
            DateWidget.days = 0;
            widgetView.setTextViewText(R.id.textView, "Менее часа до конца света!!!");
        }
        else
        if (days == -1){
            widgetView.setTextViewText(R.id.textView, "Конец света произошёл!!!");
            if (started == 0) {
                notificationEventStarted(context, widgetID);
                started = 1;
                DBHelper dbHelper = new DBHelper(context);
                dbHelper.setStarted(started, widgetID);
            }
        }else
        if (days == 0){
            DateWidget.days = 0;
            widgetView.setTextViewText(R.id.textView, "Несколько часов до конца света!!!");
        }else
        if (days > 0){
            DateWidget.days = days;
            widgetView.setTextViewText(R.id.textView, widgetText + " дней до конца света!!!");
            counter++;
        }

        //widgetView.setInt(R.id.textView, "setBackgroundColor", widgetColor);
        Intent configIntent = new Intent(context, ChooseDateActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetID,
                configIntent, 0);

        //widgetView.setOnClickPendingIntent(R.id.textView, pIntent);
        widgetView.setOnClickPendingIntent(R.id.r_layout, pIntent);

        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
        Log.d(LOG_TAG, curDate);
    }

    public static void getDate(Context context, int widgetID){
        if (context != null) {
            DBHelper dbHelper = new DBHelper(context);
            Integer[] date = dbHelper.getDate(widgetID);
            if (date != null) {
                Log.d(LOG_TAG, "ALL_RIGHT");
                dayOfMonth = date[0];
                month = date[1];
                year = date[2];
                started = date[3];
                curDate = dayOfMonth + "/" + month + "/" + year;
            } else
                Log.d(LOG_TAG, "NOOOOOOOOOOOO");
        }
    }

    public static long getDaysDiff(Context context, int dayOfMonth, int month, int year){
        String date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        String[] array = date.split("/");
        if (year > Integer.valueOf(array[2])) {
            return computeDays(dayOfMonth, month, year);
        }else {
            if (year == Integer.valueOf(array[2])) {
                if (month > Integer.valueOf(array[1])) {
                    return computeDays(dayOfMonth, month, year);
                } else {
                    if (month == Integer.valueOf(array[1])) {
                        if (dayOfMonth > Integer.valueOf(array[0])) {
                            return computeDays(dayOfMonth, month, year);
                        }else
                        if (dayOfMonth == Integer.valueOf(array[0])){
                            String time = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                            String hours = time.split(":")[0];
                            long hoursLong = Long.valueOf(hours);
                            Log.d(LOG_TAG, "HOURS = " + Long.toString(hoursLong));
                            if (hoursLong >= 9)
                                return -1;
                            else
                            if (hoursLong == 8)
                                return -2;
                            else
                                return 0;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private static long computeDays( int dayOfMonthNew, int monthNew, int yearNew){
        long days = computeDiffer(dayOfMonthNew, monthNew, yearNew) / (24 * 60 * 60 * 1000);
        return days;  // вернуть разницу в днях
    }

    private static long computeDiffer(int dayOfMonth, int month, int year){
        long curLongDate =  Calendar.getInstance().getTimeInMillis();
        Calendar calendarW = Calendar.getInstance();
        calendarW.set(Calendar.YEAR, year);
        calendarW.set(Calendar.MONTH, month);
        calendarW.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        long differ =  (calendarW.getTimeInMillis() - curLongDate);
        return differ;
    }

    public static void notificationEventStarted(Context context, int widgetID){
        final int NOTIFY_ID = 101;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Resources res = context.getResources();
            NotificationChannel notificationChannel = new NotificationChannel("EventStarted", "Событие наступило", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "EventStarted")
                    // обязательные настройки
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                    .setContentTitle("Напоминание")
                    //.setContentText(res.getString(R.string.notifytext))
                    .setContentText("СОБЫТИЕ НАСТУПИЛО = " + Integer.toString(widgetID)) // Текст уведомления
                    // необязательные настройки
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher_background)) // большая
                    // картинка
                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                    .setTicker("БЫСТРЕЕ!")
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true); // автоматически закрыть уведомление после нажатия

            // Альтернативный вариант
            // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFY_ID, builder.build());
        }else {
            Intent notificationIntent = new Intent(context, ChooseDateActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            Resources res = context.getResources();

            // до версии Android 8.0 API 26
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            builder.setContentIntent(contentIntent)
                    // обязательные настройки
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                    .setContentTitle("Напоминание")
                    //.setContentText(res.getString(R.string.notifytext))
                    .setContentText("СОБЫТИЕ НАСТУПИЛО = " + Integer.toString(widgetID)) // Текст уведомления
                    // необязательные настройки
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher_background)) // большая
                    // картинка
                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                    .setTicker("БЫСТРЕЕ!")
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true); // автоматически закрыть уведомление после нажатия

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // Альтернативный вариант
            // Notification
            // ManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFY_ID, builder.build());
        }
    }


}
