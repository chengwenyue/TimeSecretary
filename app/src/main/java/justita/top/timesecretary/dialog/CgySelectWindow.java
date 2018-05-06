package justita.top.timesecretary.dialog;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.activity.MainActivity;
import justita.top.timesecretary.adapter.CategoryAdapter;
import justita.top.timesecretary.fragment.DividerItemDecoration;
import justita.top.timesecretary.provider.Category;
import justita.top.timesecretary.service.AffairChangeReceiver;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.widget.CategoryItemColorView;
import justita.top.timesecretary.widget.SwipeLayout;

public class CgySelectWindow extends PopupWindows{

    boolean isOpen = false;

    private RecyclerView recyclerView;
    private CategoryAdapter mAdapter;
    private Button mAddCategoryBt;

    /**
     * Constructor.
     *
     * @param context Context
     */
    public CgySelectWindow(Context context) {
        super(context);

        mWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootView = View.inflate(mContext,R.layout.popupwindow,null);
        initView();
        setAnimationStyle(R.style.animation);
    }

    private void initView() {
        final LinearLayout category_color_list = (LinearLayout) mRootView.findViewById(R.id.category_item_color_list);
        final CategoryItemColorView category_color  = (CategoryItemColorView) mRootView.findViewById(R.id.category_item_color);
        final EditText editText = (EditText) mRootView.findViewById(R.id.category_item_name);

        final ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                category_color_list.setTranslationX(mRootView.getWidth());
                return true;
            }
        };

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category_color.setCategoryItemColor (((CategoryItemColorView)v).categoryItemColor);
                ObjectAnimator oa1 = ObjectAnimator.ofFloat(category_color_list, "translationX", mRootView.getWidth());
                oa1.setDuration(1000);
                oa1.start();
            }
        };
        for(int i =0;i<category_color_list.getChildCount();i++){
            CategoryItemColorView categoryItemColorView = (CategoryItemColorView) category_color_list.getChildAt(i);
            categoryItemColorView.setOnClickListener(onClickListener);
        }
        category_color_list.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);


        category_color.setOnClickListener(new View.OnClickListener() {
            ObjectAnimator oa1 = null;
            @Override
            public void onClick(View v) {
                category_color_list.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
                if(oa1 != null && oa1.isRunning()){
                    return;
                }
                if(!isOpen){
                    oa1 = ObjectAnimator.ofFloat(category_color_list, "translationX", (mRootView.getWidth()-category_color_list.getWidth())/2);

                }else{
                    oa1 = ObjectAnimator.ofFloat(category_color_list, "translationX", mRootView.getWidth());
                }

                oa1.setDuration(1000);
                oa1.start();
                isOpen = !isOpen;
            }
        });
        mAddCategoryBt = (Button) mRootView.findViewById(R.id.bt_addCategory);
        mAddCategoryBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName  = editText.getText().toString().trim();
                if(TextUtils.isEmpty(categoryName)){
                    Toast.makeText(mContext,"分类名不能为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                Category category = new Category();
                category.mName = categoryName;
                category.mColor = category_color.categoryItemColor;
                category.mUser_Id =  PreferenceUtils.getPrefLong(mContext, PreferenceConstants.USERID,-1);
                Category.addCategory(mContext.getContentResolver(),category);
                refresh();

                editText.setText("");
            }
        });
        recyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));

        mAdapter = new CategoryAdapter(mContext,null);
        mAdapter.setSwipeViewListener(swipeViewListener);
        mAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(mContext,AffairChangeReceiver.class);
                intent.setAction(MainActivity.AFFAIR_CATEGORY_SELECT);

                if(position == 0) {
                    mAdapter.mCategorySelect = -1;
                    intent.putExtra("categoryName","全部");
                }
                else {
                    mAdapter.mCategorySelect = (int) mAdapter.mCategoryList.get(position - 1).mId;
                    intent.putExtra("categoryName",mAdapter.mCategoryList.get(position - 1).mName);
                }

                PreferenceUtils.setSettingLong(mContext,PreferenceConstants.MAIN_CATEGORY,mAdapter.mCategorySelect );
                mContext.sendBroadcast(intent);

                //mAdapter.notifyDataSetChanged();
                dismiss();
            }
        });
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext,
                DividerItemDecoration.VERTICAL_LIST));

    }

    public void show(View view){
        preShow();
        mWindow.showAtLocation(view, Gravity.TOP,0,0);
    }

    @Override
    protected void onShow() {
        refresh();
    }

    public void refresh(){
        long userId = PreferenceUtils.getPrefLong(mContext, PreferenceConstants.USERID,-1);
        List<Category> categoryList = Category.getCategories(mContext.getContentResolver(),Category.USER_ID + " in(?,?)",new String[]{userId+"","0"});
        mAdapter.setCategoryList(categoryList);
        mAdapter.notifyDataSetChanged();
    }
    public void setAnimationStyle(int animationStyle){
        mWindow.setAnimationStyle(animationStyle);
    }

    public boolean isShow(){
        return mWindow.isShowing();
    }

    private SwipeLayout.SwipeViewListener swipeViewListener = new SwipeLayout.SwipeViewListener() {
        @Override
        public void onClose(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onOpenLeft(SwipeLayout mSwipeLayout) {
            long categoryId = mSwipeLayout.getDateId();
            Category.deleteCategory(mContext.getContentResolver(),categoryId);
            refresh();

            Intent intent = new Intent(mContext,AffairChangeReceiver.class);
            intent.setData(Category.getUri(categoryId));
            intent.setAction(MainActivity.AFFAIR_CATEGORY_CHANGE);
            mContext.sendBroadcast(intent);
        }

        @Override
        public void onDraging(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onStartCloseLeft(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onStartOpenLeft(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onOpenRight(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onStartCloseRight(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onStartOpenRight(SwipeLayout mSwipeLayout) {

        }
    };
}
