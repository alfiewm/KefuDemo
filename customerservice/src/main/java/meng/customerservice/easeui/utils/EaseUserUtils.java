package meng.customerservice.easeui.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.squareup.picasso.Picasso;

import meng.customerservice.R;
import meng.customerservice.utils.CircleTransform;

public class EaseUserUtils {
    public static boolean isSelf(String username) {
        return username.equals(EMClient.getInstance().getCurrentUser());
    }

    public static void setUserAvatar(Context context, String username, ImageView imageView) {
        if (!isSelf(username)) {
            Picasso.with(context).load(R.drawable.cs_my_avatar_default_round).into(imageView);
        } else {
            Picasso.with(context)
                    // TODO(mwang): 16/8/1 get from host project 
                    .load("http://thesource.com/wp-content/uploads/2015/02/Pablo_Picasso1.jpg")
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
