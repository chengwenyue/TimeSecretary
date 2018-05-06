package justita.top.timesecretary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import justita.top.timesecretary.R;
import justita.top.timesecretary.app.BaseActivity;

public class SearchActivity extends BaseActivity implements TextWatcher, Inputtips.InputtipsListener {

    private ListView mInputList;
    private EditText mLocationSerEt;
    private TextView mCancelTextView;

    private String mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search);
        initView();
    }

    private void initView() {
        mCancelTextView = (TextView) findViewById(R.id.cancel_tv);
        mCancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.finish();
            }
        });
        mInputList = (ListView) findViewById(R.id.input_list);
        mLocationSerEt = (EditText) findViewById(R.id.input_editText_position);
        Intent intent = getIntent();
        mCurrentLocation = intent.getStringExtra("CurrentLocation");
        mLocationSerEt.setHint(mCurrentLocation);
        mLocationSerEt.addTextChangedListener(SearchActivity.this);
    }

    final private List<HashMap<String, String>> listString = new ArrayList<HashMap<String, String>>();
    private PoiSearchAdapter mPoiSearchAdapter;
    @Override
    public void onGetInputtips(final List<Tip> tipList, int rCode) {

        if (rCode == AMapException.CODE_AMAP_SUCCESS) {

            for (int i = 0; i < tipList.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                String name;
                if (tipList.get(i).getDistrict().indexOf(tipList.get(i).getName()) == -1) {
                    name = tipList.get(i).getDistrict() + tipList.get(i).getName();
                }else{
                    name = tipList.get(i).getDistrict();
                }
                map.put("name", name);
                listString.add(map);
            }


            mPoiSearchAdapter = new PoiSearchAdapter();
            mInputList.setAdapter(mPoiSearchAdapter);
            mPoiSearchAdapter.notifyDataSetChanged();
            mInputList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView nameTv = (TextView) view.findViewById(R.id.poi_field_id);
                    final String name = nameTv.getText().toString().trim();
                    Intent intent = new Intent();
                    intent.putExtra("name", name);
                    SearchActivity.this.setResult(1, intent);
                    SearchActivity.this.finish();
                }
            });
        }
    }

    private class PoiSearchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listString.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(SearchActivity.this, R.layout.position_item, null);
                holder.fieldName = (TextView) convertView.findViewById(R.id.poi_field_id);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.fieldName.setText(listString.get(position).get("name"));
            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return listString.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            public TextView fieldName;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        listString.clear();
        InputtipsQuery inputQuery = new InputtipsQuery(newText, "");
        inputQuery.setCityLimit(true);
        Inputtips inputTips = new Inputtips(SearchActivity.this, inputQuery);
        inputTips.setInputtipsListener(this);
        inputTips.requestInputtipsAsyn();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
