package com.example.administrator.refreshandloadmorerecyclerview.loadMore;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.administrator.refreshandloadmorerecyclerview.util.CustomGridLayoutManager;
import com.example.administrator.refreshandloadmorerecyclerview.util.CustomLinearLayoutManager;


/**
 * Created by Administrator on 2017/3/24.
 * 按照原作者所论述的思想，下拉刷新和加载更多不是一个层级的view
 * 该加载更多的view是属于recyclerView的footView，根据滑动的位置和逻辑进行显示和隐藏，由于定制了接口，所以可以自定义；
 */

public class LoadMoreRecyclerView extends RecyclerView {
    //加载更多的几种状态 未加载状态、记载中、完成、没有更多数据
    public enum loadMoreState {
        NORMAL, LOADING, COMPLETE, DONE
    }

    private OnScrollListener listener;      //向外暴露的滑动监听
    private OnLayoutChangedListener listener2;  //向外暴露的更新数据后监听
    private OnHandleFooterListener onHandleFooterListener; //处理adapter的footer

    private LMUIhandler mUIhandler;     //加载更多UI接口
    private LMHandler mHandler;         //加载更多功能接口

    private loadMoreState mState = loadMoreState.NORMAL;    //用于记录当前状态，默认未加载

    private int lastVisibleItemPosition;    //最后一个课间的item位置
    private int totalItemCount, visibleItemCount;   //所有的item数目，可见的item数目
    private boolean isLoadMoreEnable = true;    //是否允许加载更多
    private boolean isHaveEmptyView = false;    //是否有空状态
    private boolean isSpecial = false;  //是否是特殊情况（展示空图片或不满一屏,页面错误）

    public LoadMoreRecyclerView(Context context) {
        super(context);
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (isSpecial) return; //如果是特殊情况（展示空图片或不满一屏），不再对滑动进行处理
        if (isLoadMoreEnable && (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_SETTLING)) {
            if (mUIhandler == null || mState == loadMoreState.LOADING) {
                return;
            }
            LayoutManager layoutManager = getLayoutManager();
            getRecyclerViewInfo(layoutManager);
            if (isOnRecyclerViewBottom()) {
                startLoadMore();
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (listener != null)
            listener.OnScroll(l, t, oldl, oldt);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (listener2 != null) listener2.onChange(changed, l, t, r, b);
        getRecyclerViewInfo(getLayoutManager());
        //需要空界面的时候保留footer
         /*
        totalItemCount为0的情况：初次加载（默认回调一次0）
        totalItemCount为1的情况：没有任何数据，只有一个footer
        此时需要去除footer
         */
//        Log.e("aaa", visibleItemCount + ", " + totalItemCount);
        if (isHaveEmptyView) {
            isSpecial = true;//有空界面就不能加载更多了
            return;
        } else if (totalItemCount > 1) {    //有头布局或有更多数据
            if (visibleItemCount == totalItemCount && findFirstCompletelyVisibleItemPosition() == 0) { //表示不满一屏
                mUIhandler.hide();
                isSpecial = true;
            } else {
                isSpecial = false;
            }
        }
    }

    /**
     * 设置recyclerView能否滑动
     * @param flag
     */
    private void setScroll(boolean flag){
        if (getLayoutManager() instanceof CustomLinearLayoutManager) {
            ((CustomLinearLayoutManager)getLayoutManager()).setScrollEnabled(flag);
        }
        if (getLayoutManager() instanceof CustomGridLayoutManager) {
            ((CustomGridLayoutManager)getLayoutManager()).setScrollEnabled(flag);
        }
    }

    /**
     * 计算获取屏幕item的各种位置信息，为判断做准备
     *
     * @param layoutManager
     */
    public void getRecyclerViewInfo(LayoutManager layoutManager) {
        int[] lastPositions = null;
        totalItemCount = layoutManager.getItemCount();
        if (layoutManager instanceof GridLayoutManager) {
            visibleItemCount = layoutManager.getChildCount();
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            visibleItemCount = layoutManager.getChildCount();
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            if (lastPositions == null)
                lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
            lastVisibleItemPosition = findMax(lastPositions);
        } else {
            throw new RuntimeException(
                    "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
        }
    }

    private int findMax(int[] lastPositions) {
        int max = Integer.MIN_VALUE;
        for (int value : lastPositions) {
            if (value > max)
                max = value;
        }
        return max;
    }

    public int findFirstCompletelyVisibleItemPosition() {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        int firstPosition;
        if (layoutManager instanceof LinearLayoutManager) {
            firstPosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            firstPosition = ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(lastPositions);
            firstPosition = findMin(lastPositions);
        } else {
            throw new RuntimeException(
                    "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
        }
        return firstPosition;
    }

    private static int findMin(int[] lastPositions) {
        int min = Integer.MAX_VALUE;
        for (int value : lastPositions) {
            if (value != RecyclerView.NO_POSITION && value < min)
                min = value;
        }
        return min;
    }

    /**
     * 开始加载更多
     */
    private boolean isMethodLocked;//单线程方法锁，防止回调两次导致的“类线程混乱”
    public void startLoadMore() {
        if (!isMethodLocked) {
            isMethodLocked = true;  //进来就锁上
            if (mUIhandler == null || mState == loadMoreState.LOADING || mState == loadMoreState.DONE) {//如果正在加载，就不再执行
                isMethodLocked = false;
                return;
            }
            if (mState == loadMoreState.NORMAL || mState == loadMoreState.COMPLETE) {//如果是第一次或夹在完成状态，就再次进行加载
                scrollToPosition(totalItemCount - 1);//滑动到底部（防止只出现一半footer的现象）
                mUIhandler.onStateRefreshing();//更换为加载中UI
                mState = loadMoreState.LOADING;//改变状态
                if (mHandler != null) {
                    mHandler.onLoadMoreBegain();//回调加载更多请求
                }
                isMethodLocked = false;
            }
        }

    }

    /**
     * 加载过程中不允许对屏幕的任何操作
     *
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mState == loadMoreState.LOADING) {
            return true;
        }
        return super.onTouchEvent(e);
    }

    /**
     * 判断是否处于recyclerview的底部
     * 最后一个可见的item是最后一条数据
     * 其他判断（如：竖直方向是否可以继续向下滑动）
     *
     * @return
     */
    private boolean isOnRecyclerViewBottom() {
        if (lastVisibleItemPosition >= totalItemCount - 1 || mHandler.checkCanLoadMore(this)) {
            return true;
        }
        return false;
    }

    /**
     * 完成加载，没有更多数据
     */
    public void loadMoreDone() {
        getAdapter().notifyDataSetChanged();
        mState = loadMoreState.DONE;
        mUIhandler.onStateDone();
    }

    /**
     * 加载完成，可设置最少停留时间
     */
    public void loadMoreComplete() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                getAdapter().notifyDataSetChanged();
                mState = loadMoreState.COMPLETE;
            }
        }, 1000);
    }

