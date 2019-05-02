package com.cafe24.network.chat.client;

import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

import com.cafe24.network.chat.util.NetUtil;

public class ChatWindowReceiveThread extends Thread {
	private Socket socket;
	
	private TextArea textArea;
	
	public ChatWindowReceiveThread(Socket socket, TextArea textArea) {
		this.socket = socket;
		this.textArea = textArea;
	}
	
	@Override
	public void run() {
		try {
			//4. IOStream 생성 (받아오기)
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			
			while(true) {
				//5. 데이터 읽기
				String data = br.readLine();
				if(data == null) {
					//정상 종료
					NetUtil.cntlog("closed by client");
					break;
				}
				
				NetUtil.cntlog("packet received: " + data);
				
 				if("join:ok".equals(data)) {
					continue;
				}
				
				String[] tokens = data.split(NetUtil.PROTOCOL_DIV);
				
				//6. 데이터 쓰기
				String message = "[" + tokens[tokens.length-2] + "] " + NetUtil.base64Decoding(tokens[tokens.length-1]);
				textArea.append( message);
				textArea.append("\n");
			}
		
		} catch(SocketException e) {
			NetUtil.cntlog("sudden closed by client");
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(socket != null && !socket.isClosed()) {
					socket.close();											
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
