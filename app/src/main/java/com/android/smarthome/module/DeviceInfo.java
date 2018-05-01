package com.android.smarthome.module;

/**
 * Created by stevyang on 17/6/21.
 */

public class DeviceInfo {
    public String deviceName;//设备名称
    public String deviceType; //灯，空调，冰箱，电视等
    public String deviceId; //设备厂家id
    public String deviceKey; //设备key值，唯一（设备唯一）
    public String deviceBrandName; //厂家名称
    public String deviceConnProtol; //wifi or ble
    public String deviceIconUrl;
    public String deviceDesc;
    private boolean mHasAdd;
}
