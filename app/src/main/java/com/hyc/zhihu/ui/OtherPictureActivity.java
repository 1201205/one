package com.hyc.zhihu.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.hyc.zhihu.R;
import com.hyc.zhihu.base.BaseActivity;
import com.hyc.zhihu.base.BasePresenter;
import com.hyc.zhihu.base.PresenterFactory;
import com.hyc.zhihu.base.PresenterLoader;
import com.hyc.zhihu.beans.OnePictureData;
import com.hyc.zhihu.presenter.OtherPicturePresenter;
import com.hyc.zhihu.ui.adpter.MonthPictureAdapter;
import com.hyc.zhihu.utils.AppUtil;
import com.hyc.zhihu.utils.S;
import com.hyc.zhihu.view.OtherPictureView;

import java.util.List;

/**
 * Created by hyc on 2016/5/13.
 */
public class OtherPictureActivity extends BaseActivity<OtherPicturePresenter>
        implements OtherPictureView, LoaderManager.LoaderCallbacks<OtherPicturePresenter>, OnLoadMoreListener {
    private String mID;
    private RecyclerView mRecyclerView;
    private SwipeToLoadLayout mSwipeToLoadLayout;
    private ImageView mNoItemIV;
    private MonthPictureAdapter mMonthPictureAdapter;
    private boolean mCanLoad = true;
    private GridLayoutManager mLayoutManager;
    private static final int PAGE_COUNT=20;

    @Override
    protected void handleIntent() {
        mID = getIntent().getStringExtra(S.ID);
    }


    @Override
    protected void initView() {
        mSwipeToLoadLayout = (SwipeToLoadLayout) findViewById(R.id.swipeToLoadLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.swipe_target);
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mNoItemIV = (ImageView) findViewById(R.id.no_item_iv);
        mSwipeToLoadLayout.setOnLoadMoreListener(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mCanLoad && mLayoutManager.findLastCompletelyVisibleItemPosition() == mMonthPictureAdapter.getItemCount() - 1 ) {
                        mSwipeToLoadLayout.setLoadingMore(true);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }


    @Override
    protected int getLayoutID() {
        return R.layout.activity_other_picture;
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new PresenterLoader(this, new PresenterFactory() {
            @Override
            public BasePresenter create() {
                return new OtherPicturePresenter(OtherPictureActivity.this);
            }
        });
    }


    @Override
    public void onLoadFinished(Loader loader, OtherPicturePresenter data) {
        mPresenter = data;
        mPresenter.attachView();
        mPresenter.showPicture("6109953");
    }


    @Override
    public void onLoaderReset(Loader loader) {
        mPresenter = null;
    }


    @Override
    public void showPictures(List<OnePictureData> datas) {
        mMonthPictureAdapter = new MonthPictureAdapter(datas, this);
        mRecyclerView.setAdapter(mMonthPictureAdapter);
        if (datas.size()<PAGE_COUNT) {
            mCanLoad = false;
            mSwipeToLoadLayout.setLoadMoreEnabled(false);
        }
    }

    @Override
    public void refresh(List<OnePictureData> datas) {
        mSwipeToLoadLayout.setLoadingMore(false);
        mMonthPictureAdapter.addItems(datas);
    }

    @Override
    public void nothingGet() {
        mCanLoad = false;
        if (mMonthPictureAdapter == null) {
            mNoItemIV.setVisibility(View.VISIBLE);
            mSwipeToLoadLayout.setVisibility(View.GONE);
        } else {
            mSwipeToLoadLayout.setLoadingMore(false);
            mSwipeToLoadLayout.setLoadMoreEnabled(false);
            AppUtil.showToast("客官，没有更多啦啦啦");
        }
    }

    @Override
    protected void initLoader() {
        getSupportLoaderManager().initLoader(AppUtil.getID(), null, this);
    }

    @Override
    public void onLoadMore() {
        mPresenter.refresh();
    }
}