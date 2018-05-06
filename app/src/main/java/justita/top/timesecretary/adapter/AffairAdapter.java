package justita.top.timesecretary.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.provider.Category;
import justita.top.timesecretary.uitl.DateUtils;
import justita.top.timesecretary.uitl.Utils;
import justita.top.timesecretary.widget.SwipeLayout;


public class AffairAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>implements  AffairContract{


    private Context mContext;
    private RecyclerView mRecycleView;
    private AffairCategoryContext mAffairCategoryContext;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    private OnRightButtonClickListener mRightButtonClickListener;
    private OnOpenLeftListener mOnOpenLeftListener;
    private SwipeLayout mCurrentSwpieLayout;

    public interface OnItemClickListener {
        public void onItemClick(View view,int position);
    }
    public interface OnItemLongClickListener {
        public void onItemLongClick(View view,int position);
    }

    public interface OnRightButtonClickListener {
        public void onClick(View view,Affair affair);
    }
    public interface OnOpenLeftListener {
        public void onOpenLeft(Affair affair);
    }





    public AffairAdapter(Context context, RecyclerView recyclerView,List<Affair> affairList,int catagoryStrategyModel) {
        mContext = context;
        mRecycleView = recyclerView;
        mAffairCategoryContext = new AffairCategoryContext();
        mAffairCategoryContext.setCategoryStrategyModel(catagoryStrategyModel);
        mAffairCategoryContext.formatData(affairList);
    }


