package com.example.manitto;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class DetailDataRegistrationActivity extends Activity {
	private String Region="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.enableDefaults();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_data_registration);

		System.gc();
		
		//extract users data
		Intent mIntent = getIntent();
		Bundle bundle = mIntent.getExtras();
		String [] userDataArr = bundle.getStringArray("userName");
		final String myNickname = userDataArr[0];
		final String dateNickname = userDataArr[1];
		
		//instantiation of views
		final EditText id = (EditText)findViewById(R.id.id);
		final EditText pw = (EditText)findViewById(R.id.pw);
		final EditText name = (EditText)findViewById(R.id.name);
		final EditText age = (EditText)findViewById(R.id.age);
		final EditText career = (EditText)findViewById(R.id.career);
		final Spinner region = (Spinner)findViewById(R.id.region);
		final RadioButton b = (RadioButton)findViewById(R.id.B);
		final RadioButton o = (RadioButton)findViewById(R.id.O);
		final RadioButton ab = (RadioButton)findViewById(R.id.AB);
		Button submit = (Button)findViewById(R.id.submit);
		
		//EditText(id) setup
		id.setText(myNickname);
		id.setFocusable(false);
		id.setClickable(false);
		
		//spinner(region) setup
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.regionList, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        region.setAdapter(adapter);
        region.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				// TODO Auto-generated method stub
				Region = ""+parent.getItemAtPosition(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0){
				// TODO Auto-generated method stub
			}
        });
        
        //button setup
        submit.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		// TODO Auto-generated method stub
        		//extract data from views
        		String Pw = pw.getText().toString();
        		String Name = name.getText().toString();
        		String Age = age.getText().toString();
        		String Career = career.getText().toString();
        		String Type = "A";
        		if(b.isChecked()){
        			Type = "B";
        		}
        		else if(o.isChecked()){
        			Type = "O";
        		}
        		else if(ab.isChecked()){
        			Type = "AB";
        		}
        		
        		//data empty check
        		if(Pw.isEmpty() || Name.isEmpty() || Age.isEmpty() || Career.isEmpty() || Region.isEmpty() || Type.isEmpty()){
        			System.out.println("failed : empty");
					Toast.makeText(getApplicationContext(), "please fill the form and submit.", Toast.LENGTH_LONG).show();
        		}
        		else{
        			//create thread
        			final ThreadDetailDataReg thread = new ThreadDetailDataReg(myNickname, Pw, Name, Age, Career, Region, Type);
					thread.start();				

					Handler mHandler = new Handler();
					mHandler.postDelayed(new Runnable(){
						@Override
						public void run(){
							// TODO Auto-generated method stub
							thread.receive();
							if(thread.getStat() == 's'){
								System.out.println("success");
								Intent myIntent = new Intent(getApplicationContext(), DetailWaitingActivity.class);
								Bundle myBundle = new Bundle();
								String [] userData = {myNickname, dateNickname};
								myBundle.putStringArray("userName", userData);
								myIntent.putExtras(myBundle);
								startActivity(myIntent);
								DetailDataRegistrationActivity.this.finish();
								thread.socketClose();
							}
							else if(thread.getStat() == 'f'){
								System.out.println("failed : DB");
								Toast.makeText(getApplicationContext(), "registration failed. please retry.", Toast.LENGTH_LONG).show();
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