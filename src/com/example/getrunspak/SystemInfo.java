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
		screenWidthDip = dm.widthPixels; // ��Ļ��dip���磺320dip��
		screenHeightDip = dm.heightPixels; // ��Ļ��dip���磺533dip��
		density = dm.density; // ��Ļ�ܶȣ����ر�����0.75/1.0/1.5/2.0��
		densityDPI = dm.densityDpi; // ��Ļ�ܶȣ�ÿ�����أ�120/160/240/320��
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
		String[][] systemInfo = { { mBuild.BOARD, " ����" },
				{ mBuild.BRAND, " ϵͳ������   " }, { mBuild.CPU_ABI, " cpuָ� " },
				{ mBuild.DEVICE, " �豸����    " }, { mBuild.DISPLAY, " ��ʾ������  " },
				{ mBuild.FINGERPRINT, " Ӳ������" }, { mBuild.HOST, " ����   " },
				{ mBuild.MANUFACTURER, " Ӳ��������    " },
				{ mBuild.MODEL, " �汾   " }, { mBuild.PRODUCT, " �ֻ�������  " },
				{ Build.VERSION.RELEASE, " Android�汾  " },
				{ String.valueOf(screenWidthDip), " ��Ļ���" },
				{ String.valueOf(screenHeightDip), " ��Ļ�߶�" },
				{ String.valueOf(densityDPI), " ����DPI" }

		};
		return systemInfo;
	}

}
