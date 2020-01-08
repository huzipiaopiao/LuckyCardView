package com.teaanddogdog.luckycardview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.teaanddogdog.luckycardviewhelper.LuckyCardViewHelper;

public class MainActivity extends AppCompatActivity implements LuckyCardViewHelper.LuckyCardViewHelperListener {

    private LuckyCardViewHelper mLuckyCardViewHelper1;
    private LuckyCardViewHelper mLuckyCardViewHelper2;
    private FrameLayout mFl;
    private ImageView mIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 直接在ImageView上使用LuckyCardViewHelper（不建议），这在安卓版本低于6.0的手机上，不能生效；
         * 建议在ImageView外面套一层FrameLayout，在FrameLayout使用LuckyCardViewHelper，
         */
        mIv = findViewById(R.id.iv);
        mLuckyCardViewHelper1 = new LuckyCardViewHelper().init(mIv, R.drawable.lucky_card_foreground, 60, this);
        Glide.with(this).load("http://a.cphotos.bdimg.com/timg?image&quality=100&size=b4000_4000&sec=1578454257&di=65c9b64d04e92312e89188a48fd5043f&src=http://game.gtimg.cn/images/nz/cp/a20180208kxbzl/gift.png").into(mIv);

        /**
         * 在FrameLayout使用LuckyCardViewHelper
         */
        mFl = findViewById(R.id.fl);
        mLuckyCardViewHelper2 = new LuckyCardViewHelper().init(mFl, R.drawable.lucky_card_foreground, 60, this);
    }

    public void reset(View view) {
        mLuckyCardViewHelper1.reset();
        mLuckyCardViewHelper2.reset();
    }

    @Override
    public void onHandUp(View view, int scrapedPercent) {
        Toast.makeText(this, "已刮" + scrapedPercent + " %", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onComplete(View view) {
        if (view == mFl) {
            Toast.makeText(this, "mFl完成", Toast.LENGTH_SHORT).show();
        } else if (view == mIv) {
            Toast.makeText(this, "mIv完成", Toast.LENGTH_SHORT).show();
        }
    }
}
