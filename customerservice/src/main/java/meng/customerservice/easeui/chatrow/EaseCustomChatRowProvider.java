package meng.customerservice.easeui.chatrow;

import android.app.Activity;
import android.widget.BaseAdapter;

import com.hyphenate.chat.EMMessage;

import meng.customerservice.easeui.utils.EaseCommonUtils;

/**
 * 自定义chat row提供者
 */
public class EaseCustomChatRowProvider {
    private static final int MESSAGE_TYPE_SENT_PICTURE_TXT = 1;
    private static final int MESSAGE_TYPE_RECV_PICTURE_TXT = 2;
    private static final int MESSAGE_TYPE_SENT_ROBOT_MENU = 3;
    private static final int MESSAGE_TYPE_RECV_ROBOT_MENU = 4;

    // evaluation
    private static final int MESSAGE_TYPE_SENT_EVAL = 5;
    private static final int MESSAGE_TYPE_RECV_EVAL = 6;

    // transfer to kefu message
    private static final int MESSAGE_TYPE_SENT_TRANSFER_TO_KEFU = 7;
    private static final int MESSAGE_TYPE_RECV_TRANSFER_TO_KEFU = 8;
    private Activity activity;

    public EaseCustomChatRowProvider(Activity activity) {
        this.activity = activity;
    }

    public int getCustomChatRowTypeCount() {
        return 8;
    }

    public int getCustomChatRowType(EMMessage message) {
        if (message.getType() == EMMessage.Type.TXT) {
            if (EaseCommonUtils.isRobotMenuMessage(message)) {
                // 机器人 列表菜单
                return message.direct() == EMMessage.Direct.RECEIVE
                        ? MESSAGE_TYPE_RECV_ROBOT_MENU
                        : MESSAGE_TYPE_SENT_ROBOT_MENU;
            } else if (EaseCommonUtils.isEvalMessage(message)) {
                // 满意度评价
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_EVAL : MESSAGE_TYPE_SENT_EVAL;
            } else if (EaseCommonUtils.isPictureTxtMessage(message)) {
                // 订单图文组合
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_PICTURE_TXT
                        : MESSAGE_TYPE_SENT_PICTURE_TXT;
            } else if (EaseCommonUtils.isTransferToKefuMsg(message)) {
                // 转人工消息
                return message.direct() == EMMessage.Direct.RECEIVE
                        ? MESSAGE_TYPE_RECV_TRANSFER_TO_KEFU
                        : MESSAGE_TYPE_SENT_TRANSFER_TO_KEFU;
            }
        }
        return 0;
    }

    public EaseChatRow getCustomChatRow(EMMessage message, int position, BaseAdapter adapter) {
        if (message.getType() == EMMessage.Type.TXT) {
            if (EaseCommonUtils.isRobotMenuMessage(message)) {
                return new ChatRowRobotMenu(activity, message, position, adapter);
            } else if (EaseCommonUtils.isEvalMessage(message)) {
//                return new ChatRowEvaluation(getActivity(), message, position, adapter);
                return new EaseChatRowText(activity, message, position, adapter);
            } else if (EaseCommonUtils.isPictureTxtMessage(message)) {
//                return new ChatRowPictureText(getActivity(), message, position, adapter);
                return new EaseChatRowText(activity, message, position, adapter);
            } else if (EaseCommonUtils.isTransferToKefuMsg(message)) {
                return new ChatRowTransferToKefu(activity, message, position, adapter);
            }
        }
        return null;
    }

}
