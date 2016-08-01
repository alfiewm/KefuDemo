package meng.customerservice.easeui;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMMessage;

import java.util.List;

/**
 * Created by meng on 16/8/1.
 */
public class EmptyEMMessageListener implements EMMessageListener {
    @Override
    public void onMessageReceived(List<EMMessage> list) {
        // nothing
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> list) {
        // nothing
    }

    @Override
    public void onMessageReadAckReceived(List<EMMessage> list) {
        // nothing
    }

    @Override
    public void onMessageDeliveryAckReceived(List<EMMessage> list) {
        // nothing
    }

    @Override
    public void onMessageChanged(EMMessage emMessage, Object o) {
        // nothing
    }
}
