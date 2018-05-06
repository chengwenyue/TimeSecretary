package justita.top.timesecretary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.provider.Category;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.widget.CategoryItemColorView;
import justita.top.timesecretary.widget.SwipeLayout;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{


    public interface OnItemClickListener {
        public void onItemClick(View view,int position);
    }

    public OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private Context mContext;
    public List<Category> mCategoryList;
    public int mCategorySelect = 0;

    public CategoryAdapter(Context context,List<Category> categoryList){
        mContext = context;
        mCategoryList = categoryList;
        mCategorySelect = (int) PreferenceUtils.getPrefLong(mContext, PreferenceConstants.MAIN_CATEGORY,-1);
    }

    public void setCategoryList(List<Category> categoryList) {
        this.mCategoryList = categoryList;
    }

    private SwipeLayout.SwipeViewListener swipeViewListener;

    public void setSwipeViewListener(SwipeLayout.SwipeViewListener swipeViewListener) {
        this.swipeViewListener = swipeViewListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == -1){
            view = View.inflate(mContext, R.layout.list_item_category_all,null);
        }else {
            view = View.inflate(mContext, R.layout.list_item_category,null);
        }
        return new ViewHolder(view,viewType);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.categorySelect.setVisibility(View.GONE);

        if(getItemViewType(position) == 0){
            holder.categoryItem.setSwipeViewListener(swipeViewListener);
            holder.categoryItem.setDateId(mCategoryList.get(position -1).mId);

            holder.categoryName.setText(mCategoryList.get(position -1).mName);
            holder.itemColorView.setCategoryItemColor(mCategoryList.get(position - 1).mColor);

        }else if(position == 0){
            holder.categoryName.setText("全部");
            holder.itemColorView.setVisibility(View.GONE);
        }else {
            holder.categoryName.setText(mCategoryList.get(position -1).mName);
            holder.itemColorView.setCategoryItemColor(mCategoryList.get(position - 1).mColor);
        }

        if(position == 0 && mCategorySelect != -1)
            return;
        if(position != 0 && mCategorySelect != mCategoryList.get(position - 1).mId)
            return;

        holder.categorySelect.setVisibility(View.VISIBLE);

    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0
                || mCategoryList.get(position -1).mId == 1
                || mCategoryList.get(position -1).mId == 2){
            return -1;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size() + 1;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public SwipeLayout categoryItem;
        public TextView categoryName;
        public ImageView categorySelect;
        public ImageView imgLeft;
        public CategoryItemColorView itemColorView;
        public RelativeLayout listItem;
        public ViewHolder(View itemView ,int viewType) {
            super(itemView);
            if(viewType == 0){
                categoryItem = (SwipeLayout) itemView;
                imgLeft = (ImageView) itemView.findViewById(R.id.img_left);

            }
            listItem = (RelativeLayout) itemView.findViewById(R.id.list_item);
            listItem.setOnClickListener(this);
            itemColorView = (CategoryItemColorView) itemView.findViewById(R.id.category_item_color);
            categoryName = (TextView) itemView.findViewById(R.id.tv_category_name);
            categorySelect = (ImageView) itemView.findViewById(R.id.iv_category_select);
            categorySelect.getDrawable().setTint(mContext.getResources().getColor(R.color.gray));
            categorySelect.setVisibility(View.GONE);

        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClick(v,getAdapterPosition());
        }
    }
}
