package com.devilwwj.skin;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.devilwwj.skin.SkinPackageManager.loadSkinCallBack;
/**
 * 功能：切换皮肤
 * @author devilwwj
 *
 */
public class MainActivity extends Activity implements OnClickListener,
		ISkinUpdate {
	private static final String APK_NAME = "skin.apk";
	private static final String DEX_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/skin.apk";
	private Button dayButton;
	private Button nightButton;
	private TextView textView;
	private boolean nightModel = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dayButton = (Button) findViewById(R.id.btn_day);
		nightButton = (Button) findViewById(R.id.btn_night);
		textView = (TextView) findViewById(R.id.text);

		// 把apk文件复制到sd卡
		SkinPackageManager.getInstance(this).copyApkFromAssets(this, APK_NAME,
				DEX_PATH);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (SkinPackageManager.getInstance(this).mResources != null) {
			updateTheme();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_day:
			nightModel = false;
			loadSkin();
			break;
		case R.id.btn_night:
			nightModel = true;
			loadSkin();
			break;

		default:
			break;
		}
	}

	/**
	 * 加载皮肤
	 */
	private void loadSkin() {
		SkinPackageManager.getInstance(this).loadSkinAsync(DEX_PATH,
				new loadSkinCallBack() {

					@Override
					public void startloadSkin() {
						Log.d("xiaowu", "startloadSkin");
					}

					@Override
					public void loadSkinSuccess() {
						Log.d("xiaowu", "loadSkinSuccess");
						// 然后这里更新主题
						updateTheme();
					}

					@Override
					public void loadSkinFail() {
						Log.d("xiaowu", "loadSkinFail");
					}
				});
	}

	@Override
	public void updateTheme() {
		Resources mResource = SkinPackageManager.getInstance(this).mResources;
		if (nightModel) {
			// 如果是黑夜的模式，则加载黑夜的主题
			int id1 = mResource.getIdentifier("night_btn_color", "color",
					"com.devilwwj.res");
			nightButton.setBackgroundColor(mResource.getColor(id1));
			int id2 = mResource.getIdentifier("night_background", "color",
					"com.devilwwj.res");
			nightButton.setTextColor(mResource.getColor(id2));
			textView.setTextColor(mResource.getColor(id2));
			
		} else {
			// 如果是白天模式，则加载白天的主题
			int id1 = mResource.getIdentifier("day_btn_color", "color",
					"com.devilwwj.res");
			dayButton.setBackgroundColor(mResource.getColor(id1));
			int id2 = mResource.getIdentifier("day_background", "color",
					"com.devilwwj.res");
			dayButton.setTextColor(mResource.getColor(id2));
			textView.setTextColor(mResource.getColor(id2));
		}

	}

}
