package com.example.getrunspak.setup;

import com.example.getrunspak.AboutTaskKiller;
import com.example.getrunspak.ActivityMain;
import com.example.getrunspak.R;
import com.example.getrunspak.SystemInfo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class SetUpAll extends Activity implements OnClickListener {

	private Button btn_push_notice;
	private Button btn_shortcut_show;
	private Button btn_system_info;
	private Button btn_about_tk;
	private Button btn_share_tk;
	private Button btn_exit_tk;
	private Drawable mSwitchOn;
	private Drawable mSwitchOff;
	private Drawable mShakeLeft;
	private ImageButton mFinishBtn;
	/*
	 * 获取pishum.ini里面的数据
	 */
	private SharedPreferences mSetting;// 获取pishum.ini里面的数据
	private boolean isNoticeOn = true;
	private boolean isShortCutOn = true;
	private static int THIS_PID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		Intent setup_intent = getIntent();
		THIS_PID = setup_intent.getIntExtra("THIS_PID",
				android.os.Process.myPid());
		setContentView(R.layout.setupall_layout);
		init_setup_view();
	}

	public void onResume() {
		super.onResume();
	}

	public void onStop() {
		super.onStop();
	}

	public void onDestroy() {
		super.onDestroy();
	}

	// 点击手机物理按键
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

		}
		return super.onKeyDown(keyCode, event);
	}

	public void init_setup_view() {

		btn_push_notice = (Button) findViewById(R.id.btn_push_notice);
		btn_shortcut_show = (Button) findViewById(R.id.btn_shortcut_show);
		btn_system_info = (Button) findViewById(R.id.btn_system_info);
		btn_about_tk = (Button) findViewById(R.id.btn_about_tk);
		btn_share_tk = (Button) findViewById(R.id.btn_share_tk);
		btn_exit_tk = (Button) findViewById(R.id.btn_exit_tk);
		mFinishBtn = (ImageButton) findViewById(R.id.setup_title_back);
		btn_push_notice.setOnClickListener(this);
		btn_shortcut_show.setOnClickListener(this);
		btn_system_info.setOnClickListener(this);
		btn_about_tk.setOnClickListener(this);
		btn_share_tk.setOnClickListener(this);
		btn_exit_tk.setOnClickListener(this);
		mFinishBtn.setOnClickListener(this);

		mSwitchOn = getResources().getDrawable(R.drawable.switch_on);
		mSwitchOn.setBounds(0, 0, mSwitchOn.getMinimumWidth(),
				mSwitchOn.getMinimumHeight());
		mSwitchOff = getResources().getDrawable(R.drawable.switch_off);
		mSwitchOff.setBounds(0, 0, mSwitchOff.getMinimumWidth(),
				mSwitchOff.getMinimumHeight());
		mShakeLeft = getResources().getDrawable(R.drawable.shock);
		mShakeLeft.setBounds(0, 0, mShakeLeft.getMinimumWidth(),
				mShakeLeft.getMinimumHeight());

		mSetting = getSharedPreferences("pishum", 0);
		isNoticeOn = mSetting.getBoolean("isNoticeOn", true);
		isShortCutOn = mSetting.getBoolean("isShortCutOn", true);
		if (isNoticeOn) {
			btn_push_notice.setCompoundDrawables(mShakeLeft, null, mSwitchOn,
					null);
		} else {
			btn_push_notice.setCompoundDrawables(mShakeLeft, null, mSwitchOff,
					null);
		}
		if (isShortCutOn) {
			btn_shortcut_show.setCompoundDrawables(mShakeLeft, null, mSwitchOn,
					null);
		} else {
			btn_shortcut_show.setCompoundDrawables(mShakeLeft, null,
					mSwitchOff, null);
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {

		case R.id.btn_push_notice:
			isNoticeOn = isLightOn(isNoticeOn, btn_push_notice);
			mSetting.edit().putBoolean("isNoticeOn", isNoticeOn).commit();
			break;
		case R.id.btn_shortcut_show:
			isShortCutOn = isLightOn(isShortCutOn, btn_shortcut_show);
			mSetting.edit().putBoolean("isShortCutOn", isShortCutOn).commit();
			break;
		case R.id.btn_system_info:
			system_info();
			break;
		case R.id.btn_about_tk:
			about_info();
			break;
		case R.id.btn_share_tk:
			share();
			break;
		case R.id.btn_exit_tk:
			// android.os.Process.killProcess(THIS_PID);
			ActivityMain.ActivityMain.finish();
			finish();
			break;
		case R.id.setup_title_back:
			finish();
			break;
		}
	}

	private boolean isLightOn(boolean isOn, Button btn) {
		if (!isOn) {
			btn.setCompoundDrawables(mShakeLeft, null, mSwitchOn, null);
			return isOn = true;
		} else {
			btn.setCompoundDrawables(mShakeLeft, null, mSwitchOff, null);
			return isOn = false;
		}
	}

	public void about_info() {
		Intent intent = new Intent(this, AboutTaskKiller.class);
		startActivity(intent);
	}

	public void system_info() {
		Intent intent = new Intent(this, SystemInfo.class);
		startActivity(intent);
	}

	public void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain"); // 纯文本
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
		intent.putExtra(Intent.EXTRA_TEXT, "我发现了一个好用的TaskKiller");
		startActivity(intent);
	}
}
