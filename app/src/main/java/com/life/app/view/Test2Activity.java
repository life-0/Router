package com.life.app.view;

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

@RequestMapping(url = "/index_2", description = "第二主页", container = R.id.container)
public class Test2Activity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "Test2Activity";
    //声明四个Tab的布局文件
    private LinearLayout mTab1;
    private LinearLayout mTab2;
    PageFragment1 fragment1;
    PageFragment2 fragment2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        Button getRequestBtn = findViewById(R.id.redirect_btn2);
        getRequestBtn.setOnClickListener(this);  // 初始化Fragment
        Button getRequestBtn3 = findViewById(R.id.redirect_btn3);
        getRequestBtn3.setOnClickListener(this);  // 初始化Fragment
        Button getRequestBtn4 = findViewById(R.id.redirect_btn4);
        getRequestBtn4.setOnClickListener(this);  // 初始化Fragment
        fragment1 = new PageFragment1();
        fragment2 = new PageFragment2();

        // 初始显示Fragment1
//        replaceFragment(fragment1);
        mTab1 = findViewById(R.id.id_tab1);
        mTab1.setOnClickListener(this);
        mTab2 = findViewById(R.id.id_tab2);
        mTab2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.redirect_btn2) {
            Router.getInstance().redirect("/index", new Bundle());
        } else if (id == R.id.id_tab1) {
            Log.d(TAG, "onClick..1");
            replaceFragment(fragment1);
        } else if (id == R.id.id_tab2) {
            Log.d(TAG, "onClick..2");
            replaceFragment(fragment2);
        } else if (id == R.id.redirect_btn3) {
            Router.getInstance().redirect("/index_2/demo_fragment_1", new Bundle());
        } else if (id == R.id.redirect_btn4) {
            Router.getInstance().redirect("/index_2/demo_fragment_1/demo_fragment_1_1", new Bundle());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
        Log.d(TAG, "replaceFragment...");
    }
}
