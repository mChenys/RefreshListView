package mchenys.net.csdn.blog.refreshlistview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import mchenys.net.csdn.blog.refreshlistview.refreshlistview.RefreshListView;

/**
 * Created by mChenys on 2016/12/20.
 */
public class PinHeaderListViewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PinHeaderActivity";
    private LinearLayout mPinHeaderLl;
    private RefreshListView mRefreshListView;
    private List<String> mData = new ArrayList<>();
    private String currPage = "before";
    private ArrayAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_header_listview);
        initView();
        initListener();
        loadData(false);
    }

    private void initListener() {
        mRefreshListView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currPage = "new";
                loadData(false);
            }

            @Override
            public void onLoadMore() {
                loadData(true);
            }
        });
        mRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d(TAG, "firstVisibleItem :" + firstVisibleItem);
                if (firstVisibleItem >= 2) {
                    //显示悬浮区域
                    mPinHeaderLl.setVisibility(View.VISIBLE);
                } else {
                    //隐藏悬浮区域
                    mPinHeaderLl.setVisibility(View.GONE);
                }
            }
        });
        findViewById(R.id.tv_tab1).setOnClickListener(this);
        findViewById(R.id.tv_tab2).setOnClickListener(this);
        findViewById(R.id.tv_tab3).setOnClickListener(this);

        findViewById(R.id.lv_tv_tab1).setOnClickListener(this);
        findViewById(R.id.lv_tv_tab2).setOnClickListener(this);
        findViewById(R.id.lv_tv_tab3).setOnClickListener(this);
    }

    private void initView() {
        //真正悬浮在ListView上方的布局
        mPinHeaderLl = (LinearLayout) findViewById(R.id.ll_pin_header_copy);
        mPinHeaderLl.setVisibility(View.GONE);
        mRefreshListView = (RefreshListView) findViewById(R.id.rlv);
        View bigPictureHeader = View.inflate(this, R.layout.layout_big_picture_header, null);
        View pinHeader = View.inflate(this, R.layout.layout_pin_header, null);
        int pinHeaderHeight = convertDip2Px(this, 60);
        pinHeader.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pinHeaderHeight));

        mRefreshListView.addHeaderView(bigPictureHeader);//大图-头部View
        mRefreshListView.addHeaderView(pinHeader);//ListView条目中的固定部分 添加到头部
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mData);
        mRefreshListView.setAdapter(mAdapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadData(final boolean isLoadMore) {
        if (!isLoadMore) {
            mData.clear();
        }
        for (int i = 'a'; i < 'z'; i++) {
            mData.add("TAB-" + currPage + "-" + String.valueOf((char) i));
        }
        mRefreshListView.onRefreshComplete(true);
        mAdapter.notifyDataSetChanged();
        if (currPage.contains("change")) {
            mRefreshListView.setSelection(2);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_tab1:
            case R.id.lv_tv_tab1:
                currPage = "change1";
                break;
            case R.id.tv_tab2:
            case R.id.lv_tv_tab2:
                currPage = "change2";
                break;
            case R.id.tv_tab3:
            case R.id.lv_tv_tab3:
                currPage = "change3";
                break;
        }
        loadData(false);
    }

    //转换dip为px
    public static int convertDip2Px(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }
}
