package com.devilwwj.skin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
/**
 * 皮肤包管理器
 * @author devilwwj
 *
 */
public class SkinPackageManager {
	private static SkinPackageManager mInstance;
	private Context mContext;
	
	/**
	 * 当前资源包名
	 */
	public String mPackageName;
	
	/**
	 * 皮肤资源
	 */
	public Resources mResources;
	
	
	public SkinPackageManager(Context mContext) {
		super();
		this.mContext = mContext;
	}

	/**
	 * 获取单例
	 * @param mContext
	 * @return
	 */
	public static SkinPackageManager getInstance(Context mContext) {
		if (mInstance == null) {
			mInstance = new SkinPackageManager(mContext);
		}
		return mInstance;
	}



	/**
	 * 从assets中复制apk到sd中
	 * @param context
	 * @param filename
	 * @param path
	 * @return
	 */
	public boolean copyApkFromAssets(Context context, String filename, String path) {
		boolean copyIsFinish = false;
		
		try {
			InputStream is = context.getAssets().open(filename);
			File file = new File(path);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] temp = new byte[1024];
			int i = 0;
			while(( i = is.read(temp)) > 0) {
				fos.write(temp, 0, i); // 写入到文件
			}
			
			fos.close();
			is.close();
			copyIsFinish = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return copyIsFinish;
		
	}
	
	/**
	 * 异步加载皮肤资源
	 * @param dexPath 需要加载的皮肤资源 
	 * @param callback 回调接口
	 */
	public void loadSkinAsync(String dexPath, final loadSkinCallBack callback) {
		new AsyncTask<String, Void, Resources>() {
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				if (callback != null) {
					callback.startloadSkin();
				}
			}

			@Override
			protected Resources doInBackground(String... params) {
				try {
					if (params.length == 1) {
						// 
						String dexPath_tmp = params[0];
						// 得到包管理器
						PackageManager mpm = mContext.getPackageManager();
						// 得到包信息
						PackageInfo mInfo = mpm.getPackageArchiveInfo(dexPath_tmp, PackageManager.GET_ACTIVITIES);
						mPackageName = mInfo.packageName;
						
						AssetManager assetManager = AssetManager.class.newInstance();
						Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
						addAssetPath.invoke(assetManager, dexPath_tmp);
						
						Resources superRes = mContext.getResources();
						Resources skinResource = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
						SkinConfig.getInstance(mContext).setSkinResourcePath(dexPath_tmp); // 保存资源路径
						return skinResource;
					}
				} catch (Exception e) {
					return null;
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Resources result) {
				super.onPostExecute(result);
				mResources = result;
				
				if (callback != null) {
					if (mResources != null) {
						callback.loadSkinSuccess();
					} else {
						callback.loadSkinFail();
					}
				}
			}
			
		}.execute(dexPath);
	}
	
	
	public static interface loadSkinCallBack {
		public void startloadSkin();
		public void loadSkinSuccess();
		public void loadSkinFail();
	}
	
}
