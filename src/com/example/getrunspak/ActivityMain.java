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

	// ����֪ͨ������
	private static NotificationManager notificationManager = null;
	// ����Notification����
	private static Notification notification = null;
	private static PendingIntent pendingIntent = null;
	private static Intent noticeIntent = null;
	private boolean isNoticeOn = true;// ��prefence�����ȡ�Ƿ���Ҫ��ʾ
	private boolean showNoticeNow = true;// �ж��Ƿ��ǽ��������������onStop
	// ��ݼ�����
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
		upDateMemAndPakInfo();// ��ʾMemory��Ϣ
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
		// ���õ����ݷ�ʽʱ������Activity,��Ϊ�Ǵ�Lanucher�����������԰�������Ҫдȫ��
		shortcutIntent.setComponent(new ComponentName(getPackageName(),
				getPackageName() + "." + AnimationIcon.class.getSimpleName()));
		// ����������ģʽ
		shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		Intent resultIntent = new Intent();
		// ���ÿ�ݷ�ʽͼ��
		resultIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(this,
						R.drawable.shortcut_proc_clean));
		// ������Intent
		resultIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		// ���ÿ�ݷ�ʽ������
		resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));
		// ������ʽ��ӿ�ݷ�ʽ----->��������Action��ʽ��
		if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
			setResult(RESULT_OK, resultIntent);
			finish();
		} else {
			// ���͹㲥��ʽ��ӿ�ݷ�ʽ----->�������ķ��㲥��ʽ
			resultIntent.setAction(ACTION_INSTALL_SHORTCUT);
			sendBroadcast(resultIntent);
		}
	}

	private void initPopMenu() {
		inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		// ��ȡ�����˵��Ĳ���
		popmenu_layout = inflater.inflate(R.layout.title_item_menu, null);
		// ����popupWindow�Ĳ���
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

	// �������е�
	public List<Programe> getRunningProcess() {
		pi = new PackagesInfo(this);
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		// ��ȡ�������е�Ӧ��
		run = am.getRunningAppProcesses();
		// ��ȡ������������������Ҫͨ��������ȡ�����ͼ��ͳ�����
		pm = this.getPackageManager();
		List<Programe> list = new ArrayList<Programe>();
		for (RunningAppProcessInfo ra : run) {
			// ������Ҫ�ǹ���ϵͳ��Ӧ�ú͵绰Ӧ�ã���Ȼ��Ҳ���԰���ע�͵���
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
				diyToast("ˢ�³ɹ�");
				break;
			case MSG_KILL:
				mListData = getRunningProcess();
				ListAdapter adapter2 = new ListAdapter(mListData,
						ActivityMain.this);
				mListView.setAdapter(adapter2);
				upDateMemAndPakInfo();
				diyToast("kill �ɹ�");
				break;
			}
		}
	};

	public void killAllprocess() {
		for (RunningAppProcessInfo ra : run) {
			// ������Ҫ�ǹ���ϵͳ��Ӧ�ú͵绰Ӧ�ã���Ȼ��Ҳ���԰���ע�͵���
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

	// ���¿����ڴ���Ϣ
	public void upDateMemAndPakInfo() {
		// ���MemoryInfo����
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		// ���ϵͳ�����ڴ棬������MemoryInfo������
		am.getMemoryInfo(memoryInfo);
		long memSize = memoryInfo.availMem;
		// �ַ�����ת��
		String restMemSize = Formatter
				.formatFileSize(getBaseContext(), memSize);
		mRestMemory.setText("ʣ��ռ�:" + restMemSize);
		// mPakcgeNums.setText(run.size());
		mPakcgeNums.setText("��������:" + run.size() + "��");
	}

	// ����ֻ����ؼ����ص���һ������
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				diyToast("�ٰ�һ���˳�����");
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

		menu.add(0, ABOUT, 0, "����Ӧ��");
		menu.add(0, INFO, 0, "ϵͳ��Ϣ");
		menu.add(0, SHARE, 0, "����Ӧ��");
		menu.add(0, EXIT, 0, "�˳�Ӧ��");
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
		intent.setType("text/plain"); // ���ı�
		intent.putExtra(Intent.EXTRA_SUBJECT, "����");
		intent.putExtra(Intent.EXTRA_TEXT, "�ҷ�����һ�����õ�TaskKiller");
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
		case R.id.main_title_refresh:// ˢ��
			new Thread() {
				public void run() {
					handler.sendEmptyMessage(MSG_REFRESH);
				}
			}.start();
			break;
		case R.id.main_title_show_service:// ��ʾ����
			showNoticeNow = false;
			break;
		case R.id.main_title_setup:// ����
			showNoticeNow = false;
			Intent setup_intent = new Intent(ActivityMain.this, SetUpAll.class);
			setup_intent.putExtra("THIS_PID", THIS_PID);
			startActivity(setup_intent);
			break;
		case R.id.main_title_addshortcut:
			if (isShortCutOn) {
				addShortCut2();
			} else {
				diyToast("����������� ����ʾ���������");
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

	// ɾ��֪ͨ
	public static void clearNotification(Context context) {
		// ������ɾ��֮ǰ���Ƕ����֪ͨ
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}

	// show notification add by zhangpeihang 20140924
	public static void showNotification(Context context) {
		// add by zhangpiehang 20140924
		// ��Ҫ���õ��֪ͨ��ʱ��ʾ���ݵ���

		notificationManager = (NotificationManager) context
				.getSystemService(NOTIFICATION_SERVICE);
		noticeIntent = new Intent(context, AnimationIcon.class);
		noticeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(context, 0, noticeIntent, 0);
		notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = "TaskKiller";
		// ֪ͨʱ����������
		notification.flags |= Notification.FLAG_NO_CLEAR;
		// notification.defaults = Notification.DEFAULT_SOUND;
		notification.setLatestEventInfo(context, "TaskKiller", "�����������",
				pendingIntent);
		// �������Ϊִ�����֪ͨ
		notificationManager.notify(0, notification);
	}
}
