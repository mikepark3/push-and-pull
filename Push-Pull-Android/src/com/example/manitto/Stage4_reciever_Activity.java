package com.example.manitto;
import java.util.Timer;
import java.util.TimerTask;


import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


public class Stage4_reciever_Activity extends Activity implements OnClickListener {
	
	Button btn;
	Button send;
	TextView scoreText;
	TextView timeText;
	TextView chatText;
	EditText inputText;
	ThreadStage1Chat chat;
	
	private int point_10_count=3;
	private int point_20_count=2;
	private int point_30_count=1;
	private String myNickname;
	private String dateNickname; 
	boolean stage_clear = false;
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
				
				else if(msg.what == 3){
					
					stage_clear = true;
				}
			}
		
		}
	};
	
	private Handler uploadHandler = new Handler(){
		public void handleMessage(Message msg) {
			
			if(msg.what == 0){
				
				Toast.makeText(getApplicationContext(), "upload failed", Toast.LENGTH_SHORT).show();			}
			
			else if(msg.what == 1){
				
				Toast.makeText(getApplicationContext(), "upload Success", Toast.LENGTH_SHORT).show();
			}
		};
		
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
		
		//Score setting
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
			SubMenu point = menu.addSubMenu("Give Points");
			point.add(0,2,0,"10 points");
			point.add(0,3,0,"20 points");
			point.add(0,4,0,"30 points");
			menu.add(0,5,0, "Give Ur Photo!!");
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		if(requestCode == 1){
			
			Uri imageUri = data.getData();
			String path = this.getRealPathFromURI(imageUri);
			
			ImageUpLoad uploader = new ImageUpLoad(path,dateNickname);
			uploader.setHandler(uploadHandler);
			uploader.execute();
			
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
			if(point_10_count != 0){
				point_10_count--;
				AlertDialog.Builder point_confirm_10 = new AlertDialog.Builder(this);
				point_confirm_10.setMessage("You give 10 Points!!");
				point_confirm_10.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						chat.sendScore(10);
					}
				});
				point_confirm_10.show();
				
				return true;
			}
			
			else {
				AlertDialog.Builder no_point_confirm = new AlertDialog.Builder(this);
				no_point_confirm.setMessage("You used all Points!!");
				no_point_confirm.setPositiveButton("OK", null);
				no_point_confirm.show();
				return true;
			}
			
		case 3:
			if(point_20_count != 0){
				point_20_count--;
				AlertDialog.Builder point_confirm_20 = new AlertDialog.Builder(this);
				point_confirm_20.setMessage("You give 20 Points!!");
				point_confirm_20.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						chat.sendScore(20);
					}
				});
				point_confirm_20.show();
				
				return true;
			}
			
			else{
				
				AlertDialog.Builder no_point_confirm = new AlertDialog.Builder(this);
				no_point_confirm.setMessage("You used all Points!!");
				no_point_confirm.setPositiveButton("OK", null);
				no_point_confirm.show();
				return true;
			}
			
		case 4:
			if(point_30_count != 0){
				point_30_count--;
				AlertDialog.Builder point_confirm_30 = new AlertDialog.Builder(this);
				point_confirm_30.setMessage("You give 30 Points!!");
				point_confirm_30.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						chat.sendScore(30);
					}
				});
				point_confirm_30.show();
				
				return true;
			}
			
			else{
				
				AlertDialog.Builder no_point_confirm = new AlertDialog.Builder(this);
				no_point_confirm.setMessage("You used all Points!!");
				no_point_confirm.setPositiveButton("OK", null);
				no_point_confirm.show();
				return true;
			}
			
		case 5:
			Intent myIntent = new Intent(Intent.ACTION_PICK);
			myIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
			startActivityForResult(myIntent, 1);
			return true;
		}
		return false;
		
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
		game_start_popup.setMessage("Now we start Final Stage!! You are Pusher.\n Stage 4 Misson is to have 100 intimacy Point.\n " +
				"You can give a point if you like Conversation with manitto.\n You have the Power in this Stage 1!" +
				"You can give Points for 10 times!" +
				"If you want to finish the game, having a conversation with Puller and give some Points to get 100 Intimacy Points!!\n" + "Don't forget to upload your attractive Photo!! because Puller wants it so bad!!\n"
				+"After this Stage has finished, you, two become real friends!! So good Luck!");
		
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
						//acitivity info
						
						startActivity(myIntent);
						chat.socketClose();
						Stage4_reciever_Activity.this.finish();
						
					}
					
				};
				
				gameTimer.start();
			}
		});
		
		game_start_popup.show();
		
	}
	
	
	public void game_end(){
		
		AlertDialog.Builder game_end_popup = new AlertDialog.Builder(this);
		game_end_popup.setMessage("Stage1 Clear!!");
		game_end_popup.setPositiveButton("Go Next Stage!!", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(getApplicationContext(), Stage3_reciever_Activity.class);
				Bundle myBundle = new Bundle();
				String score =""+chat.getScore();
				String [] userData = {myNickname, dateNickname,score};
				myBundle.putStringArray("userInfo", userData);
				myIntent.putExtras(myBundle);
				chat.socketClose();
				startActivity(myIntent);
				Stage4_reciever_Activity.this.finish();
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
	
	//get absolute Path from uri content
	public String getRealPathFromURI(Uri contentUri){
		
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
}
