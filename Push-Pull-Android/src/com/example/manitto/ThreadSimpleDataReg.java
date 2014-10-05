package com.example.manitto;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class ThreadSimpleDataReg extends ThreadManitto
{
	private String nickname;
	private String sex;
	private char state;
	
	public ThreadSimpleDataReg(String nickname, String sex) {
		// TODO Auto-generated constructor stub
		this.nickname = nickname;
		this.sex = sex;
		this.state = 'n';
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try	{
			//connect
			socket = new Socket("210.121.154.99",4001);
			outstream = new BufferedOutputStream(socket.getOutputStream());
			instream = new BufferedInputStream(socket.getInputStream());

			//registration. "s" is header, means "simple registration".
			String data = "s".concat(sex).concat(nickname);
			byte[] ref = data.getBytes("EUC-KR");

			outstream.write(ref);
			outstream.flush();

		}
		catch(Exception e) {
			this.state = 'n';
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
	public String getNickname() {
		return nickname;
	}

	public String getSex() {
		return sex;
	}
	
	public char getStat() {
		return state;
	}
}