package justita.top.timesecretary.provider;

import com.google.gson.JsonObject;

public interface SyncDataBean {
    public String getAddData();
    public String getUpdateData();
    public String getDeleteData();

    public String getAddUrl();
    public String getUpdateUrl();
    public String getDeleteUrl();
    public String getSyncDataByUserUrl();



    public void syncAddCallback(JsonObject jsonObject,long dataId);
    public void syncUpdateCallback(JsonObject jsonObject,long dataId);
    public void syncDeleteCallback(JsonObject jsonObject,long dataId);

    public void syncGetDataCallback(JsonObject jsonObject);
}
