package com.android.smarthome.device;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.smarthome.R;
import com.android.smarthome.base.EsptouchTask;
import com.android.smarthome.base.IEsptouchListener;
import com.android.smarthome.base.IEsptouchResult;
import com.android.smarthome.base.IEsptouchTask;
import com.android.smarthome.module.WifiInfoUtils;
import com.android.smarthome.task.__IEsptouchTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WifiConfigActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "WifiConfigActivity";
    private String PATH;
    private String FAVOSSIDPATH ="/ESP8266ssid.xml";
    private String FAVOPASSPATH ="/ESP8266pass.xml";

    private TextView mWifiSsidTV;
    private EditText mWifiPwdEdit;
    private Button mStartConfirm;

    private WifiInfoUtils mWifiInfo;

    String mWifiSsidStr = "";
    String mWifiPasswordStr = "";

    private List<String> ssid_list = new ArrayList<String>();
    private List<String> pass_list = new ArrayList<String>();
    private String str_ssid="",str_pass="";
    private String str_ssid_FLAG="",str_pass_FLAG="";
    private int index;

    private Switch mSwitchIsSsidHidden;
    private Spinner mSpinnerTaskCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_config);

        mWifiSsidTV = (TextView) findViewById(R.id.tv_wifi_sssid);
        mWifiPwdEdit = (EditText) findViewById(R.id.wifi_password);
        mStartConfirm = (Button) findViewById(R.id.start_confirm);
        mStartConfirm.setOnClickListener(this);

        mWifiInfo = new WifiInfoUtils(this);
        String wifiSsid = mWifiInfo.getWifiConnectedSsid();
        if (mWifiSsidTV != null) {
            mWifiSsidTV.setText(wifiSsid);
        } else {
            mWifiSsidTV.setText("");
        }

        boolean isSsidEmpty = TextUtils.isEmpty(wifiSsid);
        mStartConfirm.setEnabled(!isSsidEmpty);

        readSSID(FAVOSSIDPATH);
        readPASS(FAVOPASSPATH);

        mSwitchIsSsidHidden = (Switch) findViewById(R.id.switchIsSsidHidden);
        initSpinner();
    }

    private void initSpinner()
    {
        mSpinnerTaskCount = (Spinner) findViewById(R.id.spinnerTaskResultCount);
        int[] spinnerItemsInt = getResources().getIntArray(R.array.taskResultCount);
        int length = spinnerItemsInt.length;
        Integer[] spinnerItemsInteger = new Integer[length];
        for(int i=0;i<length;i++)
        {
            spinnerItemsInteger[i] = spinnerItemsInt[i];
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_list_item_1, spinnerItemsInteger);
        mSpinnerTaskCount.setAdapter(adapter);
        mSpinnerTaskCount.setSelection(1);
    }

    @Override
    protected void onResume() {
        super.onResume();


        index=-1;
        int len = ssid_list.size();
        for(int i=0; i<len; i++){
            if(mWifiSsidStr.equals(ssid_list.get(i))){
                mWifiPwdEdit.setText(pass_list.get(i));
                str_ssid_FLAG = mWifiSsidStr;
                str_pass_FLAG = pass_list.get(i);
                index = i;
                i=len;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view == mStartConfirm) {

            if(mWifiPwdEdit.getText().toString().length()==0){
                new AlertDialog.Builder(this)
                        .setTitle("请输入账号和密码！")
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

            }else{
                String apSsid = mWifiSsidTV.getText().toString();
                String apPassword = mWifiPwdEdit.getText().toString();
                String apBssid = mWifiInfo.getWifiConnectedBssid();
                Boolean isSsidHidden = mSwitchIsSsidHidden.isChecked();
                String isSsidHiddenStr = "NO";
                String taskResultCountStr = Integer.toString(mSpinnerTaskCount
                        .getSelectedItemPosition());
                if (isSsidHidden)
                {
                    isSsidHiddenStr = "YES";
                }
                if (__IEsptouchTask.DEBUG) {
                    Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
                            + ", " + " mEdtApPassword = " + apPassword);
                }

                str_ssid = apSsid;
                str_pass = apPassword;

                new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword,
                        isSsidHiddenStr, taskResultCountStr);
            }

        }
    }


    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = result.getBssid() + "正在连接 wifi中 ...";
                Toast.makeText(WifiConfigActivity.this, text,
                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(WifiConfigActivity.this);
            mProgressDialog
                    .setMessage("正在配置中，请稍等  ...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i(TAG, "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "等待中...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                        isSsidHidden, WifiConfigActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    "Confirm");
            IEsptouchResult firstResult = result.get(0);
            if (!firstResult.isCancelled()) {
//				int count = 0;
//				final int maxDisplayCount = 5;
                if (firstResult.isSuc()) {
                    try {
                        if(str_ssid_FLAG.equals(str_ssid)){
                            if(!str_pass_FLAG.equals(str_pass)){
                                if(str_ssid.length()>0 && str_pass.length()>0 && index!=-1){
                                    ssid_list.set(index, str_ssid);
                                    pass_list.set(index, str_pass);
                                    writeSSID(FAVOSSIDPATH);
                                    writePASS(FAVOPASSPATH);
                                }
                            }
                        }else{
                            if(str_ssid.length()>0 && str_pass.length()>0){
                                ssid_list.add(str_ssid);
                                pass_list.add(str_pass);
                                writeSSID(FAVOSSIDPATH);
                                writePASS(FAVOPASSPATH);
                            }
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }



                    new AlertDialog.Builder(WifiConfigActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("提示")
                            .setMessage("配置成功!")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    dialog.dismiss();
                                    finish();
                                }
                            }).show();
                } else {

                    str_ssid = null;
                    str_pass = null;

                    new AlertDialog.Builder(WifiConfigActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("提示")
                            .setMessage("配置失败，请重试!")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    private void readSSID(String res){
        PATH =this.getFilesDir().getPath();
        String path =PATH + res;

        File dirFile=new File(PATH);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }

        String str=null;

        if(!ssid_list.isEmpty()){
            ssid_list.clear();
        }
        try {
            FileInputStream fis =new  FileInputStream (path);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            while( (str=in.readLine()) != null ){
                ssid_list.add(Uri.decode(str));
            }
            in.close();
        }
        catch(FileNotFoundException e){
        }
        catch(Exception e){
        }

    }

    private void readPASS(String res){
        PATH =this.getFilesDir().getPath();
        String path =PATH + res;

        File dirFile=new File(PATH);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }

        String str=null;

        if(!pass_list.isEmpty()){
            pass_list.clear();
        }
        try {
            FileInputStream fis =new  FileInputStream (path);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            while( (str=in.readLine()) != null ){
                pass_list.add(Uri.decode(str));
            }
            in.close();
        }
        catch(FileNotFoundException e){
        }
        catch(Exception e){
        }

    }


    private void writeSSID(String res){
        PATH =this.getFilesDir().getPath();
        String path =PATH + res;

        File dirFile=new File(PATH);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }

        int len = ssid_list.size();
        File write = new File(path);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(write));
            for(int i=0; i<len; i++){
                bw.write(Uri.encode(ssid_list.get(i))+"\r\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void writePASS(String res){
        PATH =this.getFilesDir().getPath();
        String path =PATH + res;

        File dirFile=new File(PATH);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }

        int len = pass_list.size();
        File write = new File(path);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(write));
            for(int i=0; i<len; i++){
                bw.write(Uri.encode(pass_list.get(i))+"\r\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
