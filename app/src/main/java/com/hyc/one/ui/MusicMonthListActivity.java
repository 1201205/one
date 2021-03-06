package com.hyc.one.ui;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hyc.one.R;
import com.hyc.one.base.BaseActivity;
import com.hyc.one.base.BasePresenter;
import com.hyc.one.base.PresenterFactory;
import com.hyc.one.base.PresenterLoader;
import com.hyc.one.beans.DateBean;
import com.hyc.one.beans.music.MusicMonthItem;
import com.hyc.one.presenter.MusicMonthPresenter;
import com.hyc.one.ui.adpter.MonthMusicAdapter2;
import com.hyc.one.utils.AppUtil;
import com.hyc.one.utils.S;
import com.hyc.one.view.MusicMonthView;

import java.util.List;

/**
 * Created by ray on 16/5/26.
 */
public class MusicMonthListActivity extends BaseActivity<MusicMonthPresenter>
        implements MusicMonthView, LoaderManager.LoaderCallbacks<MusicMonthPresenter> {
    private RecyclerView mRecyclerView;
    private DateBean mDate;


    @Override
    protected void handleIntent() {
        mDate = getIntent().getParcelableExtra(S.DATE);
    }


    @Override
    protected void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.list_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
    }


    @Override
    protected int getLayoutID() {
        return R.layout.activity_music_month_list;
    }

    @Override
    protected void initLoader() {
        getSupportLoaderManager().initLoader(AppUtil.getID(), null, this);
    }

    @Override
    public Loader<MusicMonthPresenter> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<MusicMonthPresenter>(this, new PresenterFactory() {
            @Override
            public BasePresenter create() {
                return new MusicMonthPresenter(MusicMonthListActivity.this);
            }
        });
    }


    @Override
    public void onLoadFinished(Loader<MusicMonthPresenter> loader, MusicMonthPresenter data) {
        mPresenter = data;
        mPresenter.attachView();
        mPresenter.showList(mDate.realDate);
    }


    @Override
    public void onLoaderReset(Loader<MusicMonthPresenter> loader) {

    }


    @Override
    public void showList(List<MusicMonthItem> items) {
        MonthMusicAdapter2 adapter = new MonthMusicAdapter2(this, items);
        mRecyclerView.setAdapter(adapter);
    }


    @Override
    protected String getTitleString() {
        return mDate.date;
    }
}
