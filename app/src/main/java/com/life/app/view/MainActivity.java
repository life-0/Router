package com.life.app.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.life.router.annotation.RequestMapping;
import com.life.app.R;
import com.life.router.Router;


@RequestMapping(url = "/index", description = "默认主页")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button getRequestBtn = findViewById(R.id.redirect_btn);
        getRequestBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d(TAG, "onClick..");
        if (id == R.id.redirect_btn) {

//            Router.getInstance().redirect("/index_2", new Bundle());
            Router.getInstance().redirect("/index_2", new Bundle());
        }
    }

}