package justita.top.timesecretary.adapter;

public interface AffairContract {
    int AFFAIR_TITLE = 0x10;
    int INVALID_ITEM = -1;
    int INVALID_POSITION = -1;
    int NOT_FOUND = -1;
    int DEL_WITH_TITLE = 0x11;


    int DEFAULT_CATEGORY_STRATEGY = 0x12;
    int DEFAULT_TIME_CATEGORY_STRATEGY = 0x13;
    int HIDE_ACHIEVE_AFFAIR = 0x14;
    int HIDE_TIME_CATEGORY_STRATEGY = 0x15;


    int AFFAIR_TODO = 0x55;
    int AFFAIR_ACHIEVE = 0x56;

    int AFFAIR_LIST = 0x57;
    int AFFAIR_TIME = 0x58;


    int TIME_TODAY = 0x31;
    int TIME_TOMORROW = 0x32;
    int TIME_IMMEDIATE = 0x33;
    int TIME_OUTDATED = 0x34;

    int IMMEDIATE_TIME_BUFFER = -15;
}
