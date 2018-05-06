package justita.top.timesecretary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import justita.top.timesecretary.R;

public class BarChartView extends View{
    private int DEFAULT_LENTH = 4;
    private int MAX_Y_VALUE;

    private int barWidth;
    private int barColor;
    private int textSize;
    private int textColor;
    private int interval;//柱间距
    private int backgroundColor;//背景颜色
    private int width;
    private List<Integer> x_coord_values;//事件数量集合
    private String[] weeksArray = {"四", "五", "六", "日", "一", "二", "三"};

    public BarChartView(Context context) {
        this(context, null);
    }
    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarChart);
        barWidth = typedArray.getInt(R.styleable.BarChart_barChartWidth, 30);
        barColor = typedArray.getColor(R.styleable.BarChart_barColor, Color.GRAY);
        textColor = typedArray.getColor(R.styleable.BarChart_textColor, Color.BLACK);
        textSize = typedArray.getLayoutDimension(R.styleable.BarChart_textSize, 20);
        interval = typedArray.getLayoutDimension(R.styleable.BarChart_interval2, 100);
        backgroundColor = typedArray.getColor(R.styleable.BarChart_backgroundColor2, Color.WHITE);
        typedArray.recycle();
        x_coord_values = new ArrayList<Integer>();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        if (changed) {
            width = getWidth();
            setBackgroundColor(backgroundColor);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint barPaint = new Paint();
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setStrokeWidth(barWidth);
        barPaint.setTextSize(textSize);
        for (int i = 0; i < x_coord_values.size(); i++) {
            int y = i * interval + barWidth;
            barPaint.setColor(barColor);
            canvas.drawLine(0, y, getXValue(x_coord_values.get(i)), y, barPaint);
            barPaint.setColor(textColor);
            canvas.drawText(weeksArray[i]+" - "+ x_coord_values.get(i),
                    getXValue(x_coord_values.get(i)) + interval / 2, y + barWidth / 2 - 2, barPaint);
        }
    }

    private int getMaxXValue(List<Integer> values){
        int max = values.get(0);
        for (int i = 0; i < values.size(); i++) {
            if (max < values.get(i)) {
                max = values.get(i);
            }
        }
        return max;
    }

    private float getXValue(int value){
        float ave = (width - interval * 2.5f) / MAX_Y_VALUE;
        if (value == 0){
            return DEFAULT_LENTH;
        }
        return ave * value;
    }

    /**
     * 设置柱状图值
     *
     * @param x_coord_values 每个点的值
     */
    public void setValue(List<Integer> x_coord_values) {
        this.x_coord_values = x_coord_values;
        MAX_Y_VALUE = getMaxXValue(x_coord_values);
        invalidate();
    }
}
