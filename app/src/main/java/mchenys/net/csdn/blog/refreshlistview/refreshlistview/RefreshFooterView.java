package mchenys.net.csdn.blog.refreshlistview.refreshlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import mchenys.net.csdn.blog.refreshlistview.R;

/**
 * Created by mChenys on 2016/12/20.
 */
public class RefreshFooterView extends FrameLayout {
    private static final String TAG = "RefreshFooterView";
    //ListView底部的4种状态
    public static final int STATE_NO_MORE = -1; //暂时只有那么多数据
    public static final int STATE_RELEASE_MORE = -2;//释放后加载更多
    public static final int STATE_MORE_LOADING = -3;//正在加载更多
    public static final int STATE_MORE_FAILURE = -4;//加载更多失败
    public static final int STATE_NORMAL = -5;//默认状态是隐藏
    private int mState;

    private View mFooterView; //整个加载更多布局
    private ProgressBar mPbMore; //加载更多的加载圈
    private TextView mTvMoreTip;//加载更多的提示
    private boolean supportClickReload; //是否支持重新加载更多

    public RefreshFooterView(Context context) {
        this(context, null);
    }

    public RefreshFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mFooterView = View.inflate(getContext(), R.layout.layout_footer, null);
        mPbMore = (ProgressBar) mFooterView.findViewById(R.id.pb_load_more);
        mTvMoreTip = (TextView) mFooterView.findViewById(R.id.tv_more_tip);
        setState(STATE_NORMAL);
        addView(mFooterView);
    }

    /**
     * 通过状态刷新FootView的状态
     */
    public void setState(int state) {
        switch (state) {
            case STATE_NO_MORE: //没有更多数据
                mTvMoreTip.setText("没有更多数据了");
                mFooterView.setVisibility(View.VISIBLE);
                mPbMore.setVisibility(View.GONE);
                Log.d(TAG, "没有更多数据了");
                break;
            case STATE_RELEASE_MORE: //释放后加载更多
                mTvMoreTip.setText("释放后加载更多");
                mFooterView.setVisibility(View.VISIBLE);
                mPbMore.setVisibility(View.GONE);
                Log.d(TAG, "释放后加载更多");
                break;
            case STATE_MORE_LOADING://正在加载更多
                mTvMoreTip.setText("正在加载更多...");
                mFooterView.setVisibility(View.VISIBLE);
                mPbMore.setVisibility(View.VISIBLE);
                Log.d(TAG, "正在加载更多...");

                break;
            case STATE_MORE_FAILURE: //加载更多失败
                if (supportClickReload) {
                    mTvMoreTip.setText("加载失败,重新加载");
                    mFooterView.setVisibility(View.VISIBLE);
                    mPbMore.setVisibility(View.GONE);
                } else {
                    mFooterView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "获取更多数据失败", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "加载失败,重新加载");
                break;
            case STATE_NORMAL:
                mFooterView.setVisibility(View.GONE);
                break;
        }
        this.mState = state;
    }

    public void setSupportClickReload(boolean supportClickReload) {
        this.supportClickReload = supportClickReload;
    }

    public boolean isLoadMoreFailure() {
        return mState == STATE_MORE_FAILURE;
    }

    public boolean isReleaseMore() {
        return mState == STATE_RELEASE_MORE;
    }

    public boolean isNoMoreData() {
        return mState == STATE_NO_MORE;
    }

    public boolean isLoadingMore() {
        return mState == STATE_MORE_LOADING;
    }

    public void fullyHide() {
        setState(RefreshFooterView.STATE_NORMAL);
    }
}
