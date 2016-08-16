# PromptCalendarDemo
一个可以带红点提示的android自定义日历控件

1. 布局文件中
<com.panxiong.calendar.PromptCalendar
        android:id="@+id/promptCalendar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
        
2. Activity中
        mPromptCalendar = (PromptCalendar) findViewById(R.id.promptCalendar);

        mPromptCalendar.setCalendarInterface(new PromptCalendar.PromptCalendarInterface() {
            @Override
            public void onSelectDate(Integer years, Integer month, Integer day) {
                String dateString = years + "-" + (month < 10 ? "0" + month : month) + "-" + day;
                Toast.makeText(MainActivity.this, dateString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public List<String> getPromptDateList(Integer years, Integer month) {
                List<String> dateList = new ArrayList<String>();
                dateList.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                return dateList; // 返回提醒日期集合
            }

            @Override
            public String getTagDate() {
                return super.getTagDate();  // 突出显示今天
            }
        });
