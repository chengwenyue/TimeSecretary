package justita.top.timesecretary.widget;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import justita.top.timesecretary.R;
import justita.top.timesecretary.app.BaseActivity;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.service.AffairStateManageService;
import justita.top.timesecretary.service.IAffairServiceAIDL;

public class WidgetAlertActivity extends BaseActivity {

    private AlertDialog alertDialog;
    private Affair mCurrentAffair;
    private IAffairServiceAIDL mIAffairServiceAIDL;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIAffairServiceAIDL = IAffairServiceAIDL.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIAffairServiceAIDL = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //hide the title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.alert_layout);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        mCurrentAffair = Affair.getAffair(getContentResolver(),Affair.getId(uri));
        initDialog();
        tryBindService();
    }

    private void initDialog() {
        View view = View.inflate(this,R.layout.alert_affair_alarm,null);
        ImageButton close = (ImageButton) view.findViewById(R.id.dialog_close_bt);
        close.getDrawable().setTint(getResources().getColor(R.color.black));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                WidgetAlertActivity.this.finishAndRemoveTask();
            }
        });
        TextView affairTime = (TextView) view.findViewById(R.id.dialog_affair_time);
        affairTime.setText(mCurrentAffair.getTimeLabel());

        TextView affairName = (TextView) view.findViewById(R.id.dialog_affair_name);
        affairName.setText(mCurrentAffair.mName);

        AlertDialog.Builder builder = new AlertDialog.Builder(WidgetAlertActivity.this,R.style.AlertDialogGlobal);
        builder.setView(view)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mIAffairServiceAIDL.setAffairCompleteState(mCurrentAffair.mId);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }finally {
                            WidgetAlertActivity.this.finishAndRemoveTask();
                        }
                    }
                })
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mIAffairServiceAIDL.setAffairSilentState(mCurrentAffair.mId);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }finally {
                            WidgetAlertActivity.this.finishAndRemoveTask();
                        }
                        WidgetAlertActivity.this.finishAndRemoveTask();
                    }
                });
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }
    private void tryBindService() {
        Intent bindIntent = new Intent(this,AffairStateManageService.class);
        bindIntent.setAction(AffairStateManageService.START_ACTION);
        bindService(bindIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}
