package meng.customerservice.easeui.chatrow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;

import java.io.File;

import meng.customerservice.R;
import meng.customerservice.easeui.EaseShowBigImageActivity;
import meng.customerservice.easeui.model.EaseImageCache;
import meng.customerservice.easeui.utils.EaseCommonUtils;
import meng.customerservice.easeui.utils.EaseImageUtils;

public class EaseChatRowImage extends EaseChatRowFile {

    private  static final int THUMBNAIL_DEFAULT_SIZE = 500;
    private static final int THUMBNAIL_MIN_SIZE = 300;
    private ImageView imageView;
    private EMImageMessageBody imageMessageBody;
    private View imageBlock;

    public EaseChatRowImage(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE
                ? R.layout.ease_row_received_picture : R.layout.ease_row_sent_picture, this);
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
        imageBlock = findViewById(R.id.image_block);
    }

    @Override
    protected void onSetUpView() {
        imageMessageBody = (EMImageMessageBody) message.getBody();
        // 接收方向的消息
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (imageMessageBody
                    .thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING
                    ||
                    imageMessageBody
                            .thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
                imageView.setImageResource(R.drawable.ease_default_image);
                setMessageReceiveCallback();
            } else {
                progressBar.setVisibility(View.GONE);
                percentageView.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.ease_default_image);
                String thumbPath = imageMessageBody.thumbnailLocalPath();
                showImageView(thumbPath, imageView, imageMessageBody.getLocalUrl(), message);
            }
            return;
        }

        String filePath = imageMessageBody.getLocalUrl();
        String thumbPath = EaseImageUtils.getThumbnailImagePath(imageMessageBody.getLocalUrl());
        showImageView(thumbPath, imageView, filePath, message);
        handleSendMessage();
    }

    @Override
    protected void onUpdateView() {
        super.onUpdateView();
    }

    @Override
    protected void onBubbleClick() {
        Intent intent = new Intent(context, EaseShowBigImageActivity.class);
        File file = new File(imageMessageBody.getLocalUrl());
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            intent.putExtra("uri", uri);
        } else {
            // The local full size pic does not exist yet.
            // ShowBigImage needs to download it from the server
            // first
            intent.putExtra("secret", imageMessageBody.getSecret());
            intent.putExtra("remotepath", imageMessageBody.getRemoteUrl());
            intent.putExtra("localUrl", imageMessageBody.getLocalUrl());
        }
        if (message != null && message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked()
                && message.getChatType() == EMMessage.ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(),
                        message.getMsgId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        context.startActivity(intent);
    }

    private void showImageView(final String thumbnailPath, final ImageView imageView,
            final String localFullSizePath, final EMMessage message) {
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = EaseImageCache.getInstance().get(thumbnailPath);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            updateImageBlockRatio(bitmap.getWidth(), bitmap.getHeight());
            imageView.setImageBitmap(bitmap);
        } else {
            new AsyncTask<Object, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Object... args) {
                    File file = new File(thumbnailPath);
                    if (file.exists()) {
                        return EaseImageUtils.decodeScaleImage(thumbnailPath,
                                THUMBNAIL_DEFAULT_SIZE, THUMBNAIL_DEFAULT_SIZE);
                    } else if (new File(imageMessageBody.thumbnailLocalPath()).exists()) {
                        return EaseImageUtils.decodeScaleImage(
                                imageMessageBody.thumbnailLocalPath(), THUMBNAIL_DEFAULT_SIZE,
                                THUMBNAIL_DEFAULT_SIZE);
                    } else if (message.direct() == EMMessage.Direct.SEND
                            && localFullSizePath != null && new File(localFullSizePath).exists()) {
                        return EaseImageUtils.decodeScaleImage(localFullSizePath,
                                THUMBNAIL_DEFAULT_SIZE, THUMBNAIL_DEFAULT_SIZE);
                    }
                    return null;
                }

                protected void onPostExecute(Bitmap image) {
                    if (image != null) {
                        updateImageBlockRatio(image.getWidth(), image.getHeight());
                        imageView.setImageBitmap(image);
                        EaseImageCache.getInstance().put(thumbnailPath, image);
                    } else {
                        if (message.status() == EMMessage.Status.FAIL &&
                                EaseCommonUtils.isNetWorkConnected(activity)) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    EMClient.getInstance().chatManager()
                                            .downloadThumbnail(message);
                                }
                            }).start();
                        }
                    }
                }
            }.execute();
        }
    }

    private int getScreenWidth() {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    private void updateImageBlockRatio(int bmpWidth, int bmpHeight) {
        ViewGroup.LayoutParams params = imageBlock.getLayoutParams();
        if (params == null) {
            return;
        }
        int width = (int) Math.min(getScreenWidth() * 0.382f, bmpWidth);
        if (bmpWidth < bmpHeight) {
            width = (int) (width * 0.8);
        }
        width = width < THUMBNAIL_MIN_SIZE ? THUMBNAIL_MIN_SIZE : width;
        if (bmpWidth <= 0) {
            params.height = width;
        } else {
            params.height = bmpHeight * width / bmpWidth;
        }
        params.width = width;
        imageBlock.setLayoutParams(params);
    }

}
