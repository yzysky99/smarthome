package com.android.smarthome.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.smarthome.Constants;
import com.android.smarthome.network.TcpClient;
import com.android.smarthome.service.MinaClientHandler;
import com.android.smarthome.widget.ToastView;
import com.android.smarthome.R;

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

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";

    private AutoCompleteTextView mNameView;
    private EditText mPasswordView;
    private View mProgressView;

    private SharedPreferences mShared;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mNameView = (AutoCompleteTextView) findViewById(R.id.name);
        String name = getIntent().getStringExtra("name");
        if(name != null && !name.isEmpty()) {
            mNameView.setText(name);
        }

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextView registerTv = (TextView) findViewById(R.id.register);
        registerTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        registerTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mProgressView = findViewById(R.id.login_progress);

        mShared = getSharedPreferences(Constants.USER_INFO, Context.MODE_WORLD_READABLE);
        mEditor = mShared.edit();

        boolean isLogin = mShared.getBoolean(Constants.UserInfo.IS_LOGIN, false);
        Log.d(TAG,"isLogin: " + isLogin);
        if (isLogin) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            mEditor.putBoolean(Constants.UserInfo.IS_LOGIN, false);
            mEditor.commit();
        }
    }

    private void attemptLogin() {
        String name = mNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            Toast toast = Toast.makeText(this, R.string.error_invalid_password, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            Toast toast = Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            login(name, password);
        }
    }

    private void login(String name, String password) {
        Log.d(TAG,"login...");
        final JSONObject jsonObject = new JSONObject();
        try {
            JSONObject appMsg = new JSONObject();
            appMsg.put("cmd", "1");//login
            appMsg.put("name", name);
            appMsg.put("password", password);
//            appMsg.put("phone", password);

            jsonObject.put("msgType", "app");
            jsonObject.put("appMsg", appMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mEditor.putBoolean(Constants.UserInfo.IS_LOGIN, true);
        mEditor.commit();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

        new Thread(new Runnable() {
            @Override
            public void run() {
                TcpClient client = new TcpClient();
                client.sendData(jsonObject.toString(), new TcpClient.IDataRecvListener() {
                    @Override
                    public int onDataRecv(String data) {
                        Log.d(TAG,"recv data: " + data);
                        showProgress(false);
                        if (data.equals("ok")) {
                            mEditor.putBoolean(Constants.UserInfo.IS_LOGIN, true);
                            mEditor.commit();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastView toast = new ToastView(LoginActivity.this, getString(R.string.login_failed));
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                            });

                        }
                        return 0;
                    }
                });
            }
        }).start();

    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void CloseKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
    }

    private void showProgress(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

