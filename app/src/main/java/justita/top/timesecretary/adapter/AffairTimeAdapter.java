package justita.top.timesecretary.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.provider.Category;
import justita.top.timesecretary.uitl.DateUtils;
import justita.top.timesecretary.uitl.Utils;
import justita.top.timesecretary.widget.CustomBar;
import justita.top.timesecretary.widget.SwipeLayout;


public class AffairTimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>implements  AffairContract{


    private Context mContext;
    private RecyclerView mRecycleView;
    private AffairCategoryContext mAffairCategoryContext;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    private OnRightButtonClickListener mRightButtonClickListener;
    private OnOpenLeftListener mOnOpenLeftListener;
    private SwipeLayout mCurrentSwpieLayout;

    private HandlerThread handlerThread = new HandlerThread("AffairTimeAdapter");
    private class BarHandler extends Handler{
        List<CustomBar> customBarList = new ArrayList<>();
        List<CustomBar> addList = new ArrayList<>();
        List<CustomBar> removeList = new ArrayList<>();

        public void addCustomBar(CustomBar customBar){
            if(addList.contains(customBar))
                return;
            addList.add(customBar);
        }
        public void removeCustomBar(CustomBar customBar){
            if(removeList.contains(customBar))
                return;
            removeList.add(customBar);
        }

