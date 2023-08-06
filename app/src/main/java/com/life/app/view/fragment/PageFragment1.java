package com.life.app.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.life.annotation.RequestMapping;
import com.life.app.R;

@RequestMapping(url = "/index_2/demo_fragment", description = "第二子页")
public class PageFragment1 extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            //防止白页
            view = inflater.inflate(R.layout.fragment_1, container, false);
        }
        Log.d("Fragment_1", "onCreateView...");
        return view;
    }
}
