package justita.top.timesecretary.biz;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import justita.top.timesecretary.app.BaseApp;
import justita.top.timesecretary.entity.User;
import justita.top.timesecretary.uitl.GsonUtil;
import justita.top.timesecretary.uitl.OkHttpUtil;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.Utils;

public class UserBiz {

    public static final int CONNECTED = 0;
    public static final int DISCONNECTED = -1;
    public static final int CONNECTING = 1;
    private final String TAG = this.getClass().toString();
    private UserLoginTask mUserLoginTask;
    private UserRegisterTask mUserRegisterTask;

    public void login(final User user, final OnLoginListener loginListener) {
        String url = Utils.webUrl + "login";
        String userInfo = GsonUtil.getGson().toJson(user);
        Log.i(TAG, url);
        if (mUserLoginTask == null) {
            mUserLoginTask = new UserLoginTask(loginListener);
            mUserLoginTask.execute(new String[]{url, "userLogin", userInfo});
        }
    }

    public void cancelLogin(User user, OnLoginListener loginListener) {
        if (mUserLoginTask != null) {
            if (AsyncTask.Status.RUNNING == mUserLoginTask.getStatus() ||
                    AsyncTask.Status.PENDING == mUserLoginTask.getStatus()) {
                mUserLoginTask.cancel(true);
                mUserLoginTask = null;
                loginListener.loginCallback(DISCONNECTED,"Cancel");
            }
        }
    }

    public void register(final User user, final OnRegisterListener registerListener) {
        // http://www.justita.top:8080/TimeSecretaryWeb/register?name=admin&password=admin
        String url = Utils.webUrl + "register";
        String userInfo = GsonUtil.getGson().toJson(user);
        Log.i(TAG, url);
        if (mUserRegisterTask == null) {
            mUserRegisterTask = new UserRegisterTask(registerListener);
            mUserRegisterTask.execute(new String[]{url, "userRegister", userInfo});
        }
    }

    public void cancelRegister(User user, OnRegisterListener registerListener) {
        if (mUserRegisterTask != null) {
            if (AsyncTask.Status.RUNNING == mUserRegisterTask.getStatus() ||
                    AsyncTask.Status.PENDING == mUserRegisterTask.getStatus()) {
                mUserRegisterTask.cancel(true);
                mUserRegisterTask = null;
                registerListener.cancelRegister();
            }
        }
    }

    private class UserRegisterTask extends AsyncTask<String, Boolean, String> {
            private final OnRegisterListener mRegisterListener;

            public UserRegisterTask(final OnRegisterListener registerListener) {
                this.mRegisterListener = registerListener;
            }

