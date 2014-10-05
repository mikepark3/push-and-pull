package com.example.manitto;

import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameOverActivity extends Activity implements OnClickListener{
	
	Button retry_bt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_over);
		
		retry_bt = (Button)findViewById(R.id.game_retry);
		
	}
	
	@Override
	public void onClick(View v) {
	// TODO Auto-generated method stub
		
		if(v == retry_bt){
			
		}
	}


}
