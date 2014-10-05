package com.example.manitto;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class ThreadMiniChat extends ThreadManitto
{
	private String myNickname;
	private String dateNickname;
	private TextView textView;
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == 1){
				textView.append(msg.obj.toString() + '\n');
			}
		}
	};
	
	public ThreadMiniChat(String myNick, String dateNick, TextView textView) {
		// TODO Auto-generated constructor stub
		this.myNickname = myNick;
		this.dateNickname = dateNick;
		this.textView = textView;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			socket = new Socket("210.121.154.99",4002);
			outstream = new BufferedOutputStream(socket.getOutputStream());
			instream = new BufferedInputStream(socket.getInputStream());

			//register socket in server, "r" means registration, "`" is separator.
			String str = "r".concat(myNickname).concat("`").concat(dateNickname);
			byte[] ref = str.getBytes("EUC-KR");

			outstream.write(ref);
			outstream.flush();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(String msg){
		try {
			//"m" means message
			String str = "m".concat(msg);
			byte[] ref = str.getBytes("EUC-KR");

			outstream.write(ref);
			outstream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void receive(){
		try {
			byte[] contents = new byte[1024];
			int bytesRead=0;

			String str;
			if(instream != null){
				bytesRead = instream.read(contents);
				str = new String(contents, 0, bytesRead);

				switch (str.charAt(0)){
				case 'm':
					String [] nickAndMsg=str.split("`");
					String nickname = nickAndMsg[0].substring(1);
					String msg=nickAndMsg[1];
					str =  nickname + ": " + msg;
					break;
				}
				Message message = handler.obtainMessage(1, str);
				handler.sendMessage(message);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}