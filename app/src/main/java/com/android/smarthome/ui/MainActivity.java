package com.android.smarthome.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.android.smarthome.R;
import com.android.smarthome.utils.Utils;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private GridView homeGridView;
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();;
    private SimpleAdapter simpleAdapter;

    private int[] IconRes = { R.mipmap.switch_outlet, R.mipmap.camera};

//    R.mipmap.smart_light,
//    R.mipmap.air_conditioner, R.mipmap.refrigerator, R.mipmap.television,
//    R.mipmap.curtains, R.mipmap.water_heater,

    private String[] iconName = { "开关", "摄像头"};

//    "智能灯", "空调", "冰箱",
//            "电视", "窗帘", "热水器",

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        initData();
    }

    private void initData() {
        // 启动百度push
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(MainActivity.this, "api_key"));
    }

    private void initView() {
        homeGridView = (GridView) findViewById(R.id.home_grid_view);
        setData();

        String[] resFrom = {"image","text"};
        int [] resTo = { R.id.item_image,R.id.item_text };
        simpleAdapter = new SimpleAdapter(this, dataList, R.layout.home_item, resFrom, resTo);
        //配置适配器
        homeGridView.setAdapter(simpleAdapter);

        homeGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"position: " + position);
                switch (position) {
                    case 0://开关
                        Intent switchIntent = new Intent(MainActivity.this, SwitchActivity.class);
                        startActivity(switchIntent);
                        break;
//                    case 1://智能灯
//                        Intent smartLightIntent = new Intent(MainActivity.this, SmartLightActivity.class);
//                        startActivity(smartLightIntent);
//                        break;
//                    case 2://空调
//                        Intent airConditionerIntent = new Intent(MainActivity.this, AirConditionerActivity.class);
//                        startActivity(airConditionerIntent);
//                        break;
//                    case 3://冰箱
//                        Intent refrigeratorIntent = new Intent(MainActivity.this, RefrigeratorActivity.class);
//                        startActivity(refrigeratorIntent);
//                        break;
//                    case 4://电视
//                        Intent televisionIntent = new Intent(MainActivity.this, TelevisionActivity.class);
//                        startActivity(televisionIntent);
//                        break;
//                    case 5://窗帘
//                        Intent curtainsIntent = new Intent(MainActivity.this, CurtainsActivity.class);
//                        startActivity(curtainsIntent);
//                        break;
//                    case 6://热水器
//                        Intent waterHeaterIntent = new Intent(MainActivity.this, WaterHeaterActivity.class);
//                        startActivity(waterHeaterIntent);
//                        break;
                    case 1://摄像头
                        Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                        startActivity(cameraIntent);
                        break;
                    default:
//                        Intent i2ntent = new Intent(HomeActivity.this, DeviceActivity.class);
//                        startActivity(i2ntent);
                        break;
                }
            }
        });
    }

    private void setData() {
        for(int i = 0;i < IconRes.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("image", IconRes[i]);
            map.put("text", iconName[i]);
            dataList.add(map);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
