package com.example.manitto;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImageDownLoad extends AsyncTask<Void,Void,Void> {
	
	private String upLoadServerUri = "http://210.121.154.94:5001/download_image";
	//ProgressDialog progress = null;
	String image_file;
	String nickName;
	boolean fileExist=false;
	private Handler handler;
	
	public ImageDownLoad(String filePath,String nickname) {
		// TODO Auto-generated constructor stub
		this.image_file = filePath;
		this.nickName = nickname;
	}
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		downloadFile();
		return null;
	}
	
	public boolean getFileResult(){
		
		return fileExist;
	}
	public void setHandler(Handler h){
		
		handler = h;
		
	};
	
	
	public void downloadFile(){
		
		try{
			
			URL serverUrl = new URL(upLoadServerUri);
			HttpURLConnection conn = (HttpURLConnection)serverUrl.openConnection();
			DataOutputStream dos;
			String boundary = "^*****^";
			String delimiter = "\r\n--" + boundary + "\r\n";
			
			StringBuffer postDataBuilder = new StringBuffer();
		
			
			
			postDataBuilder.append(delimiter);
			postDataBuilder.append(setValue("type","request"));
			postDataBuilder.append(delimiter);
			postDataBuilder.append(setValue("id",nickName));
			postDataBuilder.append(delimiter);
			postDataBuilder.append(setValue("title","manittoRequest"));
			postDataBuilder.append(delimiter);
			
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			
			dos = new DataOutputStream(new BufferedOutputStream(conn.getOutputStream()));
			
			
			dos.writeUTF(postDataBuilder.toString());
			dos.flush();
			dos.close();
			
			if(conn != null){
				
				conn.setConnectTimeout(100000);
				
				
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
					
					File downFile = new File(image_file);
					FileOutputStream fileOutput = new FileOutputStream(downFile);
					InputStream inStream = conn.getInputStream();
					
					int downloadedSize = 0;
					
					byte[] buffer = new byte[1024];
					int bufferLength = 0;
					
					while((bufferLength = inStream.read(buffer)) > 0){
						
						fileOutput.write(buffer,0,bufferLength);
						downloadedSize += bufferLength;
					}
					
					fileOutput.close();
					
					Message message = handler.obtainMessage(1);
					handler.sendMessage(message);
					fileExist = true;
				}
				
				else if(conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
					Message message = handler.obtainMessage(0);
					handler.sendMessage(message);
					
				}
			}
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
