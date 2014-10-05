package com.example.manitto;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class ThreadRandomTargetSelect extends ThreadManitto
{
	private String myNickname, dateNickname;
	private String mysex, dateSex;
	private char state;

	public ThreadRandomTargetSelect(String nickname, String sex) {
		// TODO Auto-generated constructor stub
		this.myNickname = nickname;
		this.mysex = sex;
		if(sex == "m")
		{
			dateSex = "f";
		}
		else
		{
			dateSex = "m";
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try	{
			//connect
			socket = new Socket("210.121.154.99",4001);
			outstream = new BufferedOutputStream(socket.getOutputStream());	
			instream = new BufferedInputStream(socket.getInputStream());

			//registration "a" is header. means "add data in socket".
			String data = "a".concat(mysex).concat(myNickname);
			byte[] ref = data.getBytes("EUC-KR");

			outstream.write(ref);
			outstream.flush();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void send(){
		try {
			//send find signal. "f" is header and signal, means "find".
			if(outstream != null){
				outstream.write("f".getBytes("EUC-KR"));
				outstream.flush();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void receive(){
		try {
			// byte array to store input
			byte[] contents = new byte[1024];
			int bytesRead=0;

			String str;
			bytesRead = instream.read(contents);

			str = new String(contents, 0, bytesRead);

			System.out.println(str);

			state = str.charAt(0);

			if(state == 's')
			{
				dateNickname = str.substring(1);
				System.out.println("success, dateNickname: ");
				System.out.println(dateNickname);
			}
			else
			{
				System.out.println("failed : " + state);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}
	public String getMyNickname() {
		return myNickname;
	}

	public String getDateNickname() {
		return dateNickname;
	}

	public String getMysex() {
		return mysex;
	}

	public String getDateSex() {
		return dateSex;
	}

	public char getStat() {
		return state;
	}
}