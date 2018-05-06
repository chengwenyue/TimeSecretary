package justita.top.timesecretary.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;

import justita.top.timesecretary.app.BaseActivity;
import justita.top.timesecretary.provider.Affair;

public abstract class BaseAttrFragment extends Fragment{

    public Affair.Builder mBuilder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mBuilder = ((BaseActivity)activity).getBuilder();
    }
}
