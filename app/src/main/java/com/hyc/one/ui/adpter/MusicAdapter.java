package com.hyc.one.ui.adpter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.hyc.one.R;
import com.hyc.one.beans.Comment;
import com.hyc.one.beans.DateBean;
import com.hyc.one.beans.OnePictureData;
import com.hyc.one.beans.Song;
import com.hyc.one.beans.music.Music;
import com.hyc.one.beans.music.MusicRelate;
import com.hyc.one.beans.music.MusicRelateListBean;
import com.hyc.one.event.PlayCallBackEvent;
import com.hyc.one.event.PlayEvent;
import com.hyc.one.helper.PicassoHelper;
import com.hyc.one.player.ManagedMediaPlayer;
import com.hyc.one.player.MyPlayer;
import com.hyc.one.ui.MainActivity;
import com.hyc.one.ui.MusicMonthListActivity;
import com.hyc.one.ui.PictureActivity;
import com.hyc.one.utils.AppUtil;
import com.hyc.one.utils.DateUtil;
import com.hyc.one.widget.CircleImageView;
import com.hyc.one.widget.ListViewForScrollView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Administrator on 2016/5/6.
 */
public class MusicAdapter extends PagerAdapter {
    private String mCurrentId;
    private List<Music> viewBeans;
    private Context mContext;
    private List<MusicRelateListBean> mRelateLists;
    private List<CommentAdapter> mAdapters;
    private ImageView mPlayView;
    private int mPlayIndex;


    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.mLoadMoreListener = loadMoreListener;
    }


    private OnLoadMoreListener mLoadMoreListener;


    @Override
    public int getCount() {
        return viewBeans == null ? 0 : viewBeans.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    public MusicAdapter(List<MusicRelateListBean> relateBeans, List<Music> viewBean) {
        super();
        this.viewBeans = viewBean;
        //        this.mContext = context;
        this.mRelateLists = relateBeans;
        mAdapters = new ArrayList<>();
        for (int i = 0; i < relateBeans.size(); i++) {
            mAdapters.add(new CommentAdapter());
        }
        EventBus.getDefault().register(this);
    }


    @Override
    public int getItemPosition(Object object) {
        View v = (View) object;
        if (mCurrentId.equals(v.getTag())) {
            return POSITION_NONE;
        } else {
            return POSITION_UNCHANGED;
        }
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //释放view引用
        if (position < mRelateLists.size()) {
            mRelateLists.get(position).setLayout(null);
        }
        if (mPlayIndex == position) {
            mPlayView = null;
        }
        container.removeView((View) object);
        Log.e("test", "destroyItem----" + position);
    }


    @Subscribe
    public void onEvent(PlayCallBackEvent playEvent) {
        if (mPlayView == null) {
            return;
        }
        switch (playEvent.getState()) {
            case STARTED:
                mPlayView.setImageResource(R.drawable.music_pause_selector);
                break;
            case STOPPED:
            case PAUSED:
                mPlayView.setImageResource(R.drawable.music_play_selector);
                break;
            default:
                break;

        }
    }


    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        //        int delay=0;
        //        if (mRefreshIndex==position) {
        //            mRefreshIndex=-1;
        //            delay=50;
        //        }
        Context c = container.getContext();
        View view;
        final Music music = viewBeans.get(position);
        if (position == viewBeans.size() - 1) {
            view = LayoutInflater.from(c).inflate(R.layout.date_list, null);
            ListView listView = (ListView) view.findViewById(R.id.date_list);
            listView.setAdapter(new DateAdapter(getDateBeans(), MusicMonthListActivity.class));
        } else {
            view = LayoutInflater.from(c).inflate(R.layout.activity_question, null);
            final ListView listView = (ListView) view.findViewById(R.id.swipe_target);
            final SwipeToLoadLayout swipeToLoadLayout = (SwipeToLoadLayout) view.findViewById(
                    R.id.swipeToLoadLayout);
            mRelateLists.get(position).setLayout(swipeToLoadLayout);
            View mHeader = LayoutInflater.from(c).inflate(R.layout.music_header, null);
            ManagedMediaPlayer.Status s = MyPlayer.getPlayer().getSourceStatus(music.getMusic_id());

            final ImageView playIV = (ImageView) mHeader.findViewById(R.id.play_iv);
            if (s == ManagedMediaPlayer.Status.STARTED) {
                playIV.setImageResource(R.drawable.music_pause_selector);
            } else {
                playIV.setImageResource(R.drawable.music_play_selector);
            }
            playIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPlayView != null && v != mPlayView) {
                        mPlayView.setImageResource(R.drawable.music_play_selector);
                    }
                    mPlayIndex = position;
                    mPlayView = (ImageView) v;
                    ManagedMediaPlayer.Status s = MyPlayer.getPlayer()
                            .getSourceStatus(music.getMusic_id());
                    if (s == ManagedMediaPlayer.Status.IDLE ||
                            s == ManagedMediaPlayer.Status.STOPPED) {
                        PlayEvent e = new PlayEvent();
                        e.setSong(new Song(music.getTitle(), music.getMusic_id()));
                        e.setAction(PlayEvent.Action.PLAYITEM);
                        EventBus.getDefault().post(e);
                        Log.e("test---", "点击播放");
                    } else if (s == ManagedMediaPlayer.Status.PAUSED) {
                        PlayEvent e = new PlayEvent();
                        e.setAction(PlayEvent.Action.RESUME);
                        EventBus.getDefault().post(e);
                        Log.e("test---", "点击恢复");
                    } else if (s == ManagedMediaPlayer.Status.STARTED) {
                        PlayEvent e = new PlayEvent();
                        e.setAction(PlayEvent.Action.PAUSE);
                        EventBus.getDefault().post(e);
                        Log.e("test---", "点击暂停");
                    }
                }
            });
            ImageView musicIV = (ImageView) mHeader.findViewById(R.id.music_iv);
            Picasso.with(mHeader.getContext()).load(music.getCover()).placeholder(R.drawable.default_music_cover).into(musicIV);
            //            Picasso.with(mContext).load(music.getCover()).fit().into(musicIV);
            CircleImageView headIV = (CircleImageView) mHeader.findViewById(R.id.head_iv);
            PicassoHelper.load(mContext, music.getAuthor().getWeb_url(), headIV, R.drawable.head);
            //            Picasso.with(mContext).load(music.getAuthor().getWeb_url()).into(headIV);
