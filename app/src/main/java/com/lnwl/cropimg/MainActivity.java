package com.lnwl.cropimg;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();

        cropImage3();
    }

    private void cropImage3() {
        System.out.println("**********************************");
        System.out.println("**********************************");
        System.out.println("**********************************");

        final CropImageView mCropImage = (CropImageView) findViewById(R.id.cropImg);
        final LinearLayout layoutResult = (LinearLayout) findViewById(R.id.layoutResult);
        mCropImage.post(new Runnable() {
            @Override
            public void run() {
                System.out.println(mCropImage.getHeight());
            }
        });
        layoutResult.post(new Runnable() {
            @Override
            public void run() {
                System.out.println(layoutResult.getHeight());
            }
        });

        int viewHegiht = ActivityUtil.dip2px(getApplicationContext(), 300);

        System.out.println(viewHegiht);

        mCropImage.setDrawable(getResources().getDrawable(R.drawable.precrop), 150, 150, viewHegiht);

        System.out.println("**********************************");
        System.out.println("**********************************");
        System.out.println("**********************************");
    }
}
