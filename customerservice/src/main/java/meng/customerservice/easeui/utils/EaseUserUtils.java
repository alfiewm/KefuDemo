package meng.customerservice.easeui.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hyphenate.chat.EMClient;

import meng.customerservice.R;

public class EaseUserUtils {
    public static boolean isSelf(String username) {
        return username.equals(EMClient.getInstance().getCurrentUser());
    }

    public static void setUserAvatar(Context context, String username, ImageView imageView) {
        if (!isSelf(username)) {
            Glide.with(context).load(R.drawable.ease_default_image).into(imageView);
        } else {
            Glide.with(context)
                    .load("http://ytkgallery.yuanfudao.ws/android/ape/images/15645503038ed3e.jpg")
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ease_default_image)
                    .into(imageView);
        }
    }

    public static void setUserNick(String username, TextView textView) {
        if (textView != null) {
            textView.setText(username);
        }
    }
}
