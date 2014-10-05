package com.example.manitto;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
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


public class Stage3_manitto_Activity extends Activity implements OnClickListener {
	
	Button btn;
	Button send;
	TextView scoreText;
	TextView timeText;
	TextView chatText;
	EditText inputText;
	ThreadStage1Chat chat;
	MediaPlayer voice_player;
	
	boolean downVoice_clear = false;
	boolean playVoice_start = false;
	boolean playVoice_end = false;
	
	int score = 0;
	
	String myNickname ;
	String dateNickname;
	String path;
	HttpDownLoad downloader;
	
	private Timer msgTimer = new Timer();
	private Timer scoreTimer = new Timer();
	private CountDownTimer gameTimer;
	
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
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == 0){
				
				Toast.makeText(getApplicationContext(), "download failed!! Voice is not registered!!", Toast.LENGTH_SHORT).show();
			}
			
			else if(msg.what == 1){
				
				Toast.makeText(getApplicationContext(), "download success", Toast.LENGTH_SHORT).show();
			}
		}
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
		
		
		
		
		//user info extract
		Intent myIntent = getIntent();
		Bundle myBundle = myIntent.getExtras();
		final String[] userData = myBundle.getStringArray("userInfo");
	    myNickname = userData[0];
		dateNickname = userData[1];
		String score = userData[2];
		
		chat = new ThreadStage1Chat(myNickname,dateNickname,messageHandler);
		//chat start
		chat.start();
		
		
		
		//game start popup
		this.game_start();
		
		//score Setting
		chat.sendScore(Integer.parseInt(score));
		
		//message box initializing
		String intro_message = this.myNickname + " is entered!! \n" + this.dateNickname + " is entered!! \n";
		chatText.setText(intro_message);
		//score box initializing
		scoreText.setText("Score : 0");
		
		//message Listener start
		TimerTask msgListener = new TimerTask(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				chat.receive();
			}
			
		};
		
		msgTimer.schedule(msgListener,0,10);
		
		//game condition Listener start
		TimerTask gameListener = new TimerTask(){
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(downloader != null && voice_player != null){
					downVoice_clear = downloader.getFileResult();
					playVoice_end = confirmVoice();
				}
				
				if(downVoice_clear == true && playVoice_start == true && playVoice_end == false){
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
		// TODO Auto-generated method stubma
		super.onCreateContextMenu(menu, v, menuInfo);
		
		if(v==btn){
			
			menu.setHeaderTitle("SubTitle");
			menu.add(0,1,0, "Misson confirm");
			menu.add(0,2,0, "Buy Voice");
			menu.add(0,3,0, "Play Voice");
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
			AlertDialog.Builder voice_confirm = new AlertDialog.Builder(this);
			voice_confirm.setMessage("Do you really want to buy??");
			voice_confirm.setPositiveButton("Do it!!", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(chat.getScore() >= 30){
						
						chat.sendScore(-30);
						File file = Environment.getExternalStorageDirectory();
						path = file.getAbsolutePath() + "/" + myNickname + ".3gp";
						downloader = new HttpDownLoad(path,myNickname);
						downloader.setHandler(downloadHandler);
						downloader.execute();
						
					}
					
					else{
						
						Toast.makeText(getApplicationContext(), "You have to get 100 score!!", Toast.LENGTH_SHORT).show();
					}
					
				}
			});
			voice_confirm.setNegativeButton("No", null);
			voice_confirm.show();
			return true;
		case 3:
			if(downloader.getFileResult()){
				
				play_record();
				playVoice_start = true;
			}
			
			else{
				
				Toast.makeText(getApplicationContext(), "You have to buy the Voice first!!", Toast.LENGTH_SHORT).show();
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
		game_start_popup.setTitle("Stage 3 Mission");
		game_start_popup.setMessage("You've already been here Stage 3!! You are Puller.\n Stage 3 Misson is to have 200 intimacy Point to get the Voice.\n " +
									"You can get a point by having a Conversation with Pusher.\n if you want to success the mission in time, you have to ask Pusher give some points and upload her/his Voice");
		
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
						Stage3_manitto_Activity.this.finish();
						chat.socketClose();
						
					}
					
				};
				
				gameTimer.start();
			}
		});
		
		game_start_popup.show();
		
	}
	
	public void game_end(){
		
		chat.send_voiceEnd("v`end");
		AlertDialog.Builder game_end_popup = new AlertDialog.Builder(this);
		game_end_popup.setMessage("Stage3 Clear!!");
		game_end_popup.setPositiveButton("Go next Stage!!", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				Intent myIntent = new Intent(getApplicationContext(), Stage4_manitto_Activity.class);
				Bundle myBundle = new Bundle();
				String score = ""+chat.getScore();
				String[] userData = {myNickname, dateNickname,score};
				myBundle.putStringArray("userInfo",userData);
				myIntent.putExtras(myBundle);
				chat.socketClose();
				startActivity(myIntent);
			}
		});
		game_end_popup.show();
	}
	
	public void play_record(){
		
		File file = Environment.getExternalStorageDirectory();
		if(path != null){
			File mediaFile = new File(path);
		
			Toast.makeText(getApplicationContext(), "Enjoy the Voice!!", Toast.LENGTH_SHORT).show();
			Uri uriOfMedaiFile = Uri.fromFile(mediaFile);
		
			voice_player = MediaPlayer.create(getApplicationContext(),uriOfMedaiFile);
		
			voice_player.start();
	
		}
		else{
			
			Toast.makeText(getApplicationContext(), "play file don't exists", Toast.LENGTH_SHORT).show();
		}
		
		
		
		
	}
	
	public boolean confirmVoice(){
		
		return voice_player.isPlaying();
		
	}
	
	public void stopTimer(){
		
		gameTimer.cancel();
		
		msgTimer.cancel();
		msgTimer.purge();
		
		scoreTimer.cancel();
		scoreTimer.purge();
	}
}