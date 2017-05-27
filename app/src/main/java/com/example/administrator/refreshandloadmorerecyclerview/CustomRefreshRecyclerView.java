package com.example.administrator.refreshandloadmorerecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.example.administrator.refreshandloadmorerecyclerview.loadMore.LMDefaultHandler;
import com.example.administrator.refreshandloadmorerecyclerview.loadMore.LMUIhandler;
import com.example.administrator.refreshandloadmorerecyclerview.loadMore.LoadMoreRecyclerView;
import com.example.administrator.refreshandloadmorerecyclerview.refresh.nucle.PtrDefaultHandler;
import com.example.administrator.refreshandloadmorerecyclerview.refresh.nucle.PtrFrameLayout;
import com.example.administrator.refreshandloadmorerecyclerview.refresh.nucle.PtrUIHandler;


/**
 * Created by Administrator on 2017/3/24.
 * 将refresh和loadMore结合起来的一个刷新控件，针对recyclerView
 */

public class CustomRefreshRecyclerView extends PtrFrameLayout {

    private PtrUIHandler header;
    private LMUIhandler footer;
    private LoadMoreRecyclerView contentView;

    private IRefresh listener;

    public CustomRefreshRecyclerView(Context context) {
        super(context);
        init();
    }

    public CustomRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomRefreshRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        contentView = new LoadMoreRecyclerView(getContext());
        addView(contentView);
        setContentViewLayoutParams(true, true);

        setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                if (listener != null)
                    listener.refresh();
            }
        });
        contentView.setHandler(new LMDefaultHandler() {
            @Override
            public void onLoadMoreBegain() {
                if (listener != null)
                    listener.loadMore();
            }
        });
    }

    private void setContentViewLayoutParams(boolean isHeightMatchParent,
                                            boolean isWidthMatchParent) {
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) contentView.getLayoutParams();
        if (isHeightMatchParent) {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        if (isWidthMatchParent) {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        // 默认设置宽高为match_parent
        contentView.setLayoutParams(lp);
    }


    public void addLoadMoreUIhandler(LMUIhandler footer){
        this.footer = footer;
        contentView.setLoadMoreUIhandler(footer);
    }

    public void setOnRefreshListener(IRefresh listener) {
        this.listener = listener;
    }

    public PtrUIHandler getHeader() {
        return header;
    }

    public LMUIhandler getFooter() {
        return footer;
    }

    @Override
    public LoadMoreRecyclerView getContentView() {
        return contentView;
    }

    public void setLoadMoreEnable(boolean loadMoreEnable) {
        contentView.setLoadMoreEnable(loadMoreEnable);
    }

    @Override
    public void refreshComplete() {
        super.refreshComplete();
        getContentView().resetState();
        getContentView().getAdapter().notifyDataSetChanged();
    }

    public interface IRefresh {
        void refresh();

        void loadMore();
    }
}
