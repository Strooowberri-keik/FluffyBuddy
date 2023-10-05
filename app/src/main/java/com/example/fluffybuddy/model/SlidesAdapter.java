package com.example.fluffybuddy.model;

import android.content.Context;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.example.fluffybuddy.R;

public class SlidesAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SlidesAdapter(Context context) {
        this.context = context;
    }
    int imagesArray [] = {
            R.drawable.slide_image_o,
            R.drawable.slide_image_t,
            R.drawable.slide_image_th
    };
    int headingArray [] = {
            R.string.heading_o,
            R.string.heading_t,
            R.string.heading_th
    };
    int descriptionArray [] = {
            R.string.description_o,
            R.string.description_t,
            R.string.description_th
    };
    @Override
    public int getCount() {
        return headingArray.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (ConstraintLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.sliding_layout, container, false);

        ImageView imageView = view.findViewById(R.id.slider_img);
        TextView heading = view.findViewById(R.id.heading);
        TextView description = view.findViewById(R.id.description);

        imageView.setImageResource(imagesArray[position]);
        heading.setText(headingArray[position]);
        description.setText(descriptionArray[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
      container.removeView((ConstraintLayout)object);
    }
}
