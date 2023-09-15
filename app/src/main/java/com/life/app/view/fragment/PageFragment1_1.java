package com.life.app.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.life.router.annotation.RequestMapping;
import com.life.app.R;
import com.life.router.manager.RedirectThreadManager;

@RequestMapping(url = "/index_2/demo_fragment_1/demo_fragment_1_1", description = "第二子页的第一子页")
public class PageFragment1_1 extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            //防止白页
            view = inflater.inflate(R.layout.fragment_1_1, container, false);
        }
        Log.d("PageFragment1_1", "onCreateView...");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String name = this.getClass().getName();
        Log.d("Fragment_1_1", name);
        RedirectThreadManager.getInstance().trigger(this.getClass().getName());

    }

    @Override
    public void onDestroyView() {
        Log.d("PageFragment1_1", "onDestroyView...");
        super.onDestroyView();
    }
}
