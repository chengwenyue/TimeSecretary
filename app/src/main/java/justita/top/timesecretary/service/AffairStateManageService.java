package justita.top.timesecretary.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import justita.top.timesecretary.R;
import justita.top.timesecretary.activity.MainActivity;
import justita.top.timesecretary.alarms.AlarmStateManager;
import justita.top.timesecretary.provider.AlarmInstance;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.Utils;

public class AffairStateManageService extends Service{

    public final static String START_ACTION="START_ACTION";
    public final static String ALARM_CHANGE_ACTION = "alarm.change.action";
    private AffairStateManageBinder mBinder;
    public AffairStateManageService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        VectorDrawable vectorDrawable = (VectorDrawable) getDrawable(R.drawable.welcome_calendar);
        Bitmap bitmap = Utils.drawableToBitmap(vectorDrawable);

        Notification notify = new Notification.Builder(this)
                .setLargeIcon(bitmap)
                .setTicker("TimeSecretary听候差遣~~")
                .setContentTitle("待办事件")
                .setContentText("0")
                .setContentIntent(pendingIntent).setNumber(1).build();
        startForeground(1, notify);

        if(mBinder==null)
            mBinder =  new AffairStateManageBinder(this);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(TextUtils.isEmpty(intent.getAction()))
            return  super.onStartCommand(intent,flags,startId);
        LogUtils.e(intent.getAction());
        if(intent.getAction().equals(START_ACTION)){

            //服务开启后注册最近的提醒
            try {
                mBinder.registerNextAffairAlarm();

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(intent.getAction().equals(AlarmStateManager.ALARM_FIRE_ACTION)){
            Uri uri = intent.getData();
            AlarmInstance instance = AlarmInstance.getInstance(getContentResolver(),
                    AlarmInstance.getId(uri));
            if (instance == null) {
                LogUtils.e("Can not change state for unknown instance: " + uri);
            }else {
                LogUtils.e(instance.toString());
                try {
                    AlarmInstance.deleteInstance(getContentResolver(), instance.mId);

                    mBinder.changeAffairState(instance.mAffairId);

                    mBinder.registerNextAffairAlarm();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e("onDestroy");
        Intent service = new Intent(this, AffairStateManageService.class);
        this.startService(service);
    }

}
