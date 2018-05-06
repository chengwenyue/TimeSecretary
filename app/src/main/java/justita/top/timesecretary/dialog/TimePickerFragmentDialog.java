package justita.top.timesecretary.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.uitl.DateUtils;
import justita.top.timesecretary.widget.CalendarAdapter;

public class TimePickerFragmentDialog extends DialogFragment implements View.OnClickListener{

    private ViewPager viewPager;
    private List<View> viewList;//view数组

    private static int jumpMonth = 0; // 每次滑动，增加或减去一个月,默认为0（即显示当前月）
    private static int jumpYear = 0; // 滑动跨越一年，则增加或者减去一年,默认为0(即当前年)

    private String selectDate = "";
    private String lastDate = "";
    private String sysYear = "";
    private int hourOfDay ;
    private int minute;

    private int currentPosition;
    private CalendarAdapter calV = null;

    private int start;
    private TimePickerDialog timePickerDialog;


    private RelativeLayout setTimeLL;
    private ImageView setTimeImg;
    private TextView yearTv;
    private TextView selectedDateTv;
    private TextView scrollDateTv;
    private Button goTodayBt;

    private TextView timeTv;
    private ImageButton removeBt;
    private OnDateAndTimeSetListener onDateAndTimeSetListener;


    public interface OnDateAndTimeSetListener{
        void onTimeSet(String selectDate,int hourOfDay,int minute,int which);
    }

