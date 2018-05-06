package justita.top.timesecretary.adapter;

import java.util.ArrayList;
import java.util.List;

import justita.top.timesecretary.provider.Affair;

public class AffairCategory implements AffairContract{

    public int startIndex = 0; //开始索引

    public int mIndex;
    public String name;
    public String time;

    public List<Affair> mAffairList = new ArrayList<Affair>();

    public AffairCategory(int mIndex,String name , String time) {
        this.name = name;
        this.time = time;
        this.mIndex = mIndex;
    }

    public Affair findAffairById(Affair affair){
        for(int i =0;i<mAffairList.size();i++){
            Affair affair1 = mAffairList.get(i);
            if(affair1.mId == affair.mId){
                return affair1;
            }
        }
        return null;
    }

    public int findAffairPositionById(Affair affair){
        for(int i =0;i<mAffairList.size();i++){
            Affair affair1 = mAffairList.get(i);
            if(affair1.mId == affair.mId){
                return i+startIndex+1;
            }
        }
        return INVALID_POSITION;
    }

    public int delAffairById(Affair affair){
        int i =0;
        for(;i<mAffairList.size();i++){
            Affair affair1 = mAffairList.get(i);
            if(affair1.mId == affair.mId){
                mAffairList.remove(i);
                return i+startIndex+1;
            }
        }
        return NOT_FOUND;
    }
    public void addAffair(Affair affair){
        mAffairList.add(affair);
    }
    public Affair getAffair(int position){
        return mAffairList.get(position-startIndex-1);
    }

    public int getEndIndex(){
        return startIndex+mAffairList.size();
    }

    public void delAffairByPosition(int position) {
        mAffairList.remove(position-startIndex-1);
    }
}
