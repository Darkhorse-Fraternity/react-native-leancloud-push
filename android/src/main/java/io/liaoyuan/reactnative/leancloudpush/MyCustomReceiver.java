package io.liaoyuan.reactnative.leancloudpush;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.avos.avoscloud.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tong on 2016/8/17.
 */
public class MyCustomReceiver extends BroadcastReceiver {
    private static final String TAG = "MyCustomReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.log.d(TAG, "Get MyCustomReceiver");

        //sh ./shell/test.sh
        try {
            String action = intent.getAction();
            if("com.action.isForeground".equals(action)){
                //前后台广播接收
                LeanCloudPush.isForegroud = intent.getBooleanExtra("isForeground",false);
                LeanCloudPush.iconId = intent.getIntExtra("icon",-1);
            }else {
                //推送广播接收
                String channel = intent.getExtras().getString("com.avos.avoscloud.Channel");
                String data = intent.getExtras().getString("com.avos.avoscloud.Data");

                Map<String, String> map = new HashMap<String, String>();
                map.put("action", action);
                map.put("channel", channel);
                map.put("data", data);
                if(LeanCloudPush.isForegroud){
                    //前台直接回传JS
                    map.put("foreground", "1");
                    LeanCloudPush.onReceive(map);
                    Log.i(LeanCloudPush.MODULE_NAME, "onReceive: action = " + action + ", channel = " + channel + ", data = " + data);
                }else {
                    //后台创建通知栏消息
                    map.put("foreground", "0");
                    receivingNotification(context,map,intent,data);

                }
            }
        } catch (Exception e) {
            LeanCloudPush.onError(e);
            Log.e(LeanCloudPush.MODULE_NAME, "onError");
        }
    }

    /**
     * 处理通知消息
     * @param context
     * @param map
     * @param intent
     * @param data
     */
    private void receivingNotification(Context context,Map<String ,String> map, Intent intent,String data) {
        try {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder builder1 = new Notification.Builder(context);
            JSONObject jsonObject = new JSONObject(data);
            String title = jsonObject.getString("title");
            String content = jsonObject.getString("alert");
            builder1.setSmallIcon(LeanCloudPush.iconId); //设置图标
            builder1.setContentTitle(title); //设置标题
            builder1.setContentText(content); //消息内容
            builder1.setWhen(System.currentTimeMillis()); //发送时间
            builder1.setDefaults(Notification.DEFAULT_ALL); //设置默认的提示音，振动方式，灯光
            builder1.setAutoCancel(true);//打开程序后图标消失
            intent.setClass(context,LeanCloudPushClickHandlerActivity.class);

            //当任务栏上该通知被点击时执行的页面跳转
            PendingIntent pintent = PendingIntent.getActivity(context,0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder1.setContentIntent(pintent);
            mNotificationManager.notify(0,builder1.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
