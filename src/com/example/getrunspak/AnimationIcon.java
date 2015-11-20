package com.example.getrunspak;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AnimationIcon extends Activity {
	private static final String TAG = "AnimationActivity";

	private static final int MESSAGE_ROTATE_FINISHED = 0;
	private static final int MESSAGE_UPDATE_WIDTH = 1;
	private static final int MESSAGE_CLOSE_WIN = 2;
	private static final int MESSAGE_KILL_ALL_PAK = 3;
	private RelativeLayout mShortcut;
	private RelativeLayout mRelativeLayout;
	private Rect rect;

	private ImageView backImageView;
	private ImageView roateImageView;
	private TextView textView;

	private int mWidth;
	private long mCurrent;

	private PackagesInfo pi;
	private ActivityManager am;
	private List<RunningAppProcessInfo> run;
	private PackageManager pm;
	private List<Programe> mListData;
	private String mFreeMemerySize;

	private static enum Direction {
		RIGHT, LEFT
	}

	private Direction direction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_animation);
		Intent intent = getIntent();
		if (intent == null) {
			finish();
			return;
		}
		// 取得Lanucher传过来的所点击的快捷方式的矩形坐标。
		rect = intent.getSourceBounds();
		if (rect == null) {
			finish();
			return;
		}

		Log.d(TAG, rect.toShortString());

		mRelativeLayout = (RelativeLayout) findViewById(R.id.framelayout);
		mShortcut = (RelativeLayout) findViewById(R.id.shortcut);

		backImageView = (ImageView) findViewById(R.id.clean_back);
		roateImageView = (ImageView) findViewById(R.id.clean_rotate);
		// iconmageView = (ImageView) findViewById(R.id.clean_icon);
		textView = (TextView) findViewById(R.id.text);

		// DisplayMetrics dm = new DisplayMetrics();
		int width = getWindowManager().getDefaultDisplay().getWidth();
		int hight = getWindowManager().getDefaultDisplay().getHeight();

		Log.d(TAG, "width = " + width);
		Log.d(TAG, "hight = " + hight);

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mShortcut
				.getLayoutParams();
		layoutParams.topMargin = rect.top - (rect.bottom - rect.top) / 4;

		// 判断快捷方式在屏幕的哪一边，如果在左边，伸缩动画就会向右，如果在右边，伸缩动画向左。
		if (rect.left < width / 2) {
			direction = Direction.RIGHT;
			layoutParams.leftMargin = rect.left;

		} else {
			direction = Direction.LEFT;
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			layoutParams.rightMargin = width - rect.right;
			Log.d(TAG, "rightMargin = " + (width - rect.right));
		}

		mRelativeLayout.updateViewLayout(mShortcut, layoutParams);
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_ROTATE_FINISHED:
				mWidth = backImageView.getWidth();
				Log.d(TAG, "mWidth = " + mWidth);
				updateWidth();
				roateImageView.clearAnimation();
				roateImageView.setVisibility(View.INVISIBLE);
				// while (true) {
				// if (System.currentTimeMillis() - mCurrent > 2000) {
				// Log.d("zphlog", "come into the time over");
				// break;
				// }
				// }
				// mRelativeLayout.setVisibility(View.GONE);
				// finish();
				break;
			case MESSAGE_UPDATE_WIDTH:
				updateWidth();
				break;
			case MESSAGE_CLOSE_WIN:
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
			case MESSAGE_KILL_ALL_PAK:
				new Thread() {
					public void run() {
						Looper.prepare();
						mListData = getRunningProcess();
						killAllprocess();
						// handler.sendEmptyMessage(MSG_REFRESH);
						Looper.loop();
					}
				}.start();
				break;
			default:
				break;
			}
		};
	};

	private void updateWidth() {
		// 宽度没有达到原来宽度的2.5度，继续做动画
		if (backImageView.getWidth() <= 2.5f * mWidth) {
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) backImageView
					.getLayoutParams();
			// 每次增加20的宽度，可以自行设置，和用户体验有关系，可自行调整
			layoutParams.width = backImageView.getWidth() + 20;
			mShortcut.updateViewLayout(backImageView, layoutParams);
			// 继续发更新消息。也可发送delay消息，和用户体验有关系，可自行调整
			mHandler.sendEmptyMessage(MESSAGE_UPDATE_WIDTH);
		} else {
			textView.setVisibility(View.VISIBLE);
			textView.setText(mFreeMemerySize);
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// android.os.Process.killProcess(android.os.Process.myPid());
			mHandler.sendEmptyMessageDelayed(MESSAGE_CLOSE_WIN, 1500);
		}

	};

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		// 旋转动画开始
		roateImageView.startAnimation(AnimationUtils.loadAnimation(this,
				R.anim.rotate_anim));

		// 假设垃圾清理了两秒钟，然后开如做伸缩动画。
		mHandler.sendEmptyMessage(MESSAGE_KILL_ALL_PAK);
		// mHandler.sendEmptyMessageDelayed(MESSAGE_ROTATE_FINISHED, 2000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mRelativeLayout.setVisibility(View.GONE);
		finish();
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

	public void killAllprocess() {
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		// 获得系统可用内存，保存在MemoryInfo对象上
		am.getMemoryInfo(memoryInfo);
		long memSize_1 = memoryInfo.availMem;
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
		am.getMemoryInfo(memoryInfo);
		long memSize_2 = memoryInfo.availMem;
		DecimalFormat df = new DecimalFormat("0.00");
		if ((memSize_2 - memSize_1) / 1048576.00 < 1) {
			mFreeMemerySize = "已经达到最佳状态";
		} else {
			mFreeMemerySize = "释放了"
					+ df.format((memSize_2 - memSize_1) / 1000000.00) + "MB";
		}

		// mHandler.sendEmptyMessage(MESSAGE_UPDATE_TEXT);
		mHandler.sendEmptyMessage(MESSAGE_ROTATE_FINISHED);
		Log.d("zphlog",
				"释放了=" + df.format((memSize_2 - memSize_1) / 1048576.00));
	}
}
