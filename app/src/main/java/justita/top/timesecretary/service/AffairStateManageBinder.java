package justita.top.timesecretary.service;


import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import justita.top.timesecretary.alarms.AlarmStateManager;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.widget.WidgetAlertActivity;

public class AffairStateManageBinder extends IAffairServiceAIDL.Stub{

    private Context mContext;
    public AffairStateManageBinder(Context context) {
        mContext =context;
    }



    @Override
    public void registerNextAffairAlarm() throws RemoteException {
        AlarmStateManager.registerLastInstance(mContext);
    }

    @Override
    public void changeAffairState(long affairId) throws RemoteException {
        Affair affair = Affair.getAffair(mContext.getContentResolver(),affairId);
        switch (affair.mState){
            case Affair.AFFAIR_SILENT_STATE:{
                affair.mState = Affair.AFFAIR_FIRED_STATE;
                Affair.changeAffairState(mContext.getContentResolver(),affair);


                Intent intent = Affair.createIntent(mContext,WidgetAlertActivity.class,affairId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
            break;
        }
    }

    @Override
    public void setAffairCompleteState(long affairId) throws RemoteException {
        LogUtils.e("setAffairCompleteState");
        Affair affair = Affair.getAffair(mContext.getContentResolver(),affairId);

        affair.mState = Affair.AFFAIR_COMPLETE_STATE;

        removeAlarmByAffair(affairId);

        Affair.changeAffairState(mContext.getContentResolver(),affair);
        Intent intent = Affair.createIntent(mContext,AffairChangeReceiver.class,affairId);
        intent.setAction(AffairStateManageService.ALARM_CHANGE_ACTION);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void setAffairDelState(long affairId) throws RemoteException {
        Affair affair = Affair.getAffair(mContext.getContentResolver(),affairId);

        affair.mState = Affair.AFFAIR_DELETE_STATE;
        Affair.changeAffairState(mContext.getContentResolver(),affair);
        Intent intent = Affair.createIntent(mContext,AffairChangeReceiver.class,affairId);
        intent.setAction(AffairStateManageService.ALARM_CHANGE_ACTION);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void setAffairSilentState(long affairId) throws RemoteException {
        Affair affair = Affair.getAffair(mContext.getContentResolver(),affairId);
        affair.mState = Affair.AFFAIR_SILENT_STATE;
        Affair.changeAffairState(mContext.getContentResolver(),affair);
    }


    @Override
    public void removeAlarmByAffair(long affairId) throws RemoteException {
        AlarmStateManager.removeAlarmByAffair(mContext,affairId,true);
    }

    @Override
    public void addAlarmByAffair(long affairId) throws RemoteException {
        AlarmStateManager.addAlarmByAffair(mContext,affairId);
    }

    @Override
    public void updateAlarmByAffair(long affairId) throws RemoteException {

    }
}
