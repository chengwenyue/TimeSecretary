package justita.top.timesecretary.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.activity.AffairUpdateActivity;
import justita.top.timesecretary.activity.MainActivity;
import justita.top.timesecretary.adapter.AffairAdapter;
import justita.top.timesecretary.adapter.AffairContract;
import justita.top.timesecretary.app.BaseFragment;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;

public class AffairListFragment extends BaseFragment {
    private boolean isHide = false;
    private RecyclerView recyclerView;
    private List<Affair> affairList;

    private AffairAdapter mAdapter;
    private MainActivity.OnAffairStateChangeListener mOnAffairStateChangeListener;

    public void setOnAffairStateChangeListener(MainActivity.OnAffairStateChangeListener mOnAffairStateChangeListener) {
        this.mOnAffairStateChangeListener = mOnAffairStateChangeListener;
    }


    public AffairListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHide = getArguments().getBoolean(PreferenceConstants.IS_HIDE);
        }
    }


    private void initAllDate() {
        ContentResolver cr = getContext().getContentResolver();
        //select * from where time = 'nowData'
        long categoryId = PreferenceUtils.getPrefLong(getContext(),PreferenceConstants.MAIN_CATEGORY,-1);
        if(categoryId == -1){
            affairList = Affair.getAffairs(cr,Affair.STATE+ "<> ? ",Affair.AFFAIR_DELETE_STATE+"");
        }else{
            affairList = Affair.getAffairs(cr,Affair.STATE+ "<> ? and " + Affair.CATEGORY +" = ?",Affair.AFFAIR_DELETE_STATE+"",categoryId+"");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_affair_list, container, false);
        initAllDate();
        initView(view);
        return view;
    }
    private void initView(View view){
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);


        if(!isHide){
            mAdapter = new AffairAdapter(getContext(), recyclerView, affairList, AffairContract.DEFAULT_CATEGORY_STRATEGY);
        }else{
            mAdapter = new AffairAdapter(getContext(), recyclerView, affairList, AffairContract.HIDE_ACHIEVE_AFFAIR);
        }

        mAdapter.setOnItemClickListener(new AffairAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Affair affair = mAdapter.findAffair(position);
                getActivity().startActivityForResult(Affair.createIntent(getContext(), AffairUpdateActivity.class,affair.mId),MainActivity.RESULT_SAVE);
            }
        });
        mAdapter.setOnItemLongClickListener(new AffairAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
            }
        });

        mAdapter.setOnOpenLeftListener(new AffairAdapter.OnOpenLeftListener() {
            @Override
            public void onOpenLeft(Affair affair) {
                if (mOnAffairStateChangeListener != null)
                    mOnAffairStateChangeListener.onAffairStateChange(affair,1);
            }
        });

        mAdapter.setRightButtonClickListener(new AffairAdapter.OnRightButtonClickListener() {
            @Override
            public void onClick(View view, Affair affair) {
                if (mOnAffairStateChangeListener != null)
                    mOnAffairStateChangeListener.onAffairStateChange(affair,1);
            }
        });
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void insertAffair(Affair affair){
        mAdapter.addAffair(affair);
    }
    public void removeAffair(Affair affair){
        mAdapter.removeAffair(affair);
    }
    public void updateAffair(Affair affair){
        mAdapter.updateAffair(affair);
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void changeStrategy(int model) {
        initAllDate();
        mAdapter.setCategoryStrategyModel(model,affairList);
    }

}
