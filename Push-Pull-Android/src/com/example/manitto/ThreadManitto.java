package com.example.manitto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ThreadManitto extends Thread{
	protected Socket socket;
	protected BufferedOutputStream outstream;
	protected BufferedInputStream instream;
	
	//constructor
	public ThreadManitto(){}
	
	//run, connect & registration
	@Override
	public void run(){}
	
	//send
	public void send(){}
	
	//receive
	public void receive(){}
	
	//close socket
	public void socketClose(){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
