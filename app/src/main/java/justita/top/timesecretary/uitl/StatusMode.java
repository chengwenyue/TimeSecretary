package justita.top.timesecretary.uitl;


import justita.top.timesecretary.R;

public enum StatusMode {
	offline(R.string.status_offline, -1), // 离线状态，没有图标
	dnd(R.string.status_dnd, R.mipmap.ic_launcher), // 请勿打扰
	xa(R.string.status_xa, R.mipmap.ic_launcher), // 隐身
	away(R.string.status_away, R.mipmap.ic_launcher), // 离开
	available(R.string.status_online, R.mipmap.ic_launcher), // 在线
	chat(R.string.status_chat, R.mipmap.ic_launcher);// Q我吧

	private final int textId;
	private final int drawableId;

	StatusMode(int textId, int drawableId) {
		this.textId = textId;
		this.drawableId = drawableId;
	}

	public int getTextId() {
		return textId;
	}

	public int getDrawableId() {
		return drawableId;
	}

	public String toString() {
		return name();
	}

	public static StatusMode fromString(String status) {
		return StatusMode.valueOf(status);
	}

}
