package com.hyc.zhihu.beans;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Administrator on 2016/5/16.
 */
public class RealReading extends RealmObject{
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @PrimaryKey
    private String id;
    private String time;
    private int type;
    private ReadingContent content;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ReadingContent getContent() {
        return content;
    }

    public void setContent(ReadingContent content) {
        this.content = content;
    }

    public RealReading(String time, ReadingContent content, int type,String id) {
        this.time = time;
        this.content = content;
        this.type = type;
        this.id=id;
    }
}
