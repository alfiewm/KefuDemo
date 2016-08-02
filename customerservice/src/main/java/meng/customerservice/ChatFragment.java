package meng.customerservice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

import meng.customerservice.easeui.EaseChatMessageList;
import meng.customerservice.easeui.EaseConstant;
import meng.customerservice.easeui.EmptyEMMessageListener;
import meng.customerservice.easeui.chatrow.EaseCustomChatRowProvider;
import meng.customerservice.easeui.utils.EaseCommonUtils;

public class ChatFragment extends Fragment implements View.OnClickListener {
    protected InputMethodManager inputMethodManager;
    protected Bundle fragmentArgs;
    protected String toChatUsername;
    private int chatType = EaseConstant.CHATTYPE_SINGLE;
    protected boolean showUserNick;
    protected EMConversation conversation;
    protected InputMethodManager inputManager;
    protected ClipboardManager clipboard;
    protected Handler handler = new Handler();
    protected File cameraFile;
    protected boolean isLoading;
    protected boolean haveMoreData = true;
    protected int pageSize = 20;
    private boolean isMessageListInited;

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected EaseChatMessageList messageList;
    protected ListView listView;
    private EditText inputTextView;
    private TextView sendBtn;
    private ImageView choosePicView;
    private View imageBoard;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentArgs = getArguments();
        // 会话人或群组id
        toChatUsername = fragmentArgs.getString(EaseConstant.EXTRA_USER_ID);
        // 是否显示用户昵称
        showUserNick = fragmentArgs.getBoolean(EaseConstant.EXTRA_SHOW_USERNICK, false);
        String contextMsg = fragmentArgs.getString(EaseConstant.EXTRA_CONTEXT_TEXT_MESSAGE, "");
        inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        setupViews();
        if (!TextUtils.isEmpty(contextMsg)) {
            sendTextMessage(contextMsg);
        }
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    protected void setupViews() {
        View rootView = getView();
        setupTitleBar();

        choosePicView = (ImageView) rootView.findViewById(R.id.choose_pic);
        choosePicView.setOnClickListener(this);
        imageBoard = rootView.findViewById(R.id.image_board);

        inputTextView = (EditText) rootView.findViewById(R.id.input_text);
        inputTextView.addTextChangedListener(textWatcher);
        inputTextView.setOnClickListener(this);

        sendBtn = (TextView) rootView.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(this);

        // 消息列表layout
        messageList = (EaseChatMessageList) rootView.findViewById(R.id.message_list);
        listView = messageList.getListView();

        // init input menu
        swipeRefreshLayout = messageList.getSwipeRefreshLayout();
        swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);

        inputManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initConversation();
        initMessageList();
        setRefreshLayoutListener();
    }

    private void setupTitleBar() {
        getView().findViewById(R.id.navbar_left).setOnClickListener(this);
        getView().findViewById(R.id.navbar_right).setOnClickListener(this);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            sendBtn.setEnabled(s != null && s.length() > 0);
        }
    };

    protected void initConversation() {
        // 获取当前conversation对象
        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername,
                EaseCommonUtils.getConversationType(chatType), true);
        // 把此会话的未读数置为0
        conversation.markAllMessagesAsRead();
        // 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
        // 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
        final List<EMMessage> msgs = conversation.getAllMessages();
        int msgCount = msgs != null ? msgs.size() : 0;
        if (msgCount < conversation.getAllMsgCount() && msgCount < pageSize) {
            String msgId = null;
            if (msgs != null && msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            conversation.loadMoreMsgFromDB(msgId, pageSize - msgCount);
        }

    }

    protected void initMessageList() {
        messageList.init(toChatUsername, chatType, new EaseCustomChatRowProvider(getActivity()));
        // 设置list item里的控件的点击事件
        setListItemClickListener();

        messageList.getListView().setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        isMessageListInited = true;
    }

    protected void setListItemClickListener() {
        messageList.setItemClickListener(new EaseChatMessageList.MessageListItemClickListener() {

            @Override
            public void onUserAvatarClick(String username) {}

            @Override
            public void onResendClick(final EMMessage message) {
                resendMessage(message);
            }

            @Override
            public void onBubbleLongClick(EMMessage message) {}

            @Override
            public boolean onBubbleClick(EMMessage message) {
                return false;
            }
        });
    }

    private void hideKeyboard() {
        if (getActivity().getWindow()
                .getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(
                        getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void showSoftKeyBoard() {
        inputMethodManager.toggleSoftInputFromWindow(
                inputTextView.getWindowToken(), 0,
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected void setRefreshLayoutListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (listView.getFirstVisiblePosition() == 0 && !isLoading && haveMoreData) {
                            List<EMMessage> messages;
                            try {
                                messages = conversation.loadMoreMsgFromDB(
                                        messageList.getItem(0).getMsgId(),
                                        pageSize);
                            } catch (Exception e1) {
                                swipeRefreshLayout.setRefreshing(false);
                                return;
                            }
                            if (messages.size() > 0) {
                                messageList.refreshSeekTo(messages.size() - 1);
                                if (messages.size() != pageSize) {
                                    haveMoreData = false;
                                }
                            } else {
                                haveMoreData = false;
                            }

                            isLoading = false;

                        } else {
                            Toast.makeText(getActivity(), "没有更多消息了", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 600);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMessageListInited)
            messageList.refresh();
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        CustomerServiceManager.getInstance().addActivity(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
        CustomerServiceManager.getInstance().removeActivity(getActivity());
    }

    EMMessageListener msgListener = new EmptyEMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {

            for (EMMessage message : messages) {
                String username = null;
                // 群组消息
                if (message.getChatType() == EMMessage.ChatType.GroupChat
                        || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    // 单聊消息
                    username = message.getFrom();
                }

                // 如果是当前会话的消息，刷新聊天页面
                if (username.equals(toChatUsername)) {
                    messageList.refreshSelectLast();
                    // 声音和震动提示有新消息
                    CustomerServiceManager.getInstance().getNotifier().viberateAndPlayTone(message);
                } else {
                    // 如果消息不是和当前聊天ID的消息
                    CustomerServiceManager.getInstance().getNotifier().onNewMsg(message);
                }
            }
        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> messages) {
            if (isMessageListInited) {
                messageList.refresh();
            }
        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> message) {
            if (isMessageListInited) {
                messageList.refresh();
            }
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            if (isMessageListInited) {
                messageList.refresh();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send_btn) {
            if (!TextUtils.isEmpty(inputTextView.getText())) {
                sendTextMessage(inputTextView.getText().toString());
                inputTextView.setText("");
            }
        } else if (v.getId() == R.id.input_text) {
            imageBoard.setVisibility(View.GONE);
            choosePicView.setImageResource(R.drawable.cs_selector_camera);
        } else if (v.getId() == R.id.choose_pic) {
            switchBoard();
        } else if (v.getId() == R.id.navbar_left) {
            getActivity().finish();
        } else if (v.getId() == R.id.navbar_right) {
            callCustomerService();
        } else if (v.getId() == R.id.btn_take_photo) {
            // TODO(mwang): 16/8/2
        } else if (v.getId() == R.id.btn_pick_photo) {
            // TODO(mwang): 16/8/2
        }
    }

    private void switchBoard() {
        if (imageBoard.getVisibility() == View.VISIBLE) {
            choosePicView.setImageResource(R.drawable.cs_selector_camera);
            showSoftKeyBoard();
            imageBoard.setVisibility(View.GONE);
        } else {
            choosePicView.setImageResource(R.drawable.cs_keyboard);
            hideKeyboard();
            imageBoard.postDelayed(new Runnable() {
                @Override
                public void run() {
                    imageBoard.setVisibility(View.VISIBLE);
                }
            }, 300);
        }
    }

    private void callCustomerService() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:4000630100"));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "没有安装打电话的应用", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendRobotMessage(String content, String menuId) {
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        if (!TextUtils.isEmpty(menuId)) {
            JSONObject msgTypeJson = new JSONObject();
            try {
                JSONObject choiceJson = new JSONObject();
                choiceJson.put("menuid", menuId);
                msgTypeJson.put("choice", choiceJson);
            } catch (Exception e) {}
            message.setAttribute("msgtype", msgTypeJson);
        }
        sendMessage(message);
    }

    // 发送消息方法
    // ==========================================================================
    protected void sendTextMessage(String content) {
        System.out.println(content);
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        sendMessage(message);
    }

    protected void sendImageMessage(String imagePath) {
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendMessage(message);
    }

    protected void sendFileMessage(String filePath) {
        EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
        sendMessage(message);
    }

    protected void sendMessage(EMMessage message) {
        if (message == null) {
            return;
        }
        // todo 设置扩展属性 售前/售后等技能组
        // 如果是群聊，设置chattype,默认是单聊
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            message.setChatType(EMMessage.ChatType.GroupChat);
        } else if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }
        // 发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        // 刷新ui
        if (isMessageListInited) {
            messageList.refreshSelectLast();
        }
    }

    public void resendMessage(EMMessage message) {
        message.setStatus(EMMessage.Status.CREATE);
        EMClient.getInstance().chatManager().sendMessage(message);
        messageList.refresh();
    }
}
