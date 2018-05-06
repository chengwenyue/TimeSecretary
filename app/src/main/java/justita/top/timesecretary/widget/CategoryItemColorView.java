package justita.top.timesecretary.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import justita.top.timesecretary.R;

public class CategoryItemColorView extends View{
    public int categoryItemColor;
    public int categoryItemSize;
    public float categoryItemRadius;
    /**
     * 绘制时控制文本绘制的范围
     */
    private Paint mPaint;

    public CategoryItemColorView(Context context) {
        this(context,null);
    }

    public CategoryItemColorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CategoryItemColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CategoryItemColorView, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++)
        {
            int attr = a.getIndex(i);
            switch (attr)
            {
                case R.styleable.CategoryItemColorView_categoryItemColor:
                    categoryItemColor = a.getInt(attr,getResources().getColor(R.color.category_default));
                    break;
                case R.styleable.CategoryItemColorView_categoryItemRadius:
                    categoryItemRadius = a.getFloat(attr,0f);
                    break;
                case R.styleable.CategoryItemColorView_categoryItemSize:
                    categoryItemSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    break;

            }
        }
        mPaint = new Paint();
    }

    //当设置了WRAP_CONTENT时，我们需要自己进行测量，即重写onMesure方法”：
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height ;
        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        } else
        {
            width = getPaddingLeft() + categoryItemSize + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
        } else
        {
            height= getPaddingTop() + categoryItemSize + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        if(getBackground() != null)
//            getBackground().draw(canvas);
        mPaint.setAntiAlias(true); // 消除锯齿
//        mPaint.setColor(Color.WHITE);
//        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        mPaint.setColor(categoryItemColor);
        int left = (getMeasuredWidth() - categoryItemSize)/2;
        int top = (getMeasuredHeight() - categoryItemSize)/2;
        canvas.drawRoundRect(left,top,left+categoryItemSize ,top+categoryItemSize,categoryItemRadius,categoryItemRadius,mPaint);
    }

    public void setCategoryItemColor(int color) {
        categoryItemColor = color;
        postInvalidate();
    }
}
