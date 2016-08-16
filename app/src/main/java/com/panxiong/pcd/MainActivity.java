package com.panxiong.pcd;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.panxiong.calendar.PromptCalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PromptCalendar mPromptCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPromptCalendar = (PromptCalendar) findViewById(R.id.promptCalendar);

        mPromptCalendar.setCalendarInterface(new PromptCalendar.PromptCalendarInterface() {
            @Override
            public void onSelectDate(Integer years, Integer month, Integer day) {
                String dateString = years + "-" + (month < 10 ? "0" + month : month) + "-" + day;
                Toast.makeText(MainActivity.this, dateString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public List<String> getPromptDateList(Integer years, Integer month) {
                List<String> dateList = new ArrayList<String>();
                dateList.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                return dateList; // 返回提醒日期集合
            }

            @Override
            public String getTagDate() {
                return super.getTagDate();  // 突出显示今天
            }
        });
    }
}
