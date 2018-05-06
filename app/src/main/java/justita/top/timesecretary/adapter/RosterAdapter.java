package justita.top.timesecretary.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.provider.ChatProvider;
import justita.top.timesecretary.provider.RosterProvider;
import justita.top.timesecretary.provider.RosterProvider.RosterConstants;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.StatusMode;

public class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.MyViewHolder> {
	private MyViewHolder mExpandedItemHolder;
	private int DURATION  = 300;
	private static final int ROTATE_90_DEGREE = 90;
	private Interpolator mExpandInterpolator;
	private Interpolator mCollapseInterpolator;
	private RecyclerView recyclerView;
	private static final String SORT_ORDER = ChatProvider.ChatConstants.DATE + " DESC";

	public static interface OnRosterBtClick{
		void onBtClick(int which, Roster roster);
	}

	private OnRosterBtClick onRosterBtClick;

	public void setOnRosterBtClick(OnRosterBtClick onRosterBtClick) {
		this.onRosterBtClick = onRosterBtClick;
	}

	// 不在线状态
	private static final String OFFLINE_EXCLUSION = RosterConstants.STATUS_MODE
			+ " != " + StatusMode.offline.ordinal();
	// 在线人数
	private static final String COUNT_AVAILABLE_MEMBERS = "SELECT COUNT() FROM "
			+ RosterProvider.TABLE_ROSTER
			+ " inner_query"
			+ " WHERE inner_query."
			+ RosterConstants.GROUP
			+ " = "
			+ RosterProvider.QUERY_ALIAS
			+ "."
			+ RosterConstants.GROUP
			+ " AND inner_query." + OFFLINE_EXCLUSION;
	// 总人数
	private static final String COUNT_MEMBERS = "SELECT COUNT() FROM "
			+ RosterProvider.TABLE_ROSTER + " inner_query"
			+ " WHERE inner_query." + RosterConstants.GROUP + " = "
			+ RosterProvider.QUERY_ALIAS + "." + RosterConstants.GROUP;
	private static final String[] GROUPS_QUERY_COUNTED = new String[] {
			RosterConstants._ID,
			RosterConstants.GROUP,
			"(" + COUNT_AVAILABLE_MEMBERS + ") || '/' || (" + COUNT_MEMBERS
					+ ") AS members" };
	// 联系人查询序列
	public static final String[] ROSTER_QUERY = new String[] {
			RosterConstants._ID, RosterConstants.JID, RosterConstants.ALIAS,
			RosterConstants.STATUS_MODE, RosterConstants.STATUS_MESSAGE, };
	private Context mContext;
	private ContentResolver mContentResolver;
	private List<Group> mGroupList;
	private List<Roster> mRosterList;
	private boolean mIsShowOffline;// 是否显示离线联系人

	public RosterAdapter(Context context,RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
		mContext = context;
		mContentResolver = context.getContentResolver();
		mGroupList = new ArrayList<Group>();
		mRosterList = new ArrayList<>();
		mIsShowOffline = PreferenceUtils.getPrefBoolean(mContext,
				PreferenceConstants.SHOW_OFFLINE, true);

		mExpandInterpolator = new DecelerateInterpolator(DURATION);
		mCollapseInterpolator = new DecelerateInterpolator(DURATION);
	}

