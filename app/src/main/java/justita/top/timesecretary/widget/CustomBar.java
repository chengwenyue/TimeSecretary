package justita.top.timesecretary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.text.ParseException;
import java.util.Date;

import justita.top.timesecretary.R;
import justita.top.timesecretary.uitl.DateUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;


public class CustomBar extends View {
    private int mBgColor;
    private int mPgColor = Color.BLACK;
    private int mBarWidth = 2;
    private Paint mPaint;
    private float mProgress = 0f;
    private float maxProgress = 100f;
    private RectF bgRect;
    private String time;
    private long timeSlice = 0;
    private long addGrade;
    private long addAlready = 0;
    private float addRate  = 0.0f;

    private Date startDate;
    private Date endDate;
    private CompleteListener completeListener;

    public void setCompleteListener(CompleteListener completeListener) {
        this.completeListener = completeListener;
    }

    public CustomBar(Context context) {
        super(context);
        init(null, 0);
    }

    public CustomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CustomBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    public interface CompleteListener{
        void onCompleteListener(CustomBar customBar);
    }
    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CustomBar, defStyle, 0);

        mBgColor = a.getColor(R.styleable.CustomBar_bgColor,Color.WHITE);
        mPgColor = a.getColor(R.styleable.CustomBar_pgColor,mPgColor);
        mBarWidth = (int) a.getDimension(R.styleable.CustomBar_barWidth,2);
        a.recycle();
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        bgRect = new RectF(0,0,width,mBarWidth);
        setMeasuredDimension(width, mBarWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setAntiAlias(true); // 消除锯齿
        mPaint.setStyle(Paint.Style.FILL);


        //背景
        drawBackGround(canvas);

        //进度
        drawProgress(canvas);
    }

    /**
     * 边框
     * @param canvas
     */
    private void drawBackGround(Canvas canvas) {
        mPaint.setColor(mBgColor);

        canvas.drawRect(bgRect,mPaint);
    }

    /**
     * 进度
     */
    private void drawProgress(Canvas canvas) {
        mPaint.setColor(mPgColor);
        float right = (mProgress / maxProgress) * getMeasuredWidth();

        canvas.drawRect(new RectF(0,0,right,mBarWidth),mPaint);
    }

    private int dp2px(int dp){
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    public void setProgress(float progress){
        if(progress < maxProgress){
            this.mProgress = progress;
        } else {
            if(completeListener != null)
                completeListener.onCompleteListener(this);
            if(mProgress == maxProgress){
                return;
            }else {
                this.mProgress = maxProgress;
            }
        }
        invalidate();
    }

    public void setPostProgress(float progress){
        if(progress < maxProgress){
            this.mProgress = progress;
        } else {
            if(completeListener != null)
                completeListener.onCompleteListener(this);
            if(mProgress == maxProgress){
                return;
            }else {
                this.mProgress = maxProgress;
            }
        }
        postInvalidate();
    }

    public void addPostProgress(long progress){
        addAlready += progress;
        if(addAlready >= addGrade){
            addGrade();
            addAlready = 0;
        }
    }

    private void addGrade() {
        if(startDate.getTime() < System.currentTimeMillis()){
            mProgress += addRate;
            setPostProgress(mProgress);
        }
    }

    public void setMaxProgress(float maxProgress){
        this.maxProgress = maxProgress;
    }

    public void setTime(String time) {
        this.time = time;
        String[] dates = time.split(PreferenceConstants.TIME_SEPARATOR);
        try {


            startDate = DateUtils.formatDate(dates[0], DateUtils.DEFAULT_FORMAT);
            String date = DateUtils.formatDate(startDate,DateUtils.YEAR_MOUTH_DAY);
            endDate = DateUtils.formatDate(date+" "+dates[1], DateUtils.DEFAULT_FORMAT);

            timeSlice = endDate.getTime() - startDate.getTime();
            setAddGrade(timeSlice);
            if (startDate.getTime() > System.currentTimeMillis()) {
                setProgress(0f);
            } else if (endDate.getTime() < System.currentTimeMillis()) {
                setProgress(100f);
            } else {
                long progress = System.currentTimeMillis() - startDate.getTime();
                progress = progress*100;
                setProgress(progress / timeSlice );
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setAddGrade(long timeSlice) {

        //5分钟
        if(timeSlice< 5 *60 * 1000){
            //2秒前进
            addGrade = 2*1000;


            //15分钟
        }else if(timeSlice < 15 * 60 * 1000){
            addGrade = 6*1000;
        }else if(timeSlice < 30 * 60 * 1000){
            addGrade = 12*1000;
        }else if(timeSlice < 60 * 60 * 1000){
            addGrade = 24*1000;
        }else if(timeSlice < 3 * 60 * 60 *1000){
            addGrade = 72 *1000;
        }else{
            addGrade = 5*60*1000;
        }

        addRate = ((float) addGrade *100) /(float) timeSlice;
    }
}
