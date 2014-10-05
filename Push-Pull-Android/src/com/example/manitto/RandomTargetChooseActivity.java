package com.example.manitto;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class RandomTargetChooseActivity extends Activity {
	private Timer timer = new Timer();
	private Timer stopTimer = new Timer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.enableDefaults();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_random_target_choose);
		
		System.gc();
	    
		//extract user data
		Intent mIntent = getIntent();
		Bundle bundle = mIntent.getExtras();
		final String [] userDataArr = bundle.getStringArray("userInfo");
		final String nickname = userDataArr[1];
		final String sex = userDataArr[0];
		
		//create thread
		final ThreadRandomTargetSelect thread = new ThreadRandomTargetSelect(nickname,sex);
		thread.start();
		
		TimerTask stop = new TimerTask(){
			@Override
			public void run(){
				System.out.println("find failed");
				Intent myIntent = new Intent(getApplicationContext(), FailedRandomTargetChooseActivity.class);
				Bundle userData = new Bundle();
				userData.putStringArray("userInfo", userDataArr);
				myIntent.putExtras(userData);
				startActivity(myIntent);
				RandomTargetChooseActivity.this.finish();
				stopTimer();
				thread.socketClose();
			}
		};
		
		TimerTask serverCheck = new TimerTask(){
			@Override
			public void run(){
				// TODO Auto-generated method stub
				System.out.println("server check");
				
				thread.send();
				thread.receive();
				
				if(thread.getStat()=='s')
				{
					System.out.println("find success");
					Intent myIntent = new Intent(getApplicationContext(), MiniChatActivity.class);
					Bundle usersData = new Bundle();
					String [] usersNickname = {sex, thread.getMyNickname(), thread.getDateNickname()};
					usersData.putStringArray("userName", usersNickname);
					myIntent.putExtras(usersData);
					startActivity(myIntent);
					RandomTargetChooseActivity.this.finish();
					stopTimer();
					thread.socketClose();
				}
				else
				{
					System.out.println("sever msg : " + thread.getStat());
				}
			}

		};
		
		stopTimer.schedule(stop, 60000);
		timer.schedule(serverCheck, 3000, 3000);
	}
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
		if(stopTimer != null){
			stopTimer.cancel();
			stopTimer.purge();
			stopTimer = null;
		}
		System.gc();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
