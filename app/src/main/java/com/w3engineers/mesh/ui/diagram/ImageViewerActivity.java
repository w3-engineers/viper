package com.w3engineers.mesh.ui.diagram;

/*
 *  ****************************************************************************
 *  * Created by : Md.Moniruzzaman on 03/08/2019 at 12:47 PM.
 *  * Email : moniruzzaman@w3engineers.com
 *  *
 *  * Purpose: To show the list of images
 *  *
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.ActivityImageViewBinding;


import java.util.ArrayList;
import java.util.List;


public class ImageViewerActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityImageViewBinding imageViewBinding;
    private List<String> imagePathList;
    private int mPosition;

    public static void startImageViewer(Context context, ArrayList<String> pathList, int index) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra("path_list", pathList);
        intent.putExtra("index", index);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resolveFromBundle();

        imageViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_view);
        imageViewBinding.viewPagerImage.setAdapter(new ImagePagerAdapter(this, imagePathList));
        imageViewBinding.viewPagerImage.setCurrentItem(mPosition);
        imageViewBinding.backButton.setOnClickListener(this);
        imageViewBinding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_button) {
            finish();
        }
    }


    private void resolveFromBundle() {
        Intent intent = getIntent();
        imagePathList = intent.getStringArrayListExtra("path_list");
        mPosition = intent.getIntExtra("index", 0);
    }

    private class ImagePagerAdapter extends PagerAdapter {
        private Context context;
        private List<String> pathList;

        ImagePagerAdapter(Context context, List<String> imagePath) {
            this.context = context;
            this.pathList = imagePath;
        }

        @Override
        public int getCount() {
            return pathList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            String[] filenameparts =  imagePathList.get(position).split("/");
            String name = filenameparts[filenameparts.length-1];
            String[] nameParts = name.split("\\.");
            setTitle(nameParts[0]);

            View view = LayoutInflater.from(context).inflate(R.layout.item_image_view, container, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
            Glide.with(context).load(pathList.get(position)).into(imageView);
            container.addView(view);
            return view;
        }
    }
}
