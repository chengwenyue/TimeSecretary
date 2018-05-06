package justita.top.timesecretary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import justita.top.timesecretary.R;
import justita.top.timesecretary.biz.OnLoginListener;
import justita.top.timesecretary.biz.UserBiz;
import justita.top.timesecretary.entity.User;
import justita.top.timesecretary.provider.ChatProvider;
import justita.top.timesecretary.provider.RosterProvider;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;

public class UserSelectActivity extends AppCompatActivity {

    private TextView mTitleSidebarName;
    private UserBiz mUserBiz = new UserBiz();
    private ListView UserNameListTv;
    private Button mBack;
    private TextView mLoginUser;
    private TextView mCancelLoginTv;
    private Map<Long, String> mIdsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_select);
        initView();
    }

    private void initView() {
        mTitleSidebarName = (TextView) findViewById(R.id.title_sidebar_name);
        mTitleSidebarName.setText("切换用户");
        mLoginUser = (TextView) findViewById(R.id.now_login_user);
        mLoginUser.setText("当前用户: " + PreferenceUtils.getPrefString(this,PreferenceConstants.ACCOUNT,""));
        mCancelLoginTv = (TextView) findViewById(R.id.cancel_login);
        mCancelLoginTv.setText("注销");
        mCancelLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getContentResolver().delete(ChatProvider.CONTENT_URI,null,new String[0]);
                getContentResolver().delete(RosterProvider.CONTENT_URI,null,new String[0]);

                Intent intent = new Intent(UserSelectActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                UserSelectActivity.this.finish();
            }
        });
        UserNameListTv = (ListView) findViewById(R.id.user_select_lv);
        UserNameListTv.setAdapter(new ArrayAdapter<>(this, R.layout.user_item, getData()));
        UserNameListTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             *
             * @param parent 当前ListView
             * @param view 代表当前被点击的条目
             * @param position 当前条目的位置
             * @param id 当前被点击的条目的id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userId = mIdsMap.get(id);
                final User user = new User();
                user.setUserName(UserBiz.getUserDataXml(UserSelectActivity.this, userId, "userName"));
                user.setUserPwd(UserBiz.getUserDataXml(UserSelectActivity.this, userId, "userPwd"));
                user.setUserEmail(UserBiz.getUserDataXml(UserSelectActivity.this, userId, "userEmail"));
                mUserBiz.login(user, new OnLoginListener() {
                    @Override
                    public void loginCallback(int connectedState, String reason) {
                        switch (connectedState){
                            case UserBiz.CONNECTED:{
                                startActivity(new Intent(UserSelectActivity.this,MainActivity.class));
                                finish();
                                }
                            break;
                            case UserBiz.CONNECTING:{
                            }
                            break;
                            case UserBiz.DISCONNECTED:{
                                Toast.makeText(UserSelectActivity.this, reason, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                });
            }
        });

        mBack = (Button) findViewById(R.id.back_white);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private List<String> getData() {
        List<String> data = new ArrayList<>();
        List<String> ids = UserBiz.getIds(this);
        long index = 0;
        for (String id : ids){
            mIdsMap.put(index, id);
            String userName = UserBiz.getUserDataXml(this, id, "userName");
            data.add(userName);
            index++;
        }
        return data;
    }
}
