package justita.top.timesecretary.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import justita.top.timesecretary.R;
import justita.top.timesecretary.fragment.ChatFragment;
import justita.top.timesecretary.fragment.FriendListFragment;
import justita.top.timesecretary.fragment.SearchFriendFragment;
import justita.top.timesecretary.service.IConnectionStatusCallback;
import justita.top.timesecretary.service.XXService;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;

public class SocialActivity extends AppCompatActivity implements IConnectionStatusCallback {
    public final static String FRIEND_LIST_ACTION = "friend_list_action";
    public final static String FRIEND_CHAT_ACTION = "friend_chat_action";
    public final static String FRIEND_DETAIL_ACTION = "friend_detail_action";
    private FriendListFragment friendListFragment;
    private SearchFriendFragment searchFriendFragment;
    private ChatFragment chatFragment;
    private FragmentManager mFragmentManager;
    private XXService mXxService;

    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXxService = ((XXService.XXBinder) service).getService();
            mXxService.registerConnectionStatusCallback(SocialActivity.this);
            // 开始连接xmpp服务器
            if (!mXxService.isAuthenticated()) {
                String usr = PreferenceUtils.getPrefString(SocialActivity.this,
                        PreferenceConstants.ACCOUNT, "");
                String password = PreferenceUtils.getPrefString(
                        SocialActivity.this, PreferenceConstants.PASSWORD, "");
                mXxService.Login(usr, password);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXxService.unRegisterConnectionStatusCallback();
            mXxService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, XXService.class));
        setContentView(R.layout.activity_social);
        mFragmentManager = getSupportFragmentManager();
        String action = getIntent().getAction();
        if(TextUtils.isEmpty(action)
                || TextUtils.equals(action,FRIEND_LIST_ACTION)
                || TextUtils.equals(action,Intent.ACTION_MAIN)){
            setFriendListFragment();
        }else {
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        bindXMPPService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindXMPPService();
    }
    private void unbindXMPPService() {
        try {
            unbindService(mServiceConnection);
            LogUtils.i(SocialActivity.class+ "[SERVICE] Unbind");
        } catch (IllegalArgumentException e) {
            LogUtils.e(SocialActivity.class+"Service wasn't bound!");
        }
    }

    private void bindXMPPService() {
        LogUtils.i(SocialActivity.class+ "[SERVICE] Unbind");
        bindService(new Intent(SocialActivity.this, XXService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE
                        + Context.BIND_DEBUG_UNBIND);
    }
    public void setFriendListFragment() {
        if(friendListFragment == null){
            friendListFragment = new FriendListFragment();
        }
        if(!friendListFragment.isAdded()) {

            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(R.id.social_content, friendListFragment);
            ft.commit();
        }
    }

    public void setSearchFragment(){
        if(searchFriendFragment == null){
            searchFriendFragment = new SearchFriendFragment();
        }
        if(!searchFriendFragment.isAdded()) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(R.id.social_content, searchFriendFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    public void setCahtFragment(String userJid,String userName){
        if(chatFragment == null){
            chatFragment = ChatFragment.newInstance();
        }
        chatFragment.setUserName(userName);
        chatFragment.setUserJid(userJid);
        if(!chatFragment.isAdded()) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(R.id.social_content, chatFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void connectionStatusChanged(int connectedState, String reason) {
        LogUtils.e("connectionStatusChanged" + connectedState  +" : "+ reason);
    }
}
