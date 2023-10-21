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

@RequestMapping(url = "/index_2/demo_fragment_2", description = "默认子页")
public class PageFragment2 extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_2, container, false);
        Log.d("Fragment_2", "onCreateView...");
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onDestroyView() {
        Log.d("Fragment_2", "onDestroyView...");
        super.onDestroyView();
    }
}
