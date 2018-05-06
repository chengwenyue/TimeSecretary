package justita.top.timesecretary.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.adapter.AffairContract;
import justita.top.timesecretary.app.BaseActivity;
import justita.top.timesecretary.app.TSBroadcastReceiver;
import justita.top.timesecretary.dialog.CgySelectWindow;
import justita.top.timesecretary.fragment.AffairListFragment;
import justita.top.timesecretary.fragment.AffairTimeFragment;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.provider.Category;
import justita.top.timesecretary.service.AffairChangeReceiver;
import justita.top.timesecretary.service.AffairStateManageService;
import justita.top.timesecretary.service.DataSyncService;
import justita.top.timesecretary.service.IAffairServiceAIDL;
import justita.top.timesecretary.service.XXService;
import justita.top.timesecretary.uitl.AsyncHandler;
import justita.top.timesecretary.uitl.CounterUtils;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.OkHttpUtil;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;

public class MainActivity extends BaseActivity implements
        View.OnClickListener,TSBroadcastReceiver.EventHandler{
    public static final int RESULT_OK = 0x14;
    public static final int RESULT_SAVE = 0x13;
    public static final int RESULT_CANCEL = 0x12;
    public static final String AFFAIR_CATEGORY_CHANGE = "AFFAIR_CATEGORY_CHANGE";
    public static final String AFFAIR_CATEGORY_SELECT = "AFFAIR_CATEGORY_SELECT";
    private boolean isTime = false;
    private boolean isHide = false;
    private FloatingActionButton mFab;
    private DrawerLayout mDrawerLayout;
    private CgySelectWindow cgySelectWindow;
    private AffairListFragment listFragment = null;
    private AffairTimeFragment timeFragment = null;

    public View parent;

    private Button mListBt;
    private Button mTimeBt;
    private TextView mCategoryName;
    private LinearLayout mSocial;
    private LinearLayout mStat;
    private LinearLayout mPlan;
    private LinearLayout mManage;
    private LinearLayout mLogout;

    private SharedPreferences mAffairCompleteCounter;
    private SharedPreferences.Editor mEditor;

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

    public void setBackgroundAlpha(float fromAlpha,float toAlpha) {
        final WindowManager.LayoutParams lp = getWindow()
                .getAttributes();
        ValueAnimator animator = ObjectAnimator.ofFloat(fromAlpha,toAlpha);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                lp.alpha = value;
                getWindow().setAttributes(lp);
            }
        });
        animator.start();
    }

    public void openPopupWindow (View view){
        setBackgroundAlpha(1.0f,0.5f);
        if(cgySelectWindow == null)
            cgySelectWindow = new CgySelectWindow(this);

        if(cgySelectWindow.isShow())
            return;

        cgySelectWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(0.5f,1.0f);
            }
        });
        parent = this.findViewById(R.id.under);
        cgySelectWindow.show(parent);
    }

    @Override
    public void onNetChange() {
        if(OkHttpUtil.isNetworkAvailable(this)){
            DataSyncService.getInstance(this);
        }
    }

    public interface OnAffairStateChangeListener{
        void onAffairStateChange(Affair affair,int which);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        TSBroadcastReceiver.mListeners.add(this);

        isTime = PreferenceUtils.getPrefBoolean(this,PreferenceConstants.IS_TIME,false);
        isHide = PreferenceUtils.getPrefBoolean(this,PreferenceConstants.IS_HIDE,false);

        AffairChangeReceiver.addAffairStateChangeListener(mAffairChangeListener);
        tryBindService();
        initView();
    }

    private void tryBindService() {
        Intent bindIntent = new Intent(this,AffairStateManageService.class);
        bindIntent.setAction(AffairStateManageService.START_ACTION);
        bindService(bindIntent, mConnection, BIND_AUTO_CREATE);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.ic_cut);
        setSupportActionBar(toolbar);
        mCategoryName = (TextView) findViewById(R.id.tv_main_category_name);
        long categoryId = PreferenceUtils.getPrefLong(this,PreferenceConstants.MAIN_CATEGORY,-1);
        if(categoryId == -1){
            mCategoryName.setText("全部");
        }else{
            Category category = Category.getCategory(getContentResolver(),categoryId);
            mCategoryName.setText(category.mName);
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);

        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //是第二个参数的问题，该参数必须大于0才能在返回值，并激活onActivityResult方法。
                startActivityForResult(new Intent(MainActivity.this, AffairAddActivity.class), RESULT_OK);
            }
        });

        mListBt = (Button) findViewById(R.id.radio_list);
        mTimeBt = (Button) findViewById(R.id.radio_time);


        mListBt.setOnClickListener(this);
        mTimeBt.setOnClickListener(this);

        switchButtonBg(isTime);
        setContentFragment();

        mAffairCompleteCounter = getSharedPreferences(PreferenceUtils.getPrefLong(this, PreferenceConstants.USERID, 0)+
                "_AffairCompleteCounter", this.MODE_PRIVATE);
        mEditor = mAffairCompleteCounter.edit();

        mSocial = (LinearLayout) findViewById(R.id.dl_social);
        mSocial.setOnClickListener(this);
        mStat = (LinearLayout) findViewById(R.id.dl_stat);
        mStat.setOnClickListener(this);
        mPlan = (LinearLayout) findViewById(R.id.dl_plan);
        mPlan.setOnClickListener(this);
        mManage = (LinearLayout) findViewById(R.id.dl_manage);
        mManage.setOnClickListener(this);
        mLogout = (LinearLayout) findViewById(R.id.dl_logout);
        mLogout.setOnClickListener(this);

    }

    private void setContentFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(isTime){
            if(timeFragment ==null){
                timeFragment = new AffairTimeFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(PreferenceConstants.IS_HIDE,isHide);
                timeFragment.setArguments(bundle);

                timeFragment.setOnAffairStateChangeListener(mOnAffairStateChangeListener);
                ft.add(R.id.main_content,timeFragment);
            }else{
                ft.show(timeFragment);
            }

            if(listFragment != null){
                ft.hide(listFragment);
            }
        }else {
            if(listFragment ==null){
                listFragment = new AffairListFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(PreferenceConstants.IS_HIDE,isHide);
                listFragment.setArguments(bundle);

                listFragment.setOnAffairStateChangeListener(mOnAffairStateChangeListener);
                ft.add(R.id.main_content,listFragment);
            }else{
                ft.show(listFragment);
            }

            if(timeFragment != null){
                ft.hide(timeFragment);
            }

        }
        ft.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);


        MenuItem item = menu.findItem(R.id.hide_achieve_affair);
        if(isHide){
            item.setTitle("显示归档事件");
        }else{
            item.setTitle("隐藏归档事件");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.batch_summary:
            {
                Toast.makeText(this,"setting", Toast.LENGTH_SHORT).show();
            }
            break;
            case R.id.hide_achieve_affair:
            {
                if(!isHide){
                    if(listFragment != null)
                        listFragment.changeStrategy(AffairContract.HIDE_ACHIEVE_AFFAIR);
                    if(timeFragment != null)
                        timeFragment.changeStrategy(AffairContract.HIDE_TIME_CATEGORY_STRATEGY);
                    item.setTitle("显示归档事件");
                }else{
                    item.setTitle("隐藏归档事件");
                    if(listFragment != null)
                        listFragment.changeStrategy(AffairContract.DEFAULT_CATEGORY_STRATEGY);
                    if(timeFragment != null)
                        timeFragment.changeStrategy(AffairContract.DEFAULT_TIME_CATEGORY_STRATEGY);
                }
                isHide = !isHide;
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }else {
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        AffairChangeReceiver.removeAffairStateChangeListener(mAffairChangeListener);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent1= new Intent(this, XXService.class);
        intent1.setAction(XXService.LOGIN_ACTION);
        startService(intent1);
    }

    @Override
    protected void onPause() {
        PreferenceUtils.setPrefBoolean(this, PreferenceConstants.IS_TIME,isTime);
        PreferenceUtils.setPrefBoolean(this, PreferenceConstants.IS_HIDE,isHide);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.e("requestCode : "+requestCode+"  resultCode  : "+resultCode);
        switch (resultCode) {
            case RESULT_CANCEL:
                break;
            case RESULT_OK: {
                ContentResolver cr = this.getContentResolver();
                long affairId = Affair.getId(data.getData());
                Affair affair = Affair.getAffair(cr, affairId);
                if(listFragment != null)
                    listFragment.insertAffair(affair);
                if(timeFragment != null)
                    timeFragment.insertAffair(affair);
                try {
                    if(mIAffairServiceAIDL == null) {
                        tryBindService();
                        return;
                    }
                    mIAffairServiceAIDL.addAlarmByAffair(affairId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                break;
            case RESULT_SAVE: {
                ContentResolver cr = this.getContentResolver();
                long affairId = Affair.getId(data.getData());
                Affair affair = Affair.getAffair(cr, affairId);
                if(listFragment != null)
                    listFragment.updateAffair(affair);
                if(timeFragment != null)
                    timeFragment.updateAffair(affair);
            }
                break;
        }
    }


    //来自服务的事件状态变化，改变界面状态
    private AffairChangeReceiver.AffairChangeListener mAffairChangeListener = new AffairChangeReceiver.AffairChangeListener() {
        @Override
        public void onAffairChange(Intent intent) {
            String action = intent.getAction();
            if(!TextUtils.isEmpty(action)){
                if(action.equals(AffairStateManageService.ALARM_CHANGE_ACTION)){
                    LogUtils.e("ALARM_CHANGE_ACTION");
                    Affair affair = Affair.getAffair(getContentResolver(),Affair.getId(intent.getData()));
                    switch (affair.mState){
                        case Affair.AFFAIR_COMPLETE_STATE:
                            CounterUtils.affairCompleteCounter(mAffairCompleteCounter, mEditor);
                        case Affair.AFFAIR_DELETE_STATE:{
                            if(listFragment != null)
                                listFragment.updateAffair(affair);
                            if(timeFragment != null)
                                timeFragment.updateAffair(affair);
                        }
                        break;
                    }
                }
                if(action.equals(MainActivity.AFFAIR_CATEGORY_CHANGE)){
                    List<Affair> affairs = Affair.getAffairs(getContentResolver(),Affair.CATEGORY +"=?", Category.getId(intent.getData())+"");

                    ContentValues values =  new ContentValues(1);
                    values.put(Affair.CATEGORY,"1");
                    for(Affair affair :affairs){
                        Affair.updateAffair(getContentResolver(),values,affair.mId);
                        affair.mCategory = "1";
                        if(affair.mState == Affair.AFFAIR_DELETE_STATE)
                            return;

                        if(listFragment != null)
                            listFragment.updateAffair(affair);
                        if(timeFragment != null)
                            timeFragment.updateAffair(affair);
                    }

                }

                if(action.equals(MainActivity.AFFAIR_CATEGORY_SELECT)){
                    if(!isHide){
                        if(listFragment != null)
                            listFragment.changeStrategy(AffairContract.HIDE_ACHIEVE_AFFAIR);
                        if(timeFragment != null)
                            timeFragment.changeStrategy(AffairContract.HIDE_TIME_CATEGORY_STRATEGY);
                    }else{
                        if(listFragment != null)
                            listFragment.changeStrategy(AffairContract.DEFAULT_CATEGORY_STRATEGY);
                        if(timeFragment != null)
                            timeFragment.changeStrategy(AffairContract.DEFAULT_TIME_CATEGORY_STRATEGY);
                    }
                    mCategoryName.setText(intent.getStringExtra("categoryName"));

                }
            }
        }
    };


    //来自界面用户手动改变事件状态，改变提醒或数据
    private OnAffairStateChangeListener mOnAffairStateChangeListener = new OnAffairStateChangeListener() {
        @Override
        public void onAffairStateChange(final Affair affair,int which) {

            if(which == 1 && timeFragment != null)
                timeFragment.updateAffair(affair);

            if(which == 2 && listFragment != null)
                timeFragment.updateAffair(affair);
            AsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    Affair.changeAffairState(getContentResolver(),affair);

                    //事件未设置提醒时
                    if(affair.mType.equals(Affair.TIME_TYPE) || affair.mType.equals(Affair.NORMAL_TYPE)){
                        return;
                    }

                    //事件设置提醒时
                    switch (affair.mState){
                        case Affair.AFFAIR_COMPLETE_STATE:{
                            CounterUtils.affairCompleteCounter(mAffairCompleteCounter, mEditor);
                            try {
                                mIAffairServiceAIDL.removeAlarmByAffair(affair.mId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                        case Affair.AFFAIR_DELETE_STATE:{
                            try {
                                mIAffairServiceAIDL.removeAlarmByAffair(affair.mId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                        case Affair.AFFAIR_SILENT_STATE:{
                            try {
                                mIAffairServiceAIDL.addAlarmByAffair(affair.mId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                        default:{

                        }
                    }
                }
            });
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.radio_list :{
                isTime = false;
                switchButtonBg(isTime);
                setContentFragment();
            }
            break;
            case R.id.radio_time :{
                isTime = true;
                switchButtonBg(isTime);
                setContentFragment();
            }
            break;
            case R.id.dl_social :{
                startActivity(new Intent(MainActivity.this, SocialActivity.class));
            }
            break;
            case R.id.dl_stat :{
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            }
            break;
            case R.id.dl_plan :{
                startActivity(new Intent(MainActivity.this, PlanningActivity.class));
            }
            break;
            case R.id.dl_manage :{
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
            break;
            case R.id.dl_logout :{
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            break;
        }
    }
    private void switchButtonBg(boolean isTime){
        if(isTime){
            mTimeBt.setTextColor(getResources().getColor(R.color.toolbar_menu));
            mTimeBt.setBackgroundColor(getResources().getColor(R.color.white));

            mListBt.setBackground(getDrawable(R.drawable.shape_radio_button_left));
            mListBt.setTextColor(getResources().getColor(R.color.white));
        }else{
            mListBt.setTextColor(getResources().getColor(R.color.toolbar_menu));
            mListBt.setBackgroundColor(getResources().getColor(R.color.white));

            mTimeBt.setBackground(getDrawable(R.drawable.shape_radio_button_right));
            mTimeBt.setTextColor(getResources().getColor(R.color.white));
        }
    }
}