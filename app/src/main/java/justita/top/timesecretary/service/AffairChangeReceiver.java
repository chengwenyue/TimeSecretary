package justita.top.timesecretary.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class AffairChangeReceiver extends BroadcastReceiver {

    public interface AffairChangeListener{
        public void onAffairChange(Intent intent);
    }

    public static List<AffairChangeListener> mAffairChangeListeners = new ArrayList<>();

    public static void addAffairStateChangeListener(AffairChangeListener mAffairChangeListener) {
        if(!mAffairChangeListeners.contains(mAffairChangeListener))
            mAffairChangeListeners.add(mAffairChangeListener);
    }
    public static void removeAffairStateChangeListener(AffairChangeListener mAffairChangeListener) {
        if(mAffairChangeListeners.contains(mAffairChangeListener))
            mAffairChangeListeners.remove(mAffairChangeListener);
    }

    public AffairChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        for(AffairChangeListener affairChangeListener : mAffairChangeListeners){
            affairChangeListener.onAffairChange(intent);
        }
    }

}
