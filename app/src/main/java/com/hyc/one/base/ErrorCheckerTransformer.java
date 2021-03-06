package com.hyc.one.base;

import com.hyc.one.beans.BaseBean;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by hyc on 2016/7/7.
 */
public class ErrorCheckerTransformer<T extends BaseBean<R>, R>
        implements Observable.Transformer<T, R> {


    @Override
    public Observable<R> call(Observable<T> observable) {
        return observable.map(new Func1<T, R>() {
            @Override
            public R call(T t) {
                if (null == t || null == t.getData() || (t.getData() instanceof List && ((List) t.getData()).size() == 0)) {
                    throw new NoThingGetException();
                }
                return t.getData();
            }
        });
    }
}
