package justita.top.timesecretary.service;

public interface IConnectionStatusCallback {
	public void connectionStatusChanged(int connectedState, String reason);
}
