package justita.top.timesecretary.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import justita.top.timesecretary.R;
import justita.top.timesecretary.dialog.TimePickerFragmentDialog;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.uitl.DateUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;


public class TimeFragment extends BaseAttrFragment implements View.OnClickListener{

    private LinearLayout mAffairStartBt;
    private LinearLayout mAffairCloseBt;
    private LinearLayout mAffairContinueBt;
    private TextView selectTimeTv;
    private Calendar selectTime ;
    private Calendar endTime;
    TimePickerFragmentDialog timePickerFragmentDialog;
    AlertDialog timeContinueDialog;
    public TimeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View  view  = inflater.inflate(R.layout.fragment_time, container, false);
        selectTime = Calendar.getInstance();
        initView(view);
        return view;
    }

    private void initView(View view) {
        mAffairStartBt = (LinearLayout) view.findViewById(R.id.affair_start_time_bt);
        mAffairCloseBt = (LinearLayout) view.findViewById(R.id.affair_close_continue_bt);
        mAffairContinueBt = (LinearLayout) view.findViewById(R.id.affair_continue_time_bt);
        selectTimeTv = (TextView) view.findViewById(R.id.select_time);

        selectTimeTv.setText(mBuilder.getAffair().mTime);
        mAffairStartBt.setOnClickListener(this);
        mAffairCloseBt.setOnClickListener(this);
        mAffairContinueBt.setOnClickListener(this);
    }
    private TimePickerFragmentDialog.OnDateAndTimeSetListener
            onDateAndTimeSetListener = new TimePickerFragmentDialog.OnDateAndTimeSetListener() {
        @Override
        public void onTimeSet(String selectDate, int hourOfDay, int minute,int which) {
            try {
                Date date = DateUtils.formatDate(selectDate,DateUtils.DEFAULT_FORMAT_TWO);

                selectTime.setTime(date);
                selectTime.set(Calendar.HOUR_OF_DAY,hourOfDay);
                selectTime.set(Calendar.MINUTE,minute);

                String time = DateUtils.formatDate(selectTime,DateUtils.DEFAULT_FORMAT);

                mBuilder.setType(Affair.NORMAL_TYPE);
                mBuilder.setStartAlarm(false);
                mBuilder.setEndAlarm(false);

                mBuilder.setTime(time);

                selectTimeTv.setText(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.affair_start_time_bt:{
                if(timePickerFragmentDialog == null) {
                    timePickerFragmentDialog = new TimePickerFragmentDialog();
                    timePickerFragmentDialog.setOnDateAndTimeSetListener(onDateAndTimeSetListener);
                }
                timePickerFragmentDialog.show(getFragmentManager(),"TimePicker");
            }
            break;
            case R.id.affair_close_continue_bt:{
                endTime = (Calendar) selectTime.clone();
                final String time  = DateUtils.formatDate(selectTime,DateUtils.DEFAULT_FORMAT);

                if(mBuilder.isHasEndAlarm()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
                    builder.setTitle("警告").setMessage("已设置结束提醒，若关闭持续则关闭结束提醒，是否继续？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBuilder.setTime(time);
                                    mBuilder.setEndAlarm(false);
                                    mBuilder.setType(Affair.NORMAL_TYPE);

                                    selectTimeTv.setText(time);
                                }
                            }).setNegativeButton("否",null).create().show();
                }else {
                    mBuilder.setTime(time);
                    mBuilder.setType(Affair.NORMAL_TYPE);

                    selectTimeTv.setText(time);
                }
            }
            break;
            case R.id.affair_continue_time_bt:{
                endTime = (Calendar) selectTime.clone();
                if(timeContinueDialog == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
                    View view  = View.inflate(getContext(),R.layout.list_item_time_cont,null);
                    final NumberPicker numberPicker_minute = (NumberPicker) view.findViewById(R.id.numberPicker_minute);
//                    numberPicker_minute.setFormatter(new NumberPicker.Formatter() {
//                        @Override
//                        public String format(int value) {
//                            String tmpStr = String.valueOf(value);
//                            if (value < 10) {
//                                tmpStr = "0" + tmpStr;
//                            }
//                            return tmpStr;
//                        }
//                    });

                    View.OnClickListener onClickListener =  new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    };
                    numberPicker_minute.setMinValue(0);
                    numberPicker_minute.setMaxValue(59 - selectTime.get(Calendar.MINUTE));
                    numberPicker_minute.setOnClickListener(onClickListener);

                    final NumberPicker numberPicker_hour = (NumberPicker) view.findViewById(R.id.numberPicker_hour);
                    numberPicker_hour.setMinValue(0);
                    numberPicker_hour.setMaxValue(23 - selectTime.get(Calendar.HOUR_OF_DAY));
                    numberPicker_hour.setOnClickListener(onClickListener);

                    builder.setView(view).setTitle("持续时间").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(numberPicker_hour.getValue() == 0 && numberPicker_minute.getValue()==0)
                                return;

                            endTime.add(Calendar.HOUR_OF_DAY,numberPicker_hour.getValue());
                            endTime.add(Calendar.MINUTE,numberPicker_minute.getValue());

                            String time = DateUtils.formatDate(selectTime,DateUtils.DEFAULT_FORMAT)+
                                    PreferenceConstants.TIME_SEPARATOR+
                                    DateUtils.formatDate(endTime,DateUtils.HOUR_MINUTE);

                            mBuilder.setTime(time);

                            mBuilder.setType(Affair.TIME_TYPE);
                            selectTimeTv.setText(time);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    timeContinueDialog = builder.create();
                    timeContinueDialog.show();
                }else
                    timeContinueDialog.show();
            }
            break;
        }
    }

}
