package com.life.app.view;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.life.annotation.RequestMapping;
import com.life.app.R;
import com.life.app.view.fragment.PageFragment1;
import com.life.app.view.fragment.PageFragment2;
import com.life.router.Router;



@SuppressLint("NonConstantResourceId")
@RequestMapping(url = "/index", description = "默认主页")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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