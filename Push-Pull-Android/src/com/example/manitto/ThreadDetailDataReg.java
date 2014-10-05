package com.example.manitto;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class ThreadDetailDataReg extends ThreadManitto
{
	private String nickname;
	private String password;
	private String name;
	private String age;
	private String career;
	private String region;
	private String type;
	private char state;
	
	public ThreadDetailDataReg(String myNickname, String Pw, String Name, String Age, String Career, String Region, String Type){
		// TODO Auto-generated constructor stub
		this.nickname = myNickname;
		this.password = Pw;
		this.name = Name;
		this.age = Age;
		this.career = Career;
		this.region = Region;
		this.type = Type;
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

			//registration. "d" is header, means "detail registration".
			String data ="d"
					.concat(nickname).concat("`").concat(password).concat("`").concat(name).concat("`")
					.concat(age).concat("`").concat(career).concat("`").concat(region).concat("`").concat(type);
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
	
	public char getStat() {
		return state;
	}
}