            @Override
            protected String doInBackground(String... url) {
                String json = null;
                try {
                    publishProgress(true);
                    Thread.sleep(1000);
                    json = OkHttpUtil.postJson(url[0], url[1], url[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress(false);
                }
                return json;
            }


            @Override
            protected void onPostExecute(String json) {
            if (TextUtils.isEmpty(json)) {
                mRegisterListener.registerFailed("连接失败");
                return;
            }
            JsonObject result = GsonUtil.getGson().fromJson(json, JsonObject.class);
            if (!result.get("user").isJsonNull()) {
                JsonElement userEmt = result.get("user");
                User user = GsonUtil.getGson().fromJson(userEmt.toString(), User.class);
                mRegisterListener.registerSuccess("登陆成功", user);
            } else {
                mRegisterListener.registerFailed("请检查用户名密码");
            }
        }


        @Override
        protected void onProgressUpdate(Boolean... values) {
            if (!values[0]) {
                mRegisterListener.registerFailed("连接失败");
            } else {
                mRegisterListener.registernNow();
            }
        }
    }

    private class UserLoginTask extends AsyncTask<String, Boolean, String> {
        private OnLoginListener mLoginListener;

        public UserLoginTask(final OnLoginListener loginListener) {
            this.mLoginListener = loginListener;
        }

        @Override
        protected String doInBackground(String... url) {
            String json = null;
            try {
                publishProgress(true);
                Thread.sleep(1000);
                json = OkHttpUtil.postJson(url[0], url[1], url[2]);
            } catch (Exception e) {
                e.printStackTrace();
                publishProgress(false);
            }
            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            if (TextUtils.isEmpty(json)) {
                mLoginListener.loginCallback(DISCONNECTED,"连接失败");
                return;
            }
            JsonObject result = GsonUtil.getGson().fromJson(json, JsonObject.class);
            if (result.get("success").getAsBoolean()) {
                JsonElement userEmt = result.get("user");
                if(userEmt != null && !userEmt.isJsonNull()){
                    User user = GsonUtil.getGson().fromJson(userEmt.toString(), User.class);
                    PreferenceUtils.setSettingLong(BaseApp.getInstance(), PreferenceConstants.USERID,user.getId());
                    PreferenceUtils.setPrefString(BaseApp.getInstance(), PreferenceConstants.ACCOUNT, user.getUserName());
                    PreferenceUtils.setPrefString(BaseApp.getInstance(), PreferenceConstants.PASSWORD, user.getUserPwd());
                    PreferenceUtils.setPrefString(BaseApp.getInstance(), PreferenceConstants.EMAIL, user.getUserEmail());
                    UserBiz.onSaveItemXml(BaseApp.getInstance(), user);
                    mLoginListener.loginCallback(CONNECTED,"登陆成功");
                }else{
                    mLoginListener.loginCallback(DISCONNECTED,"请检查用户名密码");
                }
            } else {
                mLoginListener.loginCallback(DISCONNECTED,"请检查用户名密码");
            }
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            if (!values[0]) {
                mLoginListener.loginCallback(DISCONNECTED,"连接失败");
            } else {
                mLoginListener.loginCallback(CONNECTING,"连接中");
            }
        }
    }

    public static void onSaveItemXml(Context context, User user) {
        Document doc;
        Element root;
        try {
            SAXReader reader = new SAXReader();
            doc = reader.read(new File(context.getFilesDir().toString() + "/UsersItem.xml"));
            root = doc.getRootElement();
        } catch (DocumentException e) {
            doc = DocumentHelper.createDocument();
            root = doc.addElement("users");
        }

        // 判断重复
        List<Element> listElement = root.elements();
        for (Element e : listElement) {
            List<Attribute> listAttr = e.attributes();
            for (Attribute attr : listAttr) {
                String value = attr.getValue();
                if (String.valueOf(user.getId()).equals(value)) {
                    return;
                }
            }
        }

        List list = root.elements();
        Element userEle = DocumentHelper.createElement("user");
        userEle.addAttribute("id", String.valueOf(user.getId()));
        Element nameEle = userEle.addElement("userName");
        nameEle.setText(user.getUserName());
        Element pwdEle = userEle.addElement("userPwd");
        pwdEle.setText(user.getUserPwd());
        Element emailEle = userEle.addElement("userEmail");
        emailEle.setText(user.getUserEmail());
        list.add(userEle);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("utf-8");
        Writer out;
        try {
            out = new FileWriter(new File(context.getFilesDir() + "/UsersItem.xml"));
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(doc);
            writer.close();
            System.out.println(context.getFilesDir().toString() + "/UsersItem.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUserDataXml(Context context, String userId, String userData) {
        String userName;
        String userPwd;
        String userEmail;
        SAXReader reader = new SAXReader();
        Document doc;
        try {
            doc = reader.read(new File(context.getFilesDir().toString() + "/UsersItem.xml"));
            Element root = doc.getRootElement();
            List<Element> listElement = root.elements();
            for (Element e : listElement) {
                Attribute attr = e.attribute("id");
                String id = attr.getText();
                if (userId.equals(id)) {
                    List<Element> listElement2 = e.elements();
                    for (Element e2 : listElement2) {
                        if (e2.getName().equals(userData)) {
                            userName = e2.getTextTrim();
                            return userName;
                        }else if (e2.getName().equals(userData)) {
                            userPwd = e2.getTextTrim();
                            return userPwd;
                        }else if (e2.getName().equals(userData)) {
                            userEmail = e2.getTextTrim();
                            return userEmail;
                        }
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getIds(Context context){
        List<String> list = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document doc;
        try {
            doc = reader.read(new File(context.getFilesDir().toString() + "/UsersItem.xml"));
            Element root = doc.getRootElement();
            List<Element> listElement = root.elements();
            for (Element e : listElement) {
                Attribute attr = e.attribute("id");
                String id = attr.getText();
                list.add(id);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return list;
    }
}
