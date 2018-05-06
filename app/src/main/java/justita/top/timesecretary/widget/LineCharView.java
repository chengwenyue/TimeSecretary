package justita.top.timesecretary.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import justita.top.timesecretary.R;

public class LineCharView extends View {
    private int MAX_Y_VALUE;

    private int xori;
    private int yori;
    private int xinit;
    private int xylinewidth;
    private int xytextcolor;
    private int xytextsize;
    private int linecolor;
    private int interval;//坐标间的间隔
    private int backgroundColor;
    private List<Integer> x_coord_values;//事件数集合
    private String[] weeksArray = {"四", "五", "六", "日", "一", "二", "三"};

    private int heigth;
    private float textwidth;

    public LineCharView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineChar);
        xylinewidth = typedArray.getLayoutDimension(R.styleable.LineChar_xyLineWidth, 5);
        xytextcolor = typedArray.getColor(R.styleable.LineChar_xyTextColor, Color.BLACK);
        xytextsize = typedArray.getLayoutDimension(R.styleable.LineChar_xyTextSize, 20);
        linecolor = typedArray.getColor(R.styleable.LineChar_lineColor, getResources().getColor(R.color.colorPrimaryDark));
        interval = typedArray.getLayoutDimension(R.styleable.LineChar_interval, 100);
        backgroundColor = typedArray.getColor(R.styleable.LineChar_backgroundColor, Color.WHITE);
        typedArray.recycle();
        x_coord_values = new ArrayList<Integer>();
        for (int i = 0; i < 7; i++) {
            x_coord_values.add(0);
        }

    }

    public LineCharView(Context context) {
        super(context, null);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        if (changed) {
            heigth = getHeight();
            Paint paint = new Paint();
            paint.setTextSize(xytextsize);
            textwidth = paint.measureText("A");// 取得字符串显示的宽度值
            xori = (int) (textwidth + 6 + 2 * xylinewidth);//6 为与y轴的间隔
            yori = heigth - xytextsize - 2 * xylinewidth - 3;//3 为x轴的间隔
            xinit = interval / 2;
            setBackgroundColor(backgroundColor);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @SuppressLint("ResourceAsColor")
    private void drawX(Canvas canvas) {
        Paint x_coordPaint = new Paint();
        x_coordPaint.setTextSize(xytextsize);
        x_coordPaint.setStyle(Paint.Style.FILL);
        x_coordPaint.setAntiAlias(true);
        Path path = new Path();
        int x = 0;
        for (int i = 0; i < x_coord_values.size(); i++) {
            x = i * interval + xinit;
            if (i == 0) {
                path.moveTo(x, getYValue(x_coord_values.get(i)));
            } else {
                path.lineTo(x, getYValue(x_coord_values.get(i)));
            }
            x_coordPaint.setColor(linecolor);
            canvas.drawCircle(x, getYValue(x_coord_values.get(i)), xylinewidth * 2, x_coordPaint);
            String text = weeksArray[i];
            x_coordPaint.setColor(xytextcolor);
            canvas.drawText(text, x - x_coordPaint.measureText(text) / 2, yori + xytextsize + xylinewidth * 2, x_coordPaint);
            x_coordPaint.setColor(getResources().getColor(R.color.gray_white));
            x_coordPaint.setStrokeWidth(xylinewidth / 2);
            canvas.drawLine(x, getYValue(x_coord_values.get(i)), x, yori, x_coordPaint);
        }
        x_coordPaint.setStyle(Paint.Style.STROKE);
        x_coordPaint.setStrokeWidth(xylinewidth);
        x_coordPaint.setColor(linecolor);
        canvas.drawPath(path, x_coordPaint);
        x_coordPaint.setAlpha(80);
        x_coordPaint.setStyle(Paint.Style.FILL);
        path.lineTo(x, yori);
        path.lineTo(xinit, yori);
        path.lineTo(xinit, getYValue(x_coord_values.get(0)));
        //画折线
        canvas.drawPath(path, x_coordPaint);
    }

    private int getMaxYValue(List<Integer> values) {
        int max = values.get(0);
        for (int i = 0; i < values.size(); i++) {
            if (max < values.get(i)) {
                max = values.get(i);
            }
        }
        return max;
    }

    private float getYValue(int value) {
        if (MAX_Y_VALUE == 0) {
            return yori;
        } else {
            float ave = (float) (yori - interval / 2) / MAX_Y_VALUE;
            return yori - (ave * value);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawX(canvas);
    }

    /**
     * 设置坐标折线图值
     *
     * @param x_coord_values 每个点的值
     */
    public void setValue(List<Integer> x_coord_values) {
        this.x_coord_values = x_coord_values;
        MAX_Y_VALUE = getMaxYValue(x_coord_values);
        invalidate();
    }
}
