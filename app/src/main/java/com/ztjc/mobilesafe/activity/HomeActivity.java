package com.ztjc.mobilesafe.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ztjc.mobilesafe.R;

/**
 * Android手机卫士，主页面
 */
public class HomeActivity extends Activity {

    private GridView gridView;

    private String[] mImtes = {"手机防盗", "通讯卫士", "软件管理", "进程管理", "流量统计", "手机杀毒", "缓存清理", "高级工具", "设置中心"};

    private int[] mPic = {R.mipmap.home_safe, R.mipmap.home_callmsgsafe, R.mipmap.home_apps,
            R.mipmap.home_taskmanager, R.mipmap.home_netmanager, R.mipmap.home_trojan,
            R.mipmap.home_sysoptimize, R.mipmap.home_tools, R.mipmap.home_settings};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        gridView = (GridView) findViewById(R.id.gv_home);
        gridView.setAdapter(new HomeAdapter());
    }

    /**
     * 主功能菜单适配器
     */
    class HomeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mImtes.length;
        }

        @Override
        public Object getItem(int position) {
            return mImtes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(HomeActivity.this, R.layout.home_list_item, null);
                viewHolder.ivItem = (ImageView) convertView.findViewById(R.id.iv_item);
                viewHolder.tvItem = (TextView) convertView.findViewById(R.id.tv_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.ivItem.setImageResource(mPic[position]);
            viewHolder.tvItem.setText(mImtes[position]);
            return convertView;
        }

        class ViewHolder {
            ImageView ivItem;
            TextView tvItem;
        }
    }
}
