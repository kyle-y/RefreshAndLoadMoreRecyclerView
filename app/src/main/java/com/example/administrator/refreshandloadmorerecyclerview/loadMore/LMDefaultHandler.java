package com.example.administrator.refreshandloadmorerecyclerview.loadMore;

import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2017/3/24.
 */

public abstract class LMDefaultHandler implements LMHandler{
    private boolean canScrollUp(View view){
        if (view instanceof AbsListView) {
            AbsListView absListView = (AbsListView) view;
            return view.canScrollVertically(1)
                    || absListView.getLastVisiblePosition() != absListView.getAdapter().getCount() - 1;
        } else if (view instanceof WebView) {
            WebView webview = (WebView) view;
            return view.canScrollVertically(1)
                    || webview.getContentHeight() * webview.getScale() != webview.getHeight() + webview.getScrollY();
        } else if (view instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) view;
            View childView = scrollView.getChildAt(0);
            if (childView != null) {
                return view.canScrollVertically(1)
                        || scrollView.getScrollY() != childView.getHeight()
                        - scrollView.getHeight();
            }
        } else {
            return view.canScrollVertically(1);
        }
        return true;
    }

    @Override
    public boolean checkCanLoadMore(View view) {
        return !canScrollUp(view);
    }
}
