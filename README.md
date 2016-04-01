# Android更换皮肤解决方案

>转载请注明出处：[IT_xiao小巫](http://www.devilwwj.com/)

本篇博客要给大家分享的一个关于Android应用换肤的Demo，大家可以到我的github去下载demo，以后博文涉及到的代码均会上传到github中统一管理。
github地址：[https://github.com/devilWwj/Android-skin-update](https://github.com/devilWwj/Android-skin-update)

![](http://www.devilwwj.com/images/Snip20150603_1.png)

## 思路
换肤功能一般有什么？
元素一般有背景颜色、字体颜色、图片、布局等等

我们知道Android中有主题Theme还有style，theme是针对整个activity的，而style可以针对指定控件，如果比较少的替换可以在app内做，但如果需要动态来做，可以选择下面这种思路：
**把app和skin分开，将skin做成一个apk，作为一个插件来提供给app使用，这样可以做到在线下载皮肤，然后动态更换皮肤**

下面这个demo，小巫是建立了一个res的工程项目，简单提供了一个colors.xml，在里面指定了背景颜色和按钮颜色：
```java 
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="day_btn_color">#E61ABD</color>
    <color name="day_background">#38F709</color>
    
    <color name="night_btn_color">#000000</color>
    <color name="night_background">#FFFFFF</color>
</resources>

```
里面没有任何逻辑代码，只提供资源文件，然后我们导出为skin.apk文件，复制到目标项目的assets中去。
![](http://www.devilwwj.com/images/Snip20150603_2.png)

因为这里不涉及到下载皮肤这个操作，所以直接放到assets目录下，然后在程序中把assets下的apk文件复制到sd卡中.
在程序中提供一个皮肤包管理器
```java 
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
 * 
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
     * 
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
     * 
     * @param context
     * @param filename
     * @param path
     * @return
     */
    public boolean copyApkFromAssets(Context context, String filename,
            String path) {
        boolean copyIsFinish = false;

        try {
            // 打开assets的输入流
            InputStream is = context.getAssets().open(filename);
            File file = new File(path);
            // 创建一个新的文件
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
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
     * 
     * @param dexPath
     *            需要加载的皮肤资源
     * @param callback
     *            回调接口
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
                        PackageInfo mInfo = mpm.getPackageArchiveInfo(
                                dexPath_tmp, PackageManager.GET_ACTIVITIES);
                        mPackageName = mInfo.packageName;

                        // AssetManager实例
                        AssetManager assetManager = AssetManager.class
                                .newInstance();
                        // 通过反射调用addAssetPath方法
                        Method addAssetPath = assetManager.getClass()
                                .getMethod("addAssetPath", String.class);
                        addAssetPath.invoke(assetManager, dexPath_tmp);

                        // 得到资源实例
                        Resources superRes = mContext.getResources();
                        // 实例化皮肤资源
                        Resources skinResource = new Resources(assetManager,
                                superRes.getDisplayMetrics(),
                                superRes.getConfiguration());
                        // 保存资源路径
                        SkinConfig.getInstance(mContext).setSkinResourcePath(
                                dexPath_tmp);
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

                // 这里执行回调方法
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

```
重点关注这个类，里面提供了一个异步方法对包和asset进行操作，这里用到了反射机制，反射调用addAssetPath来添加assets的路径，这个路径就是我们skin.apk的路径。具体细节，各位查看代码。

我们在Activity界面中使用上面提供的方法：
```java 
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

```
我们可以保存一个模式，比如黑夜白天模式，每次启动按照前面保存的模式来显示皮肤。我们可以看到上面是通过调用getIdentifier方法来得到指定的资源的id，name是我们在资源文件中指定的名字。

最后，各位自己跑一遍这样的流程：
1. 导出res的apk文件
2. 复制到目标项目的assets目录下
3. 查看切换皮肤的效果

参考博文：[http://blog.csdn.net/yuanzeyao/article/details/42390431](http://blog.csdn.net/yuanzeyao/article/details/42390431)
