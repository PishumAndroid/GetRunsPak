package com.example.getrunspak;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutTaskKiller extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		int screenWidthDip = dm.widthPixels; // 屏幕宽（dip，如：320dip）
		int screenHeightDip = dm.heightPixels; // 屏幕宽（dip，如：533dip）
		float density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
		int densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
		float xdpi = dm.xdpi;
		float ydpi = dm.ydpi;
		Log.d("zphlog", "screenWidthDip=" + screenWidthDip
				+ "\tscreenHeightDip=" + screenHeightDip + "\n density="
				+ density + "\t densityDPI=" + densityDPI + "\n" + "xdpi="
				+ xdpi + "\t ydpi=" + ydpi);
		TextView share = (TextView) findViewById(R.id.share);
		share.setOnClickListener(new OnClickListener() {
			public void onClick(View source) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain"); // 纯文本
				intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
				intent.putExtra(Intent.EXTRA_TEXT,
						"我发现了一个好用的杀进程 APP，TaskKiller。");
				startActivity(intent);
			}
		});
		Button btn = (Button) findViewById(R.id.about_title_back);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
}
