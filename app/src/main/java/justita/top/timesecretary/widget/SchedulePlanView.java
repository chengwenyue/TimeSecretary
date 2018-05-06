package justita.top.timesecretary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import justita.top.timesecretary.R;

public class SchedulePlanView extends View {

    private int width;
    private int height;
    private int interval;
    private int textColor;
    private int bgColor;
    private int occupyColor; //占用颜色
    private int busyColor; // 忙碌颜色
    private int XYLineWidth; // 线宽
    private int textSize;
    private int xori;
    private int yori;

    private List<String> dateList;
    private Map<Integer, List<String>> dateMap;

    private Paint paint;
    private float textWidth;

    private String[] weeks = {"日", "一", "二", "三", "四", "五", "六"};

    public SchedulePlanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlanningChart);
        interval = typedArray.getLayoutDimension(R.styleable.PlanningChart_PlanningChartInterval, 14);
        XYLineWidth = typedArray.getLayoutDimension(R.styleable.PlanningChart_PlanningChartXYLineWidth, 1);
        textColor = typedArray.getColor(R.styleable.PlanningChart_PlanningChartTextColor, Color.GRAY);
        occupyColor = typedArray.getColor(R.styleable.PlanningChart_PlanningChartOccupyColor, Color.GRAY);
        busyColor = typedArray.getColor(R.styleable.PlanningChart_PlanningChartBusyColor, Color.BLUE);
        textSize = typedArray.getLayoutDimension(R.styleable.PlanningChart_PlanningChartTextSize, 24);
        bgColor = typedArray.getColor(R.styleable.PlanningChart_PlanningChartBgColor, Color.WHITE);
        typedArray.recycle();
        dateMap = new HashMap<>();
        dateList = new ArrayList<>();
    }

    public SchedulePlanView(Context context) {
        this(context, null);
        System.out.println("一个参数");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            width = getWidth();
            height = getHeight();
            getPaint().setTextSize(textSize);
            textWidth = getPaint().measureText("A");
            yori = height - textSize - 2 * XYLineWidth - 3;
            xori = textSize * 3 + 5 + 2 * XYLineWidth;
            setBackgroundColor(bgColor);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawX(canvas);
        drawY(canvas);
        drawBar(canvas);
        drawIcon(canvas);
    }

    private void drawIcon(Canvas canvas) {
        int textSizeic = XYLineWidth * 40;
        getPaint().reset();
        getPaint().setStyle(Paint.Style.FILL);
        getPaint().setAntiAlias(true);
        getPaint().setColor(occupyColor);
        getPaint().setTextSize(textSizeic);
        getPaint().setStrokeWidth(textSizeic);
        canvas.drawLine(4 * interval + xori, interval * 0.75f, 4 * interval + xori + textSizeic, interval * 0.75f, getPaint());
        canvas.drawText("占用", 4 * interval + xori + interval / 2, interval * 0.75f + textSizeic * 0.4f, getPaint());
        canvas.drawText("空闲", 4 * interval + xori + interval * 2.5f, interval * 0.75f + textSizeic * 0.4f, getPaint());
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(XYLineWidth);
        canvas.drawRect(4 * interval + xori + interval * 2, interval * 0.75f - 12, 4 * interval + xori + interval * 2 + textSizeic, interval * 0.75f + 12, getPaint());
    }

    private void drawX(Canvas canvas) {
        getPaint().reset();
        getPaint().setColor(occupyColor);
        getPaint().setTextSize(textSize);
        getPaint().setStyle(Paint.Style.FILL);
        getPaint().setStrokeWidth(XYLineWidth * 2);
        getPaint().setAntiAlias(true);
        canvas.drawLine(xori + 10, yori, xori + 7 * interval + 12 * XYLineWidth, yori, getPaint());
        for (int i = 0; i < 7; i++) {
            int x = i * interval + xori;
            canvas.drawText(weeks[i], x + getPaint().measureText(weeks[i]), yori + textSize + 2 * XYLineWidth, getPaint());
        }
    }

    private void drawY(Canvas canvas) {
        getPaint().reset();
        getPaint().setStyle(Paint.Style.FILL);
        getPaint().setTextSize(textSize);
        getPaint().setAntiAlias(true);
        getPaint().setColor(occupyColor);
        getPaint().setStrokeWidth(XYLineWidth * 4);
        canvas.drawLine(xori, yori + interval / 4, xori, yori - 6 * interval - interval / 4, getPaint());
        for (int i = 24; i >= 0; i--) {
            float y = yori - i * interval / 4;
            if (i == 0 || i % 6 == 0) {
                getPaint().setColor(busyColor);
                if (i == 24 || i == 18) {
                    canvas.drawText(String.valueOf(24 - i), xori / 4 + textWidth / 2, y + textWidth / 2, getPaint());
                } else {
                    canvas.drawText(String.valueOf(24 - i), xori / 4, y + textWidth / 2, getPaint());
                }
                canvas.drawLine(xori / 2 + textSize / 2, y, xori - 10, y, getPaint());
            } else {
                getPaint().setColor(occupyColor);
                canvas.drawLine(xori / 2 + textSize, y, xori - 10, y, getPaint());
            }
        }
    }

    private void drawBar(Canvas canvas) {
        getPaint().reset();
        getPaint().setStyle(Paint.Style.FILL);
        getPaint().setColor(occupyColor);
        getPaint().setTextSize(textSize);
        getPaint().setAntiAlias(true);
        getPaint().setStrokeWidth(XYLineWidth * textSize);
        for (int i = 0; i < 7; i++) {
            if (dateMap.get(i) != null) {
                for (String date : dateMap.get(i)) {
                    if (date != null) {
                        String[] dates = date.split("--");
                        int barLength = timetoNumber(dates[1]) - timetoNumber(dates[0]);
                        if (barLength > 0) {
                            canvas.drawLine(xori + i * interval + textSize * 1.5f, getBarPosition(dates[0]), xori + i * interval + textSize * 1.5f, getBarPosition(dates[1]), getPaint());
                        }
                    }
                }
            }
        }
    }

    private int getBarPosition(String time) {
        int defPosition = yori - 6 * interval;
        int timeNumber = timetoNumber(time);
        int pos = defPosition + 6 * interval * timeNumber / 1440;
        return pos;
    }

    private int timetoNumber(String time) {
        String[] times = time.split(":");
        return Integer.parseInt(times[0]) * 60 + Integer.parseInt(times[1]);
    }

    private Paint getPaint() {
        if (paint == null) {
            paint = new Paint();
        }
        return paint;
    }

    /**
     * @param dateMap 保存有某天的所有事件的List
     */
    public void setValue(Map<Integer, List<String>> dateMap) {
        this.dateMap = dateMap;
        invalidate();
    }
}
