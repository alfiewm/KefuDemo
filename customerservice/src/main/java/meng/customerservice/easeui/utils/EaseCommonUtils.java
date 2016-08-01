/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package meng.customerservice.easeui.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.hyphenate.chat.EMConversation.EMConversationType;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import meng.customerservice.easeui.EaseConstant;

public class EaseCommonUtils {
    private static final String TAG = "CommonUtils";

    /**
     * 检测网络是否可用
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }

        return false;
    }

    /**
     * 检测Sdcard是否存在
     */
    public static boolean isExitsSdcard() {
        return android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static EMMessage createExpressionMessage(String toChatUsername, String expressioName,
                                                    String identityCode) {
        EMMessage message = EMMessage.createTxtSendMessage("[" + expressioName + "]",
                toChatUsername);
        if (identityCode != null) {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, identityCode);
        }
        message.setAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, true);
        return message;
    }

    /**
     * 根据消息内容和消息类型获取消息内容提示
     */
    public static String getMessageDigest(EMMessage message) {
        if (message == null) {
            return "";
        }
        String digest;
        switch (message.getType()) {
            case IMAGE: // 图片消息
                digest = "[图片]";
                break;
            case TXT: // 文本消息
                EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
                digest = txtBody.getMessage();
                break;
            case FILE: // 普通文件消息
                digest = "[文件]";
                break;
            default:
                EMLog.e(TAG, "[未知类型]");
                return "";
        }

        return digest;
    }

    /**
     * 获取栈顶的activity
     */
    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null)
            return runningTaskInfos.get(0).topActivity.getClassName();
        else
            return "";
    }

    public static boolean isRobotMenuMessage(EMMessage message) {
        try {
            JSONObject jsonObj = message.getJSONObjectAttribute(EaseConstant.MESSAGE_ATTR_MSGTYPE);
            if (jsonObj.has("choice") && !jsonObj.isNull("choice")) {
                JSONObject jsonChoice = jsonObj.getJSONObject("choice");
                if (jsonChoice.has("items") || jsonChoice.has("list")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 检测是否为转人工的消息，如果是则需要显示转人工的按钮
     */
    public static boolean isTransferToKefuMsg(EMMessage message) {
        try {
            JSONObject jsonObj = message.getJSONObjectAttribute(EaseConstant.WEICHAT_MSG);
            if (jsonObj.has("ctrlType")) {
                String type = jsonObj.getString("ctrlType");
                if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("TransferToKfHint")) {
                    return true;
                }
            }
        } catch (JSONException | HyphenateException ignored) {
        }
        return false;
    }

    /**
     * 将应用的会话类型转化为SDK的会话类型
     */
    public static EMConversationType getConversationType(int chatType) {
        if (chatType == EaseConstant.CHATTYPE_SINGLE) {
            return EMConversationType.Chat;
        } else if (chatType == EaseConstant.CHATTYPE_GROUP) {
            return EMConversationType.GroupChat;
        } else {
            return EMConversationType.ChatRoom;
        }
    }

    public static boolean isEvalMessage(EMMessage message) {
        try {
            JSONObject jsonObj = message.getJSONObjectAttribute(EaseConstant.WEICHAT_MSG);
            if (jsonObj.has("ctrlType")) {
                String type = jsonObj.getString("ctrlType");
                if (!TextUtils.isEmpty(type) && (type.equalsIgnoreCase("inviteEnquiry")
                        || type.equalsIgnoreCase("enquiry"))) {
                    return true;
                }
            }
        } catch (JSONException | HyphenateException ignored) {
        }
        return false;
    }

    public static boolean isPictureTxtMessage(EMMessage message) {
        JSONObject jsonObj = null;
        try {
            jsonObj = message.getJSONObjectAttribute(EaseConstant.MESSAGE_ATTR_MSGTYPE);
        } catch (HyphenateException ignored) {
        }
        return jsonObj != null && (jsonObj.has("order") || jsonObj.has("track"));
    }
}
