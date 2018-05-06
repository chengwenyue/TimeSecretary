package justita.top.timesecretary.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import justita.top.timesecretary.R;
import justita.top.timesecretary.provider.Affair;

public class AlarmFragment extends BaseAttrFragment implements View.OnClickListener{

    private Button startAlarmBt;
    private Button endAlarmBt;
    private Button closeAlarmBt;
    private Button advance5Bt;
    private Button advance15Bt;
    private Button advance30Bt;

    private boolean hasStartAlarm = false;
    private boolean hasEndAlarm = false;
    private boolean hasCloseAlarm = true;
    private int advanceTime = 0;
    public AlarmFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        initView(view);

        String mType = mBuilder.getAffair().mType;
        if(mType.equals(Affair.NORMAL_TYPE_WITH_START_ALARM)
                || mType.equals(Affair.TIME_TYPE_WITH_START_ALARM)
                || mType.equals(Affair.TIME_TYPE_BOTH)){
            mBuilder.setStartAlarm(true);
        }
        if(mType.equals(Affair.TIME_TYPE_WITH_END_ALARM)
                || mType.equals(Affair.TIME_TYPE_BOTH)){
            mBuilder.setEndAlarm(true);
        }

        initViewButton();
        if(mBuilder.getAffair().getAdvanceTime() == 5){
            setButtonPressed(advance5Bt);
        }else if(mBuilder.getAffair().getAdvanceTime() == 15){
            setButtonPressed(advance15Bt);
        }else if(mBuilder.getAffair().getAdvanceTime() == 30){
            setButtonPressed(advance30Bt);
        }

        return view;
}


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            initViewButton();
        }
    }

    private void initViewButton() {
        hasStartAlarm = mBuilder.isHasStartAlarm();
        hasEndAlarm = mBuilder.isHasEndAlarm();
        hasCloseAlarm = (!hasStartAlarm && !hasEndAlarm);
        if(hasStartAlarm){
            setButtonPressed(startAlarmBt);
        }else {
            setButtonNormal(startAlarmBt);
        }

        if(hasEndAlarm){
            setButtonPressed(endAlarmBt);
        }else {
            setButtonNormal(endAlarmBt);
        }

        if(hasCloseAlarm){
            setButtonPressed(closeAlarmBt);
        }else {
            setButtonNormal(closeAlarmBt);
        }

    }

    private void initView(View view) {
        startAlarmBt = (Button) view.findViewById(R.id.start_alarm_bt);
        startAlarmBt.setOnClickListener(this);
        endAlarmBt = (Button) view.findViewById(R.id.end_alarm_bt);
        endAlarmBt.setOnClickListener(this);
        closeAlarmBt = (Button) view.findViewById(R.id.close_alarm_bt);
        closeAlarmBt.setOnClickListener(this);

        advance5Bt = (Button) view.findViewById(R.id.advance_5);
        advance5Bt.setOnClickListener(this);
        advance15Bt = (Button) view.findViewById(R.id.advance_15);
        advance15Bt.setOnClickListener(this);
        advance30Bt = (Button) view.findViewById(R.id.advance_30);
        advance30Bt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_alarm_bt:{
                if(hasStartAlarm)
                    return;

                mBuilder.setStartAlarm(true);
                initViewButton();
            }
            break;
            case R.id.end_alarm_bt:{
                if(hasEndAlarm)
                    return;

                if(mBuilder.getAffair().mType != Affair.TIME_TYPE){
                    Toast.makeText(getContext(),"您未设置持续时间",Toast.LENGTH_SHORT).show();
                    return;
                }
                mBuilder.setEndAlarm(true);
                initViewButton();

            }
            break;
            case R.id.close_alarm_bt:{
                if(hasCloseAlarm)
                    return;

                mBuilder.setStartAlarm(false);
                mBuilder.setEndAlarm(false);
                initViewButton();
            }
            break;
            case R.id.advance_5:{
                if(advanceTime != 5) {
                    setButtonNormal(advance15Bt);
                    setButtonNormal(advance30Bt);
                    setButtonPressed(advance5Bt);
                    advanceTime = 5;
                    mBuilder.setAdvanceTime(advanceTime);
                }
            }
            break;
            case R.id.advance_15:{
                if(advanceTime != 15) {
                    setButtonNormal(advance5Bt);
                    setButtonNormal(advance30Bt);
                    setButtonPressed(advance15Bt);
                    advanceTime = 15;
                    mBuilder.setAdvanceTime(advanceTime);
                }
            }
            break;
            case R.id.advance_30:{
                if(advanceTime != 30) {
                    setButtonNormal(advance5Bt);
                    setButtonNormal(advance15Bt);
                    setButtonPressed(advance30Bt);
                    advanceTime = 30;
                    mBuilder.setAdvanceTime(advanceTime);
                }
            }
            break;
        }
    }

    private void setButtonPressed(Button v){
        v.setBackground(getContext().getDrawable(R.drawable.bt_bg_alarm_selected));
        v.setTextColor(getResources().getColor(R.color.white));
    }
    private void setButtonNormal(Button v){
        v.setBackground(getContext().getDrawable(R.drawable.bt_bg_alarm));
        v.setTextColor(getResources().getColor(R.color.fragment3));
    }
}
