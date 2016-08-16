package com.panxiong.calendar;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by panxi on 2016/5/24.
 * <p/>
 * 自定义带提示的日历
 */
public class PromptCalendar extends FrameLayout {
    private static String TAG = "PromptCalendar";
    private static final Integer startValue = Integer.MAX_VALUE / 2; // 1073741823
    private Context context = null;
    private LayoutInflater inflater = null;
    private View rootView = null;
    private ViewPager viewPager = null;
    private PromptCalendarInterface calendarInterface = null;

    /*设置回调函数*/
    public void setCalendarInterface(PromptCalendarInterface calendarInterface) {
        this.calendarInterface = calendarInterface;
    }

    public PromptCalendar(Context context) {
        super(context);
        this.context = context;
    }

    public PromptCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    /*初始化*/
    private void init() {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.calendar_viewpager_layout, null);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter());
        // viewPager.setPageMargin(40);
        viewPager.setCurrentItem(startValue);
        removeAllViews();
        addView(rootView);
    }

    /*ViewPager适配器*/
    private class ViewPagerAdapter extends PagerAdapter {
        private Integer recordPosition = -1;

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View contentView = inflater.inflate(R.layout.calendar_gridview_layout, null);
            TextView textView = (TextView) contentView.findViewById(R.id.textView);
            GridView gridView = (GridView) contentView.findViewById(R.id.gridView);
            String ym = getDataByOffset(position - startValue);
            textView.setText(ym);
            final String[] ymArr = ym.split("-");
            final GridViewAdapter gridViewAdapter = new GridViewAdapter(
                    Integer.valueOf(ymArr[0]),
                    Integer.valueOf(ymArr[1])
            );
            gridView.setAdapter(gridViewAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String day = gridViewAdapter.gridViewList.get(position).day;
                    if (day.length() <= 0) return;
                    if (calendarInterface != null) {
                        calendarInterface.onSelectDate(
                                Integer.valueOf(ymArr[0]), Integer.valueOf(ymArr[1]), Integer.valueOf(day));
                    }
                }
            });
            container.addView(contentView);
            recordPosition = position;
            return contentView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(container.getChildAt(recordPosition));
        }
    }

    /*GridView适配器*/
    private class GridViewAdapter extends BaseAdapter {
        public List<ItemDataModel> gridViewList = null;
        private Integer y;
        private Integer m;

        public GridViewAdapter(Integer years, Integer month) {
            this.gridViewList = getDayByYeaAndMon(years, month);
            this.y = years;
            this.m = month;
        }

        @Override
        public int getCount() {
            return gridViewList.size();
        }

        @Override
        public Object getItem(int position) {
            return gridViewList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.calendar_item_layout, null);
                viewHolder = new ItemViewHolder();
                viewHolder.frameLayout = convertView.findViewById(R.id.frameLayout);
                viewHolder.textViewPrompt = (TextView) convertView.findViewById(R.id.textView_prompt);
                viewHolder.textViewNumber = (TextView) convertView.findViewById(R.id.textView_number);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemViewHolder) convertView.getTag();
            }
            ItemDataModel itemDataModel = gridViewList.get(position);
            if (itemDataModel != null) {
                if (itemDataModel.isPrompt) {
                    viewHolder.textViewPrompt.setVisibility(VISIBLE);
                } else {
                    viewHolder.textViewPrompt.setVisibility(INVISIBLE);
                }
                viewHolder.textViewNumber.setText(itemDataModel.day.toString());
                String tagDate = (y + "-" +
                        (m < 10 ? "0" + m : m) + "-" +
                        (itemDataModel.day.length() <= 1 ? "0" + itemDataModel.day : itemDataModel.day)
                ).replace(" ", "");
                if (tagDate.equals(getTagDate().trim())) {
                    viewHolder.frameLayout.setBackgroundResource(R.drawable.bg_calendar_item_2);
                    viewHolder.textViewNumber.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            return convertView;
        }
    }

    /* 日历控件核心代码 获取指定年月的日期集合 */
    private List<ItemDataModel> getDayByYeaAndMon(Integer years, Integer month) {
        List<ItemDataModel> list = new ArrayList<>();
        int ymMax = getYeaMonMaxDay(years, month);
        int week = getWeekByDate(years, month);
        /*填充空白符*/
        for (int i = 0; i < week; i++) {
            ItemDataModel model = new ItemDataModel();
            model.isPrompt = false;
            model.day = "";
            list.add(model);
        }
        /*填充日期*/
        for (Integer i = 1; i <= ymMax; i++) {
            ItemDataModel model = new ItemDataModel();
            model.isPrompt = isShowPrompt(years, month, i);
            model.day = i.toString();
            list.add(model);
        }
        return list;
    }

    /* 根据偏移量获取年月 2016-5 */
    private String getDataByOffset(Integer offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, offset);
        return new SimpleDateFormat("yyyy-MM").format(calendar.getTime());
    }

    /* 获取指定年月有多少天 */
    private Integer getYeaMonMaxDay(Integer years, Integer month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, years);
        calendar.set(Calendar.MONTH, month - 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /* 获取每月的第一天是星期几 （0.星期天 依次类推） */
    private Integer getWeekByDate(Integer years, Integer month) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTime(
                    new SimpleDateFormat("yyyy-MM-dd").parse(years + "-" + month + "-01"));
            return calendar.get(Calendar.DAY_OF_WEEK) - 1;
        } catch (Exception e) {
            throw new RuntimeException("获取每月一号的星期值错误");
        }
    }

    /* 是否显示提示 */
    private boolean isShowPrompt(Integer years, Integer month, Integer day) {
        if (calendarInterface == null) return false;
        List<String> dates = calendarInterface.getPromptDateList(years, month);
        String dateString = years + "-" + (month < 10 ? "0" + month : month) + "-" + day;
        for (String str : dates) {
            if (dateString.trim().equals(str.trim())) {
                return true;
            }
        }
        return false;
    }

    /*获取突出显示的日期 默认当天*/
    private String getTagDate() {
        if (calendarInterface == null) {
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        } else {
            return calendarInterface.getTagDate();
        }
    }

    static class ItemViewHolder {
        View frameLayout;
        TextView textViewPrompt;
        TextView textViewNumber;
    }

    static class ItemDataModel {
        String day;
        boolean isPrompt;
    }

    public static class PromptCalendarInterface {
        /*日期选择回调*/
        public void onSelectDate(Integer years, Integer month, Integer day) {
        }

        /*获取指定月的提醒日期列表 格式2016-05-25*/
        public List<String> getPromptDateList(Integer years, Integer month) {
            return new ArrayList<>();
        }

        /*返回要突出标记的日期 格式2016-05-25*/
        public String getTagDate() {
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }
    }
}
