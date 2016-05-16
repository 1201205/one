package com.hyc.zhihu.presenter.base;

import android.graphics.Picture;

import com.hyc.zhihu.base.BasePresenter;
import com.hyc.zhihu.beans.OnePictureData;
import com.hyc.zhihu.view.TestView;

import java.util.List;

/**
 * Created by ray on 16/4/19.
 */
public interface IPicturePresenter {
    void getPictureIdsAndFirstItem();
    OnePictureData getPictureById(String id);
    void checkAndGetPictureIds();
    void gotoPosition(int position);

}