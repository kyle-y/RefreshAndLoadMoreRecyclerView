package com.example.administrator.refreshandloadmorerecyclerview.loadMore;

import android.view.View;

/**
 * Created by Administrator on 2017/3/24.
 */

public interface LMHandler {
    void onLoadMoreBegain();
    boolean checkCanLoadMore(View view);
}
