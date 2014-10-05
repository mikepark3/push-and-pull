package com.example.manitto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageActivity extends Activity implements OnClickListener{

	ImageView image;
	Button close;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		
		close = (Button)findViewById(R.id.image_close);
		image = (ImageView)findViewById(R.id.imageView1);
		Intent myIntent = getIntent();
		Bundle myBundle = myIntent.getExtras();
		String[] imageData = myBundle.getStringArray("imageInfo");
		String path = imageData[0];
		
		//BitmapFactory.Options options = new BitmapFactory.Options();
		//options.inJustDecodeBounds = true;
		//BitmapFactory.decodeFile(path,options);
		close.setOnClickListener(this);
		
		Bitmap bmp;
		
		bmp = BitmapFactory.decodeFile(path);
		
		BitmapDrawable dbmp = new BitmapDrawable(bmp);
		Drawable dr = (Drawable)dbmp;
		image.setImageDrawable(dr);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	
		if(v == close){
			
			ImageActivity.this.finish();
		}
	}
	
}
