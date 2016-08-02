package meng.customerservice.easeui.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.squareup.picasso.Picasso;

import meng.customerservice.CustomerServiceManager;
import meng.customerservice.R;
import meng.customerservice.utils.CircleTransform;

public class EaseUserUtils {
    public static boolean isSelf(String username) {
        return username.equals(EMClient.getInstance().getCurrentUser());
    }

    public static void setUserAvatar(Context context, String username, ImageView imageView) {
        if (!isSelf(username)) {
            Picasso.with(context).load(R.drawable.cs_customer_service).into(imageView);
        } else {
            Picasso.with(context)
                    .load(CustomerServiceManager.getInstance().getUserAvatarUrl())
                    .placeholder(R.drawable.cs_my_avatar_default_round)
                    .centerCrop()
                    .resize(100, 100)
                    .transform(new CircleTransform())
                    .into(imageView);
        }
    }

    public static void setUserNick(String username, TextView textView) {
        if (textView != null) {
            textView.setText(username);
        }
    }
}
