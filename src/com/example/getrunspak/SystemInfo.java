package com.example.getrunspak;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SystemInfo extends Activity {

	private ListView mLisview;
	private Button btn_back;
	private int screenWidthDip, screenHeightDip, densityDPI;
	private float density, xdpi, ydpi;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.systeminfo);
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		screenWidthDip = dm.widthPixels; // 屏幕宽（dip，如：320dip）
		screenHeightDip = dm.heightPixels; // 屏幕宽（dip，如：533dip）
		density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
		densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
		xdpi = dm.xdpi;
		ydpi = dm.ydpi;
		String[][] systemInfo = getSystemInfo();
		MyAdapter myAdapter = new MyAdapter(getApplicationContext(), systemInfo);
		mLisview = (ListView) findViewById(R.id.sysinfo_List);
		mLisview.setAdapter(myAdapter);

		btn_back = (Button) findViewById(R.id.system_title_back);
		btn_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	private class MyAdapter extends BaseAdapter {
		private String[][] systemInfo;

		MyAdapter(Context context, String[][] systemInfo) {
			this.systemInfo = systemInfo;
		}

		@Override
		public int getCount() {
			return systemInfo.length;
		}

		@Override
		public Object getItem(int position) {
			return systemInfo[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getLayoutInflater().inflate(R.layout.system_item,
					null);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView value = (TextView) convertView.findViewById(R.id.value);
			name.setText(systemInfo[position][1]);
			value.setText(systemInfo[position][0]);
			return convertView;
		}
	}

	private String[][] getSystemInfo() {
		Build mBuild = new Build();
		String[][] systemInfo = { { mBuild.BOARD, " 主板" },
				{ mBuild.BRAND, " 系统定制商   " }, { mBuild.CPU_ABI, " cpu指令集 " },
				{ mBuild.DEVICE, " 设备参数    " }, { mBuild.DISPLAY, " 显示屏参数  " },
				{ mBuild.FINGERPRINT, " 硬件名称" }, { mBuild.HOST, " 主机   " },
				{ mBuild.MANUFACTURER, " 硬件制造商    " },
				{ mBuild.MODEL, " 版本   " }, { mBuild.PRODUCT, " 手机制造商  " },
				{ Build.VERSION.RELEASE, " Android版本  " },
				{ String.valueOf(screenWidthDip), " 屏幕宽度" },
				{ String.valueOf(screenHeightDip), " 屏幕高度" },
				{ String.valueOf(densityDPI), " 所属DPI" }

		};
		return systemInfo;
	}

}
