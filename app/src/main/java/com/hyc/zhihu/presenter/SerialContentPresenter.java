package com.hyc.zhihu.presenter;

import com.hyc.zhihu.base.BasePresenter;
import com.hyc.zhihu.beans.Comment;
import com.hyc.zhihu.beans.Comments;
import com.hyc.zhihu.beans.Serial;
import com.hyc.zhihu.beans.SerialContent;
import com.hyc.zhihu.beans.SerialWrapper;
import com.hyc.zhihu.beans.Serials;
import com.hyc.zhihu.net.Requests;
import com.hyc.zhihu.presenter.base.IReadingContentPresenter;
import com.hyc.zhihu.view.ReadingContentView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/5/19.
 */
public class SerialContentPresenter extends BasePresenter<ReadingContentView<SerialContent, Serial>> implements IReadingContentPresenter {
    private String mId;
    private String mLastIndex;

    public SerialContentPresenter(ReadingContentView<SerialContent, Serial> view) {
        super(view);
    }

    @Override
    public void getAndShowContent(String id) {
        mId = id;
        Observable.just(Requests.getApi().getSerialContentByID(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<SerialWrapper>() {
            @Override
            public void call(SerialWrapper serialWrapper) {
                mView.showContent(serialWrapper.getData());
            }
        }), Requests.getApi().getSerialRelateByID(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Serials>() {
            @Override
            public void call(Serials serials) {
                mView.showRelate(serials.getData());
            }
        }), Requests.getApi().getSerialCommentsByIndex(id, "0").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(new Func1<Comments, List<Comment>[]>() {
            @Override
            public List<Comment>[] call(Comments comments) {
                List<Comment> hot = new ArrayList<Comment>();
                List<Comment> normal = new ArrayList<Comment>();
                List<Comment> all = comments.getData().getData();
                int count = all.size();
                for (int i = 0; i < count; i++) {
                    Comment c = all.get(i);
                    if (all.get(i).getType() == 0) {
                        hot.add(c);
                    } else {
                        normal.add(c);
                    }
                }
                List<Comment>[] types = new List[2];
                types[0] = hot;
                types[1] = normal;
                if (normal.size() > 0) {
                    mLastIndex = normal.get(normal.size() - 1).getId();
                }
                return types;
            }
        }).subscribe(new Action1<List<Comment>[]>() {
            @Override
            public void call(List<Comment>[] comments) {
                mView.showHotComments(comments[0]);
                mView.refreshCommentList(comments[1]);
            }
        })).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void getAndShowCommentList() {
        Requests.getApi().getSerialCommentsByIndex(mId, mLastIndex).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Comments>() {
            @Override
            public void call(Comments comments) {
                if (comments==null||comments.getData()==null||comments.getData().getData()==null||comments.getData().getData().size()==0) {
                    mView.showNoComments();
                }
                List<Comment> c = comments.getData().getData();
                if (c != null && c.size() > 0) {
                    mLastIndex = c.get(c.size() - 1).getId();
                }
                mView.refreshCommentList(comments.getData().getData());
            }
        });
    }

    @Override
    public void getAndShowRelate(String id) {

    }
}
