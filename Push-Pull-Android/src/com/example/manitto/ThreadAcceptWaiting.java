package com.example.manitto;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class ThreadAcceptWaiting extends ThreadManitto
{
	private String nickname,dateNickname;
	private char state;
	
	public ThreadAcceptWaiting(String nickname, String dateNickname) {
		// TODO Auto-generated constructor stub
		this.nickname = nickname;
		this.dateNickname = dateNickname;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try	{
			//connect
			socket = new Socket("210.121.154.99",4003);
			outstream = new BufferedOutputStream(socket.getOutputStream());
			instream = new BufferedInputStream(socket.getInputStream());

			//"ar" is header, "a" means "accept waiting". "r" means "registration"
			String data = "ar".concat(nickname).concat("`").concat(dateNickname);
			byte[] ref = data.getBytes("EUC-KR");
			
			if(outstream != null){
				outstream.write(ref);
				outstream.flush();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(){
		try {
			//"ac" is header, "a" means "accept waiting". "c" means "check datemate's state"
			String data = "ac";
			byte[] ref = data.getBytes("EUC-KR");
			
			if(outstream != null){
				outstream.write(ref);
				outstream.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void receive(){
		try {
			if(instream != null){
				this.state = (char)instream.read();	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public char getStat() {
		return state;
	}
}