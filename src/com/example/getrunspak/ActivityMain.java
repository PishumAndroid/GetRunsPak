package com.example.getrunspak;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.getrunspak.menu.ShortCutEditMenu;
import com.example.getrunspak.setup.SetUpAll;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMain extends Activity implements OnClickListener,
		OnTouchListener, OnItemLongClickListener {
	public static Activity ActivityMain;
	private static final int MSG_REFRESH = 555;
	private static final int MSG_KILL = 444;
	private ListView mListView;
	private List<Programe> mListData;
	private Button mKillBtn;
	private TextView mPakcgeNums;
	private TextView mRestMemory;
	private PackagesInfo pi;
	private ActivityManager am;
	private List<RunningAppProcessInfo> run;
	private PackageManager pm;
	private long mExitTime;
	private static final int EXIT = 0x113;
	private static final int ABOUT = 0x114;
	private static final int INFO = 0x115;
	private static final int SHARE = 0x116;

	private Toast mToast;
	private RelativeLayout layout_main;
	public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

	// if (!ra.processName.equals("com.example.getrunspak")) {
	// android.os.Process.killProcess(ra.pid);
	// }

	private PopupWindow popmenu;
	private LayoutInflater inflater;
	private View popmenu_layout;
	private TextView main_title_refresh;
	private TextView main_title_addshortcut;
	private TextView main_title_show_service;
	private TextView main_title_setup;
	private boolean isPopMenuShow = false;
	private SharedPreferences mSetting;

	// 声明通知管理器
	private static NotificationManager notificationManager = null;
	// 声明Notification对象
	private static Notification notification = null;
	private static PendingIntent pendingIntent = null;
	private static Intent noticeIntent = null;
	private boolean isNoticeOn = true;// 在prefence里面获取是否需要显示
	private boolean showNoticeNow = true;// 判断是否是进入其他界面调用onStop
	// 快捷加速球
	private boolean isShortCutOn = true;

	private static int THIS_PID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// addShortCut();
		ActivityMain = this;
		clearNotification(this);
		mSetting = getSharedPreferences("pishum", 0);
		isShortCutOn = mSetting.getBoolean("isShortCutOn", true);
		isNoticeOn = mSetting.getBoolean("isNoticeOn", true);
		boolean isFirstTime = mSetting.getBoolean("isFirstTime", true);
		if (isFirstTime) {
			addShortCut2();
			mSetting.edit().putBoolean("isFirstTime", false).commit();
		}
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getWindow()
				.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.list_task);
		mKillBtn = (Button) findViewById(R.id.kill_all);
		mPakcgeNums = (TextView) findViewById(R.id.packagenums);
		mRestMemory = (TextView) findViewById(R.id.restmemory);
		layout_main = (RelativeLayout) findViewById(R.id.layout_main);
		layout_main.setOnTouchListener(this);
		mListView.setOnTouchListener(this);
		mKillBtn.setOnClickListener(this);
		mListData = getRunningProcess();
		ListAdapter adapter = new ListAdapter(mListData, this);
		mListView.setAdapter(adapter);
		mListView.setOnItemLongClickListener(this);
		upDateMemAndPakInfo();// 显示Memory信息
		// processOnItemClick(savedInstanceState);
		initPopMenu();
	}

	protected void onResume() {
		super.onResume();
		clearNotification(this);
		mSetting = getSharedPreferences("pishum", 0);
		isNoticeOn = mSetting.getBoolean("isNoticeOn", true);
		isShortCutOn = mSetting.getBoolean("isShortCutOn", true);
		boolean needRefresh = mSetting.getBoolean("needRefresh", false);
		if (needRefresh) {
			mListData = getRunningProcess();
			ListAdapter adapter = new ListAdapter(mListData, ActivityMain.this);
			mListView.setAdapter(adapter);
			upDateMemAndPakInfo();
		}
		mSetting.edit().putBoolean("needRefresh", false).commit();
		Log.d("zphlogkill", "onResume");
	}

	public void onStop() {
		super.onStop();
		if (showNoticeNow) {
			if (isNoticeOn) {
				showNotification(this);
			}
		} else {
			showNoticeNow = true;
		}
	}

	public void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void addShortCut2() {
		final Intent launchIntent = getIntent();
		final String action = launchIntent.getAction();

		Intent shortcutIntent = new Intent();
		// 设置点击快捷方式时启动的Activity,因为是从Lanucher中启动，所以包名类名要写全。
		shortcutIntent.setComponent(new ComponentName(getPackageName(),
				getPackageName() + "." + AnimationIcon.class.getSimpleName()));
		// 设置启动的模式
		shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		Intent resultIntent = new Intent();
		// 设置快捷方式图标
		resultIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(this,
						R.drawable.shortcut_proc_clean));
		// 启动的Intent
		resultIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		// 设置快捷方式的名称
		resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));
		// 长按方式添加快捷方式----->即被动的Action方式。
		if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
			setResult(RESULT_OK, resultIntent);
			finish();
		} else {
			// 发送广播方式添加快捷方式----->即主动的发广播方式
			resultIntent.setAction(ACTION_INSTALL_SHORTCUT);
			sendBroadcast(resultIntent);
		}
	}

	private void initPopMenu() {
		inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		// 获取弹出菜单的布局
		popmenu_layout = inflater.inflate(R.layout.title_item_menu, null);
		// 设置popupWindow的布局
		popmenu = new PopupWindow(popmenu_layout,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		main_title_refresh = (TextView) popmenu_layout
				.findViewById(R.id.main_title_refresh);
		main_title_addshortcut = (TextView) popmenu_layout
				.findViewById(R.id.main_title_addshortcut);
		main_title_show_service = (TextView) popmenu_layout
				.findViewById(R.id.main_title_show_service);
		main_title_setup = (TextView) popmenu_layout
				.findViewById(R.id.main_title_setup);

		main_title_refresh.setOnClickListener(this);
		main_title_addshortcut.setOnClickListener(this);
		main_title_show_service.setOnClickListener(this);
		main_title_setup.setOnClickListener(this);

	}

	// 正在运行的
	public List<Programe> getRunningProcess() {
		pi = new PackagesInfo(this);
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		// 获取正在运行的应用
		run = am.getRunningAppProcesses();
		// 获取包管理器，在这里主要通过包名获取程序的图标和程序名
		pm = this.getPackageManager();
		List<Programe> list = new ArrayList<Programe>();
		for (RunningAppProcessInfo ra : run) {
			// 这里主要是过滤系统的应用和电话应用，当然你也可以把它注释掉。
			if (ra.processName.equals("system")
					|| ra.processName.equals("com.android.phone")) {
				continue;
			}
			if (pi.getInfo(ra.processName) == null) {
				continue;
			}
			int[] myMempid = new int[] { ra.pid };
			Programe pr = new Programe();
			String xx = "" + ra.processName;
			Log.d("zphlog", "ra.processName=" + xx);
			if (ra.processName.equals("com.example.getrunspak")) {
				THIS_PID = ra.pid;
			}
			pr.setIcon(pi.getInfo(ra.processName).loadIcon(pm));
			pr.setName(ra.processName);

			System.out.println(pi.getInfo(ra.processName).loadLabel(pm)
					.toString());
			// PID
			pr.setPID(ra.pid);// mod by zhangpeihang 20140925
			// memory
			Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(myMempid);
			double memSize = memoryInfo[0].dalvikPrivateDirty / 1024.0;
			int temp = (int) (memSize * 100);
			memSize = temp / 100.0;
			pr.setMemory("Memory:" + memSize + "MB");
			list.add(pr);
		}
		return list;
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_REFRESH:
				mListData = getRunningProcess();
				ListAdapter adapter = new ListAdapter(mListData,
						ActivityMain.this);
				mListView.setAdapter(adapter);
				upDateMemAndPakInfo();
				diyToast("刷新成功");
				break;
			case MSG_KILL:
				mListData = getRunningProcess();
				ListAdapter adapter2 = new ListAdapter(mListData,
						ActivityMain.this);
				mListView.setAdapter(adapter2);
				upDateMemAndPakInfo();
				diyToast("kill 成功");
				break;
			}
		}
	};

	public void killAllprocess() {
		for (RunningAppProcessInfo ra : run) {
			// 这里主要是过滤系统的应用和电话应用，当然你也可以把它注释掉。
			String kill_str = "system|android.phone|android.launcher|.home";
			Pattern patx = Pattern.compile(kill_str);
			Matcher matx = patx.matcher(ra.processName);
			if (matx.find()) {
				continue;
			}
			if (pi.getInfo(ra.processName) == null) {
				continue;
			}
			am.killBackgroundProcesses(ra.processName);
		}
		handler.sendEmptyMessage(MSG_KILL);
	}

	// 更新可用内存信息
	public void upDateMemAndPakInfo() {
		// 获得MemoryInfo对象
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		// 获得系统可用内存，保存在MemoryInfo对象上
		am.getMemoryInfo(memoryInfo);
		long memSize = memoryInfo.availMem;
		// 字符类型转换
		String restMemSize = Formatter
				.formatFileSize(getBaseContext(), memSize);
		mRestMemory.setText("剩余空间:" + restMemSize);
		// mPakcgeNums.setText(run.size());
		mPakcgeNums.setText("正在运行:" + run.size() + "个");
	}

	// 点击手机返回键返回到上一个界面
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				diyToast("再按一次退出程序");
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// public void processOnItemClick(Bundle savedInstanceState) {
	// mListView.setOnItemClickListener(new OnItemClickListener() {
	//
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View view,
	// int postion, long id) {
	// // TODO Auto-generated method stub
	// if (popmenu != null && isPopMenuShow) {
	// popmenu.dismiss();
	// isPopMenuShow = false;
	// }
	// }
	// });
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, ABOUT, 0, "关于应用");
		menu.add(0, INFO, 0, "系统信息");
		menu.add(0, SHARE, 0, "分享应用");
		menu.add(0, EXIT, 0, "退出应用");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		if (isPopMenuShow) {
			popmenu.dismiss();
			isPopMenuShow = false;
		}
		switch (mi.getItemId()) {
		case ABOUT:
			about_info();
			break;
		case INFO:
			system_info();
			break;
		case SHARE:
			share();
			break;
		case EXIT:
			finish();
			break;
		}
		return true;
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

	public void diyToast(String str) {
		if (mToast != null) {
			mToast.setText(str);
			mToast.setDuration(1500);
			mToast.show();
		} else {
			mToast = Toast.makeText(ActivityMain.this, str, 1500);
			mToast.show();
		}
	}

	public void onTitlePopMenuClick(View v) {
		if (!isPopMenuShow) {
			showPopUp(v);
		} else {
			popmenu.dismiss();
			isPopMenuShow = false;
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (isPopMenuShow) {
			popmenu.dismiss();
			isPopMenuShow = false;
		}
		switch (arg0.getId()) {
		case R.id.main_title_refresh:// 刷新
			new Thread() {
				public void run() {
					handler.sendEmptyMessage(MSG_REFRESH);
				}
			}.start();
			break;
		case R.id.main_title_show_service:// 显示服务
			showNoticeNow = false;
			break;
		case R.id.main_title_setup:// 设置
			showNoticeNow = false;
			Intent setup_intent = new Intent(ActivityMain.this, SetUpAll.class);
			setup_intent.putExtra("THIS_PID", THIS_PID);
			startActivity(setup_intent);
			break;
		case R.id.main_title_addshortcut:
			if (isShortCutOn) {
				addShortCut2();
			} else {
				diyToast("请在设置里打开 “显示桌面加速球”");
			}
			break;
		case R.id.kill_all:
			new Thread() {
				public void run() {
					killAllprocess();
					// handler.sendEmptyMessage(MSG_REFRESH);
				}
			}.start();
			break;
		}
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		if (isPopMenuShow) {
			popmenu.dismiss();
			isPopMenuShow = false;
		}
		return super.onTouchEvent(arg1);
	}

	private void showPopUp(View v) {
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		int height = v.getHeight();
		popmenu.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1]
				+ height);
		isPopMenuShow = true;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			int position, long id) {
		// TODO Auto-generated method stub

		String pakName = mListData.get(position).getName();
		Intent edit_intent_menu = new Intent(ActivityMain.this,
				ShortCutEditMenu.class);
		edit_intent_menu.putExtra("runlist_pkg_item", pakName);
		startActivity(edit_intent_menu);
		return true;
	}

	// 删除通知
	public static void clearNotification(Context context) {
		// 启动后删除之前我们定义的通知
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}

	// show notification add by zhangpeihang 20140924
	public static void showNotification(Context context) {
		// add by zhangpiehang 20140924
		// 主要设置点击通知的时显示内容的类

		notificationManager = (NotificationManager) context
				.getSystemService(NOTIFICATION_SERVICE);
		noticeIntent = new Intent(context, AnimationIcon.class);
		noticeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(context, 0, noticeIntent, 0);
		notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = "TaskKiller";
		// 通知时发出的声音
		notification.flags |= Notification.FLAG_NO_CLEAR;
		// notification.defaults = Notification.DEFAULT_SOUND;
		notification.setLatestEventInfo(context, "TaskKiller", "点击智能清理",
				pendingIntent);
		// 可以理解为执行这个通知
		notificationManager.notify(0, notification);
	}
}
