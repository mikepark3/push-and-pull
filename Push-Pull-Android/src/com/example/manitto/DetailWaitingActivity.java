package com.example.manitto;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class DetailWaitingActivity extends Activity {
	private Timer timer = new Timer();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.enableDefaults();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_waiting);
		
		System.gc();

		//extract users data
		Intent mIntent = getIntent();
		Bundle bundle = mIntent.getExtras();
		String [] userDataArr = bundle.getStringArray("userName");
		final String myNickname = userDataArr[0];
		final String dateNickname = userDataArr[1];
		
		//create thread
		final ThreadDetailWaiting thread = new ThreadDetailWaiting(myNickname, dateNickname);
		
		thread.start();
		
		//check task
		TimerTask check = new TimerTask(){
			@Override
			public void run(){
				thread.send();
				thread.receive();
				if(thread.getStat() == 'm'){
					System.out.println("success : manitto");
					Intent myIntent = new Intent(getApplicationContext(), Stage1_manitto_Activity.class);
					Bundle myBundle = new Bundle();
					String [] userData = {myNickname, dateNickname};
					myBundle.putStringArray("userInfo", userData);
					myIntent.putExtras(myBundle);
					startActivity(myIntent);
					DetailWaitingActivity.this.finish();
					stopTimer();
					thread.socketClose();
				}
				else if(thread.getStat() == 'r'){
					System.out.println("success : reciever");
					Intent myIntent = new Intent(getApplicationContext(), Stage1_reciever_Activity.class);
					Bundle myBundle = new Bundle();
					String [] userData = {myNickname, dateNickname};
					myBundle.putStringArray("userInfo", userData);
					myIntent.putExtras(myBundle);
					startActivity(myIntent);
					DetailWaitingActivity.this.finish();
					stopTimer();
					thread.socketClose();
				}
				else if(thread.getStat() == 'n'){
					System.out.println("failed : Not yet detail Registration");
				}
			}
		};
		timer.schedule(check, 3000, 3000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
		System.gc();
	}
}
