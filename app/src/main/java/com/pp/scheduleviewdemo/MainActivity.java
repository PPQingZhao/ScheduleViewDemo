package com.pp.scheduleviewdemo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.pp.scheduleviewdemo.widget.ScheduleExcelView;

public class MainActivity extends AppCompatActivity {

    private ScheduleExcelView mSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setupScheduleView();
    }

    private void initView() {
        mSchedule = findViewById(R.id.main_schedule);
    }

    private void setupScheduleView() {
        // 设置横向标题 (日期:总共7列 S, M, T, W, T, F, S)
        String[] week = getResources().getStringArray(R.array.Week);
        mSchedule.clearColumTitle();
        for (String weekDay : week) {
            mSchedule.addColumTitle(weekDay);
        }
        // 设置纵向标题 (时间： 总共48行,00:00, 00:30, 01:00, ...)
        mSchedule.clearRowTitle();
        String[] amHour = getResources().getStringArray(R.array.AMHalfHour);
        for (int i = 0; i < amHour.length; i++) {
            String hour = amHour[i];
            ScheduleExcelView.ExcelTitle excelTitle = mSchedule.newTitle()
                    .setTitle(hour);
            if (i == 0) {
                excelTitle.setTextSize(8)
                        .setTextColor(R.color.colorPrimaryDark)
                        .setDivider(true)
                        .setDividerColor(R.color.colorPrimaryDark);
            }
            mSchedule.addRowTitle(excelTitle);
        }
        String[] pmHour = getResources().getStringArray(R.array.PMHalfHour);
        for (int i = 0; i < pmHour.length; i++) {
            String hour = pmHour[i];
            ScheduleExcelView.ExcelTitle excelTitle = mSchedule.newTitle()
                    .setTitle(hour);
            if (i == 0) {
                excelTitle.setTextSize(8)
                        .setTextColor(R.color.colorPrimaryDark).setDivider(true)
                        .setDividerColor(R.color.colorPrimaryDark);
            }
            mSchedule.addRowTitle(excelTitle);
        }

        // 设置单元格大小
        mSchedule.setSpanSize(new ScheduleExcelView.SpanSize() {
            @Override
            public float getSpanWidth(ScheduleExcelView view, int columCount) {
                return view.getExcelWidth() / columCount;
            }

            @Override
            public float getSpanHeight(ScheduleExcelView view, int rowCount) {
                return view.getExcelHeight() / (view.getSpanRowCount() * 0.5f + 1);
            }
        });
        mSchedule.setScheduleColor(Color.BLUE);
        mSchedule.commit();

        // 计划变化监听
        mSchedule.setOnScheduleChangeListener(new ScheduleExcelView.OnScheduleChangeListener() {
            @Override
            public void onScheduleChange(int[][] schedule) {
            }
        });
    }

}
