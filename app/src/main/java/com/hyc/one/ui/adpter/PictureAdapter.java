package com.hyc.one.ui.adpter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hyc.one.R;
import com.hyc.one.beans.DateBean;
import com.hyc.one.beans.OnePictureData;
import com.hyc.one.beans.PictureViewBean;
import com.hyc.one.helper.DelayHandle;
import com.hyc.one.ui.MainActivity;
import com.hyc.one.ui.MonthPictureActivity;
import com.hyc.one.ui.PictureActivity;
import com.hyc.one.utils.AppUtil;
import com.hyc.one.utils.DateUtil;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Administrator on 2016/5/6.
 */
public class PictureAdapter extends PagerAdapter {
    private String mCurrentId;
    private List<PictureViewBean> viewBeans;
    private Context mContext;

    @Override
    public int getCount() {
        return viewBeans == null ? 0 : viewBeans.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public PictureAdapter(List<PictureViewBean> viewBean, Context context) {
        super();
        this.viewBeans = viewBean;
        this.mContext = context;
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
        container.removeView((View) object);
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        View view;
        if (position == viewBeans.size() - 1) {
            view = LayoutInflater.from(mContext).inflate(R.layout.date_list, null);
            ListView listView = (ListView) view.findViewById(R.id.date_list);
            listView.setAdapter(new DateAdapter(getDateBeans(), MonthPictureActivity.class));
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.picture_adpter_item, null);
            final ImageView picture = (ImageView) view.findViewById(R.id.picture_sdv);
            final TextView vol = (TextView) view.findViewById(R.id.vol_tv);
            final TextView name = (TextView) view.findViewById(R.id.name_tv);
            final TextView content = (TextView) view.findViewById(R.id.main_tv);
            final TextView date = (TextView) view.findViewById(R.id.date_tv);
            final PictureViewBean viewBean = viewBeans.get(position);
            if (viewBean.state == PictureViewBean.NORMAL) {
                final OnePictureData bean = viewBeans.get(position).data;
                picture.setOnClickListener(getOnclickListener(bean.getHp_title(), bean.getHp_img_original_url(), vol));
                name.setText(bean.getHp_author());
                vol.setText(bean.getHp_title());
                content.setText(bean.getHp_content());
                date.setText(DateUtil.getOneDate(bean.getHp_makettime()));
                Picasso.with(mContext).load(bean.getHp_img_original_url()).placeholder(R.drawable.default_hp_image).fit().into(picture);
            }
            view.setTag(viewBean.id);
        }

        ((ViewPager) container).addView(view);
        return view;

    }

    private View.OnClickListener getOnclickListener(final String title, final String url, final View vol) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ActivityOptionsCompat compat=  ActivityOptionsCompat.makeSceneTransitionAnimation((MainActivity) mContext,new Pair<View, String>(v,PictureActivity.SHARE_TITLE),new Pair<View, String>(vol,PictureActivity.SHARE_PICTURE));
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation((MainActivity) mContext, v, PictureActivity.SHARE_PICTURE);
                Intent intent = PictureActivity.newIntent(mContext, url, title);
//                ((MainActivity) mContext).getWindow().setSharedElementEnterTransition(new ChangeImageTransform(mContext, null));
                ActivityCompat.startActivity((MainActivity) mContext, intent, compat.toBundle());
//                mContext.startActivity(intent, compat.toBundle());
            }
        };
    }


    public void setCurrentItem(String id, OnePictureData data) {
        mCurrentId = id;
        for (int i = 0; i < viewBeans.size(); i++) {
            if (viewBeans.get(i).id.equals(id)) {
                viewBeans.get(i).data = data;
                viewBeans.get(i).state = PictureViewBean.NORMAL;
                DelayHandle.delay(150, new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
                break;
            }
        }

    }

    private int mCurrentPage;

    public void setCurrentPage(int page) {
        mCurrentPage = page;
    }

    private List<DateBean> getDateBeans() {
        List<DateBean> dateBeans = new ArrayList<>();
        GregorianCalendar calendar = new GregorianCalendar();
        GregorianCalendar temp = new GregorianCalendar(2012, 9, 1);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        while (temp.before(calendar)) {
            String s = format.format(temp.getTime());
            String month = DateUtil.getMonthDate(s);
            dateBeans.add(new DateBean(month, s + "%2000:00:00"));
            temp.add(GregorianCalendar.MONTH, 1);
        }
        Collections.reverse(dateBeans);
        dateBeans.get(0).date = AppUtil.getString(R.string.current_month);
        return dateBeans;
    }

    public static void main(String[] args) {
        GregorianCalendar calendar = new GregorianCalendar();
        GregorianCalendar temp = new GregorianCalendar(2012, 9, 1);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        while (temp.before(calendar)) {
            System.out.println(format.format(temp.getTime()));
            temp.add(GregorianCalendar.MONTH, 1);
        }

    }

}