        public BarHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x11) {
                if(!removeList.isEmpty()) {
                    customBarList.removeAll(removeList);
                    removeList.clear();
                }
                if(!addList.isEmpty()) {
                    customBarList.addAll(addList);
                    addList.clear();
                }
                for(CustomBar customBar :customBarList){
                    customBar.addPostProgress(1000);
                }
                mHandler.sendEmptyMessageDelayed(0x11, 1000);
            }
        }
    }
    private BarHandler mHandler;

    public void releaseThread(){
        handlerThread.quitSafely();
    }

    public static class CategoryStrategyTimeDefault extends CategoryStrategyTime{
        @Override
        public boolean doCategory(AffairCategory affairCategory, Affair affair) {

            if(affair.mState == Affair.AFFAIR_DELETE_STATE)
                return false;
            String startTime = affair.mTime.split("--")[0];
            try {
                return DateUtils.defaultToMD(startTime).equals(affairCategory.time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class CategoryStrategyTimeHide extends CategoryStrategyTime{
        @Override
        public boolean doCategory(AffairCategory affairCategory, Affair affair) {

            if(affair.mState == Affair.AFFAIR_DELETE_STATE
                    || affair.mState == Affair.AFFAIR_COMPLETE_STATE
                    ||affair.mState == Affair.AFFAIR_FIRED_STATE)
                return false;

            String startTime = affair.mTime.split("--")[0];
            try {
                return DateUtils.defaultToMD(startTime).equals(affairCategory.time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public abstract static class CategoryStrategyTime implements AffairCategoryContext.ICategoryStrategy {
        private List<AffairCategory> affairCategoryList = new ArrayList<>();

        @Override
        public int doRankAndAdd(List<Affair> affairList, Affair affair) {
            List<Affair> timeAffair = new ArrayList<>();
            List<Affair> normalAffair = new ArrayList<>();
            List<Affair> otherAffair = new ArrayList<>();

            for(int i = 0;i<affairList.size();i++){
                Affair affair1 = affairList.get(i);
                if(affair1.isTimeAffair()  && affair1.mState == Affair.AFFAIR_SILENT_STATE){
                    timeAffair.add(affair1);
                }else if(!affair1.isTimeAffair() && affair1.mState == Affair.AFFAIR_SILENT_STATE){
                    normalAffair.add(affair1);
                }else {
                    otherAffair.add(affair1);
                }
            }
            if(affair.isTimeAffair() && affair.mState == Affair.AFFAIR_SILENT_STATE) {
                int i = 0, l = timeAffair.size();
                for (; i < l; i++) {
                    Affair affair1 = timeAffair.get(i);
                    try {
                        Date data1 = DateUtils.formatDate(affair1.mTime, DateUtils.DEFAULT_FORMAT);
                        Date data2 = DateUtils.formatDate(affair.mTime, DateUtils.DEFAULT_FORMAT);

                        //data1 >= data2
                        if (data1.getTime() >= data2.getTime()) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i < l) {
                    timeAffair.add(i, affair);
                } else {
                    timeAffair.add(affair);
                }

                affairList.clear();
                affairList.addAll(timeAffair);
                affairList.addAll(normalAffair);
                affairList.addAll(otherAffair);
                return i+1;
            }else if(!affair.isTimeAffair() && affair.mState == Affair.AFFAIR_SILENT_STATE){
                int i = 0, l = normalAffair.size();
                for (; i < l; i++) {
                    Affair affair1 = normalAffair.get(i);
                    try {
                        Date data1 = DateUtils.formatDate(affair1.mTime, DateUtils.DEFAULT_FORMAT);
                        Date data2 = DateUtils.formatDate(affair.mTime, DateUtils.DEFAULT_FORMAT);

                        //data1 >= data2
                        if (data1.getTime() >= data2.getTime()) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i < l) {
                    normalAffair.add(i, affair);
                } else {
                    normalAffair.add(affair);
                }

                affairList.clear();
                affairList.addAll(timeAffair);
                affairList.addAll(normalAffair);
                affairList.addAll(otherAffair);

                return i + timeAffair.size() +1;
            }

            affairList.add(affair);
            return affairList.size();
        }

        @Override
        public boolean isEmptyDelTitle() {
            return true;
        }




        @Override
        public List<AffairCategory> initCategories() {
            Calendar calendar = Calendar.getInstance();
            affairCategoryList.add(new AffairCategory(calendar.get(Calendar.DAY_OF_WEEK),"今天", DateUtils.formatDate(calendar,DateUtils.MOUTH_DAY)));
            calendar.add(Calendar.DAY_OF_YEAR,1);
            affairCategoryList.add(new AffairCategory(calendar.get(Calendar.DAY_OF_WEEK),"明天", DateUtils.formatDate(calendar,DateUtils.MOUTH_DAY)));
            calendar.add(Calendar.DAY_OF_YEAR,1);
            affairCategoryList.add(new AffairCategory(calendar.get(Calendar.DAY_OF_WEEK),"2天后", DateUtils.formatDate(calendar,DateUtils.MOUTH_DAY)));
            calendar.add(Calendar.DAY_OF_YEAR,1);
            affairCategoryList.add(new AffairCategory(calendar.get(Calendar.DAY_OF_WEEK),"3天后", DateUtils.formatDate(calendar,DateUtils.MOUTH_DAY)));
            calendar.add(Calendar.DAY_OF_YEAR,1);
            affairCategoryList.add(new AffairCategory(calendar.get(Calendar.DAY_OF_WEEK),"4天后", DateUtils.formatDate(calendar,DateUtils.MOUTH_DAY)));
            calendar.add(Calendar.DAY_OF_YEAR,1);
            affairCategoryList.add(new AffairCategory(calendar.get(Calendar.DAY_OF_WEEK),"5天后", DateUtils.formatDate(calendar,DateUtils.MOUTH_DAY)));
            calendar.add(Calendar.DAY_OF_YEAR,1);
            affairCategoryList.add(new AffairCategory(calendar.get(Calendar.DAY_OF_WEEK),"6天后", DateUtils.formatDate(calendar,DateUtils.MOUTH_DAY)));

            return affairCategoryList;
        }

        @Override
        public AffairCategory initCategory(List<AffairCategory> categoryList) {
            return null;
        }
    }




    public AffairTimeAdapter(Context context, RecyclerView recyclerView, List<Affair> affairList, int catagoryStrategyModel) {
        mContext = context;
        mRecycleView = recyclerView;
        mAffairCategoryContext = new AffairCategoryContext();
        mAffairCategoryContext.setCategoryStrategyModel(catagoryStrategyModel);
        mAffairCategoryContext.formatData(affairList);
        handlerThread.start();
        mHandler =  new BarHandler(handlerThread.getLooper());
        mHandler.sendEmptyMessage(0x11);
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
        notifyDataSetChanged();
    }

    public void removeAffair(Affair affair){
        int position = mAffairCategoryContext.delAffair(affair);
        notifyDataSetChanged();
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
                notifyDataSetChanged();
            else
                notifyItemMoved(position[0],position[1]);
        }
    }

    public void setCategoryStrategyModel(int model,List<Affair> affairList){
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
            case AFFAIR_TIME:{
                View view = View.inflate(mContext, R.layout.list_item_affair_time,null);
                return new AffairTimeToDoItemHolder(view,mItemClickListener,mItemLongClickListener);
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
        }else if(holder instanceof  AffairTimeToDoItemHolder){
            ((AffairTimeToDoItemHolder) holder).setDataAndRefreshUI(position);
        }

    }


    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if(holder instanceof AffairTimeToDoItemHolder){
            mHandler.removeCustomBar(((AffairTimeToDoItemHolder) holder).mCustomBar);
        }
    }

    @Override
    public int getItemViewType(int position) {
        AffairCategory affairCategory = mAffairCategoryContext.getAffairCategory(position);
        if(affairCategory == null)
            return INVALID_ITEM;

        int offset = position -affairCategory.startIndex;
        if (offset==0){
            return AFFAIR_TITLE;
        }
        Affair affair = affairCategory.getAffair(position);
        if(affair.mState == Affair.AFFAIR_SILENT_STATE || affair.mState == Affair.AFFAIR_FIRED_STATE){

            if(affair.isTimeAffair()){
                return AFFAIR_TIME;
            }else{
                return  AFFAIR_TODO;
            }
        }else {
            return AFFAIR_ACHIEVE;
        }
    }


    @Override
    public int getItemCount() {
        return mAffairCategoryContext.getItemCount();
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }
    public interface OnItemLongClickListener {
        public void onItemLongClick(View view, int position);
    }

    public interface OnRightButtonClickListener {
        public void onClick(View view, Affair affair);
    }
    public interface OnOpenLeftListener {
        public void onOpenLeft(Affair affair);
    }
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
    private SwipeLayout.SwipeViewListener swipeViewListener = new SwipeLayout.SwipeViewListener() {
        @Override
        public void onClose(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onOpenLeft(SwipeLayout mSwipeLayout) {

            final Affair affair = Affair.getAffair(mContext.getContentResolver(),mSwipeLayout.getDateId());
            int position = mAffairCategoryContext.findAffair(affair);
            int viewType = getItemViewType(position);
            if(viewType ==AFFAIR_TODO ){
                affair.mState = Affair.AFFAIR_COMPLETE_STATE;
                updateAffair(affair);
            }else if(viewType == AFFAIR_ACHIEVE){
                affair.mState = Affair.AFFAIR_DELETE_STATE;
                removeAffair(affair);
            }else if(viewType == AFFAIR_TIME){
                affair.mState = Affair.AFFAIR_COMPLETE_STATE;
                updateAffair(affair);
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

    public CustomBar.CompleteListener completeListener = new CustomBar.CompleteListener() {
        @Override
        public void onCompleteListener(CustomBar customBar) {
            mHandler.removeCustomBar(customBar);
        }
    };
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

    public class AffairTimeToDoItemHolder extends BaseViewHolder{

        private SwipeLayout mAffairItem;
        private CustomBar mCustomBar;
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

        public AffairTimeToDoItemHolder(View itemView, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
            super(itemView, onItemClickListener, onItemLongClickListener);
            mAffairItem = (SwipeLayout) itemView;
            mCustomBar = (CustomBar) itemView.findViewById(R.id.customBar);
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
            mCustomBar.setTime(affair.mTime);

            mHandler.addCustomBar(mCustomBar);

            mCustomBar.setCompleteListener(completeListener);



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

    public class AffairTitleHolder extends RecyclerView.ViewHolder{

        private TextView mAffairTitle;

        public AffairTitleHolder(View itemView) {
            super(itemView);
            mAffairTitle = (TextView) itemView.findViewById(R.id.affair_state);
        }
        public void setDataAndRefreshUI(int position) {
            AffairCategory affairCategory =  mAffairCategoryContext.getAffairCategory(position);
            String text = affairCategory.name + " ("+affairCategory.time+") " +"周" +DateUtils.formatWeek(affairCategory.mIndex);
            mAffairTitle.setText(text);
        }
    }

}