    /**
     * 设置Item点击监听
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.mItemLongClickListener = listener;
    }

    public void setOnOpenLeftListener(OnOpenLeftListener mOnOpenLeftListener) {
        this.mOnOpenLeftListener = mOnOpenLeftListener;
    }

    public void setRightButtonClickListener(OnRightButtonClickListener mRightButtonClickListener) {
        this.mRightButtonClickListener = mRightButtonClickListener;
    }

    public void addAffair(Affair affair){
        int position = mAffairCategoryContext.addAffair(affair);
        notifyItemInserted(position);
    }

    public void removeAffair(Affair affair){
        int position = mAffairCategoryContext.delAffair(affair);
        notifyItemRemoved(position);
    }

    public Affair findAffair(int position){
        return mAffairCategoryContext.findAffair(position);
    }

    public void updateAffair(Affair affair){
        int[] position = mAffairCategoryContext.updateAffair(affair);

        //位置不变时调用
        if(position[0]==position[1]){
            notifyItemChanged(position[0]);
        }else{

            //解决不同数据结构导致的错误
        if(position[1] == INVALID_POSITION)
                notifyItemRemoved(position[0]);
            else
                notifyItemMoved(position[0],position[1]);
        }
    }

    public void setCategoryStrategyModel(int model,List<Affair> affairList ){
        mAffairCategoryContext.setCategoryStrategyModel(model);
        mAffairCategoryContext.formatData(affairList);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case AFFAIR_TITLE:
            {
                View view = View.inflate(mContext, R.layout.affair_state,null);
                return new AffairTitleHolder(view);
            }
            case AFFAIR_TODO:
            {
                View view = View.inflate(mContext, R.layout.list_item_affair_normal,null);
                return new AffairToDoItemHolder(view,mItemClickListener,mItemLongClickListener);
            }
            default:{
                View view = View.inflate(mContext, R.layout.list_item_affair_normal,null);
                return new AffairAchieveItemHolder(view,mItemClickListener,mItemLongClickListener);
            }
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof AffairTitleHolder){
            ((AffairTitleHolder) holder).setDataAndRefreshUI(position);
        }
        else if(holder instanceof AffairAchieveItemHolder){
            ((AffairAchieveItemHolder) holder).setDataAndRefreshUI(position);

        }else if(holder instanceof AffairToDoItemHolder){
            ((AffairToDoItemHolder) holder).setDataAndRefreshUI(position);
        }

    }


    @Override
    public int getItemViewType(int position) {
        AffairCategory affairCategory = mAffairCategoryContext.getAffairCategory(position);
        if(affairCategory == null){
            return INVALID_ITEM;
        }
        int offset = position -affairCategory.startIndex;
        if (offset==0){
            return AFFAIR_TITLE;
        }else {
            return affairCategory.mIndex;
        }
    }


    @Override
    public int getItemCount() {
        return mAffairCategoryContext.getItemCount();
    }


    private SwipeLayout.SwipeViewListener swipeViewListener = new SwipeLayout.SwipeViewListener() {
        @Override
        public void onClose(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onOpenLeft(SwipeLayout mSwipeLayout) {

            final Affair affair = Affair.getAffair(mContext.getContentResolver(),mSwipeLayout.getDateId());
            int position = mAffairCategoryContext.findAffair(affair);
            int viewType = getItemViewType(position);
            if(viewType ==AFFAIR_TODO){
                affair.mState = Affair.AFFAIR_COMPLETE_STATE;
                updateAffair(affair);
            }else if(viewType == AFFAIR_ACHIEVE){
                affair.mState = Affair.AFFAIR_DELETE_STATE;
                removeAffair(affair);
            }
            mCurrentSwpieLayout = null;
            if(mOnOpenLeftListener != null)
                mOnOpenLeftListener.onOpenLeft(affair);
        }

        @Override
        public void onDraging(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onStartCloseLeft(SwipeLayout mSwipeLayout) {
            mCurrentSwpieLayout =null;
        }

        @Override
        public void onStartOpenLeft(SwipeLayout mSwipeLayout) {
            if(mCurrentSwpieLayout != null ){
                mCurrentSwpieLayout.closeView();
            }
            mCurrentSwpieLayout = mSwipeLayout;
        }

        @Override
        public void onOpenRight(SwipeLayout mSwipeLayout) {
            mCurrentSwpieLayout = mSwipeLayout;
        }

        @Override
        public void onStartCloseRight(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onStartOpenRight(SwipeLayout mSwipeLayout) {
            if(mCurrentSwpieLayout != null ){
                mCurrentSwpieLayout.closeView();
            }
            mCurrentSwpieLayout = mSwipeLayout;
        }
    };
    public class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        private OnItemClickListener mItemClickListener;
        private OnItemLongClickListener mItemLongClickListener;

        private SwipeLayout mSwipeLayout;
        public BaseViewHolder(View itemView,OnItemClickListener onItemClickListener,
                              OnItemLongClickListener onItemLongClickListener) {
            super(itemView);
            this.mItemClickListener = onItemClickListener;
            this.mItemLongClickListener = onItemLongClickListener;
            if(itemView instanceof SwipeLayout){
                mSwipeLayout = (SwipeLayout) itemView;
                View  view = mSwipeLayout.getFrontView();
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
            }else{
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }


        }

        @Override
        public void onClick(View v) {
            if(mItemClickListener != null) {
                if(mCurrentSwpieLayout != null) {
                    mCurrentSwpieLayout.closeView();
                    //不响应事件
                    return;
                }
                if (mSwipeLayout.getCurrStatus() == SwipeLayout.Status.CLOSE){
                    mItemClickListener.onItemClick(v, getAdapterPosition());
                }else{
                    mSwipeLayout.closeView();
                }

            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mItemLongClickListener != null){
                if(mCurrentSwpieLayout != null) {
                    mCurrentSwpieLayout.closeView();
                    //不响应事件
                    return true;
                }
                if (mSwipeLayout.getCurrStatus() == SwipeLayout.Status.CLOSE){
                    mItemLongClickListener.onItemLongClick(v,getAdapterPosition());
                }else{
                    mSwipeLayout.closeView();
                }
            }
            return true;
        }
    }
    public class AffairAchieveItemHolder extends BaseViewHolder{
        private SwipeLayout mAffairItem;
        private LinearLayout mLeftBg;
        private ImageView mLeftImage;
        private ImageButton mRightImgBt;
        private TextView mAffairNameTv;
        private View mAffairNameCover;
        private TextView mAffairTimeTv;
        private View mAffairTimeCover;
        private TextView mAffairCgyTv;
        private ImageView mAffairCgyColor;
        private View mAffairCgyCover;

        public AffairAchieveItemHolder(View itemView, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
            super(itemView, onItemClickListener, onItemLongClickListener);
            mAffairItem = (SwipeLayout) itemView;
            mLeftBg = (LinearLayout) itemView.findViewById(R.id.bg_left);
            mLeftImage = (ImageView) itemView.findViewById(R.id.img_left);
            mRightImgBt = (ImageButton) itemView.findViewById(R.id.bt_right);
            mAffairNameTv = (TextView) itemView.findViewById(R.id.affair_name);
            mAffairNameCover = itemView.findViewById(R.id.affair_name_cover);
            mAffairTimeTv = (TextView) itemView.findViewById(R.id.affair_time);
            mAffairTimeCover = itemView.findViewById(R.id.affair_time_cover);
            mAffairCgyTv  = (TextView) itemView.findViewById(R.id.affair_category);
            mAffairCgyColor = (ImageView) itemView.findViewById(R.id.category_item_color);
            mAffairCgyCover = itemView.findViewById(R.id.affair_category_cover);
        }
        public void setDataAndRefreshUI(int position) {
            mAffairItem.setSwipeViewListener(swipeViewListener);


            final Affair affair = mAffairCategoryContext.getAffairItem(position);

            mAffairItem.setDateId(affair.mId);
            mLeftBg.setBackgroundResource(R.color.red);
            mLeftImage.setImageResource(R.drawable.ic_del);

            mRightImgBt.setImageResource(R.drawable.ic_reback);

            mRightImgBt.setBackgroundResource(R.drawable.affair_item_del_bt_bg);
            mRightImgBt.setBackgroundColor(mContext.getResources().getColor(R.color.yellow));

            mRightImgBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // //处理界面代码
                    affair.mState = Affair.AFFAIR_SILENT_STATE;
                    updateAffair(affair);

                    //处理数据代码，改变事件状态
                    mRightButtonClickListener.onClick(v,affair);
                }
            });
            mAffairNameTv.setText(affair.mName);

            ViewTreeObserver mViewTreeObserver = mAffairNameTv.getViewTreeObserver();
            mViewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
            {
                @Override
                public boolean onPreDraw()
                {
                    //设置掩盖线的宽度与姓名相同
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAffairNameCover.getLayoutParams();
                    if(params.width != mAffairNameTv.getWidth()){
                        params.width = mAffairNameTv.getWidth();
                        mAffairNameCover.setLayoutParams(params);
                    }
                    return true;
                }
            });


            mAffairTimeTv.setText(affair.getTimeLabel());

            ViewTreeObserver mTimeObserver = mAffairTimeTv.getViewTreeObserver();
            mTimeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
            {
                @Override
                public boolean onPreDraw()
                {
                    //设置掩盖线的宽度与姓名相同
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAffairTimeCover.getLayoutParams();
                    if(params.width != mAffairTimeTv.getWidth()){
                        params.width = mAffairTimeTv.getWidth();
                        mAffairTimeCover.setLayoutParams(params);
                    }
                    return true;
                }
            });


            Category category = Category.getCategory(mContext.getContentResolver(),Long.valueOf(affair.mCategory));
            if(category == null){
                //"默认"分类的id为1
                category = Category.getCategory(mContext.getContentResolver(),1);
            }
            mAffairCgyTv.setText(category.mName);
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_category_item_bg);
            drawable.setTint(category.mColor);

            mAffairCgyColor.setImageDrawable(drawable);


            ViewTreeObserver mCgyObserver = mAffairCgyTv.getViewTreeObserver();
            mCgyObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
            {
                @Override
                public boolean onPreDraw()
                {
                    //设置掩盖线的宽度与姓名相同
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAffairCgyCover.getLayoutParams();
                    int width = mAffairCgyTv.getWidth()+Utils.dip2px(mContext,10.0f);
                    if(params.width != width){
                        params.width = width;
                        //视图重绘会导致不能滑动
                        mAffairCgyCover.setLayoutParams(params);
                    }
                    return true;
                }
            });
        }

    }

    public class AffairToDoItemHolder extends BaseViewHolder{

        private SwipeLayout mAffairItem;
        private LinearLayout mLeftBg;
        private ImageView mLeftImage;
        private ImageButton mRightImgBt;
        private TextView mAffairNameTv;
        private View mAffairNameCover;
        private TextView mAffairTimeTv;
        private View mAffairTimeCover;
        private TextView mAffairCgyTv;
        private ImageView mAffairCgyColor;
        private View mAffairCgyCover;

        public AffairToDoItemHolder(View itemView, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
            super(itemView, onItemClickListener, onItemLongClickListener);
            mAffairItem = (SwipeLayout) itemView;
            mLeftBg = (LinearLayout) itemView.findViewById(R.id.bg_left);
            mLeftImage = (ImageView) itemView.findViewById(R.id.img_left);
            mRightImgBt = (ImageButton) itemView.findViewById(R.id.bt_right);
            mAffairNameTv = (TextView) itemView.findViewById(R.id.affair_name);
            mAffairNameCover = itemView.findViewById(R.id.affair_name_cover);
            mAffairTimeTv = (TextView) itemView.findViewById(R.id.affair_time);
            mAffairTimeCover = itemView.findViewById(R.id.affair_time_cover);
            mAffairCgyTv  = (TextView) itemView.findViewById(R.id.affair_category);
            mAffairCgyColor = (ImageView) itemView.findViewById(R.id.category_item_color);
            mAffairCgyCover = itemView.findViewById(R.id.affair_category_cover);
        }
        public void setDataAndRefreshUI(int position) {
            mAffairItem.setSwipeViewListener(swipeViewListener);

            final Affair affair = mAffairCategoryContext.getAffairItem(position);
            mAffairItem.setDateId(affair.mId);
            mLeftBg.setBackgroundResource(R.color.green);
            mLeftImage.setImageResource(R.drawable.ic_achieve);


            mRightImgBt.setImageResource(R.drawable.ic_del);
            mRightImgBt.setBackgroundResource(R.drawable.affair_item_del_bt_bg);

            mRightImgBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //处理界面代码
                    affair.mState = Affair.AFFAIR_DELETE_STATE;
                    removeAffair(affair);

                    //处理数据代码，改变事件状态
                    mRightButtonClickListener.onClick(v,affair);
                }
            });
            mAffairNameTv.setText(affair.mName);
            mAffairNameCover.setVisibility(View.INVISIBLE);

            mAffairTimeTv.setText(affair.getTimeLabel());
            mAffairTimeCover.setVisibility(View.INVISIBLE);


            Category category = Category.getCategory(mContext.getContentResolver(),Long.valueOf(affair.mCategory));
            if(category == null){
                //"默认"分类的id为1
                category = Category.getCategory(mContext.getContentResolver(),1);
            }

            mAffairCgyTv.setText(category.mName);
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_category_item_bg);
            drawable.setTint(category.mColor);

            mAffairCgyColor.setImageDrawable(drawable);

            mAffairCgyCover.setVisibility(View.INVISIBLE);
        }

    }

    public class AffairTitleHolder extends RecyclerView.ViewHolder {

        private TextView mAffairTitle;

        public AffairTitleHolder(View itemView) {
            super(itemView);
            mAffairTitle = (TextView) itemView.findViewById(R.id.affair_state);
        }

        public void setDataAndRefreshUI(int position) {
            AffairCategory affairCategory = mAffairCategoryContext.getAffairCategory(position);
            mAffairTitle.setText(affairCategory.name);


        }
    }


    public static class CategoryStrategyState implements AffairCategoryContext.ICategoryStrategy {

        protected List<AffairCategory> affairCategoryList = new ArrayList<>();

        public CategoryStrategyState(){
        }
        @Override
        public boolean doCategory(AffairCategory affairCategory, Affair affair) {
            if(affairCategory.mIndex == AFFAIR_TODO){
                if(affair.mState == Affair.AFFAIR_SILENT_STATE || affair.mState==Affair.AFFAIR_SNOOZE_STATE)
                    return true;
                else
                    return false;
            }
            if(affair.mState == Affair.AFFAIR_DELETE_STATE)
                return false;
            return true;
        }

        @Override
        public int doRankAndAdd(List<Affair> affairList, Affair affair) {
            int i = 0,l = affairList.size();
            for(;i< l;i++){
                Affair affair1 = affairList.get(i);
                try {
                    Date data1 = DateUtils.formatDate(affair1.mTime,DateUtils.DEFAULT_FORMAT);
                    Date data2 = DateUtils.formatDate(affair.mTime,DateUtils.DEFAULT_FORMAT);

                    //data1 >= data2
                    if(data1.getTime() >= data2.getTime()){
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(i<l){
                affairList.add(i,affair);
            }else{
                affairList.add(affair);
            }
            return i+1;
        }

        @Override
        public boolean isEmptyDelTitle() {
            return false;
        }


        @Override
        public List<AffairCategory> initCategories() {
            return affairCategoryList;
        }

        @Override
        public AffairCategory initCategory(List<AffairCategory> categoryList) {
            return null;
        }
    }


    public static class CategoryHideAchieveStrategy extends CategoryStrategyState {
        public CategoryHideAchieveStrategy() {
            super();
            affairCategoryList.add(new AffairCategory(AFFAIR_TODO,"待办事件",""));
        }
    }
    public static class CategoryDefaultStrategy extends CategoryStrategyState{
        public CategoryDefaultStrategy() {
            super();
            affairCategoryList.add(new AffairCategory(AFFAIR_TODO,"待办事件",""));
            affairCategoryList.add(new AffairCategory(AFFAIR_ACHIEVE,"已归档",""));
        }
    };

}
