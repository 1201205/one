package com.hyc.zhihu.base;

import com.hyc.zhihu.beans.BaseBean;
import com.hyc.zhihu.utils.S;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/7/7.
 */
public class ErrorCheckerTransformer<T extends BaseBean<R>, R>
        implements Observable.Transformer<T, R> {


    @Override
    public Observable<R> call(Observable<T> observable) {
        return observable.map(new Func1<T, R>() {
            @Override
            public R call(T t) {
                if (null == t || null == t.getData()) {
                    try {
                        throw new NoThingGetException(S.NO_THING_GET);
                    } catch (NoThingGetException e) {
                        throw Exceptions.propagate(e);
                    }
                }
                return t.getData();
            }
        });
    }
}
