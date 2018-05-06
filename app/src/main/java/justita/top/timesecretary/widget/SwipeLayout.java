package justita.top.timesecretary.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class SwipeLayout extends FrameLayout {

    private final String TAG = getClass().toString();
    private ViewDragHelper.Callback mCallback;
    private ViewDragHelper mDragHelper;
    private View mLeftView;//item的左边布局
    private View mRightView; //item的右边布局  
    private View mFrontView;//当前显示的item布局  
    private int mWidth; //屏幕的宽度,mFrontView的宽度  
    private int mHeight; //mFrontView的高度  
    private int mRangeRight;//mFrontView侧拉时向左移动的最大距离,即mRightView的宽度
    private int mRangeLeft;

    private long mDateId;//为listview提供数据接口

    public long getDateId() {
        return mDateId;
    }

    public void setDateId(long mDateId) {
        this.mDateId = mDateId;
    }

    //以下是定义SwipeLayout的打开,关闭,滑动的3种状态
    public enum Status {
        CLOSE, OPEN_LEFT,OPEN_RIGHT,DRAGING_LEFT,DRAGING_RIGHT;
    }
    //默认关闭
    private Status mStatus = Status.CLOSE;
    //滑动的监听器
    private SwipeViewListener mSwipeViewListener;


    public View getFrontView(){
        return mFrontView;
    }
    public interface SwipeViewListener {
        //关闭
        void onClose(SwipeLayout mSwipeLayout);

        //打开左边
        void onOpenLeft(SwipeLayout mSwipeLayout);

        //正在侧拉
        void onDraging(SwipeLayout mSwipeLayout);

        //开始要去关闭左边
        void onStartCloseLeft(SwipeLayout mSwipeLayout);

        //开始要去开启左边
        void onStartOpenLeft(SwipeLayout mSwipeLayout);

        //打开右边
        void onOpenRight(SwipeLayout mSwipeLayout);

        //开始要去关闭右边
        void onStartCloseRight(SwipeLayout mSwipeLayout);

        //开始要去开启右边
        void onStartOpenRight(SwipeLayout mSwipeLayout);
    }

    //设置监听器
    public void setSwipeViewListener(SwipeViewListener swipeViewListener) {
        mSwipeViewListener = swipeViewListener;
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //1.初始ViewDragHelper  
    private void init() {
        mCallback = new ViewDragHelper.Callback() {
            //3.在回调方法中处理触摸事件  
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                //当有控件滑动时，静止父元素响应事件
                getParent().requestDisallowInterceptTouchEvent(true);
                return true; //允许所有子控件的滑动  
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return mWidth;
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return mHeight;
            }

            //设定滑动的边界值
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {

                if (child == mFrontView) {
                    //前景View的滑动范围是(mRangeLeft~ -mRangeRight)
                    if (left > mRangeLeft) {
                        left = mRangeLeft;
                    } else if (left < -mRangeRight) {
                        left = -mRangeRight;
                    }
                }
                if (child == mRightView) {
                    //右View的滑动范围是(mWidth - mRangeRight ~ mWidth)
                    if (left > mWidth) {
                        left = mWidth;
                    } else if (left < (mWidth - mRangeRight)) {
                        left = mWidth - mRangeRight;
                    }
                }

                if (child == mLeftView) {
                    //左View的滑动范围是(-mRangeLeft - 0)
                    if (left > 0) {
                        left = 0;
                    } else if (left < -mRangeLeft) {
                        left = mRangeLeft;
                    }
                }
                //返回修正过的建议值  
                return left;
            }

            //监听View的滑动位置的改变,同步前景View和背景View的滑动事件  
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                if (changedView == mFrontView) {
                    //当滑动前景View时,也需要滑动左 右View
                    mLeftView.offsetLeftAndRight(dx);
                    mRightView.offsetLeftAndRight(dx);
                } else if (changedView == mRightView) {
                    //当滑动右View时,也需要滑动前景View   左View
                    mFrontView.offsetLeftAndRight(dx);
                    mLeftView.offsetLeftAndRight(dx);
                }else if(changedView == mLeftView){
                    //当滑动左View时,也需要滑动前景View   右View
                    mFrontView.offsetLeftAndRight(dx);
                    mRightView.offsetLeftAndRight(dx);
                }
                dispatchSwipeEvent();
                // 兼容老版本  
                invalidate();
            }

            //处理释放后的开启和关闭动作  
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                if(mFrontView.getLeft()>0){
                    if(xvel > 0) {
                        openLeftView();
                    }else if(xvel == 0 && mFrontView.getLeft() > mRangeLeft / 2.0f){
                        openLeftView();
                    }else{
                        closeLeftView();
                    }
                }else if(mFrontView.getLeft() == mWidth){
                    if(xvel < 0 ){
                        closeLeftView();
                    }else if(xvel == 0 && mFrontView.getLeft() < mRangeLeft / 2.0f){
                        closeLeftView();
                    }else{
                        openLeftView();
                    }
                }else if(mFrontView.getLeft() == 0){
                    if(xvel > 0 ){
                        closeRightView();
                    }else if(xvel == 0 && mFrontView.getLeft() > -mRangeRight / 2.0f){
                        closeRightView();
                    }else{
                        openRightView();
                    }
                }else{
                    if(xvel < 0) {
                        openRightView();
                    }else if(xvel == 0 &&mFrontView.getLeft() < -mRangeRight / 2.0f){
                        openRightView();
                    }else{
                        closeRightView();
                    }
                }
            }
        };
        mDragHelper = ViewDragHelper.create(this, mCallback);
    }
    /**
     * 处理滑动,打开,关闭的3种情况
     * 在onViewPositionChanged 调用
     */
    private void dispatchSwipeEvent() {
        if (mSwipeViewListener != null) {
            mSwipeViewListener.onDraging(this);
        }
        //记录上一次的状态
        Status preStatus = mStatus;
        //获取当前的状态
        mStatus = getCurrStatus();
        if (preStatus != mStatus && null != mSwipeViewListener) {
            //说明有状态发生变化
            if (mStatus == Status.CLOSE) {
                //关闭
                mSwipeViewListener.onClose(this);
            } else if (mStatus == Status.OPEN_LEFT) {
                //打开
                mSwipeViewListener.onOpenLeft(this);
            } else if (mStatus == Status.OPEN_RIGHT) {
                //打开
                mSwipeViewListener.onOpenRight(this);
            } else if (mStatus == Status.DRAGING_LEFT) {
                //这里有2中情况,要么要打开,要么要关闭
                if (preStatus == Status.CLOSE) {
                    //如果之前是关闭的,那么就是要打开
                    mSwipeViewListener.onStartOpenLeft(this);
                } else if (preStatus == Status.OPEN_LEFT) {
                    //如果之前是打开,那么就是要关闭
                    mSwipeViewListener.onStartCloseLeft(this);
                }
            }else if (mStatus == Status.DRAGING_RIGHT) {
                //这里有2中情况,要么要打开,要么要关闭
                if (preStatus == Status.CLOSE) {
                    //如果之前是关闭的,那么就是要打开
                    mSwipeViewListener.onStartOpenRight(this);
                } else if (preStatus == Status.OPEN_RIGHT) {
                    //如果之前是打开,那么就是要关闭
                    mSwipeViewListener.onStartCloseRight(this);
                }
            }
        }
    }

    /**
     * 获取当前的状态
     *
     * @return
     */
    public Status getCurrStatus() {
        int left = mFrontView.getLeft();
        if (left == 0) {
            return Status.CLOSE;
        } else if (left == -mRangeRight) {
            return Status.OPEN_RIGHT;
        }else if(left == mRangeLeft){
            return Status.OPEN_LEFT;
        }else if(left < mRangeLeft && left >0){
            return Status.DRAGING_LEFT;
        }else if(left > -mRangeRight && left <0){
            return Status.DRAGING_RIGHT;
        }
        return Status.CLOSE;
    }


    //2.传递触摸事件  
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //获取子控件的引用  
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftView = getChildAt(0);
        mRightView = getChildAt(1); //获取背景View,即展示数据的Item的右边隐藏的侧滑布局  
        mFrontView = getChildAt(2);//获取前景View,即展示数据的Item  
    }

    //获取子控件的相关宽高信息  
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = mFrontView.getMeasuredWidth();
        mHeight = mFrontView.getMeasuredHeight();
        mRangeRight = mRightView.getMeasuredWidth();
        mRangeLeft = mLeftView.getMeasuredWidth();
    }

    //确定子控件的初始位置  
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutChildView(false);
    }

    /**
     * 放置子控件的位置 
     *
     * @param isOpen 是否是打开前景View,true打开,false关闭 
     */
    private void layoutChildView(boolean isOpen) {
        //计算前景View的位置,将坐标信息封装到矩形中  
        Rect fontRect = computerFontViewRect(isOpen);
        //摆放前景View  
        mFrontView.layout(fontRect.left, fontRect.top, fontRect.right, fontRect.bottom);


        int left = fontRect.right;

        int right = fontRect.left;
        mRightView.layout(left, 0, left + mRangeRight, mHeight);


        mLeftView.layout(right-mWidth ,0 ,right,mHeight);
        //由于上面是后摆放背景View,所以会覆盖前景View,因此需要通过下面的方式将前景View显示在前面  
        bringChildToFront(mFrontView);
    }

    /**
     * 计算前景View的坐标 
     *
     * @param isOpen 是否是打开前景View 
     * @return
     */
    private Rect computerFontViewRect(boolean isOpen) {
        int left = isOpen ? -mRangeRight : 0;
        return new Rect(left, 0, left + mWidth, mHeight);
    }

    /**
     * 打开侧边栏mRightView,默认平滑打开 
     */
    public void openRightView() {
        openRightView(true);
    }

    /**
     * 打开侧边栏mRightView 
     *
     * @param isSmooth 是否平滑打开 
     */
    public void openRightView(boolean isSmooth) {
        if (isSmooth) {
            if (mDragHelper.smoothSlideViewTo(mFrontView, -mRangeRight, 0)) {
                //动画在继续  
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutChildView(true);
        }
    }

    /**
     * 关闭侧边栏mRightView,默认平滑关闭 
     */
    public void closeRightView() {
        closeRightView(true);
    }

    public void closeView(){
        if(getCurrStatus() == Status.OPEN_RIGHT){
            closeRightView();
        }else if(getCurrStatus()==Status.OPEN_LEFT){
            closeLeftView();
        }
    }

    /**
     * 关闭侧边栏mRightView 
     *
     * @param isSmooth 是否平滑关闭 
     */
    public void closeRightView(boolean isSmooth) {
        if (isSmooth) {
            if (mDragHelper.smoothSlideViewTo(mRightView, mWidth, 0)) {
                //动画在继续  
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutChildView(false);
        }
    }


    /**
     * 打开侧边栏mLeftView,默认平滑打开
     */
    public void openLeftView() {
        openLeftView(true);
    }
    /**
     * 打开侧边栏mLeftView
     *
     * @param isSmooth 是否平滑打开
     */
    public void openLeftView(boolean isSmooth) {
        if (isSmooth) {
            if (mDragHelper.smoothSlideViewTo(mFrontView, mRangeLeft, 0)) {
                //动画在继续
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutChildView(true);
        }
    }

    /**
     * 关闭侧边栏mLeftView,默认平滑关闭
     */
    public void closeLeftView() {
        closeRightView(true);
    }

    /**
     * 关闭侧边栏mLeftView
     *
     * @param isSmooth 是否平滑关闭
     */
    public void closeLeftView(boolean isSmooth) {
        if (isSmooth) {
            if (mDragHelper.smoothSlideViewTo(mRightView, 0, 0)) {
                //动画在继续
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutChildView(false);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            //动画还在继续  
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}  
