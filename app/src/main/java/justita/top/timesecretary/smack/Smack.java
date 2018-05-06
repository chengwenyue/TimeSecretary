package justita.top.timesecretary.smack;

import justita.top.timesecretary.exception.XXException;

public interface Smack {
	public boolean login(String account, String password) throws XXException, XXException, XXException;

	public boolean logout();

	public int register(String account, String password, String mail) throws XXException;
	public boolean isAuthenticated();

	public void addRosterItem(String user, String alias, String group)
			throws XXException;

	public void removeRosterItem(String user) throws XXException;

	public void renameRosterItem(String user, String newName)
			throws XXException;

	public void moveRosterItemToGroup(String user, String group)
			throws XXException;

	public void renameRosterGroup(String group, String newGroup);

	public void requestAuthorizationForRosterItem(String user);

	public void addRosterGroup(String group);

	public void setStatusFromConfig();

	public void sendMessage(String user, String message);

	public void sendServerPing();

	public String getNameForJID(String jid);
}
