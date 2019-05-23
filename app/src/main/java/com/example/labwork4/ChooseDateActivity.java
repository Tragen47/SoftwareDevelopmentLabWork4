package com.example.labwork4;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChooseDateActivity extends Activity {

    Activity contextActivity;
    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    static String curDate;

    final String LOG_TAG = "myLogs";
    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_TEXT = "widget_text_";
    public final static String WIDGET_COLOR = "widget_color_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate config");

        // извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        // отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.choose_date_activity_layout);
        Button button = findViewById(R.id.choose_button);
        contextActivity = this;
        final CalendarView calendar = findViewById(R.id.calendarView);
        DateWidget.getDate(this, widgetID);
        if (DateWidget.curDate != null && !DateWidget.curDate.equals("")){
            Calendar calendarW = Calendar.getInstance();
            calendarW.set(Calendar.YEAR,  DateWidget.year);
            calendarW.set(Calendar.MONTH,  DateWidget.month-1);
            calendarW.set(Calendar.DAY_OF_MONTH,  DateWidget.dayOfMonth);
            long milliTime = calendarW.getTimeInMillis();
            calendar.setDate(milliTime);
        }

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                long curLongDate =  Calendar.getInstance().getTimeInMillis();
                Calendar calendarW = Calendar.getInstance();
                calendarW.set(Calendar.YEAR,  year);
                calendarW.set(Calendar.MONTH,  month);
                calendarW.set(Calendar.DAY_OF_MONTH,  dayOfMonth);
                long differ = calendarW.getTimeInMillis() - curLongDate;
                if (differ < 0){
                    Toast toast = Toast.makeText(contextActivity, "Вы не можете выбрать дату, которая раньше текущей", Toast.LENGTH_LONG);
                    toast.show();
                    String date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
                    String[] array = date.split("/");
                    Calendar calendarNow = Calendar.getInstance();
                    year = Integer.valueOf(array[2]);
                    month = Integer.valueOf(array[1])-1;
                    dayOfMonth = Integer.valueOf(array[0]);
                    calendarNow.set(Calendar.YEAR,  year);
                    calendarNow.set(Calendar.MONTH,  month);
                    calendarNow.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    long milliTime = calendarNow.getTimeInMillis();
                    calendar.setDate(milliTime);
                }
                curDate = String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year);
                DateWidget.curDate = curDate;
                DateWidget.dayOfMonth = dayOfMonth;
                DateWidget.month = month + 1;
                DateWidget.year = year;
                DateWidget.started = 0;
                DBHelper dbHelper = new DBHelper(contextActivity);
                dbHelper.setDate(dayOfMonth, month + 1, year, 0, widgetID);
                long days = differ / (24 * 60 * 60 * 1000);
                DateWidget.days = days;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(LOG_TAG, curDate);

                // Записываем значения с экрана в Preferences
                SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(WIDGET_TEXT + widgetID, Long.toString(DateWidget.days));
                editor.commit();

                // положительный ответ
                setResult(RESULT_OK, resultValue);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(contextActivity);
                DateWidget.updateWidget(contextActivity, appWidgetManager, widgetID);
                finish();
            }
        });
    }
}
