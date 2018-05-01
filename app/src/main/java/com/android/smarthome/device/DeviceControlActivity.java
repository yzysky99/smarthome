package com.android.smarthome.device;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.smarthome.R;

public class DeviceControlActivity extends AppCompatActivity {

    TextView switchStatText;
    TextView subscribeTimeText;
    ImageButton switchBtn;
    ImageButton switchSubscribeBtn;
    int year = 2017;
    int month = 8;
    int day = 30;
    int hour = 12;
    int minute = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        initView();
        initData();
    }

    private void initView() {
        switchStatText = (TextView) findViewById(R.id.switch_state);
        subscribeTimeText = (TextView) findViewById(R.id.subscribe_time);
        switchBtn = (ImageButton) findViewById(R.id.switch_btn);
        switchSubscribeBtn = (ImageButton) findViewById(R.id.switch_subscribe);
    }

    int flag = 0;
    private void initData() {
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag == 0) {
                    switchStatText.setText(R.string.switch_off);
                    switchBtn.setBackgroundResource(R.mipmap.switch_off);
                    flag = 1;
                } else {
                    switchStatText.setText(R.string.switch_on);
                    switchBtn.setBackgroundResource(R.mipmap.switch_on);
                    flag = 0;
                }
            }
        });

        switchSubscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(view);
            }
        });
    }

    public void getDate(View v) {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                DeviceControlActivity.this.year = year;
                DeviceControlActivity.this.month = monthOfYear;
                DeviceControlActivity.this.day = dayOfMonth;
                getTime(view);
            }
        }, 2017, 8, 30).show();
    }

    public void getTime(View v) {
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                DeviceControlActivity.this.hour = hourOfDay;
                DeviceControlActivity.this.minute = minute;
                subscribeTimeText.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute);
            }
        }, 12, 20, true).show();
    }
}
