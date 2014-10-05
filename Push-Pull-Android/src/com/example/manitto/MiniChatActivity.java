
package com.example.manitto;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

public class MiniChatActivity extends Activity {
	private Timer msgTimer = new Timer();
	private Timer checktimer = new Timer();
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == 1){
				String sex = ""+msg.obj.toString().charAt(0);
				String nickname = msg.obj.toString().substring(1);
				alertRefuseDialog(sex, nickname);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.enableDefaults();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mini_chat);

		System.gc();

		//extract users data
		Intent mIntent = getIntent();
		Bundle bundle = mIntent.getExtras();
		String [] userDataArr = bundle.getStringArray("userName");
		final String mySex = userDataArr[0];
		final String myNickname = userDataArr[1];
		final String dateNickname = userDataArr[2];

		//instantiation of views
		TextView textView = (TextView)findViewById(R.id.textView);
		final EditText inputField = (EditText)findViewById(R.id.inputField);
		Button btnSend = (Button)findViewById(R.id.send);
		final TextView time = (TextView)findViewById(R.id.time);

		//notice
		textView.append(myNickname + " is entered." + "\n");
		textView.append(dateNickname + " is entered." + "\n");

		//create thread
		final ThreadMiniChat thread = new ThreadMiniChat(myNickname, dateNickname, textView);
		thread.start();

		final TimerTask msgListener = new TimerTask(){
			@Override
			public void run(){
				thread.receive();
			}
		};
		msgTimer.schedule(msgListener, 0, 10);
		new CountDownTimer(10000, 1000) {
			public void onTick(long millisUntilFinished) {
				time.setText("" + millisUntilFinished/1000);
			}

			public void onFinish() {
				stopMsgTimer();
				time.setText("0");
				if(msgListener != null){
					msgListener.cancel();
				}
				alertDialog(mySex, myNickname, dateNickname);
				thread.socketClose();
			}
		}.start();

		btnSend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				// TODO Auto-generated method stub
				if (inputField.getText().toString() == ""){
					return;
				}
				else {
					String text = inputField.getText().toString();
					inputField.setText("");
					thread.send(text);
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
	public void stopMsgTimer(){
		if(msgTimer != null){
			msgTimer.cancel();
			msgTimer.purge();
			msgTimer = null;
		}
	}
	public void stopCheckTimer(){
		if(checktimer != null){
			checktimer.cancel();
			checktimer.purge();
			checktimer = null;
		}
	}
	public void alertDialog(final String mySex, final String myNickname, final String dateNickname){
		AlertDialog.Builder popupMsg = new AlertDialog.Builder(MiniChatActivity.this);
		popupMsg.setTitle("Do you want to continue the chat?");
		popupMsg.setMessage(
				"If you want to carry on a conversation, press 'Accept' button and then register as a member.\n" +
				"(Once you register, you don't have to do it again. Just press 'Accept' button.)\n" +
				"But if you want to change partners, press 'Refuse' button and then return to the screen that find target.");
		
		popupMsg.setPositiveButton("Accept", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				final ThreadAcceptWaiting thread = new ThreadAcceptWaiting(myNickname, dateNickname);
				thread.start();
				
				AlertDialog.Builder popupMsg = new AlertDialog.Builder(MiniChatActivity.this);
				popupMsg.setTitle("Please Wait...");
				popupMsg.setMessage("Your mate is not yet accept. Wait a few seconds.\n");
				popupMsg.show();
				//create thread
				
				//check task
				TimerTask check = new TimerTask(){
					@Override
					public void run(){
						thread.send();
						thread.receive();
						if(thread.getStat() == 'f'){
							System.out.println("failed : refuse");
							handler.sendMessage(handler.obtainMessage(1, mySex.concat(myNickname)));
							thread.socketClose();
						}
						else if(thread.getStat() == 's'){
							Intent myIntent = new Intent(getApplicationContext(), DetailDataRegistrationActivity.class);
							Bundle myBundle = new Bundle();
							String [] userData = {myNickname, dateNickname};
							myBundle.putStringArray("userName", userData);
							myIntent.putExtras(myBundle);
							startActivity(myIntent);
							MiniChatActivity.this.finish();
							stopCheckTimer();
							thread.socketClose();
						}
						else if(thread.getStat() == 'd'){
							Intent myIntent = new Intent(getApplicationContext(), DetailWaitingActivity.class);
							Bundle myBundle = new Bundle();
							String [] userData = {myNickname, dateNickname};
							myBundle.putStringArray("userName", userData);
							myIntent.putExtras(myBundle);
							startActivity(myIntent);
							MiniChatActivity.this.finish();
							stopCheckTimer();
							thread.socketClose();
						}
						else if(thread.getStat() == 'n'){
							System.out.println("failed : Not yet accepted");
						}
					}
				};
				checktimer.schedule(check, 3000, 3000);
			}
		});
		popupMsg.setNegativeButton("Refuse", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(getApplicationContext(), RandomTargetChooseActivity.class);
				Bundle myBundle = new Bundle();
				String [] userData = {mySex, myNickname};
				myBundle.putStringArray("userInfo", userData);
				myIntent.putExtras(myBundle);
				startActivity(myIntent);
				MiniChatActivity.this.finish();
			}
		});
		popupMsg.show();
	}
	public void alertRefuseDialog(final String mySex, final String myNickname){
		AlertDialog.Builder popupMsg = new AlertDialog.Builder(MiniChatActivity.this);
		popupMsg.setTitle("refused");
		popupMsg.setMessage(
				"So sorry... Your mate refused you.\n" + "If you press the button, return to Find Random target page.");
		popupMsg.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub		
				Intent myIntent = new Intent(getApplicationContext(), RandomTargetChooseActivity.class);
				Bundle myBundle = new Bundle();
				String [] usersNickname = {mySex, myNickname};
				myBundle.putStringArray("userInfo", usersNickname);
				myIntent.putExtras(myBundle);
				startActivity(myIntent);
				MiniChatActivity.this.finish();
				stopCheckTimer();
			}
		});
		popupMsg.show();
	}
}
