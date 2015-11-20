/**
 * 
 */
package com.example.getrunspak.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.getrunspak.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author zhangpeihang
 * 
 */
public class ShortCutEditMenu extends Activity implements OnItemClickListener {
	//
	// private InfraredData mInfraredDara = new InfraredData(
	// ShortCutEditMenu.this, "real_tv");
	private ListView mListView;
	private List<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
	private String package_item_name;
	private ActivityManager am;

	private SharedPreferences mSetting;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shortcut_edit_menu);
		Intent intent = getIntent();
		package_item_name = intent.getStringExtra("runlist_pkg_item");
		initView();
	}

	private void initView() {
		mSetting = getSharedPreferences("pishum", 0);
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mListView = (ListView) findViewById(R.id.shortcut_edit_menu);
		mListView.setOnItemClickListener(this);
		mData = getData();

		SimpleAdapter menuAdapter = new SimpleAdapter(this, mData,
				R.layout.simple_textview_item,
				new String[] { "shortcut_edit_menu_text" },
				new int[] { R.id.simple_textview_item });
		mListView.setAdapter(menuAdapter);
	}

	protected void onStop() {
		super.onStop();
		// mInfraredDara.close();
		// mInfraredDara = null;
		mData = null;
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> listInfoData = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("shortcut_edit_menu_text", "����");
		map.put("shortcut_edit_menu_key", 1);
		listInfoData.add(map);
		map = new HashMap<String, Object>();
		map.put("shortcut_edit_menu_text", "�л����ó���");
		map.put("shortcut_edit_menu_key", 2);
		listInfoData.add(map);
		map = new HashMap<String, Object>();
		map.put("shortcut_edit_menu_text", "��������");
		map.put("shortcut_edit_menu_key", 3);
		listInfoData.add(map);
		map = new HashMap<String, Object>();
		map.put("shortcut_edit_menu_text", "ж�س���");
		map.put("shortcut_edit_menu_key", 4);
		listInfoData.add(map);
		return listInfoData;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		// TODO Auto-generated method stub
		// intent_str
		HashMap<String, Object> item = (HashMap<String, Object>) arg0
				.getItemAtPosition(position);
		int menu_key = (Integer) item.get("shortcut_edit_menu_key");
		Log.d("zphlogkillmenu", "pak_name = " + package_item_name + "\nmenu="
				+ menu_key);
		doMenuKey(package_item_name, menu_key);
		ShortCutEditMenu.this.finish();
	}

	// ���ݲ˵���������ͬ��menu_keyѡ��ͬ�Ĳ���
	private void doMenuKey(String package_item_name, int menu_key) {
		switch (menu_key) {
		case 1:
			break;
		case 2:// �л����ó���
			doStartApplicationWithPackageName(package_item_name);
			break;
		case 3:
			// ִ�н�������
			mSetting.edit().putBoolean("needRefresh", true).commit();
			am.killBackgroundProcesses(package_item_name);
			break;
		case 4:// ж�س���
			mSetting.edit().putBoolean("needRefresh", true).commit();
			Uri uri = Uri.fromParts("package", package_item_name, null);
			Intent it = new Intent(Intent.ACTION_DELETE, uri);
			try {
				startActivity(it);
			} catch (Exception e) {
				Toast.makeText(ShortCutEditMenu.this, e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	@SuppressWarnings("unused")
	private void doStartApplicationWithPackageName(String packagename) {

		// ͨ��������ȡ��APP��ϸ��Ϣ������Activities��services��versioncode��name�ȵ�
		PackageInfo packageinfo = null;
		try {
			packageinfo = getPackageManager().getPackageInfo(packagename, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) {
			return;
		}

		// ����һ�����ΪCATEGORY_LAUNCHER�ĸð�����Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		// ͨ��getPackageManager()��queryIntentActivities��������
		List<ResolveInfo> resolveinfoList = getPackageManager()
				.queryIntentActivities(resolveIntent, 0);
		if (resolveinfoList.iterator().hasNext()) {
			ResolveInfo resolveinfo = resolveinfoList.iterator().next();
			if (resolveinfo != null) {
				// packagename = ����packname
				String packageName = resolveinfo.activityInfo.packageName;
				// �����������Ҫ�ҵĸ�APP��LAUNCHER��Activity[��֯��ʽ��packagename.mainActivityname]
				String className = resolveinfo.activityInfo.name;
				// LAUNCHER Intent
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				// ����ComponentName����1:packagename����2:MainActivity·��
				ComponentName cn = new ComponentName(packageName, className);
				intent.setComponent(cn);
				try {
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(ShortCutEditMenu.this, e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		} else {
			Toast.makeText(ShortCutEditMenu.this, "�޷��л����ó���", Toast.LENGTH_LONG)
					.show();
		}
	}
}
