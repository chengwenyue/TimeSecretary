package justita.top.timesecretary.uitl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
    private static Gson gson = new GsonBuilder().create();
    public static Gson getGson() {
        return gson;
    }

}
