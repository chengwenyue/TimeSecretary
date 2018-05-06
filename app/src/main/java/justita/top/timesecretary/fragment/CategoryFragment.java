package justita.top.timesecretary.fragment;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.activity.AffairAddActivity;
import justita.top.timesecretary.activity.MainActivity;
import justita.top.timesecretary.provider.Category;
import justita.top.timesecretary.service.AffairChangeReceiver;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.widget.CategoryItemColorView;

public class CategoryFragment extends BaseAttrFragment {

    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList = null;
    private int selectPosition = -1;
    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        long userId = PreferenceUtils.getPrefLong(getContext(), PreferenceConstants.USERID,-1);
        categoryList = Category.getCategories(getContext().getContentResolver(),Category.USER_ID + " in(?,?)",new String[]{userId+"","0"});

        for (int i = 0; i < categoryList.size(); i++) {
            if (mBuilder.getAffair().mCategory.equals(categoryList.get(i).mId)) {
                selectPosition = i;
            }
        }
        initView(view);

        return view;
    }

    private void initView(View view) {

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        if(categoryAdapter == null)
            categoryAdapter = new CategoryAdapter();

        recyclerView.setAdapter(categoryAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
    }
    private void notDismissDialog(AlertDialog alertDialog , boolean isDismiss) {
        try {
            Field field = alertDialog.getClass()
                    .getSuperclass().getSuperclass().getDeclaredField(
                            "mShowing");
            field.setAccessible(true);
            // 将mShowing变量设为false，表示对话框已关闭
            field.set(alertDialog, !isDismiss);
            alertDialog.dismiss();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        boolean isOpen = false;
        AlertDialog alertDialog = null;
        @Override
        public void onItemClick(View itemView, final int position) {
            if(position == categoryList.size()){
                isOpen = false;
                AffairAddActivity.isListenerKeyBoard = false;
                AlertDialog.Builder builder =   new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
                final View view = View.inflate(getContext(),R.layout.alert_layout_add_category,null);


                final LinearLayout category_color_list = (LinearLayout) view.findViewById(R.id.category_item_color_list);
                final EditText editText = (EditText) view.findViewById(R.id.category_item_name);
                final CategoryItemColorView category_color  = (CategoryItemColorView) view.findViewById(R.id.category_item_color);


                final ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        category_color_list.setTranslationX( - category_color_list.getWidth());
                        return true;
                    }
                };
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        category_color.setCategoryItemColor (((CategoryItemColorView)v).categoryItemColor);
                        ObjectAnimator oa1 = ObjectAnimator.ofFloat(category_color_list, "translationX", -category_color_list.getWidth());
                        oa1.setDuration(1000);
                        oa1.start();
                    }
                };
                for(int i =0;i<category_color_list.getChildCount();i++){
                    CategoryItemColorView categoryItemColorView = (CategoryItemColorView) category_color_list.getChildAt(i);
                    categoryItemColorView.setOnClickListener(onClickListener);
                }
                category_color_list.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);


                category_color.setOnClickListener(new View.OnClickListener() {
                    ObjectAnimator oa1 = null;
                    @Override
                    public void onClick(View v) {
                        category_color_list.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
                        if(oa1 != null && oa1.isRunning()){
                            return;
                        }
                        if(!isOpen){
                            oa1 = ObjectAnimator.ofFloat(category_color_list, "translationX", (view.getWidth()-category_color_list.getWidth())/2);

                        }else{
                            oa1 = ObjectAnimator.ofFloat(category_color_list, "translationX", -category_color_list.getWidth());
                        }

                        oa1.setDuration(1000);
                        oa1.start();
                        isOpen = !isOpen;
                    }
                });
                builder.setView(view).setTitle("分类添加")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AffairAddActivity.isListenerKeyBoard = true;
                                String categoryName  = editText.getText().toString().trim();
                                if(TextUtils.isEmpty(categoryName)){
                                    Toast.makeText(getContext(),"分类名不能为空！",Toast.LENGTH_SHORT).show();
                                    notDismissDialog(alertDialog,true);
                                    return;
                                }
                                Category category = new Category();
                                category.mName = categoryName;
                                category.mColor = category_color.categoryItemColor;
                                category.mUser_Id =  PreferenceUtils.getPrefLong(getContext(), PreferenceConstants.USERID,-1);
                                Category.addCategory(getContext().getContentResolver(),category);
                                categoryList.add(category);
                                categoryAdapter.notifyDataSetChanged();
                                notDismissDialog(alertDialog,false);
                            }

                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AffairAddActivity.isListenerKeyBoard = true;
                                notDismissDialog(alertDialog,false);
                            }
                        });
                alertDialog = builder.create();
                alertDialog.show();

            }else{
                selectPosition = position;
                mBuilder.setCategory(categoryList.get(position).mId+"");
                categoryAdapter.notifyDataSetChanged();
            }
        }
    };
    private OnItemLongClickListener mItemLongClickListener =new OnItemLongClickListener() {
        @Override
        public void onItemLongClick(View view, final int position) {
            if(position == categoryList.size())
                return;
            if(categoryList.get(position).mId == 1
                    || categoryList.get(position).mId == 2)
                return;

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
            builder.setMessage("确定删除？").
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Category category = categoryList.get(position);
                            Category.deleteCategory(getContext().getContentResolver(),category.mId);


                            Intent intent = new Intent(getContext(),AffairChangeReceiver.class);
                            intent.setData(Category.getUri(category.mId));
                            intent.setAction(MainActivity.AFFAIR_CATEGORY_CHANGE);
                            getContext().sendBroadcast(intent);


                            categoryList.remove(position);
                            categoryAdapter.notifyItemRemoved(position);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();
        }
    };
    public interface OnItemClickListener {
        public void onItemClick(View view,int position);
    }
    public interface OnItemLongClickListener {
        public void onItemLongClick(View view,int position);
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;

            switch (viewType) {
                case 0: {
                    view = View.inflate(getContext(), R.layout.list_item_affair_categary, null);
                }
                break;
                case -1: {
                    view = View.inflate(getContext(), R.layout.list_item_affair_categary, null);
                }
                break;
            }

            return new ViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if(position == getItemCount()-1){
                return -1;
            }
            return 0;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if(getItemViewType(position) == -1){

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.view.getLayoutParams();
                layoutParams.height=24;
                holder.view.requestLayout();
                Drawable drawable = getContext().getDrawable(R.drawable.add);
                drawable.setTint(getResources().getColor(R.color.fragment5));
                holder.view.setBackground(drawable);
                holder.textView.setText("添加");
            }else{
                final int color = categoryList.get(position).mColor;
                String categoryName = categoryList.get(position).mName;

                Drawable drawable = holder.view.getBackground();
                drawable.setTint(color);
                holder.view.setBackground(drawable);

                holder.textView.setTextColor(color);
                holder.textView.setText(categoryList.get(position).mName);


                if( (selectPosition == -1 &&  categoryName.equals("默认"))||selectPosition == position){

                    holder.imageView.getDrawable().setTint(color);
                    holder.imageView.setVisibility(View.VISIBLE);
                }else{
                    holder.imageView.setVisibility(View.INVISIBLE);
                }

            }
        }

        @Override
        public int getItemCount() {
            return categoryList.size()+1;
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
            public View view;
            public TextView textView;
            public ImageView imageView;
            public ViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);

                view = itemView.findViewById(R.id.category_item_color);
                textView = (TextView) itemView.findViewById(R.id.category_item_text);
                textView.setMaxLines(2);
                imageView = (ImageView) itemView.findViewById(R.id.category_item_select);
                imageView.setVisibility(View.INVISIBLE);
            }


            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(v,getAdapterPosition());
            }

            @Override
            public boolean onLongClick(View v) {
                mItemLongClickListener.onItemLongClick(v,getAdapterPosition());
                return true;
            }
        }
    }
}
