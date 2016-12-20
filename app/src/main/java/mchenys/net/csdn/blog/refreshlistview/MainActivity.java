package mchenys.net.csdn.blog.refreshlistview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import mchenys.net.csdn.blog.refreshlistview.refreshlistview.RefreshListView;

public class MainActivity extends AppCompatActivity {
    private RefreshListView mRefreshListView;
    private int pageNo = 1;
    private List<String> mData = new ArrayList<>();
    private ArrayAdapter mAdapter;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRefreshListView = new RefreshListView(this);
        setContentView(mRefreshListView);
        initView();
        initListener();
        loadData(false);
    }

    private void initView() {
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mData);
        mRefreshListView.setAdapter(mAdapter);
    }

    private void initListener() {
        mRefreshListView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(false);
            }

            @Override
            public void onLoadMore() {
                loadData(true);
            }

        });
        mRefreshListView.setSupportClickReload(true);
    }

    private boolean isFailure;

    private void loadData(final boolean isLoadMore) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLoadMore) {
                    if (pageNo == 4 && !isFailure) {
                        isFailure = true;
                        mRefreshListView.onLoadMoreFailure();
                        return;
                    } else if (pageNo == 6) {
                        mRefreshListView.onNoMoreData();
                        return;
                    }
                    pageNo++;
                } else {
                    pageNo = 1;
                }
                List temp = new ArrayList();
                for (int i = 0; i < 5; i++) {

                    temp.add("第" + pageNo + "页,数据" + (i + 1));
                }
                if (!isLoadMore) {
                    mData.clear();
                }
                mData.addAll(temp);
                mRefreshListView.onRefreshComplete(true);
                mAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }
}
