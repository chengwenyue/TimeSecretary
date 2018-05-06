package justita.top.timesecretary;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.test.InstrumentationTestCase;

import java.util.List;

import justita.top.timesecretary.adapter.RosterAdapter;
import justita.top.timesecretary.provider.ChatProvider;
import justita.top.timesecretary.provider.Operation;
import justita.top.timesecretary.provider.RosterProvider;
import justita.top.timesecretary.service.DataSyncService;
import justita.top.timesecretary.uitl.LogUtils;

public class Test extends InstrumentationTestCase {

    public void test(){
    }

    public void testaddOpreatiorn(){
        Operation operation = new Operation();
        operation.mTableName ="affairs";
        operation.mDataId = 1;
        operation.mUser_Id = 2;
        operation.mOperation = Operation.INSERT;
        operation.mState = Operation.NOT_SYNC;
        operation.mTime = System.currentTimeMillis();

        Operation.addOperation(getInstrumentation().getContext().getContentResolver(),operation);
    }

    public void testDeleteOperation(){
        getInstrumentation().getContext().getContentResolver().delete(Operation.CONTENT_URI,Operation.USER_ID+" = ?"
        ,new String[]{"2"});
//        Operation.deleteOperation(getInstrumentation().getContext().getContentResolver(),"affairs",1);
        //testgetOperation();
    }

    public void testgetOperation(){

        List<Operation> operationList = Operation.getOperations(getInstrumentation().getContext().getContentResolver(),null,new String[0]);
        for(Operation operation :operationList) {
            LogUtils.e(operation.getData(getInstrumentation().getContext().getContentResolver()).getAddData());
        }
    }

    public void testSyncAdd(){
        Context context = getInstrumentation().getContext();
        Intent intent = new Intent(context,DataSyncService.class);
        context.startService(intent);

    }

    public void testRoster(){
        Cursor childCursor = getInstrumentation().getContext().getContentResolver().
                query(RosterProvider.CONTENT_URI, RosterAdapter.ROSTER_QUERY,null,new String[0],null);

        childCursor.moveToFirst();
        while (!childCursor.isAfterLast()) {
            String jid  = childCursor.getString(childCursor
                    .getColumnIndexOrThrow(RosterProvider.RosterConstants.JID));
            String alits = childCursor.getString(childCursor
                    .getColumnIndexOrThrow(RosterProvider.RosterConstants.ALIAS));
            String status_message = childCursor.getString(childCursor
                    .getColumnIndexOrThrow(RosterProvider.RosterConstants.STATUS_MESSAGE));
            String status_mode = childCursor.getString(childCursor
                    .getColumnIndexOrThrow(RosterProvider.RosterConstants.STATUS_MODE));
            childCursor.moveToNext();
        }
        childCursor.close();
    }

    public void testDel(){
        getInstrumentation().getContext().getContentResolver().delete(ChatProvider.CONTENT_URI,null,new String[0]);
        getInstrumentation().getContext().getContentResolver().delete(RosterProvider.CONTENT_URI,null,new String[0]);
    }
}
