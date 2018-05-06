package justita.top.timesecretary.adapter;


import java.util.ArrayList;
import java.util.List;

import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.uitl.LogUtils;


/**
 * AffairCategoryContext类用于维护 RecycleView中显示的数据集List<affair>，
 * 并且提供一些对事件分类的策略，直接与adapter交互。
 *
 */
public class AffairCategoryContext implements AffairContract{
    private List<AffairCategory> mAffairCategoryList = new ArrayList<>();
    private ICategoryStrategy mCategoryStrategy;



    public interface ICategoryStrategy extends AffairContract{
        boolean doCategory(AffairCategory affairCategory,Affair affair);
        int doRankAndAdd(List<Affair> affairList,Affair affair);
        boolean isEmptyDelTitle();
        List<AffairCategory> initCategories();
        AffairCategory initCategory(List<AffairCategory> categoryList);
    }



    /**
     * 选择分类策略
     * @param model
     */
    public void setCategoryStrategyModel(int model){
        switch (model){
            case DEFAULT_CATEGORY_STRATEGY:
                setICategoryStrategy(new AffairAdapter.CategoryDefaultStrategy());
                break;
            case DEFAULT_TIME_CATEGORY_STRATEGY:
                setICategoryStrategy(new AffairTimeAdapter.CategoryStrategyTimeDefault());
                break;
            case HIDE_TIME_CATEGORY_STRATEGY:
                setICategoryStrategy(new AffairTimeAdapter.CategoryStrategyTimeHide());
                break;
            case HIDE_ACHIEVE_AFFAIR:
                setICategoryStrategy(new AffairAdapter.CategoryHideAchieveStrategy());
                break;
        }
    }




    /**
     * 设置分类策略
     * @param categoryStrategy
     */
    public void setICategoryStrategy(ICategoryStrategy categoryStrategy){
        this.mCategoryStrategy = categoryStrategy;
    }


    public void formatData(List<Affair> affairList){
        if(!mAffairCategoryList.isEmpty()){
            mAffairCategoryList.clear();
        }
        mAffairCategoryList = mCategoryStrategy.initCategories();

        for(Affair affair :affairList){
            addAffair(affair);
        }

        refreshAffairStateList();
        logAffairCategoryList();
    }


    /**
     * 根据事件的id来查找事件在List<Affair>中的位置
     * @param affair
     * @return
     */
    public int findAffair(Affair affair){
        for(AffairCategory affairCategory:mAffairCategoryList){
            int position = affairCategory.findAffairPositionById(affair);
            if(position != INVALID_POSITION)
                return  position;
        }
        return INVALID_POSITION;
    }

    /**
     * 根据位置查找affair
     * @param position
     * @return
     */
    public Affair findAffair(int position){
        for(int i = 0 ; i< mAffairCategoryList.size();i++){
            AffairCategory affairCategory = mAffairCategoryList.get(i);
            if(affairCategory.startIndex< position && affairCategory.getEndIndex() >= position){
                Affair affair = mAffairCategoryList.get(i).getAffair(position);
                return  affair;
            }
        }
        return mAffairCategoryList.get(mAffairCategoryList.size()-1).getAffair(position);
    }


    /**
     * 根据affair的mId 删除affair 如果AffairCategory中的affairList
     * @param affair
     * @return
     */
    public int delAffair(Affair affair){
        int delId = NOT_FOUND;
        int i = 0;
        for(;i<mAffairCategoryList.size();i++){
            AffairCategory affairCategory = mAffairCategoryList.get(i);
            delId =  affairCategory.delAffairById(affair);
            refreshAffairStateList();
            if(delId!=NOT_FOUND)
                return delId;
        }
        return delId;
    }


    /**
     * 更新affair
     * @param affair
     * @return affair的oldPosition 和 newPosition
     */
    public int[] updateAffair(Affair affair){
        int oldPosition = delAffair(affair);
        int newPosition = addAffair(affair);
        return new int[]{oldPosition,newPosition};
    }



    /**
     * 添加一个事件
     * @param affair
     * @return 添加的position
     */
    public int addAffair(Affair affair){
        if(mAffairCategoryList.isEmpty())
            return INVALID_POSITION;

        for (int i = 0 ,l = mAffairCategoryList.size();i<l;i++) {
            AffairCategory affairCategory = mAffairCategoryList.get(i);
            if (mCategoryStrategy.doCategory(affairCategory,affair)) {


                int position = mCategoryStrategy.doRankAndAdd(affairCategory.mAffairList ,affair);
                refreshAffairStateList();
                return position + affairCategory.startIndex;
            }
        }
        return INVALID_POSITION;
    }


    public void refreshAffairStateList(){
        AffairCategory lastAffairCategory = null;
        if(mCategoryStrategy.isEmptyDelTitle()) {
            for(int i =0;i<mAffairCategoryList.size();i++){
                if(!mAffairCategoryList.get(i).mAffairList.isEmpty()){
                    if(lastAffairCategory == null) {
                        mAffairCategoryList.get(i).startIndex = 0;
                    }else{
                        mAffairCategoryList.get(i).startIndex = lastAffairCategory.getEndIndex() +1;
                    }
                    lastAffairCategory = mAffairCategoryList.get(i);
                }else{
                    mAffairCategoryList.get(i).startIndex = -1;
                }
            }
        }else{
            for(int i =0;i<mAffairCategoryList.size()-1;i++){
                mAffairCategoryList.get(i+1).startIndex = mAffairCategoryList.get(i).getEndIndex()+1;
            }
        }
    }

    public void logAffairCategoryList(){
        for(AffairCategory affairCategory :mAffairCategoryList){
            String content = "";
            content += affairCategory.startIndex + "  "+affairCategory.name + " " +affairCategory.time+ " " +affairCategory.mIndex;
            for(Affair affair :affairCategory.mAffairList){
                content+=affair.toString();
            }
            LogUtils.e(content);
        }
    }
    /**
     * 根据 position 获取affair
     * @param position
     * @return
     */
    public Affair getAffairItem(int position){
        for(int i =0 ;i<mAffairCategoryList.size();i++){
            AffairCategory affairCategory = mAffairCategoryList.get(i);
            if(position>=affairCategory.startIndex&&affairCategory.getEndIndex()>=position){
                return affairCategory.getAffair(position);
            }
        }
        return null;
    }

    /**
     * 根据 position 获取 affairCategory
     * @param position
     * @return
     */
    public AffairCategory getAffairCategory(int position){
        for(int i =0 ;i<mAffairCategoryList.size();i++){
            AffairCategory affairCategory = mAffairCategoryList.get(i);
            if(affairCategory.startIndex <=position && affairCategory.getEndIndex()>=position ){
                return affairCategory;
            }
        }
        return null;
    }

    /**
     * 获取分类事件的总数
     * @return
     */
    public int getItemCount() {
        if (!mAffairCategoryList.isEmpty()) {
            if(mCategoryStrategy.isEmptyDelTitle()){
                AffairCategory temp = null;
                for(AffairCategory affairCategory : mAffairCategoryList){
                    if(!affairCategory.mAffairList.isEmpty()){
                        temp = affairCategory;
                    }
                }
                if(temp == null)
                    return 0;
                return temp.getEndIndex() +1 ;
            }
            return mAffairCategoryList.get(mAffairCategoryList.size()-1).getEndIndex()+1;
        }
        return 0;
    }
}
