package com.example.administrator.refreshandloadmorerecyclerview.refresh.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.refreshandloadmorerecyclerview.R;
import com.example.administrator.refreshandloadmorerecyclerview.loadMore.LMUIhandler;


/**
 * Created by Administrator on 2017/3/25.
 */

public class SimpleFooter extends FrameLayout implements LMUIhandler {
    private ViewGroup footerView;
    private TextView textNoMore;
    private ProgressBar progress;
    private RelativeLayout emptyView;


    public SimpleFooter(Context context) {
        super(context);
        init(context);
    }

    public SimpleFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        footerView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.footer, null, true);
        footerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(footerView);
        textNoMore = (TextView) footerView.findViewById(R.id.textNoMore);
        progress = (ProgressBar) footerView.findViewById(R.id.progress);
        emptyView = (RelativeLayout) footerView.findViewById(R.id.emptyView);
    }


    @Override
    public void onStateRefreshing() {
        emptyView.setVisibility(GONE);
        textNoMore.setVisibility(INVISIBLE);
        progress.setVisibility(VISIBLE);
    }

    @Override
    public void onStateDone() {
        emptyView.setVisibility(GONE);
        textNoMore.setVisibility(VISIBLE);
        progress.setVisibility(INVISIBLE);
    }

    @Override
    public void hide() {
        emptyView.setVisibility(GONE);
        textNoMore.setVisibility(INVISIBLE);
        progress.setVisibility(INVISIBLE);
    }

    @Override
    public void gone() {
        emptyView.setVisibility(GONE);
        textNoMore.setVisibility(GONE);
        progress.setVisibility(GONE);
    }

    @Override
    public void onEmpty() {
        emptyView.setVisibility(VISIBLE);
        textNoMore.setVisibility(INVISIBLE);
        progress.setVisibility(INVISIBLE);
    }

    public View getEmptyView(){
        return emptyView;
    }
}
