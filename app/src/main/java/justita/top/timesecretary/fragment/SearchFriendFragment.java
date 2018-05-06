package justita.top.timesecretary.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.entity.User;
import justita.top.timesecretary.service.IConnectionStatusCallback;
import justita.top.timesecretary.service.XXService;
import justita.top.timesecretary.uitl.AsyncHandler;
import justita.top.timesecretary.uitl.GsonUtil;
import justita.top.timesecretary.uitl.OkHttpUtil;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.Utils;

public class SearchFriendFragment extends Fragment implements IConnectionStatusCallback{

    private Button mSearchBt;
    private EditText mSearchContent;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private UserSearchAdapter mAdapter;
    private List<User> userList = new ArrayList<>();

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x11:{
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"请重新尝试",Toast.LENGTH_SHORT).show();
                }
                break;
                case 0x12:{
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    closeKeyboard();
                }
                break;
                case 0x13:{
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    };

    private XXService mXxService;// Main服务
    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXxService = ((XXService.XXBinder) service).getService();
            mXxService.registerConnectionStatusCallback(SearchFriendFragment.this);
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
    public SearchFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_friend, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().bindService(new Intent(getContext(),XXService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unbindService(mServiceConnection);
    }

    private void initView(View view) {
        mSearchContent = (EditText) view.findViewById(R.id.et_search_content);
        mSearchContent.setFocusable(true);
        mSearchContent.setFocusableInTouchMode(true);
        mSearchContent.requestFocus();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager =
                        (InputMethodManager)mSearchContent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mSearchContent, 0);
            }
        },500);

        mSearchContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mProgressBar.setVisibility(View.GONE);
                return false;
            }
        });
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mSearchBt = (Button) view.findViewById(R.id.bt_search);
        mSearchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userName = mSearchContent.getText().toString().trim();

                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(getContext(),"请输入内容",Toast.LENGTH_SHORT).show();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                AsyncHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject o = new JSONObject();
                        try {
                            o.put("userName",userName);
                            String result = OkHttpUtil.postJson(Utils.webUrl +"search","userSearch",o.toString());
                            JsonObject root = GsonUtil.getGson().fromJson(result, JsonObject.class);
                            if(!root.has("success")) {
                                mHandler.sendEmptyMessage(0x11);
                                return;
                            }
                            if(!userList.isEmpty())
                                userList.clear();

                            if(!root.has("users")){
                                mHandler.sendEmptyMessage(0x13);
                                return;
                            }

                            JsonArray jsonArray = root.getAsJsonArray("users");

                            for(int i =0;i<jsonArray.size();i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                User user = GsonUtil.getGson().fromJson(jsonObject.toString(),User.class);
                                userList.add(user);
                            }

                            mHandler.sendEmptyMessage(0x12);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mHandler.sendEmptyMessage(0x11);
                        }
                    }
                });
            }
        });
        mAdapter = new UserSearchAdapter();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycleView_user_search);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
//        mSearchCancel = (Button) view.findViewById(R.id.bt_search_cancel);
//        mSearchCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                closeKeyboard();
//
//                //1
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.popBackStack();
//
//                //2
//                //getActivity().onBackPressed();
//            }
//        });
    }

    private void closeKeyboard(){
        View view = getActivity().getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void connectionStatusChanged(int connectedState, String reason) {

    }

    private class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(getContext(),R.layout.list_item_user_search,null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindData(position);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView userNameTv;
            public ImageButton userAddBt;
            public ViewHolder(View itemView) {
                super(itemView);
                userNameTv = (TextView) itemView.findViewById(R.id.tv_userName);
                userAddBt = (ImageButton) itemView.findViewById(R.id.bt_userAdd);
            }

            public void bindData(int position){
                final User user = userList.get(position);

                userNameTv.setText(user.getUserName());
                userAddBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mXxService.addRosterItem(user.getUserName()+"@localhost",user.getUserName(),"我的好友");
                        Toast.makeText(getContext(),"好友请求发送成功",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }
}
