package com.devilwwj.skin;

import android.app.Application;
import android.text.TextUtils;

public class SkinApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		String skinPath = SkinConfig.getInstance(this).getSkinResourcePath();
		if (!TextUtils.isEmpty(skinPath)) {
			// 如果已经换皮肤，那么第二次进来时，需要加载该皮肤
			SkinPackageManager.getInstance(this).loadSkinAsync(skinPath, null);
		}
	}
}