//            FrescoHelper.loadImage(headIV, music.getAuthor().getWeb_url());
            TextView mAuthorTV = (TextView) mHeader.findViewById(R.id.name_tv);
            mAuthorTV.setText(music.getAuthor().getUser_name());
            View.OnClickListener listener = AppUtil.getOtherClickListener(music.getAuthor().getUser_id(), mAuthorTV.getContext());
            mAuthorTV.setOnClickListener(listener);
            headIV.setOnClickListener(listener);
            TextView desTV = (TextView) mHeader.findViewById(R.id.des_tv);
            desTV.setText(music.getAuthor().getDesc());
            TextView musicTitleTV = (TextView) mHeader.findViewById(R.id.music_title_tv);
            musicTitleTV.setText(music.getTitle());
            TextView timeTV = (TextView) mHeader.findViewById(R.id.time_tv);
            timeTV.setText(DateUtil.getCommentDate(music.getMaketime()));
            TextView titleTV = (TextView) mHeader.findViewById(R.id.title_tv);
            titleTV.setText(music.getStory_title());
            TextView authorNameTV = (TextView) mHeader.findViewById(R.id.author_name_tv);
            authorNameTV.setText(music.getStory_author().getUser_name());
            final LinearLayout contentLL = (LinearLayout) mHeader.findViewById(R.id.content_ll);
            final TextView contentTV = (TextView) mHeader.findViewById(R.id.content_tv);
            contentTV.setText(Html.fromHtml(music.getStory()));
            final TextView lyricTV = (TextView) mHeader.findViewById(R.id.lyric_tv);
            lyricTV.setText(music.getLyric());
            final TextView infoTV = (TextView) mHeader.findViewById(R.id.info_tv);
            infoTV.setText(music.getInfo());
            TextView editorTV = (TextView) mHeader.findViewById(R.id.editor_tv);
            editorTV.setText(music.getCharge_edt());
            TextView likeNumTV = (TextView) mHeader.findViewById(R.id.like_num_tv);
            likeNumTV.setText(String.valueOf(music.getPraisenum()));
            TextView commentNumTV = (TextView) mHeader.findViewById(R.id.comment_num_tv);
            commentNumTV.setText(String.valueOf(music.getCommentnum()));
            TextView shareNumTV = (TextView) mHeader.findViewById(R.id.share_num_tv);
            shareNumTV.setText(String.valueOf(music.getSharenum()));
            View hot = mHeader.findViewById(R.id.hot_ll);
            List<MusicRelate> musicRelates = mRelateLists.get(position).getMusics();
            List<Comment> comments = mRelateLists.get(position).getHotComment();
            if (musicRelates != null && musicRelates.size() > 0) {
                RecyclerView r = (RecyclerView) mHeader.findViewById(R.id.relate_rv);
                MusicRelateAdapter a = new MusicRelateAdapter(musicRelates);
                r.setAdapter(a);
                LinearLayoutManager m = new LinearLayoutManager(container.getContext());
                m.setOrientation(LinearLayoutManager.HORIZONTAL);
                r.setLayoutManager(m);
            } else {
                View v = mHeader.findViewById(R.id.relate_ll);
                v.setVisibility(View.GONE);
            }
            if (comments != null && comments.size() > 0) {
                ListViewForScrollView hotCommentsLV = (ListViewForScrollView) mHeader.findViewById(
                        R.id.hot_lv);
                CommentAdapter adapter = new CommentAdapter();
                hotCommentsLV.setAdapter(adapter);
                adapter.refreshComments(comments);
                hot.setVisibility(View.VISIBLE);
            } else {
                hot.setVisibility(View.GONE);
            }
            if (mLoadMoreListener != null) {
                swipeToLoadLayout.setOnLoadMoreListener(
                        new com.aspsine.swipetoloadlayout.OnLoadMoreListener() {
                            @Override
                            public void onLoadMore() {
                                mLoadMoreListener.loadMore(position,
                                        mRelateLists.get(position).getLastIndex());
                            }
                        });
            }

            //            mAuthorHeaderIV = (CircleImageView) mHeader.findViewById(R.id.author_head_iv);
            //            mHeaderIV = (CircleImageView) mHeader.findViewById(R.id.head_iv);
            //            mAuthorNameTV = (TextView) mHeader.findViewById(R.id.author_name_tv);
            //            mRelateLV = (ListViewForScrollView) mHeader.findViewById(R.id.relate_lv);
            //            mRelateLL = (LinearLayout) mHeader.findViewById(R.id.relate_ll);
            listView.addHeaderView(mHeader);
            listView.setAdapter(mAdapters.get(position));
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        if (mRelateLists.get(position).hasMoreComment() &&
                                view.getLastVisiblePosition() == view.getCount() - 1 &&
                                !ViewCompat.canScrollVertically(view, 1)) {
                            swipeToLoadLayout.setLoadingMore(true);
                        }
                    }
                }


                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });
            final ImageView storyIV = (ImageView) mHeader.findViewById(R.id.story_iv);
            ImageView lyricIV = (ImageView) mHeader.findViewById(R.id.lyric_iv);
            final ImageView infoIV = (ImageView) mHeader.findViewById(R.id.info_iv);
            //            swipeToLoadLayout.setOnLoadMoreListener(this);
            storyIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contentLL.getVisibility() != View.VISIBLE) {
                        contentLL.setVisibility(View.VISIBLE);
                        lyricTV.setVisibility(View.GONE);
                        infoTV.setVisibility(View.GONE);
                    }
                }
            });
            lyricIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lyricTV.getVisibility() != View.VISIBLE) {
                        lyricTV.setVisibility(View.VISIBLE);
                        contentLL.setVisibility(View.GONE);
                        infoTV.setVisibility(View.GONE);
                    }
                }
            });
            infoIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (infoTV.getVisibility() != View.VISIBLE) {
                        infoTV.setVisibility(View.VISIBLE);
                        lyricTV.setVisibility(View.GONE);
                        contentLL.setVisibility(View.GONE);
                    }
                }
            });
            view.setTag(music.getId());
        }

        ((ViewPager) container).addView(view);
        return view;

    }


    public void setRelate(int page, List<MusicRelate> musicRelates) {
        mCurrentId = mRelateLists.get(page).getId();
        mRelateLists.get(page).setHasRequested(true);
        mRelateLists.get(page).setMusics(musicRelates);
        if (musicRelates.size() > 0) {
            notifyDataSetChanged();
        }
    }


    public boolean neeRequest(int page) {
        return !mRelateLists.get(page).hasRequested();
    }


    public void setComment(int page, List<Comment> hot, List<Comment> normal) {
        mCurrentId = mRelateLists.get(page).getId();
        mRelateLists.get(page).setHasRequested(true);
        mRelateLists.get(page).setHotComment(hot);
        String index = null;
        if (normal.size() > 0) {
            index = normal.get(normal.size() - 1).getId();
        } else {
            index = hot.get(hot.size() - 1).getId();
        }
        mRelateLists.get(page).setLastIndex(index);
        mAdapters.get(page).refreshComments(normal);
        notifyDataSetChanged();
    }


    public void refreshComment(int page, List<Comment> comments) {
        String index = null;
        MusicRelateListBean bean = mRelateLists.get(page);
        SwipeToLoadLayout v = bean.getLayout();
        if (v != null) {
            v.setLoadingMore(false);
        }
        if (comments.size() > 0) {
            index = comments.get(comments.size() - 1).getId();
            mCurrentId = mRelateLists.get(page).getId();
            mAdapters.get(page).refreshComments(comments);
        } else {
            bean.setHasMoreComment(false);
            AppUtil.showToast(R.string.no_more);
            if (v != null) {
                v.setLoadMoreEnabled(false);
            }
        }
        bean.setLastIndex(index);
    }


    private View.OnClickListener getOnclickListener(final String title, final String url, final View vol) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                ActivityOptionsCompat compat=  ActivityOptionsCompat.makeSceneTransitionAnimation((MainActivity) mContext,new Pair<View, String>(v,PictureActivity.SHARE_TITLE),new Pair<View, String>(vol,PictureActivity.SHARE_PICTURE));

                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (MainActivity) mContext, v, PictureActivity.SHARE_PICTURE);
                Intent intent = PictureActivity.newIntent(mContext, url, title);
                //                ((MainActivity) mContext).getWindow().setSharedElementEnterTransition(new ChangeImageTransform(mContext, null));
                ActivityCompat.startActivity((MainActivity) mContext, intent, compat.toBundle());
                //                mContext.startActivity(intent, compat.toBundle());
            }
        };
    }


    public void setCurrentItem(String id, OnePictureData data) {
        //        Log.e("test1", "显示信息");
        //        mCurrentId = id;
        //        for (int i = 0; i < viewBeans.size(); i++) {
        //            if (viewBeans.get(i).id.equals(id)) {
        //                viewBeans.get(i).data = data;
        //                viewBeans.get(i).state = PictureViewBean.NORMAL;
        //                Log.e("test1", "notifyDataSetChanged");
        //                DelayHandle.delay(150, new Runnable() {
        //                    @Override
        //                    public void run() {
        //                       notifyDataSetChanged();
        //                    }
        //                });
        //                break;
        //            }
        //        }

    }


    private int mCurrentPage;


    public void setCurrentPage(int page) {
        mCurrentPage = page;
    }


    private List<DateBean> getDateBeans() {
        List<DateBean> dateBeans = new ArrayList<>();
        GregorianCalendar calendar = new GregorianCalendar();
        GregorianCalendar temp = new GregorianCalendar(2016, 1, 1);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        while (temp.before(calendar)) {
            String s = format.format(temp.getTime());
            String moth = DateUtil.getMonthDate(s);
            dateBeans.add(new DateBean(moth, s + "%2000:00:00"));
            temp.add(GregorianCalendar.MONTH, 1);
        }
        Collections.reverse(dateBeans);
        dateBeans.get(0).date = AppUtil.getString(R.string.current_month);
        return dateBeans;
    }

    public void clear() {
        viewBeans.clear();
        mAdapters.clear();
        mRelateLists.clear();
        for (MusicRelateListBean bean : mRelateLists) {
            bean.setLayout(null);
        }
        mPlayView = null;
        EventBus.getDefault().unregister(this);
    }


    public interface OnLoadMoreListener {
        void loadMore(int page, String lastIndex);
    }
}
