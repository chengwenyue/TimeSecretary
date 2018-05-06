package justita.top.timesecretary.fragment;


import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import justita.top.timesecretary.R;
import justita.top.timesecretary.adapter.ChatAdapter;
import justita.top.timesecretary.provider.ChatProvider;
import justita.top.timesecretary.provider.ChatProvider.ChatConstants;
import justita.top.timesecretary.provider.RosterProvider;
import justita.top.timesecretary.service.IConnectionStatusCallback;
import justita.top.timesecretary.service.XXService;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.StatusMode;
import justita.top.timesecretary.uitl.XMPPHelper;
import justita.top.timesecretary.widget.MsgListView;


public class ChatFragment extends Fragment implements IConnectionStatusCallback,
        View.OnClickListener,View.OnTouchListener,MsgListView.IXListViewListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private MsgListView mMsgListView;// 对话ListView
    private Button mSendMsgBtn;// 发送消息button
    private TextView mTitleNameView;// 标题栏
    private EditText mChatEditText;// 消息输入框
    private InputMethodManager mInputMethodManager;
    private Handler mHandler = new Handler();
    private WindowManager.LayoutParams mWindowNanagerParams;


    private static final String[] PROJECTION_FROM = new String[] {
            ChatProvider.ChatConstants._ID, ChatProvider.ChatConstants.DATE,
            ChatProvider.ChatConstants.DIRECTION,
            ChatProvider.ChatConstants.JID, ChatProvider.ChatConstants.MESSAGE,
            ChatProvider.ChatConstants.DELIVERY_STATUS };// 查询字段

    private ContentObserver mContactObserver = new ContactObserver();// 联系人数据监听，主要是监听对方在线状态
    private XXService mXxService;// Main服务
    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXxService = ((XXService.XXBinder) service).getService();
            mXxService.registerConnectionStatusCallback(ChatFragment.this);
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

    private String userJid;
    private String userName;

    public void setUserJid(String userJid) {
        this.userJid = userJid;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ChatFragment() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_send:// 发送消息
                sendMessageIfNotNull();
                break;
            default:
                break;
        }
    }

    private void sendMessageIfNotNull() {
        if (mChatEditText.getText().length() >= 1) {
            if (mXxService != null) {
                mXxService.sendMessage(userJid, mChatEditText.getText()
                        .toString());
                if (!mXxService.isAuthenticated())
                    Toast.makeText(getContext(),"消息已经保存随后发送",Toast.LENGTH_SHORT).show();
            }
            mChatEditText.setText(null);
            mSendMsgBtn.setEnabled(false);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.msg_listView:
                mInputMethodManager.hideSoftInputFromWindow(
                        mChatEditText.getWindowToken(), 0);
                break;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        mMsgListView.stopRefresh();
    }

    @Override
    public void onLoadMore() {

    }

    /**
     * 联系人数据库变化监听
     *
     */
    private class ContactObserver extends ContentObserver {
        public ContactObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            LogUtils.d("ContactObserver.onChange: " + selfChange);
            updateContactStatus();// 联系人状态变化时，刷新界面
        }
    }

    // 查询联系人数据库字段
    private static final String[] STATUS_QUERY = new String[] {
            RosterProvider.RosterConstants.STATUS_MODE,
            RosterProvider.RosterConstants.STATUS_MESSAGE, };

    private void updateContactStatus() {
        Cursor cursor = getContext().getContentResolver().query(RosterProvider.CONTENT_URI,
                STATUS_QUERY, RosterProvider.RosterConstants.JID + " = ?",
                new String[] { userJid }, null);
        int MODE_IDX = cursor
                .getColumnIndex(RosterProvider.RosterConstants.STATUS_MODE);
        int MSG_IDX = cursor
                .getColumnIndex(RosterProvider.RosterConstants.STATUS_MESSAGE);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            int status_mode = cursor.getInt(MODE_IDX);
            String status_message = cursor.getString(MSG_IDX);
            LogUtils.d("contact status changed: " + status_mode + " " + status_message);
            mTitleNameView.setText(XMPPHelper.splitJidAndServer(userName));
            int statusId = StatusMode.values()[status_mode].getDrawableId();
            if (statusId != -1) {// 如果对应离线状态
                // Drawable icon = getResources().getDrawable(statusId);
                // mTitleNameView.setCompoundDrawablesWithIntrinsicBounds(icon,
                // null,
                // null, null);
//                mTitleNameView.append("(离线)");
            } else {
            }
        }
        cursor.close();
    }
    /**
     * 设置聊天的Adapter
     */
    private void setChatWindowAdapter() {
        String selection = ChatConstants.JID + "='" + userJid + "'";
        // 异步查询数据库
        new AsyncQueryHandler(getContext().getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie,
                                           Cursor cursor) {
                // ListAdapter adapter = new ChatWindowAdapter(cursor,
                // PROJECTION_FROM, PROJECTION_TO, mWithJabberID);
                ListAdapter adapter = new ChatAdapter(getContext(),
                        cursor, PROJECTION_FROM);
                mMsgListView.setAdapter(adapter);
                mMsgListView.setSelection(adapter.getCount() - 1);
            }

        }.startQuery(0, null, ChatProvider.CONTENT_URI, PROJECTION_FROM,
                selection, null, null);
        // 同步查询数据库，建议停止使用,如果数据庞大时，导致界面失去响应
        // Cursor cursor = managedQuery(ChatProvider.CONTENT_URI,
        // PROJECTION_FROM,
        // selection, null, null);
        // ListAdapter adapter = new ChatWindowAdapter(cursor, PROJECTION_FROM,
        // PROJECTION_TO, mWithJabberID);
        // mMsgListView.setAdapter(adapter);
        // mMsgListView.setSelection(adapter.getCount() - 1);
    }

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        mChatEditText.setFocusable(true);
        mChatEditText.setFocusableInTouchMode(true);
        mChatEditText.requestFocus();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager =
                        (InputMethodManager)mChatEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mChatEditText, 0);
            }
        },500);

        bindXMPPService();
        setChatWindowAdapter();// 初始化对话数据

        updateContactStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        initView(view);
        setChatWindowAdapter();// 初始化对话数据
        getContext().getContentResolver().registerContentObserver(
                RosterProvider.CONTENT_URI, true, mContactObserver);// 开始监听联系人数据库
        return view;
    }



    /**
     * 解绑服务
     */
    private void unbindXMPPService() {
        try {
            getContext().unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            LogUtils.e("Service wasn't bound!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindXMPPService();
        getContext().getContentResolver().unregisterContentObserver(mContactObserver);// 开始监听联系人数据库
    }

    /**
     * 绑定服务
     */
    private void bindXMPPService() {
        Intent mServiceIntent = new Intent(getContext(), XXService.class);
        Uri chatURI = Uri.parse(userJid);
        mServiceIntent.setData(chatURI);
        getContext().bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initView(View view) {
        mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mWindowNanagerParams = getActivity().getWindow().getAttributes();

        mMsgListView = (MsgListView) view.findViewById(R.id.msg_listView);
        // 触摸ListView隐藏表情和输入法
        mMsgListView.setOnTouchListener(this);
        mMsgListView.setPullLoadEnable(false);
        mMsgListView.setXListViewListener(this);
        mSendMsgBtn = (Button) view.findViewById(R.id.bt_send);
        mChatEditText = (EditText) view.findViewById(R.id.et_input);
        mChatEditText.setOnTouchListener(this);
        mTitleNameView = (TextView) view.findViewById(R.id.ivTitleName);
        mChatEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mSendMsgBtn.setEnabled(true);
                } else {
                    mSendMsgBtn.setEnabled(false);
                }
            }
        });
        mSendMsgBtn.setOnClickListener(this);
    }

    @Override
    public void connectionStatusChanged(int connectedState, String reason) {

    }
}
