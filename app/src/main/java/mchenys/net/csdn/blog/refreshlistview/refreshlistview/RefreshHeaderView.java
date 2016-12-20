package mchenys.net.csdn.blog.refreshlistview.refreshlistview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import mchenys.net.csdn.blog.refreshlistview.R;

/**
 * Created by mChenys on 2016/12/20.
 */
public class RefreshHeaderView extends FrameLayout {
    private static final String TAG = "RefreshHeaderView";
    //ListView头部的3种状态
    public static final int STATE_PULL_REFRESH = 0; //下拉可刷新
    public static final int STATE_RELEASE_REFRESH = 1;//释放后刷新
    public static final int STATE_REFRESHING = 2;//正在刷新
    private int mState;//当前状态

    private View mHeaderContent;
    private ImageView mIvArrow; //箭头
    private ProgressBar mPbLoading;//下拉刷新的加载圈
    private TextView mTvState;//状态信息
    private TextView mTvTime;//最后一次刷新的时间
    private int mHeaderViewHeight; //头部控件的默认高度
    //箭头滚动的相关动画
    private RotateAnimation mDownAnimation, mUpAnimation;
    private SharedPreferences mSharedPreferences;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //ListView头部的paddingTop的移动因子,避免下拉刷新时移动的范围太大
    private float factor = 0.55f;

    public RefreshHeaderView(Context context) {
        this(context, null);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRotateAnimation();
        initView();
    }

    /**
     * 初始化箭头的滚动动画
     */
    private void initRotateAnimation() {
        //箭头向上动画
        mUpAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mUpAnimation.setDuration(300);
        mUpAnimation.setFillAfter(true);
        //箭头向下动画
        mDownAnimation = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mDownAnimation.setDuration(300);
        mDownAnimation.setFillAfter(true);
    }

    private void initView() {
        mHeaderContent = View.inflate(getContext(), R.layout.layout_header, null);
        addView(mHeaderContent);
        mSharedPreferences = getContext().getSharedPreferences("sp_refresh_time", Context.MODE_PRIVATE);
        mIvArrow = (ImageView) mHeaderContent.findViewById(R.id.iv_arrow);
        mPbLoading = (ProgressBar) mHeaderContent.findViewById(R.id.pb_rotate);
        mTvState = (TextView) mHeaderContent.findViewById(R.id.tv_state);
        mTvTime = (TextView) mHeaderContent.findViewById(R.id.tv_time);
        mTvTime.setText(getRefreshTime());
        mHeaderContent.measure(0, 0);
        mHeaderViewHeight = mHeaderContent.getMeasuredHeight();

        fullyHide();
        Log.d(TAG, "mHeaderViewHeight:" + mHeaderViewHeight);
    }

    /**
     * 通过修改headerView的paddingTop来控制显示和隐藏
     *
     * @param deltaY 下拉滑动的距离
     */
    public void updateVisiableHeightByScroll(int deltaY) {
        int newPaddingTop = deltaY - mHeaderViewHeight;
        Log.d(TAG, "deltaY:" + deltaY + " newPaddingTop:" + newPaddingTop);
        mHeaderContent.setPadding(0, (int) (newPaddingTop * factor), 0, 0);
        //更新状态(下拉刷新和释放后刷新之间切换),由于该方法在下过程中会一直回调,所以需要通过状态作为前提条件
        if (isReleaseToRefresh() && newPaddingTop < 0) {
            setState(STATE_PULL_REFRESH);
        } else if (isPullToRefresh()&&newPaddingTop >= 0) {
            setState(STATE_RELEASE_REFRESH);
        }
    }

    /**
     * 完全隐藏headerView,通过设置paddingTop为自身高度的复数即可
     */
    public void fullyHide() {
        mHeaderContent.setPadding(0, -mHeaderViewHeight, 0, 0);
        setState(STATE_PULL_REFRESH);
    }
    /**
     * 实现HeaderView隐藏的时候平滑的收起效果
     */
    public void fullyHideSmooth() {
        ValueAnimator animator = ValueAnimator.ofInt(mHeaderContent.getPaddingTop(), -mHeaderViewHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //不断的修改paddingTop的值,达到平滑收起的效果
                int newPaddingTop = (int) animation.getAnimatedValue();
                mHeaderContent.setPadding(0,newPaddingTop,0,0);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setState(STATE_PULL_REFRESH);
            }
        });
        animator.setDuration(500);
        animator.start();
    }
    /**
     * 完全显示headerView,通过设置paddingTop为0即可
     */
    public void fullyShow() {
        mHeaderContent.setPadding(0, 0, 0, 0);
    }

    /**
     * 设置头部状态
     *
     * @param state
     */
    public void setState(int state) {
        switch (state) {
            case STATE_PULL_REFRESH://"下拉刷新"状态,需要显示箭头,隐藏加载圈,显示下拉刷新文字和最后刷新的时间,同时启动箭头动画
                mIvArrow.setVisibility(View.VISIBLE);
                mPbLoading.setVisibility(View.INVISIBLE);
                mTvTime.setVisibility(View.VISIBLE);
                mTvState.setText("下拉刷新");
                mTvTime.setText(getRefreshTime());
                //显示箭头朝下
                mIvArrow.startAnimation(mDownAnimation);
                break;
            case STATE_RELEASE_REFRESH: //"释放后刷新"状态,需要显示箭头,隐藏加载圈,显示释放后刷新文字和最后刷新的时间,同时启动箭头动画
                mIvArrow.setVisibility(View.VISIBLE);
                mPbLoading.setVisibility(View.INVISIBLE);
                mTvTime.setVisibility(View.VISIBLE);
                mTvState.setText("释放后刷新");
                //启动一个箭头的由下到上逆时针旋转的动画
                mIvArrow.startAnimation(mUpAnimation);
                break;
            case STATE_REFRESHING:
                //"正在刷新"状态,需要显示加载圈,隐藏箭头,显示正在刷新文字,隐藏最后刷新的时间
                mIvArrow.clearAnimation();//避免向上的旋转动画有可能没有执行完
                mPbLoading.setVisibility(View.VISIBLE);
                mIvArrow.setVisibility(View.INVISIBLE);
                mTvTime.setVisibility(View.GONE);
                mTvState.setText("正在刷新...");
                break;
        }
        this.mState = state; //更新当前状态
    }

    /**
     * 通过此方法获取最后一次刷新的时间
     *
     * @return
     */
    private String getRefreshTime() {
        return "上次刷新时间:" + mSharedPreferences.getString("key_refresh_time", mDateFormat.format(new Date()));
    }

    /**
     * 通过此方法保存下拉刷新的时间
     */
    public void saveRefreshTime() {
        mSharedPreferences.edit().putString("key_refresh_time", mDateFormat.format(new Date())).commit();
    }


    public boolean isRefreshing() {
        return mState == STATE_REFRESHING;
    }

    public boolean isReleaseToRefresh() {
        return mState == STATE_RELEASE_REFRESH;
    }

    public boolean isPullToRefresh() {
        return mState == STATE_PULL_REFRESH;
    }

}
