package com.hyc.zhihu.presenter;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.hyc.zhihu.base.BasePresenter;
import com.hyc.zhihu.beans.DateReading;
import com.hyc.zhihu.beans.HeadItems;
import com.hyc.zhihu.beans.Question;
import com.hyc.zhihu.beans.Reading;
import com.hyc.zhihu.beans.ReadingContent;
import com.hyc.zhihu.beans.Readings;
import com.hyc.zhihu.beans.RealArticle;
import com.hyc.zhihu.beans.RealReading;
import com.hyc.zhihu.beans.SerialContent;
import com.hyc.zhihu.net.Requests;
import com.hyc.zhihu.presenter.base.IReadingPresenter;
import com.hyc.zhihu.utils.JsonUtil;
import com.hyc.zhihu.view.ReadingView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/5/16.
 */
public class ReadingPresenter extends BasePresenter<ReadingView> implements IReadingPresenter {
    private List<String> mDate;
    public ReadingPresenter(ReadingView view) {
        super(view);

    }

    @Override
    public void showContent() {
//        getAndShowHead();
        getAndShowList(0);
    }

    @Override
    public void getAndShowHead() {
        Requests.getApi().getScrollHeads().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<HeadItems>() {
            @Override
            public void call(HeadItems headItems) {
//                mView.showHead(headItems.getData());
            }
        });
    }

    @Override
    public void getAndShowList(int index) {
        Requests.getApi().getReadingList(index).map(new Func1<Readings, List<RealReading>>() {
            @Override
            public List<RealReading> call(Readings readings) {
                List<RealReading> realReadings=new ArrayList<RealReading>();
                List<DateReading> dateReading=readings.getData();
                if (mDate==null) {
                    mDate=new ArrayList<String>();
                }
                int dateCount=dateReading.size();
                for (int i=0;i<dateCount;i++) {
                    List<Reading> readingList=dateReading.get(i).getItems();
                    mDate.add(readingList.get(0).getTime());
                    int readingCount=readingList.size();
                    for (int j=0;j<readingCount;j++) {
                        RealReading r=getRealReading(readingList.get(j));
                        realReadings.add(r);
                        if (j==0) {
                            mDate.add(r.getContent().getTitle());
                        }
                    }
                }
                return realReadings;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<RealReading>>() {
            @Override
            public void call(List<RealReading> realReadings) {
                mView.showList(realReadings,mDate);
            }
        });
    }
    private RealReading getRealReading(Reading r){
        ReadingContent content=new ReadingContent();
        switch (r.getType()){
            case 1:
                RealArticle a= (RealArticle) JsonUtil.fromJson(r.getContent(),RealArticle.class);
                content.setAuthor(a.getAuthor().get(0).getUser_name());
                content.setContent(a.getGuide_word());
                content.setHasAudio(a.getHas_audio());
                content.setId(a.getContent_id());
                content.setTitle(a.getHp_title());
                break;
            case 2:
                SerialContent s= (SerialContent) JsonUtil.fromJson(r.getContent(),SerialContent.class);
                content.setTitle(s.getTitle());
                content.setAuthor(s.getAuthor().getUser_name());
                content.setContent(s.getExcerpt());
                content.setId(s.getId());
                break;
            case 3:
                Question q= (Question) JsonUtil.fromJson(r.getContent(),Question.class);
                content.setTitle(q.getQuestion_title());
                content.setAuthor(q.getAnswer_title());
                content.setContent(q.getAnswer_content());
                content.setId(q.getQuestion_id());
        }
        return new RealReading(r.getTime(),content,r.getType());
    }
}