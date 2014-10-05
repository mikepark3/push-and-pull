package com.example.manitto;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class SimpleDataRegistrationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.enableDefaults();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_data_registration);

		System.gc();

		//instantiation of views
		final EditText inputField = (EditText)findViewById(R.id.nicknameInput);
		final RadioButton male = (RadioButton)findViewById(R.id.male);
		Button start = (Button)findViewById(R.id.send);

		//button event setup
		start.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//extract data from views
				String nickname = inputField.getText().toString();
				String sex = "f";
				if(male.isChecked()){
					sex="m";
				}
				
				//nickname is empty
				if(nickname.isEmpty()){
					System.out.println("failed : empty");
					Toast.makeText(getApplicationContext(), "nickname is empty.", Toast.LENGTH_LONG).show();
					
				}
				//nickname is not empty
				else{
					//create thread
					final ThreadSimpleDataReg thread = new ThreadSimpleDataReg(nickname, sex);
					thread.start();				

					Handler mHandler = new Handler();
					mHandler.postDelayed(new Runnable(){
						@Override
						public void run(){
							// TODO Auto-generated method stub
							thread.receive();
							if(thread.getStat() == 's'){
								System.out.println("success");
								Intent myIntent = new Intent(getApplicationContext(), RandomTargetChooseActivity.class);
								Bundle myBundle = new Bundle();
								String [] userData = {thread.getSex(), thread.getNickname()};
								myBundle.putStringArray("userInfo", userData);
								myIntent.putExtras(myBundle);
								startActivity(myIntent);
								SimpleDataRegistrationActivity.this.finish();
								thread.socketClose();
							}
							else if(thread.getStat() == 'f'){
								System.out.println("failed : DB");
								Toast.makeText(getApplicationContext(), "no available nickname. please retry.", Toast.LENGTH_LONG).show();
							}
							else if(thread.getStat() == 'n'){
								System.out.println("failed : disconnection");
								Toast.makeText(getApplicationContext(), "sever disconnection.", Toast.LENGTH_LONG).show();
							}
						}
					}, 3000);
				}
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