    /**
     * 重置状态，用于刷新完成之后
     */
    public void resetState(){
        mState = loadMoreState.NORMAL;
    }

    /**
     * 滑动监听
     *
     * @param listener
     */
    public void setOnScrollListener(OnScrollListener listener) {
        this.listener = listener;
    }

    /**
     * 数据更新后重新摆放布局回调
     *
     * @param listener2
     */
    public void setOnLayoutChangedLister(OnLayoutChangedListener listener2) {
        this.listener2 = listener2;
    }

    /**
     * 设置自定义加载更多的view
     *
     * @param mUIhandler
     */
    public void setLoadMoreUIhandler(LMUIhandler mUIhandler) {
        this.mUIhandler = mUIhandler;
    }

    /**
     * 设置时候能加载更多，针对不需要加载更多的情况
     *
     * @param isLoadMoreEnable
     */
    public void setLoadMoreEnable(boolean isLoadMoreEnable) {
        this.isLoadMoreEnable = isLoadMoreEnable;
        if (mUIhandler != null && !isLoadMoreEnable){
            mUIhandler.gone();
        }
    }

    /**
     * 设置footer的监听
     *
     * @param onHandleFooterListener
     */
    public void setOnHandleFooterListener(OnHandleFooterListener onHandleFooterListener) {
        this.onHandleFooterListener = onHandleFooterListener;
    }

    public void setOnError(){
        if (mUIhandler != null) {
            mUIhandler.hide();
        }
        isSpecial = true;
    }

    /**
     * 设置功能性handler--加载更多
     *
     * @param handler
     */
    public void setHandler(LMHandler handler) {
        this.mHandler = handler;
    }

    /**
     * 是否有空状态view，针对带头部的recyclerView列表的空状态，
     * 一般用于空状态和非空状态是切换，二者都需设置
     * @param haveEmptyView
     */
    public void setHaveEmptyView(boolean haveEmptyView) {
        isHaveEmptyView = haveEmptyView;
        if (mUIhandler == null) return;
        if (haveEmptyView) {     //如果没有数据，显示空状态
            mUIhandler.onEmpty();
        } else {                //有数据
            if (isLoadMoreEnable) {     //可以加载更多，不做处理，让onLayout来处理

            } else {            //不能加载更多
                mUIhandler.gone();
            }
        }
    }

    /**
     * 更新数据，针对多层嵌套的adapter（装饰者模式），保证最外层的adpter更新，需手动调用
     */
    public void notifyDataChange() {
        getAdapter().notifyDataSetChanged();
    }

    public interface OnScrollListener {
        void OnScroll(int l, int t, int oldl, int oldt);
    }

    public interface OnLayoutChangedListener {
        void onChange(boolean changed, int l, int t, int r, int b);
    }

    public interface OnHandleFooterListener {
        void onRemoveFooter();

        void onAddFooter();
    }
}
