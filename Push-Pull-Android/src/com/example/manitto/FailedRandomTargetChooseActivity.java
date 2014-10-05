package com.example.manitto;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FailedRandomTargetChooseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_failed_random_choose);
		
		System.gc();
		
		Intent mIntent = getIntent();
		Bundle bundle = mIntent.getExtras();
		String [] userDataArr = bundle.getStringArray("userInfo");

		final String nickname = userDataArr[1];
		final String sex = userDataArr[0];
		
		Button retry = (Button)findViewById(R.id.retry);
		retry.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(getApplicationContext(), RandomTargetChooseActivity.class);
				Bundle userData = new Bundle();
				String [] userArr = {sex, nickname};
				userData.putStringArray("userInfo", userArr);
				myIntent.putExtras(userData);
				startActivity(myIntent);
				FailedRandomTargetChooseActivity.this.finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
