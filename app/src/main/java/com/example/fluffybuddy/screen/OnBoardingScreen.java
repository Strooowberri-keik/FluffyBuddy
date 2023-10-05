package com.example.fluffybuddy.screen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fluffybuddy.MainActivity;
import com.example.fluffybuddy.R;
import com.example.fluffybuddy.model.SlidesAdapter;

public class OnBoardingScreen extends AppCompatActivity {

    ViewPager viewPager;
    LinearLayout dotsLayout;

    Button btn;
    TextView[] dots;
    Animation animation;
    SlidesAdapter slidesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding_screen);

        viewPager =findViewById(R.id.slider);
        dotsLayout = findViewById(R.id.dots);

        btn = findViewById(R.id.get_started_btn);
        addDots(0);

        viewPager.addOnPageChangeListener(changeListener);

        slidesAdapter = new SlidesAdapter(this);
        viewPager.setAdapter(slidesAdapter);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OnBoardingScreen.this, MainActivity.class));
            }
        });
    }
    private void addDots(int position){
        dots = new TextView[3];
        dotsLayout.removeAllViews();
        for(int i= 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dotsLayout.addView(dots[i]);
        }

        if(dots.length > 0) {
            dots[position].setTextColor(getResources().getColor(R.color.fourth));
        }
    }
    ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }


        @Override
        public void onPageSelected(int position) {
            addDots(position);

            if(position == 0) {
                btn.setVisibility(View.INVISIBLE);
            }else if (position == 1) {
                btn.setVisibility(View.INVISIBLE);
            }else {
                animation = AnimationUtils.loadAnimation(OnBoardingScreen.this, R.anim.slide_animation);
                btn.setAnimation(animation);
                btn.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}