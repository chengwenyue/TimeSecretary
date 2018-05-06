package justita.top.timesecretary.biz;

import justita.top.timesecretary.entity.User;

public interface OnRegisterListener {
    void registerCallback(int connectedState, String reason);
    void registerSuccess(String message, User user);
    void registerFailed(String message);
    void registernNow();
    void cancelRegister();
}
