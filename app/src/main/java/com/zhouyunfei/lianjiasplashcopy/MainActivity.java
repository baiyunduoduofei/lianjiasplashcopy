package com.zhouyunfei.lianjiasplashcopy;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final HouseView houseView = findViewById(R.id.house_view);
        final ImageView imageView = findViewById(R.id.main_bg);
        final TextView noteTv = findViewById(R.id.note_tv);

        final ScaleAnimation animation = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f);
        animation.setDuration(6000);


        houseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteTv.setAlpha(0);
                houseView.startAnimation();
            }
        });

        houseView.setAnimatorListener(new HouseView.AnimationListener() {
            @Override
            public void onStart() {
                AnimatorSet animatorSetsuofang = new AnimatorSet();//组合动画
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.5f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.5f);
                animatorSetsuofang.setDuration(6000);
                animatorSetsuofang.setInterpolator(new DecelerateInterpolator());
                animatorSetsuofang.play(scaleX).with(scaleY);//两个动画同时开始
                animatorSetsuofang.start();
            }

            @Override
            public void onEnd() {
                ObjectAnimator animator = ObjectAnimator.ofFloat(noteTv, "alpha", 0f, 1f);
                animator.setDuration(3000);
                animator.start();
            }

            @Override
            public void onStateChange(HouseView.State state) {

            }
        });
        houseView.startAnimation();
    }
}
