// IAffairServiceAIDL.aidl
package justita.top.timesecretary.service;

// Declare any non-default types here with import statements

interface IAffairServiceAIDL {
    void registerNextAffairAlarm();
    void changeAffairState(long affairId);
    void setAffairCompleteState(long affairId);
    void setAffairDelState(long affairId);
    void setAffairSilentState(long affairId);

    void removeAlarmByAffair(long affairId);

    void addAlarmByAffair(long affairId);
    void updateAlarmByAffair(long affairId);
}
