package mchenys.net.csdn.blog.refreshlistview.refreshlistview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by mChenys on 2016/12/20.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {
    private static final String TAG = "RefreshListView";
    private RefreshHeaderView mHeaderView;
    private RefreshFooterView mFooterView;
    private int mDownY;//ListView按下时的y坐标
    private boolean isPullingUp;//标记当前是否是向上滚动滑动
    private OnScrollListener mOnScrollListener;

    //下拉刷新和滚动加载的监听回调方法
    public interface OnRefreshListener {
        //下拉刷新
        void onRefresh();

        //滚动加载更多
        void onLoadMore();
    }

    private OnRefreshListener mOnRefreshListener;

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }

    public RefreshListView(Context context) {
        this(context, null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSelector(new ColorDrawable());
        setCacheColorHint(Color.TRANSPARENT);
        mHeaderView = new RefreshHeaderView(getContext());
        mFooterView = new RefreshFooterView(getContext());
        addHeaderView(mHeaderView);
        addFooterView(mFooterView);
        super.setOnScrollListener(this);
    }

    /**
     * 通过重写onTouchEvent来实现HeaderView的显示和状态切换
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mHeaderView.isRefreshing() || mFooterView.isLoadingMore()) {
                    break;
                }
                int deltaY = (int) (ev.getY() - mDownY);
                isPullingUp = deltaY < 0;
                if (deltaY > 0 && getFirstVisiblePosition() == 0) {
                    mHeaderView.updateVisiableHeightByScroll(deltaY);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                //up事件需要处理当前为下拉刷新和正在刷新的2种情况
                if (mHeaderView.isPullToRefresh()) {
                    mHeaderView.fullyHideSmooth();
                } else if (mHeaderView.isReleaseToRefresh()) {
                    mHeaderView.fullyShow();
                    mHeaderView.setState(RefreshHeaderView.STATE_REFRESHING);
                    //同时需要通知调用者去处理刷新逻辑
                    if (null != mOnRefreshListener) {
                        mOnRefreshListener.onRefresh();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 下拉刷新/加载更多成功后需要重置状态,由调用者调用,注意:必须要在UI线程调用
     *
     * @param success true表示下拉刷新成功,将会保存刷新的时间
     */
    public void onRefreshComplete(final boolean success) {
        if (success) {
            mHeaderView.saveRefreshTime();
        }
        mHeaderView.fullyHideSmooth();
        mFooterView.fullyHide();
        Log.d(TAG, "下拉或加载更多结束");

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (null != mOnScrollListener) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
        switch (scrollState) {
            case SCROLL_STATE_IDLE://闲置状态，就是手指松开
                if (getLastVisiblePosition() == getCount() - 1 && isPullingUp && mFooterView.isReleaseMore()) {
                    mFooterView.setState(RefreshFooterView.STATE_MORE_LOADING);
                    setSelection(getCount());//让listview最后一条显示出来
                    //通知调用者去处理加载更多的逻辑
                    if (null != mOnRefreshListener) {
                        mOnRefreshListener.onLoadMore();
                    }
                }
                break;
            default: //其他状态
                if (getLastVisiblePosition() == getCount() - 1 && !mFooterView.isLoadingMore() && isPullingUp) {
                    if (!mFooterView.isNoMoreData()) {
                        //如果isNoMoreData = false 表示还有更多的数据,显示"释放后加载更多",否则则显示默认的"没有更多数据了"
                        mFooterView.setState(RefreshFooterView.STATE_RELEASE_MORE);
                    } else if (mFooterView.isNoMoreData()) {
                        mFooterView.setState(RefreshFooterView.STATE_NO_MORE);
                    }
                }
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (null != mOnScrollListener) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.mOnScrollListener = l;
    }

    /**
     * 没有更多数据可加载时,由调用者调用.
     */
    public void onNoMoreData() {
        mFooterView.setState(RefreshFooterView.STATE_NO_MORE);
    }

    /**
     * 加载更多失败,由调用者调用
     */
    public void onLoadMoreFailure() {
        mFooterView.setState(RefreshFooterView.STATE_MORE_FAILURE);
    }

    public void setSupportClickReload(boolean supportClickReload) {
        mFooterView.setSupportClickReload(supportClickReload);
    }
}
