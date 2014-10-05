package com.example.manitto;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

public class ThreadStage1Chat extends ThreadManitto {


	private TextView chatView;
	private TextView scoreView;
	private String myNickname;
	private String dateNickname;
	private String chatMessage;
	private int score=0;
	private boolean serverResult=false;
	
	private final Handler handler;
	
	
	public void setScore(int score){
		
		this.score = score;
	}
	public int getScore(){
		
		return score;
	}
	
	public ThreadStage1Chat(String myNickname, String dateNickname, Handler handler) {
		// TODO Auto-generated constructor stub
		this.myNickname = myNickname;
		this.dateNickname = dateNickname;
		this.handler = handler;

	}
	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try{
		
			socket = new Socket("210.121.154.94",5000);
			outstream = new BufferedOutputStream(socket.getOutputStream());
			instream = new BufferedInputStream(socket.getInputStream());
			
			
			String userInfo = "u".concat(myNickname).concat("`").concat(dateNickname);
			byte[] data = userInfo.getBytes("EUC-KR");
			outstream.write(data);
			outstream.flush();
			
			
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void receive() {
		// TODO Auto-generated method stub
		try {
		
			byte[] contents = new byte[1024];
			int bytesread = 0;
			String data;
			Message message=null;
			if(instream != null){
				
				bytesread = instream.read(contents);
				data = new String(contents,0,bytesread);
				
				switch(data.charAt(0)){
				
				case 'm':
					data = data.substring(1);
					String[] msg = data.split("`");
					chatMessage = msg[1] + " : " + msg[0] + "\n";
					message = handler.obtainMessage(1,chatMessage);
					break;
					
				case 's':
					data = data.substring(1);
					score = Integer.parseInt(data);
					message = handler.obtainMessage(2, data);
					break;
					
				case 'v':
					message = handler.obtainMessage(3);
					break;
					
				}
				handler.sendMessage(message);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(String msg){
		
		try{
			String message = "m".concat(msg);
			byte[] data = message.getBytes("EUC-KR");
			
			outstream.write(data);
			outstream.flush();
			
		}
		catch(IOException e){
			
			e.printStackTrace();
		}
	}
	
	public void sendScore(int score){
		
		try{
			
			int temp = this.score;
			temp += score;
			String message = "s".concat("" + temp);
			
			byte[] data = message.getBytes("EUC-KR");
			
			outstream.write(data);
			outstream.flush();
		}
		catch(IOException e){
			
			e.printStackTrace();
		}
	}
	
	public void send_voiceEnd(String msg){
		
		try{
			byte[] data = msg.getBytes("EUC-KR");
			
			outstream.write(data);
			outstream.flush();
			
		}
		catch(IOException e){
			
			e.printStackTrace();
		}
	}
	
	public boolean confirmScore(){
		
		boolean flag = false;
		
		if(score >= 100){
			
			flag = true;
		}
		
		return flag;

				
	}
	
	
}