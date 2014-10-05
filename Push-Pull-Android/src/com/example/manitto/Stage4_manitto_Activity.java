package com.example.manitto;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;



import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


public class Stage4_manitto_Activity extends Activity implements OnClickListener {
	
	Button btn;
	Button send;
	TextView scoreText;
	TextView timeText;
	TextView chatText;
	EditText inputText;
	ThreadStage1Chat chat;
	boolean stage_clear = false;
	
	String path;
	
	private Timer msgTimer = new Timer();
	private Timer scoreTimer = new Timer();
	private CountDownTimer gameTimer;
	private String myNickname;
	private String dateNickname;
	
	private Handler game_end_handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == 1){
				game_end();
				
				
			}
		}
	};
	
	private Handler downloadHandler = new Handler(){
		public void handleMessage(Message msg) {
			
			if(msg.what == 0){
				
				Toast.makeText(getApplicationContext(), "download failed", Toast.LENGTH_SHORT).show();			}
			
			else if(msg.what == 1){
				
				Toast.makeText(getApplicationContext(), "download Success", Toast.LENGTH_SHORT).show();
			}
		};
		
	};
	
	private final Handler messageHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			if(msg != null){
				
				if(msg.what == 1){
					
					chatText.append(msg.obj.toString());
				}
				
				else if(msg.what == 2){
					scoreText.setText("Score : " + msg.obj.toString());
					
				}
			}
		
		}
	};
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stage1_chat);
	
		
		btn = (Button)findViewById(R.id.plusBt);
		scoreText = (TextView)findViewById(R.id.score);
		timeText = (TextView)findViewById(R.id.time);
		chatText = (TextView)findViewById(R.id.chat);
		inputText = (EditText)findViewById(R.id.input);
		send = (Button)findViewById(R.id.send);
		
		send.setOnClickListener(this);
		
		registerForContextMenu(btn);
		
		scoreText.setText("Score : 0");
		
		//user info extract
		Intent myIntent = getIntent();
		Bundle myBundle = myIntent.getExtras();
		final String[] userData = myBundle.getStringArray("userInfo");
		myNickname = userData[0];
		dateNickname = userData[1];
		String score = userData[2];
		
		chat = new ThreadStage1Chat(myNickname,dateNickname,messageHandler);
		
		chat.start();
		
		
		this.game_start();
		
		
		//Socre Setting
		chat.sendScore(Integer.parseInt(score));
				
		TimerTask msgListener = new TimerTask(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				chat.receive();
			}
			
		};
		
		msgTimer.schedule(msgListener,0,10);
		
		TimerTask gameListener = new TimerTask(){
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				if(stage_clear == true){
					stopTimer();
					Message message = game_end_handler.obtainMessage(1);
					game_end_handler.sendMessage(message);
									
				}
			}
		};
	scoreTimer.schedule(gameListener,0,10);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		
		if(v==btn){
			
			menu.setHeaderTitle("SubTitle");
			menu.add(0,1,0, "Misson confirm");
			menu.add(0,2,0, "Buy Photo");
			menu.add(0,3,0, "Enjoy Photo");
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		//requestCode is a code when call StartActivityForResult(Intent,requestCode); if Called Activity finishes, it returns data and request Code. but if user presses back Button,
		//request Code is RESULT_CANCELED. so it has to be coded about that situation.
		if(requestCode == 1){
			stage_clear = true;
		}
		
		else if(requestCode == RESULT_CANCELED){
			
			stage_clear = true;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		
		case 1:
			AlertDialog.Builder mission_confirm = new AlertDialog.Builder(this);
			mission_confirm.setTitle("Mission Confirm");
			mission_confirm.setMessage("Get 100 Intimacy Point!!");
			mission_confirm.setPositiveButton("I got it!", null);
			mission_confirm.show();
			return true;
		
		case 2:
			AlertDialog.Builder buy_photo = new AlertDialog.Builder(this);
			buy_photo.setMessage("Do you really want to buy the photo?");
			buy_photo.setPositiveButton("Do it!!", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(chat.getScore() >= 30){
						File file = Environment.getExternalStorageDirectory();
						path = file.getAbsolutePath() + "/" + myNickname + ".jpg";
						ImageDownLoad downloader = new ImageDownLoad(path,myNickname);
						downloader.setHandler(downloadHandler);
						downloader.execute();
						
					}
					
					else{
						
						Toast.makeText(getApplicationContext(), "You have to get 100 Score!!",Toast.LENGTH_SHORT ).show();
						
					}
					
				}
			});
			
			buy_photo.setNegativeButton("No, later", null);
			buy_photo.show();
			return true;
			
		case 3:
			if(path != null){
				Intent myIntent = new Intent(getApplicationContext(), ImageActivity.class);
				Bundle myBundle = new Bundle();
				String [] imageData = {path};
				myBundle.putStringArray("imageInfo", imageData);
				myIntent.putExtras(myBundle);
				startActivityForResult(myIntent,1);
				return true;
			}
			
			else{
				
				Toast.makeText(getApplicationContext(), "You have to buy Photo First!!", Toast.LENGTH_SHORT).show();
				return true;
			}
			
		default :
			return true;
		
		}
		
	}
	

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if(v == send){
			
			chat.send(inputText.getText().toString());
			inputText.setText("");
			
		}
	}
	
	public void game_start(){
		
		AlertDialog.Builder game_start_popup = new AlertDialog.Builder(this);
		game_start_popup.setTitle("Stage 4 Mission");
		game_start_popup.setMessage("We Start Final Stage!! You are Puller.\n Stage 4 Misson is to have 100 intimacy Point to get his/her Photo.\n " + "Wow, Finally you can see Pusher's face!!\n" +
									"You can get a point by having a Conversation with Pusher.\n if you want to success the mission in time, you have to ask reciever give some points and upload his/her Photo");
		
		game_start_popup.setPositiveButton("Start!!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			
			
				gameTimer = new CountDownTimer(121000, 1000){
					
					@Override
					public void onTick(long millisUntilFinished) {
						// TODO Auto-generated method stub
						timeText.setText("Time : " + millisUntilFinished / 1000);
					}
					
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						timeText.setText("Time : 0");
						Intent myIntent = new Intent(getApplicationContext(),GameOverActivity.class);
						startActivity(myIntent);
						chat.socketClose();
						Stage4_manitto_Activity.this.finish();
						
					}
					
				};
				
				gameTimer.start();
			}
		});
		
		game_start_popup.show();
		
	}
	
	public void game_end(){
		
		//receiver's Game end statement
		chat.send_voiceEnd("v`end");
		AlertDialog.Builder game_end_popup = new AlertDialog.Builder(this);
		game_end_popup.setMessage("Game Clear!!");
		game_end_popup.setPositiveButton("Congratulations!!", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(getApplicationContext(), GameOverActivity.class);
				Bundle myBundle = new Bundle();
				String score = ""+chat.getScore();
				String [] userData = {myNickname, dateNickname,score};
				myBundle.putStringArray("userInfo", userData);
				myIntent.putExtras(myBundle);
				chat.socketClose();
				startActivity(myIntent);
				Stage4_manitto_Activity.this.finish();
			}
		});
	
		game_end_popup.show();
	}
	
	public void stopTimer(){
		scoreTimer.cancel();
		scoreTimer.purge();
		gameTimer.cancel();
		msgTimer.cancel();
		msgTimer.purge();
	}

}