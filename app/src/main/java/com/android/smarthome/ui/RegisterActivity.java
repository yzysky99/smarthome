package com.android.smarthome.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.smarthome.utils.Constants;
import com.android.smarthome.R;
import com.android.smarthome.service.MinaClientHandler;
import com.android.smarthome.service.MinaMsgResult;
import com.android.smarthome.widget.ToastView;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";
    private EditText mNameView;
    private EditText mPhoneView;
    private EditText mPasswordView;
    private EditText mEmailView;
    private EditText mAddresView;
    private Button mRegister;
    private TextView mLoginView;
    private View mProgressView;

    private SharedPreferences mShared;
    private SharedPreferences.Editor mEditor;

    String mName = "";
    String mPhone = "";
    String mPassword = "";
    String mEmail = "";
    String mAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initData();
    }

    private void initData() {
        mNameView = (EditText) findViewById(R.id.name);
        mPhoneView = (EditText) findViewById(R.id.phone);
        mPasswordView = (EditText) findViewById(R.id.password);
        mEmailView = (EditText) findViewById(R.id.email);
        mAddresView = (EditText) findViewById(R.id.addres);
        mRegister = (Button) findViewById(R.id.register);
        mLoginView = (TextView) findViewById(R.id.login);
        mProgressView = findViewById(R.id.register_progress);

        mShared = getSharedPreferences(Constants.USER_INFO, Context.MODE_WORLD_READABLE);
        mEditor = mShared.edit();

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resigster();
            }
        });

        mLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void resigster() {
        mName = mNameView.getText().toString();
        mPhone = mPhoneView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mEmail = mEmailView.getText().toString();
        mAddress = mAddresView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mName)) {
            Toast toast = Toast.makeText(this, R.string.error_invalid_name, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            focusView = mNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mPassword) && !isPasswordValid(mPassword)) {
            Toast toast = Toast.makeText(this, R.string.error_invalid_password, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mPhone)) {
            Toast toast = Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(mPhone)) {
            Toast toast = Toast.makeText(this, R.string.error_invalid_phone, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            JSONObject jsonObject = new JSONObject();
            try {
                JSONObject appMsg = new JSONObject();
                appMsg.put("cmd", "0");//register
                appMsg.put("name", mName);
                appMsg.put("password", mPassword);
                appMsg.put("mobilePhone", mPhone);
                appMsg.put("email", mEmail);
                appMsg.put("address", mAddress);

                jsonObject.put("msgType", "app");
                jsonObject.put("appMsg", appMsg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendDataToServer(jsonObject.toString());
        }
    }

    private void sendDataToServer(final String appMsg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IoSession session = null;
                IoConnector connector = new NioSocketConnector();
                connector.setConnectTimeoutMillis(30000);
                connector.getFilterChain().addLast("codec",
                        new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),
                                        LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));
                connector.setHandler(new MinaClientHandler(new MinaMsgResult() {
                    @Override
                    public void registerResult(String msg) {
                        Log.d(TAG, "registerResult: " + msg);
                        showProgress(false);
                        if (msg.equals("ok")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastView toast = new ToastView(RegisterActivity.this, getString(R.string.register_success));
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                            });
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG,"start to loging");
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("name", mName);
                            startActivity(intent);
                            finish();
                        } else {
                            mPasswordView.setError(getString(R.string.error_register));
                            mPasswordView.requestFocus();
                        }
                    }

                    @Override
                    public void loginResult(String msg) {

                    }
                }));
                try {
                    ConnectFuture future = connector.connect(new InetSocketAddress(Constants.IP_ADDRESS, Constants.PORT));
                    future.awaitUninterruptibly();
                    session = future.getSession();
                    session.write(appMsg);
                } catch (Exception e) {
                    Log.d(TAG, "connect error: " + e.getMessage());
                }

//                session.getCloseFuture().awaitUninterruptibly();
//                Log.d(TAG,"connector...");
//                connector.dispose();
            }
        }).start();
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() >= 11;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void showProgress(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
