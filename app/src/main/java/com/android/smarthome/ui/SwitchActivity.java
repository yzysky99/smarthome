package com.android.smarthome.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.smarthome.R;
import com.android.smarthome.device.AddDeviceActivity;
import com.android.smarthome.device.DeviceControlActivity;
import com.android.smarthome.module.DeviceInfo;

import java.util.LinkedList;
import java.util.List;

public class SwitchActivity extends AppCompatActivity {
    private static final String TAG = "Msg.SwitchActivity";

    private static final int REQ_ADD_DEVICE_CODE = 1;
    private ListView mDeviceListView;
    private DeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        initView();
    }

    private void initView() {
        View addDeviceView = View.inflate(this, R.layout.add_device_header, null);

        mDeviceListView = (ListView) findViewById(R.id.device_list);
        mDeviceListView.addHeaderView(addDeviceView, null, false);

        addDeviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(SwitchActivity.this, AddDeviceActivity.class);
                startActivityForResult(addIntent, REQ_ADD_DEVICE_CODE);
            }
        });

        mAdapter = new DeviceAdapter();
        mDeviceListView.setAdapter(mAdapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent deviceControlIntent = new Intent(SwitchActivity.this, DeviceControlActivity.class);
                startActivity(deviceControlIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == REQ_ADD_DEVICE_CODE) {

        }

        Log.d(TAG,"onActivityResult");
        List<DeviceInfo> deviceInfoList = new LinkedList<DeviceInfo>();

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.deviceName = "开关1";
        deviceInfo.deviceIconUrl = "开关1";
        deviceInfoList.add(deviceInfo);

        DeviceInfo deviceInfo1 = new DeviceInfo();
        deviceInfo1.deviceName = "开关2";
        deviceInfo1.deviceIconUrl = "开关2";
        deviceInfoList.add(deviceInfo1);

        DeviceInfo deviceInfo2 = new DeviceInfo();
        deviceInfo2.deviceName = "开关3";
        deviceInfo2.deviceIconUrl = "开关3";
        deviceInfoList.add(deviceInfo2);

        DeviceInfo deviceInfo3 = new DeviceInfo();
        deviceInfo3.deviceName = "开关4";
        deviceInfo3.deviceIconUrl = "开关4";
        deviceInfoList.add(deviceInfo3);

        mAdapter.updateData(deviceInfoList);
        mAdapter.notifyDataSetChanged();
    }


    private class DeviceAdapter extends BaseAdapter {
        private List<DeviceInfo> mDeviceInfos;

        public DeviceAdapter() {
            mDeviceInfos = new LinkedList<DeviceInfo>();
        }

        @Override
        public int getCount() {
            return mDeviceInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceInfos.get(position);
        }

        public void updateData(List<DeviceInfo> deviceInfo) {
            mDeviceInfos.clear();
            if (deviceInfo == null || deviceInfo.size() == 0) {
                return;
            }
            for (DeviceInfo device : deviceInfo) {
                mDeviceInfos.add(device);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceInfo deviceInfo = (DeviceInfo) getItem(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(parent.getContext(), R.layout.add_device_item, null);
                holder.deviceIcon = (ImageView) convertView.findViewById(R.id.device_icon);
                holder.deviceName = (TextView) convertView.findViewById(R.id.deviceName);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.deviceName.setText(deviceInfo.deviceName);
            Log.d(TAG, "position: " + position + ", name: " + holder.deviceName.getText());
            return convertView;
        }

        private class ViewHolder {
            ImageView deviceIcon;
            TextView deviceName;
        }
    }
}