	public void requery() {
		if (mGroupList != null && mGroupList.size() > 0)
			mGroupList.clear();
		if(mRosterList != null && mRosterList.size() > 0)
			mRosterList.clear();

		// 是否显示在线人数
		mIsShowOffline = PreferenceUtils.getPrefBoolean(mContext,
				PreferenceConstants.SHOW_OFFLINE, true);
		String selectWhere = null;
		if (!mIsShowOffline)
			selectWhere = OFFLINE_EXCLUSION;
		Cursor groupCursor = mContentResolver.query(RosterProvider.GROUPS_URI,
				GROUPS_QUERY_COUNTED, selectWhere, null, RosterConstants.GROUP);
		groupCursor.moveToFirst();
		while (!groupCursor.isAfterLast()) {
			Group group = new Group();
			group.setGroupName(groupCursor.getString(groupCursor
					.getColumnIndex(RosterConstants.GROUP)));
			group.setMembers(groupCursor.getString(groupCursor
					.getColumnIndex("members")));
			mGroupList.add(group);
			groupCursor.moveToNext();
		}
		groupCursor.close();
		for(Group group : mGroupList){
			mRosterList.addAll(getChildrenRosters(group.getGroupName()));
		}
		LogUtils.i("cursor size = " + mGroupList.size());
		notifyDataSetChanged();
	}

	protected List<Roster> getChildrenRosters(String groupname) {
		// Given the group, we return a cursor for all the children within
		// that group
		List<Roster> childList = new ArrayList<Roster>();

		String selectWhere = RosterConstants.GROUP + " = ?";
		if (!mIsShowOffline)
			selectWhere += " AND " + OFFLINE_EXCLUSION;
		Cursor childCursor = mContentResolver.query(RosterProvider.CONTENT_URI,
				ROSTER_QUERY, selectWhere, new String[] { groupname }, null);
		childCursor.moveToFirst();
		while (!childCursor.isAfterLast()) {
			Roster roster = new Roster();
			roster.setJid(childCursor.getString(childCursor
					.getColumnIndexOrThrow(RosterConstants.JID)));
			roster.setAlias(childCursor.getString(childCursor
					.getColumnIndexOrThrow(RosterConstants.ALIAS)));
			roster.setStatus_message(childCursor.getString(childCursor
					.getColumnIndexOrThrow(RosterConstants.STATUS_MESSAGE)));
			roster.setStatusMode(childCursor.getString(childCursor
					.getColumnIndexOrThrow(RosterConstants.STATUS_MODE)));
			childList.add(roster);
			childCursor.moveToNext();
		}

		childCursor.close();
		return childList;
	}


	public Group getGroup(int groupPosition) {
		return mGroupList.get(groupPosition);
	}