    public void setOnDateAndTimeSetListener(OnDateAndTimeSetListener onDateAndTimeSetListener) {
        this.onDateAndTimeSetListener = onDateAndTimeSetListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        View view  = View.inflate(getContext(),R.layout.time_picker_dialog,null);
        initDate();
        initView(view);
        start = Integer.MAX_VALUE/2;
        viewList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            View calendar_item = View.inflate(getContext(),R.layout.calendar_gridview,null);
            viewList.add(calendar_item);
        }

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(start);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onDateAndTimeSetListener != null)
                    onDateAndTimeSetListener.onTimeSet(selectDate,hourOfDay,minute,which);
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onDateAndTimeSetListener != null)
                    onDateAndTimeSetListener.onTimeSet(selectDate,hourOfDay,minute,which);
            }
        }).setView(view);

        AlertDialog alertDialog =  builder.create();
        return alertDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initView(View view){
        setTimeLL = (RelativeLayout) view.findViewById(R.id.set_time);
        setTimeLL.setOnClickListener(this);
        setTimeImg = (ImageView) view.findViewById(R.id.set_time_img);
        VectorDrawable vectorDrawable = (VectorDrawable) setTimeImg.getDrawable();
        vectorDrawable.setTint(getResources().getColor(R.color.gray));

        timeTv = (TextView) view.findViewById(R.id.time_tv);
        removeBt = (ImageButton) view.findViewById(R.id.remove_time_bt);
        VectorDrawable vectorDrawable2 = (VectorDrawable) removeBt.getDrawable();
        vectorDrawable2.setTint(getResources().getColor(R.color.time_dialog_bg));
        removeBt.setOnClickListener(TimePickerFragmentDialog.this);
        removeBt.setVisibility(View.INVISIBLE);

        String nowYear = selectDate.split("-")[0];
        String nowMonth = selectDate.split("-")[1];
        String nowDay = selectDate.split("-")[2];
        yearTv = (TextView) view.findViewById(R.id.year_tv);
        yearTv.setText(nowYear);
        selectedDateTv = (TextView) view.findViewById(R.id.selectedDate_tv);
        selectedDateTv.setText(nowMonth+"月"+nowDay+"日");

        scrollDateTv = (TextView) view.findViewById(R.id.scrollDate_tv);
        goTodayBt = (Button) view.findViewById(R.id.goToday_bt);
        goTodayBt.setOnClickListener(this);

    }
    private PagerAdapter pagerAdapter = new PagerAdapter() {
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
            View view  = viewList.get(position % viewList.size());
            GridView gridView = (GridView)view .findViewById(R.id.bg_view);

            if(gridView.getOnItemClickListener() == null)
                gridView.setOnItemClickListener(onItemClickListener);
            jumpMonth = position - start;
            calV = new CalendarAdapter(getContext(), jumpMonth, jumpYear, selectDate);
            gridView.setAdapter(calV);
            container.addView(view);
            return view;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = viewList.get(position % viewList.size());
            container.removeView(view);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    };
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CalendarAdapter calendarAdapter1 = (CalendarAdapter) parent.getAdapter();
            String year = calendarAdapter1.getCurrentYear();
            String mouth = calendarAdapter1.getCurrentMonth();
            int day = calendarAdapter1.getDateByClickItem(position);
            selectDate = year +"-"+ mouth+"-"+day;

            if(lastDate.equals(selectDate))
                return;

            for(View view1 : viewList){
                GridView gridView = (GridView) view1.findViewById(R.id.bg_view);
                CalendarAdapter calendarAdapter = (CalendarAdapter) gridView.getAdapter();

                if(calendarAdapter != null){
                    calendarAdapter.changeBackgroundByDate(lastDate,CalendarAdapter.DEFALUT_STYLE);
                    calendarAdapter.changeBackgroundByDate(selectDate,CalendarAdapter.SELECTED_STYLE);
                }
            }
            selectedDateTv.setText(mouth+"月"+day+"日");
            lastDate = selectDate;
        }
    };


    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            currentPosition = position;
            View view = viewList.get(position % viewList.size());
            GridView gridView = (GridView) view.findViewById(R.id.bg_view);
            CalendarAdapter calendarAdapter = (CalendarAdapter) gridView.getAdapter();

            if(calendarAdapter == null)
                return;

            String month = calendarAdapter.getCurrentMonth();
            String year  = calendarAdapter.getCurrentYear();

            String scrollDate = DateUtils.formatDigit(Integer.valueOf(month) -1);
            if(calendarAdapter.getCurrentYear().equals(sysYear)){
                scrollDateTv.setText(scrollDate +"月");
            }else{
                scrollDateTv.setText(scrollDate +"月 " + calendarAdapter.getCurrentYear());
            }

            yearTv.setText(year);
            selectedDateTv.setText(month+"月"+"1"+"日");
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            TimePickerFragmentDialog.this.hourOfDay = hourOfDay;
            TimePickerFragmentDialog.this.minute = minute;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
            calendar.set(Calendar.MINUTE,minute);


            removeBt.setVisibility(View.VISIBLE);
            timeTv.setText(DateUtils.formatDate(calendar,"HH:mm"));
            timeTv.setTextColor(getResources().getColor(R.color.time_dialog_bg));

            VectorDrawable vectorDrawable = (VectorDrawable) setTimeImg.getDrawable();
            vectorDrawable.setTint(getResources().getColor(R.color.time_dialog_bg));
        }
    };

    public void initDate() {
        Calendar calendar = Calendar.getInstance();
        selectDate =lastDate = DateUtils.formatDate(calendar,DateUtils.DEFAULT_FORMAT_TWO);
        sysYear = selectDate.split("-")[0];
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.goToday_bt:{
                initDate();
                start = currentPosition;
                pagerAdapter.notifyDataSetChanged();
                String nowYear = selectDate.split("-")[0];
                String nowMonth = selectDate.split("-")[1];
                String nowDay = selectDate.split("-")[2];
                yearTv.setText(nowYear);
                selectedDateTv.setText(nowMonth+"月"+nowDay+"日");
            }
            break;
            case R.id.set_time:{
                if(timePickerDialog == null)
                    timePickerDialog = new TimePickerDialog(getContext(),R.style.AlertDialogCustom,onTimeSetListener,hourOfDay,minute,true);
                timePickerDialog.show();
            }
            break;
            case R.id.remove_time_bt:{
                removeBt.setVisibility(View.INVISIBLE);

                timeTv.setText("设置时间");
                timeTv.setTextColor(getResources().getColor(R.color.gray));

                VectorDrawable vectorDrawable = (VectorDrawable) setTimeImg.getDrawable();
                vectorDrawable.setTint(getResources().getColor(R.color.gray));
            }
            break;
        }
    }
}
