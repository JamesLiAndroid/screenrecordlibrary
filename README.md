# ScreenRecordLibrary

## Intro
ScreenRecord library is used to capture screen for devices with android 5.0 and above. It used the latest 
[Media Projection API](https://developer.android.com/intl/es/reference/android/media/projection/MediaProjection.html) 
exposed by android since API level 21.

## Demo
If you want to use this library in your code, you can download the project, and refer to `app` module in the code.<br>
If you just want to use this Screen Record Tool, you can download it from 
[google play](https://play.google.com/store/apps/details?id=com.eversince.screenrecord&hl=en) 
or [应用宝](http://sj.qq.com/myapp/detail.htm?apkName=com.eversince.screenrecord)
## Usage
This library is uploaded to [sonatype](https://oss.sonatype.org/#welcome), you can use it following these steps:<br>
1. add sonatype repository in your build.gradle<br>
```java
    
    allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven {
            url 'https://oss.sonatype.org/content/groups/public'
        }
    }
    
```
2. need to compile this library<br>
```java
 
        compile 'com.github.charlesjean:recordlibrary:0.0.2'
  
  ```
3. after you acquire media projection permission from user, you need to start screen record service with the following code<br>
```java

          Intent intent = new Intent(this, RecordService.class);
            int width = ParameterManager.getInstance(this).getVideoWidth();
            int height = ParameterManager.getInstance(this).getVideoHeight();
            boolean needAudio = ParameterManager.getInstance(this).needAudio();
            boolean isLandScapeMode = ParameterManager.getInstance(this).isLandScapeModeOn();
            int quality = ParameterManager.getInstance(this).getVideoQuality();

            intent.putExtra(RecordConst.RECORD_INTENT_RESULT, resultCode);
            intent.putExtra(RecordConst.RECORD_DATA_INTENT, data);
            intent.putExtra(RecordConst.KEY_RECORD_SCREEN_WITH, width);
            intent.putExtra(RecordConst.KEY_RECORD_SCREEN_HEIGHT, height);
            intent.putExtra(RecordConst.KEY_RECORD_NEED_AUDIO, needAudio);
            intent.putExtra(RecordConst.KEY_RECORD_IS_LANDSCAPE, isLandScapeMode);
            intent.putExtra(RecordConst.KEY_VIDEO_QUALITY, quality);
            intent.putExtra(RecordConst.KEY_VIDEO_DIR, getResources().getString(R.string.save_dir));
            intent.putExtra(RecordConst.KEY_NOTIFICATION_ICON, R.drawable.ic_launcher);
            startService(intent);
  
  ```
For more detail, you can refer to the demo project. 

# License
This library is under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html), not including the demo project.