	public Roster getChild(int groupPosition, int childPosition) {
		return getChildrenRosters(mGroupList.get(groupPosition).getGroupName())
				.get(childPosition);
	}


	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(mContext, R.layout.list_item_friend,null);
		return new MyViewHolder(view);
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		holder.bindView(position);
	}

	@Override
	public int getItemCount() {
		return mRosterList.size();
	}


	private void expandAlarm(final MyViewHolder itemHolder, boolean animate) {

		if (mExpandedItemHolder != null) {
			// Only allow one alarm to expand at a time.
			collapseAlarm(mExpandedItemHolder, animate);
			return;
		}

		//设置监听
		bindListener(itemHolder);

		mExpandedItemHolder = itemHolder;

		// Save the starting height so we can animate from this value.
		final int startingHeight = itemHolder.friendItem.getHeight();


		itemHolder.expandArea.setVisibility(View.VISIBLE);


		if (!animate) {
			// Set the "end" layout and don't do the animation.
			itemHolder.arrow.setRotation(ROTATE_90_DEGREE);
			return;
		}


		final ViewTreeObserver observer = recyclerView.getViewTreeObserver();
		observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				// We don't want to continue getting called for every listview drawing.
				if (observer.isAlive()) {
					observer.removeOnPreDrawListener(this);
				}
				// Calculate some values to help with the animation.
				final int endingHeight = itemHolder.friendItem.getHeight();
				final int distance = endingHeight - startingHeight;

				// Set the height back to the start state of the animation.
				itemHolder.friendItem.getLayoutParams().height = startingHeight;
				LinearLayout.LayoutParams expandParams = (LinearLayout.LayoutParams)
						itemHolder.expandArea.getLayoutParams();
				expandParams.setMargins(0, -distance, 0, 0);
				itemHolder.expandArea.setAlpha(0.0f);
				itemHolder.friendItem.requestLayout();

				// Set up the animator to animate the expansion.
				ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f)
						.setDuration(DURATION);
				animator.setInterpolator(mExpandInterpolator);
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animator) {
						Float value = (Float) animator.getAnimatedValue();

						// For each value from 0 to 1, animate the various parts of the layout.
						itemHolder.friendItem.getLayoutParams().height =
								(int) (value * distance + startingHeight);
						LinearLayout.LayoutParams expandParams = (LinearLayout.LayoutParams)
								itemHolder.expandArea.getLayoutParams();
						expandParams.setMargins(
								0, (int) -((1 - value) * distance), 0, 0);

						itemHolder.expandArea.setAlpha(value);
						itemHolder.arrow.setRotation(ROTATE_90_DEGREE * value);

						itemHolder.friendItem.requestLayout();
					}
				});
				// Set everything to their final values when the animation's done.
				animator.addListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationEnd(Animator animation) {
						// Set it back to wrap content since we'd explicitly set the height.
						itemHolder.friendItem.getLayoutParams().height =
								ViewGroup.LayoutParams.WRAP_CONTENT;
						itemHolder.expandArea.setAlpha(1.0f);
						itemHolder.arrow.setRotation(ROTATE_90_DEGREE);
					}
					@Override
					public void onAnimationCancel(Animator animation) {}
					@Override
					public void onAnimationRepeat(Animator animation) { }
					@Override
					public void onAnimationStart(Animator animation) { }
				});
				animator.start();
				// Return false so this draw does not occur to prevent the final frame from
				// being drawn for the single frame before the animations start.
				return false;
			}
		});
	}

	private void bindListener(MyViewHolder viewHolder) {
		final Roster roster = mRosterList.get(viewHolder.getAdapterPosition());
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()){
					case R.id.ib_sms:{
						if (onRosterBtClick != null)
							onRosterBtClick.onBtClick(1,roster);
					}
					break;
					case R.id.ib_detail:{
						if (onRosterBtClick != null)
							onRosterBtClick.onBtClick(2,roster);
					}
					break;
					case R.id.ib_delete:{
						if (onRosterBtClick != null)
							onRosterBtClick.onBtClick(3,roster);
					}
					break;
				}
			}
		};
		viewHolder.smsBt.setOnClickListener(onClickListener);
		viewHolder.detailBt.setOnClickListener(onClickListener);
		viewHolder.deleteBt.setOnClickListener(onClickListener);
	}


	private void collapseAlarm(final MyViewHolder itemHolder, boolean animate) {
		mExpandedItemHolder = null;

		// Save the starting height so we can animate from this value.
		final int startingHeight = itemHolder.friendItem.getHeight();

		itemHolder.expandArea.setVisibility(View.GONE);


		if (!animate) {
			// Set the "end" layout and don't do the animation.
			itemHolder.arrow.setRotation(0);
			return;
		}

		final ViewTreeObserver observer = recyclerView.getViewTreeObserver();
		observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				if (observer.isAlive()) {
					observer.removeOnPreDrawListener(this);
				}

				// Calculate some values to help with the animation.
				final int endingHeight = itemHolder.friendItem.getHeight();
				final int distance = endingHeight - startingHeight;

				// Re-set the visibilities for the start state of the animation.
				itemHolder.expandArea.setVisibility(View.VISIBLE);
				itemHolder.expandArea.setAlpha(1.0f);
				// Set up the animator to animate the expansion.
				ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f)
						.setDuration(DURATION);
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animator) {
						Float value = (Float) animator.getAnimatedValue();

						// For each value from 0 to 1, animate the various parts of the layout.
						itemHolder.friendItem.getLayoutParams().height =
								(int) (value * distance + startingHeight);
						LinearLayout.LayoutParams expandParams = (LinearLayout.LayoutParams)
								itemHolder.expandArea.getLayoutParams();
						expandParams.setMargins(
								0, (int) (value * distance), 0, 0);

						itemHolder.expandArea.setAlpha(1 - value);
						itemHolder.arrow.setRotation(ROTATE_90_DEGREE * (1 - value));

						itemHolder.friendItem.requestLayout();
					}
				});
				animator.setInterpolator(mCollapseInterpolator);
				// Set everything to their final values when the animation's done.
				animator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						// Set it back to wrap content since we'd explicitly set the height.
						itemHolder.friendItem.getLayoutParams().height =
								ViewGroup.LayoutParams.WRAP_CONTENT;

						LinearLayout.LayoutParams expandParams = (LinearLayout.LayoutParams)
								itemHolder.expandArea.getLayoutParams();
						expandParams.setMargins(0, 0, 0, 0);

						itemHolder.expandArea.setVisibility(View.GONE);
						itemHolder.arrow.setRotation(0);
					}
				});
				animator.start();

				return false;
			}
		});
	}



	public class Group {
		private String groupName;
		private String members;

		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}

		public String getMembers() {
			return members;
		}

		public void setMembers(String members) {
			this.members = members;
		}

	}

	public class Roster {
		private String jid;
		private String alias;
		private String statusMode;
		private String statusMessage;

		public String getJid() {
			return jid;
		}

		public void setJid(String jid) {
			this.jid = jid;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getStatusMode() {
			return statusMode;
		}

		public void setStatusMode(String statusMode) {
			this.statusMode = statusMode;
		}

		public String getStatusMessage() {
			return statusMessage;
		}

		public void setStatus_message(String statusMessage) {
			this.statusMessage = statusMessage;
		}

	}


	public class MyViewHolder extends RecyclerView.ViewHolder{

		private LinearLayout friendItem;
		private FrameLayout expandArea;
		private ImageView arrow;
		private TextView friendName;
		private ImageButton smsBt;
		private ImageButton detailBt;
		private ImageButton deleteBt;
		private TextView unReadView;

		public MyViewHolder(View itemView) {
			super(itemView);
			friendItem= (LinearLayout) itemView;
			expandArea = (FrameLayout) itemView.findViewById(R.id.expand_area);
			friendName = (TextView) itemView.findViewById(R.id.tv_friend_name);
			arrow = (ImageView) itemView.findViewById(R.id.iv_arrow);
			unReadView = (TextView) itemView.findViewById(R.id.unreadmsg);

			smsBt = (ImageButton) itemView.findViewById(R.id.ib_sms);
			detailBt = (ImageButton) itemView.findViewById(R.id.ib_detail);
			deleteBt = (ImageButton) itemView.findViewById(R.id.ib_delete);
		}

		public void bindView(int position){
			Roster roster = mRosterList.get(position);
			friendItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					expandAlarm(MyViewHolder.this,false);
				}
			});
			friendName.setText(roster.getAlias());

			String selection = ChatProvider.ChatConstants.JID + " = '" + roster.getJid()+ "' AND "
					+ ChatProvider.ChatConstants.DIRECTION + " = " + ChatProvider.ChatConstants.INCOMING
					+ " AND " +ChatProvider.ChatConstants.DELIVERY_STATUS + " = "
					+ ChatProvider.ChatConstants.DS_NEW;// 新消息数量字段
			Cursor msgcursor = mContentResolver.query(ChatProvider.CONTENT_URI,
					new String[] { "count(" + ChatProvider.ChatConstants.PACKET_ID + ")",
							ChatProvider.ChatConstants.DATE, ChatProvider.ChatConstants.MESSAGE }, selection,
					null, SORT_ORDER);
			msgcursor.moveToFirst();
			int count = msgcursor.getInt(0);
			unReadView.setText(msgcursor.getString(0));
			unReadView.setVisibility(count > 0 ? View.VISIBLE
					: View.GONE);
			unReadView.bringToFront();
			msgcursor.close();
		}
	}
}
