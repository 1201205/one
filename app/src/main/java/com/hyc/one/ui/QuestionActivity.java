package com.hyc.one.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.hyc.one.R;
import com.hyc.one.base.BaseActivity;
import com.hyc.one.base.BasePresenter;
import com.hyc.one.base.PresenterFactory;
import com.hyc.one.base.PresenterLoader;
import com.hyc.one.beans.Comment;
import com.hyc.one.beans.Question;
import com.hyc.one.beans.QuestionContent;
import com.hyc.one.presenter.QuestionContentPresenter;
import com.hyc.one.ui.adpter.CommentAdapter;
import com.hyc.one.ui.adpter.QuestionAdapter;
import com.hyc.one.utils.AppUtil;
import com.hyc.one.view.ReadingContentView;
import com.hyc.one.widget.ListViewForScrollView;

import java.util.List;

/**
 * Created by ray on 16/5/18.
 */
public class QuestionActivity extends BaseActivity<QuestionContentPresenter> implements
        ReadingContentView<QuestionContent, Question>,
        OnLoadMoreListener,
        LoaderManager.LoaderCallbacks<QuestionContentPresenter> {
    private SwipeToLoadLayout swipeToLoadLayout;
    private View mHeader;
    private TextView mTitleTV;
    private TextView mDesTV;
    private TextView mAuthorTV;
    private TextView mDateTV;
    private TextView mContentTV;
    private TextView mEditorTV;
    private ListView listView;
    private ListViewForScrollView mRelateLV;
    private ListViewForScrollView mHotCommentsLV;
    private LinearLayout mRelateLL;
    private CommentAdapter mCommentAdapter;
    private LinearLayout mHotLL;
    private String mID;
    public static final String ID = "id";
    private boolean mHasMoreComments = true;


    @Override
    protected void handleIntent() {
        mID = getIntent().getStringExtra(ID);
    }


    @Override
    protected void initView() {
        listView = (ListView) findViewById(R.id.swipe_target);
        swipeToLoadLayout = (SwipeToLoadLayout) findViewById(R.id.swipeToLoadLayout);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (mHasMoreComments &&
                        scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() == view.getCount() - 1 &&
                            !ViewCompat.canScrollVertically(view, 1)) {
                        swipeToLoadLayout.setLoadingMore(true);
                    }
                }
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        mHeader = LayoutInflater.from(this).inflate(R.layout.qustion_header, null);
        mTitleTV = (TextView) mHeader.findViewById(R.id.title_tv);
        mAuthorTV = (TextView) mHeader.findViewById(R.id.author_tv);
        mContentTV = (TextView) mHeader.findViewById(R.id.content_tv);
        mDesTV = (TextView) mHeader.findViewById(R.id.des_tv);
        mDateTV = (TextView) mHeader.findViewById(R.id.date_tv);
        mEditorTV = (TextView) mHeader.findViewById(R.id.editor_tv);
        mRelateLV = (ListViewForScrollView) mHeader.findViewById(R.id.relate_lv);
        mRelateLL = (LinearLayout) mHeader.findViewById(R.id.relate_ll);
        mHotLL = (LinearLayout) mHeader.findViewById(R.id.hot_ll);

        mHotCommentsLV = (ListViewForScrollView) mHeader.findViewById(R.id.hot_lv);
        listView.addHeaderView(mHeader);
        mCommentAdapter = new CommentAdapter();
        listView.setAdapter(mCommentAdapter);
        swipeToLoadLayout.setOnLoadMoreListener(this);
    }


    @Override
    protected int getLayoutID() {
        return R.layout.activity_question;
    }


    @Override
    public void showContent(QuestionContent content) {
        mTitleTV.setText(content.getQuestion_title());
        mDesTV.setText(content.getQuestion_content());
        mContentTV.setText(Html.fromHtml(content.getAnswer_content()));
        mEditorTV.setText(content.getCharge_edt());
        mAuthorTV.setText(content.getAnswer_title());
    }


    @Override
    public void showRelate(List<Question> questions) {
        if (questions == null || questions.size() == 0) {
            mRelateLL.setVisibility(View.GONE);
        } else {
            QuestionAdapter adapter = new QuestionAdapter(this, questions);
            mRelateLV.setAdapter(adapter);
            adapter.setItemClickListener(new QuestionAdapter.OnReadingItemClickListener() {
                @Override
                public void onItemClicked(Question question) {
                    jumpToNewQuestion(question);
                }
            });
        }
    }


    @Override
    protected String getTitleString() {
        return AppUtil.getString(R.string.question);
    }


    private void jumpToNewQuestion(Question s) {
        Intent i = new Intent(this, QuestionActivity.class);
        i.putExtra(QuestionActivity.ID, s.getQuestion_id());
        startActivity(i);
    }


    @Override
    public void refreshCommentList(List<Comment> comments) {
        mCommentAdapter.refreshComments(comments);
        swipeToLoadLayout.setLoadingMore(false);

    }


    @Override
    public void showHotComments(List<Comment> comments) {
        if (comments == null || comments.size() == 0) {
            mHotLL.setVisibility(View.GONE);
        } else {
            CommentAdapter adapter = new CommentAdapter();
            mHotCommentsLV.setAdapter(adapter);
            adapter.refreshComments(comments);
        }
    }


    @Override
    public void showNoComments() {
        AppUtil.showToast(R.string.no_more);
        mHasMoreComments = false;
        swipeToLoadLayout.setLoadingMore(false);
        swipeToLoadLayout.setLoadMoreEnabled(false);
    }

    @Override
    protected void initLoader() {
        getSupportLoaderManager().initLoader(AppUtil.getID(), null, this);
    }

    @Override
    public Loader<QuestionContentPresenter> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<QuestionContentPresenter>(this, new PresenterFactory() {
            @Override
            public BasePresenter create() {
                return new QuestionContentPresenter(QuestionActivity.this);
            }
        });
    }


    @Override
    public void onLoadFinished(Loader<QuestionContentPresenter> loader, QuestionContentPresenter data) {
        mPresenter = data;
        mPresenter.attachView();
        mPresenter.getAndShowContent(mID);
    }


    @Override
    public void onLoaderReset(Loader<QuestionContentPresenter> loader) {
        mPresenter = null;
    }


    @Override
    public void onLoadMore() {
        mPresenter.getAndShowCommentList();
    }


    static class ViewHolder {
        TextView tv;
    }


    class TestAdapter extends BaseAdapter {
        TestAdapter() {

        }


        @Override
        public int getCount() {
            return 100;
        }


        @Override
        public Integer getItem(int position) {
            return position;
        }


        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder h = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(QuestionActivity.this)
                        .inflate(R.layout.layout_title, null);
                h = new ViewHolder();
                h.tv = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(h);
            } else {
                h = (ViewHolder) convertView.getTag();
            }
            h.tv.setText(String.valueOf(position));
            return convertView;
        }
    }
}