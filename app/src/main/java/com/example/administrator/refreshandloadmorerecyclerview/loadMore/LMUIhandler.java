package com.example.administrator.refreshandloadmorerecyclerview.loadMore;

/**
 * Created by Administrator on 2017/3/24.
 */

public interface LMUIhandler {
    void onStateRefreshing();
    void onStateDone();
    void hide();
    void gone();
    void onEmpty();
}
