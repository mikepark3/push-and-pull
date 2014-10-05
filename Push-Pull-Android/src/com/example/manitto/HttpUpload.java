package com.example.manitto;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpUpload extends AsyncTask<Void,Void,Void> {
	
	private String upLoadServerUri = "http://210.121.154.94:5001/upload_voice";
	//ProgressDialog progress = null;
	String voice_file;
	private Handler handler;
	
	public HttpUpload(String filePath) {
		// TODO Auto-generated constructor stub
		this.voice_file = filePath;
	}
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		uploadFile();
		return null;
	}
	
	public void setHandler(Handler h){
		
		handler = h;
		
	};
	
	
	public void uploadFile(){
		
		
		
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		
		String boundary = "^*****^";
		String delimiter = "\r\n--" + boundary + "\r\n";
		
		StringBuffer postDataBuilder = new StringBuffer();
	
		
		
		File sourceFile = new File(voice_file);
		
			
			try{
				
				postDataBuilder.append(delimiter);
				postDataBuilder.append(setValue("type","audio"));
				postDataBuilder.append(delimiter);
				postDataBuilder.append(setValue("id","receiver"));
				postDataBuilder.append(delimiter);
				postDataBuilder.append(setValue("title","receiver's voice"));
				postDataBuilder.append(delimiter);
				
				postDataBuilder.append(setFile("uploadFile",sourceFile.getName()));
				postDataBuilder.append("\r\n");
			
				//opne a url connection to the servlet
				URL serverUrl = new URL(upLoadServerUri);
				
				//open a http connection to the url
				
				conn = (HttpURLConnection)serverUrl.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
				
				
				
				FileInputStream fileInstream = new FileInputStream(voice_file);
				dos = new DataOutputStream(new BufferedOutputStream(conn.getOutputStream()));
				
			
				dos.writeUTF(postDataBuilder.toString());
				
				//create a buffer of maximum size
				int maxBufferSize = 1024;
				int bufferSize = Math.min(fileInstream.available(), maxBufferSize);
				byte[] buffer = new byte[bufferSize];
				
				
				int byteRead = fileInstream.read(buffer,0,bufferSize);
				
				while(byteRead>0){
					
					dos.write(buffer);
					bufferSize = Math.min(fileInstream.available(),maxBufferSize);
					byteRead = fileInstream.read(buffer,0,bufferSize);
				}
				
				dos.writeBytes(delimiter);
				dos.flush();
				dos.close();
				fileInstream.close();
				
				Message message = handler.obtainMessage(1);
				handler.sendMessage(message);
				conn.getInputStream();
				conn.disconnect();
				
			}
			catch(IOException e){
				
				e.printStackTrace();
				Message message = handler.obtainMessage(0);
				handler.sendMessage(message);
			}
			
		}
	
	
	public static String setValue(String key, String value){
		
		return "Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n" + value;
	}
	
	public static String setFile(String key, String fileName){
		
		return "Content-Disposition: form-data; name=\"" + key + "\";filename=\"" + fileName + "\"\r\n";
	}

}
