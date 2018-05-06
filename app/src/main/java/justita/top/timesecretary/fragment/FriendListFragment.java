package justita.top.timesecretary.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import justita.top.timesecretary.R;
import justita.top.timesecretary.activity.SocialActivity;
import justita.top.timesecretary.adapter.RosterAdapter;
import justita.top.timesecretary.app.BaseFragment;
import justita.top.timesecretary.provider.ChatProvider;
import justita.top.timesecretary.provider.RosterProvider;
import justita.top.timesecretary.service.IConnectionStatusCallback;
import justita.top.timesecretary.service.XXService;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;


public class FriendListFragment extends BaseFragment implements IConnectionStatusCallback{

    private Button mBack;
    private FrameLayout mSearchBt;
    private RecyclerView recyclerView;
    private ContentObserver mRosterObserver = new RosterObserver();
    private ContentObserver mChatObserver = new ChatObserver();
    private Handler mainHandler = new Handler();
    private RosterAdapter mRosterAdapter;


    private XXService mXxService;// Main服务
    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXxService = ((XXService.XXBinder) service).getService();
            mXxService.registerConnectionStatusCallback(FriendListFragment.this);
            // 如果没有连接上，则重新连接xmpp服务器
            if (!mXxService.isAuthenticated()) {
                String usr = PreferenceUtils.getPrefString(getContext(),
                        PreferenceConstants.ACCOUNT, "");
                String password = PreferenceUtils.getPrefString(getContext()
                        , PreferenceConstants.PASSWORD, "");
                mXxService.Login(usr, password);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXxService.unRegisterConnectionStatusCallback();
            mXxService = null;
        }

    };

    public FriendListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);
        setBackPressed(view);
        initView(view);
        return view;
    }

    private void setBackPressed(View view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    getActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        });
    }


    private void initView(View view) {
        mBack = (Button) view.findViewById(R.id.back_white);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        mSearchBt = (FrameLayout) view.findViewById(R.id.bt_search_friend);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), OrientationHelper.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        mRosterAdapter= new RosterAdapter(getContext(),recyclerView);
        mRosterAdapter.setOnRosterBtClick(onRosterBtClick);
        recyclerView.setAdapter(mRosterAdapter);
        mRosterAdapter.requery();
        mSearchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SocialActivity)getActivity()).setSearchFragment();
            }
        });
    }

    private RosterAdapter.OnRosterBtClick onRosterBtClick = new RosterAdapter.OnRosterBtClick() {
        @Override
        public void onBtClick(int which, final RosterAdapter.Roster roster) {

            switch (which) {
                case 0x01:{
                    ((SocialActivity)getActivity()).setCahtFragment(roster.getJid(),roster.getAlias());
                }
                break;
                case 0x02:{
                    Toast.makeText(getContext(),"等待开发",Toast.LENGTH_SHORT).show();
                }
                break;
                case 0x03:{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
                    builder.setTitle("删除")
                            .setMessage("确定要删除该好友吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(!isConnected()) {
                                        Toast.makeText(getContext(), "请先连接上再试", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    mXxService.removeRosterItem(roster.getJid());
                                }
                            }).setNegativeButton("取消",null);
                    builder.create().show();
                }
                break;
            }
            if(which == 1){
                ((SocialActivity)getActivity()).setCahtFragment(roster.getJid(),roster.getAlias());
            }

        }
    };

    private boolean isConnected() {
        return mXxService != null && mXxService.isAuthenticated();
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().getContentResolver().registerContentObserver(
                RosterProvider.CONTENT_URI, true, mRosterObserver);
        getContext().getContentResolver().registerContentObserver(ChatProvider.CONTENT_URI,
                true, mChatObserver);
        mRosterAdapter.requery();
        getContext().bindService(new Intent(getContext(),XXService.class),mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().getContentResolver().unregisterContentObserver(mRosterObserver);
        getContext().getContentResolver().unregisterContentObserver( mChatObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unbindService(mServiceConnection);
    }

    @Override
    public void connectionStatusChanged(int connectedState, String reason) {

    }

    private class RosterObserver extends ContentObserver {
        public RosterObserver() {
            super(mainHandler);
        }

        public void onChange(boolean selfChange) {
            LogUtils.d(FriendListFragment.class+ "RosterObserver.onChange: " + selfChange);
            if (mRosterAdapter != null)
                mainHandler.postDelayed(new Runnable() {
                    public void run() {
                        updateRoster();
                    }
                }, 100);
        }
    }

    private class ChatObserver extends ContentObserver {
        public ChatObserver() {
            super(mainHandler);
        }

        public void onChange(boolean selfChange) {
            if (mRosterAdapter != null)
                mainHandler.postDelayed(new Runnable() {
                    public void run() {
                        updateRoster();
                    }
                }, 100);
        }
    }

    public void updateRoster() {
        mRosterAdapter.requery();
    }

}
