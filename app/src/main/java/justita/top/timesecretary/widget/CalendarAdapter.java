package justita.top.timesecretary.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Date;

import justita.top.timesecretary.R;
import justita.top.timesecretary.uitl.DateUtils;

/**
 * 日历gridview中的每一个item显示的textview
 * 
 * @author Vincent Lee
 * 
 */
public class CalendarAdapter extends BaseAdapter {
	public final static int DEFALUT_STYLE = 0;
	public final static int SELECTED_STYLE = 1;

	private boolean isLeapyear = false; // 是否为闰年
	private int daysOfMonth = 0; // 某月的天数
	private int dayOfWeek = 0; // 具体某一天是星期几
	private int lastDaysOfMonth = 0; // 上一个月的总天数
	private Context context;
	private int[] dayNumber = new int[42]; // 一个gridview中的日期存入此数组中

	private TextView[] textViews = new TextView[42];
	// private static String week[] = {"周日","周一","周二","周三","周四","周五","周六"};
	private SpecialCalendar sc = null;
	private Drawable drawable = null;

	private String currentYear = "";
	private String currentMonth = "";
	private String currentDay = "";
	private String currentDate = "";

	private int selectFlag = -1; // 用于标记选择的日期
	private int[] schDateTagFlag = null; // 存储当月所有的日程日期

	private String animalsYear = "";
	private String leapMonth = ""; // 闰哪一个月
	private String cyclical = ""; // 天干地支


	// 系统当前时间
	private String selectDate = "";
	private String select_year = "";
	private String select_month = "";
	private String select_day = "";

	public CalendarAdapter(Context context,int jumpMonth, int jumpYear, String selectDate) {
		this.context = context;
		sc = new SpecialCalendar();

		this.selectDate = selectDate;
		select_year = selectDate.split("-")[0];
		select_month = selectDate.split("-")[1];
		select_day = selectDate.split("-")[2];

		currentDate = DateUtils.formatDate(new Date(),DateUtils.DEFAULT_FORMAT_TWO);
		int year_c = Integer.parseInt(currentDate.split("-")[0]);
		int month_c = Integer.parseInt(currentDate.split("-")[1]);
		int day_c = Integer.parseInt(currentDate.split("-")[2]);

		int stepYear = year_c + jumpYear;
		int stepMonth = month_c + jumpMonth;
		if (stepMonth > 0) {
			// 往下一个月滑动
			if (stepMonth % 12 == 0) {
				stepYear = year_c + stepMonth / 12 - 1;
				stepMonth = 12;
			} else {
				stepYear = year_c + stepMonth / 12;
				stepMonth = stepMonth % 12;
			}
		} else {
			// 往上一个月滑动
			stepYear = year_c - 1 + stepMonth / 12;
			stepMonth = stepMonth % 12 + 12;
			if (stepMonth % 12 == 0) {

			}
		}

		currentYear = String.valueOf(stepYear); // 得到当前的年份
		currentMonth = String.valueOf(stepMonth); // 得到本月
													// （jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
		currentDay = String.valueOf(day_c); // 得到当前日期是哪天

		getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));

	}


	@Override
	public int getCount() {
		return dayNumber.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.calendar_item, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.tvtext);
		int d = dayNumber[position];
		textView.setText(d+"");
		textView.setTextColor(Color.GRAY);
		textViews[position] = textView;

		if (position < daysOfMonth + dayOfWeek && position >= dayOfWeek) {
			// 当前月信息显示
			textView.setTextColor(Color.BLACK);// 当月字体设黑
			drawable = new ColorDrawable(Color.rgb(255, 255, 255));
			textView.setBackgroundDrawable(drawable);
		}

		if (selectFlag == position) {
			// 设置当天的背景
			drawable = new ColorDrawable(Color.rgb(23, 126, 214));
			textView.setBackgroundDrawable(drawable);
			textView.setTextColor(Color.WHITE);
		}
		return convertView;
	}


	// 得到某年的某月的天数且这月的第一天是星期几
	public void getCalendar(int year, int month) {
		isLeapyear = sc.isLeapYear(year); // 是否为闰年
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month); // 某月的总天数

		// 0 -6 周日-周六
		dayOfWeek = sc.getWeekdayOfMonth(year, month); // 某月第一天为星期几
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month - 1); // 上一个月的总天数
		Log.d("DAY", isLeapyear + " ======  " + daysOfMonth + "  ============  " + dayOfWeek + "  =========   " + lastDaysOfMonth);
		getWeek(year, month);
	}

	// 将一个月中的每一天的值添加入数组dayNumber中
	private void getWeek(int year, int month) {
		int j = 1;

		// 得到当前月的所有日程日期(这些日期需要标记)

		for (int i = 0; i < dayNumber.length; i++) {

			if (i < dayOfWeek) { // 前一个月
				int temp = lastDaysOfMonth - dayOfWeek + 1;
				dayNumber[i] = temp + i;

			} else if (i < daysOfMonth + dayOfWeek) { // 本月
				String day = String.valueOf(i - dayOfWeek + 1); // 得到的日期
				dayNumber[i] = i - dayOfWeek + 1;
				// 对于当前月才去标记当前日期
				if (select_year.equals(String.valueOf(year)) && select_month.equals(String.valueOf(month)) && select_day.equals(day)) {
					// 标记当前日期
					selectFlag = i;
				}
			} else { // 下一个月
				dayNumber[i] = j;
				j++;
			}
		}
	}

	public void matchScheduleDate(int year, int month, int day) {

	}

	/**
	 * 点击每一个item时返回item中的日期
	 *
	 * @param position
	 * @return
	 */
	public int getDateByClickItem(int position) {
		return dayNumber[position];
	}

	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 *
	 * @return
	 */
	public int getStartPositon() {
		return dayOfWeek + 7;
	}

	public String getCurrentYear() {
		return currentYear;
	}

	public String getCurrentMonth() {
		return currentMonth;
	}

	public void changeBackgroundByDate(String date,int style){
		if(!date.split("-")[0].equals(currentYear) || !date.split("-")[1].equals(currentMonth))
			return;

		int day  = Integer.valueOf(date.split("-")[2]);
		TextView textView = textViews[day + dayOfWeek -1];
		if(style == DEFALUT_STYLE){
			textView.setTextColor(Color.BLACK);// 当月字体设黑
			drawable = new ColorDrawable(Color.rgb(255, 255, 255));
			textView.setBackgroundDrawable(drawable);
		}else{
			drawable = new ColorDrawable(Color.rgb(23, 126, 214));
			textView.setBackgroundDrawable(drawable);
			textView.setTextColor(Color.WHITE);
		}
	}
}
