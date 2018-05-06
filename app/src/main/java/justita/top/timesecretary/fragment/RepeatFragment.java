package justita.top.timesecretary.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import justita.top.timesecretary.R;
import justita.top.timesecretary.provider.DaysOfWeek;


public class RepeatFragment extends BaseAttrFragment implements View.OnClickListener{

    private LinearLayout mEverydayBt;
    private LinearLayout mWorkdayBt;
    private LinearLayout mDefinedDayBt;
    private boolean[] daysOfWeek = new boolean[7];
    // This determines the order in which it is shown and processed in the UI.

    public RepeatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_repeat, container, false);
        initView(view);

        DaysOfWeek daysOfWeeks = mBuilder.getAffair().getDaysOfWeek();
        daysOfWeeks.getSetDays();

        if (daysOfWeeks.getBitSet() == DaysOfWeek.ALL_DAYS_SET)
        {
            setButtonPress(mEverydayBt);
        }else if(daysOfWeeks.getBitSet() == DaysOfWeek.WORK_DAYS_SET){
            setButtonPress(mWorkdayBt);
        }else {
            for(int i = 0 ;i<7;i++){
                daysOfWeek[i] =daysOfWeeks.isBitEnabled(i);
            }
            setButtonPress(mDefinedDayBt);
        }

        return view;
    }

    private void initView(View view) {
        mEverydayBt = (LinearLayout) view.findViewById(R.id.bt_repeat_every_day);
        mEverydayBt.setOnClickListener(this);
        mWorkdayBt = (LinearLayout) view.findViewById(R.id.bt_repeat_work_day);
        mWorkdayBt.setOnClickListener(this);
        mDefinedDayBt = (LinearLayout) view.findViewById(R.id.bt_repeat_defined_day);
        mDefinedDayBt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_repeat_every_day :{
                setButtonPress(mEverydayBt);
                setButtonNormal(mWorkdayBt);
                setButtonNormal(mDefinedDayBt);
                for(int i = 0 ;i<7 ;i++){
                    daysOfWeek[i] = true;
                }
                mBuilder.setDaysOfWeek(daysOfWeek);
            }
            break;
            case R.id.bt_repeat_work_day :{
                setButtonPress(mWorkdayBt);
                setButtonNormal(mEverydayBt);
                setButtonNormal(mDefinedDayBt);
                for(int i = 0 ;i<7 ;i++){
                    if(i == 5 || i==6) {
                        daysOfWeek[i] = false;
                    }else {
                        daysOfWeek[i] = true;
                    }
                }
                mBuilder.setDaysOfWeek(daysOfWeek);
            }
            break;
            case R.id.bt_repeat_defined_day:{
                setButtonPress(mDefinedDayBt);
                setButtonNormal(mWorkdayBt);
                setButtonNormal(mEverydayBt);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
                View view = View.inflate(getContext(),R.layout.alert_layout_set_daysofweek,null);
                final LinearLayout dayButtons = (LinearLayout) view.findViewById(R.id.day_buttons);

                for(int i =0 ;i<7;i++){

                    final int buttonIndex = i;
                    final Button button = (Button) dayButtons.getChildAt(i);

                    //初始化按钮状态
                    if (daysOfWeek[buttonIndex]) {
                        turnOnButton(button);
                    } else {
                        turnOffButton(button);
                    }

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean isActivated = button.isActivated();
                            if (!isActivated) {
                                turnOnButton(button);
                            } else {
                                turnOffButton(button);
                            }
                        }
                    });
                }


                builder.setView(view).setTitle("自定义")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i =0 ;i<7;i++) {
                                    Button button = (Button) dayButtons.getChildAt(i);
                                    daysOfWeek[i] = button.isActivated();
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.create().show();
            }
            break;
            default:
        }
    }

    private void turnOffButton(Button button) {
        button.setActivated(false);
        button.setTextColor(getResources().getColor(R.color.black));
        button.setBackground(getResources().getDrawable(R.drawable.bt_day_normal));
    }

    private void turnOnButton(Button button) {
        button.setActivated(true);
        button.setTextColor(getResources().getColor(R.color.white));
        button.setBackground(getResources().getDrawable(R.drawable.bt_day_press));
    }

    private void setButtonPress(LinearLayout v) {
        v.setBackground(getContext().getResources().getDrawable(R.drawable.bt_repeat_press));
        ImageView imageView = (ImageView) v.getChildAt(0);
        imageView.getDrawable().setTint(getResources().getColor(R.color.white));
        TextView textView = (TextView) v.getChildAt(1);
        textView.setTextColor(getResources().getColor(R.color.white));
    }

    private void setButtonNormal(LinearLayout v) {
        v.setBackground(getContext().getResources().getDrawable(R.drawable.bt_repeat_normal));
        ImageView imageView = (ImageView) v.getChildAt(0);
        imageView.getDrawable().setTint(getResources().getColor(R.color.fragment4));
        TextView textView = (TextView) v.getChildAt(1);
        textView.setTextColor(getResources().getColor(R.color.fragment4));
    }
}
