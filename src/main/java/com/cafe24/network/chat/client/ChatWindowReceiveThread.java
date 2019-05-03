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
	
	private String nickName;
	
	public ChatWindowReceiveThread(Socket socket, TextArea textArea, String nickName) {
		this.socket = socket;
		this.textArea = textArea;
		this.nickName = nickName;
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
				
				String[] packet = data.split(NetUtil.PROTOCOL_DIV);
				String msgDiv 	= packet[0];
				String sndUser 	= packet[1];
				String contents = NetUtil.base64Decoding(packet[2]);
				
				String message = "";
					
				//6. 데이터 쓰기
 				if(NetUtil.PTC_DIV_WHISPER.equals(msgDiv)) {	//귓속말이라면..
 					NetUtil.cntlog("whisper rcv, contents: " + contents);
					String[] tokens = contents.split(" ");
					String rcvUser = tokens[1];
					String rcvContent = NetUtil.base64Decoding(tokens[2]);
					
					if(this.nickName.equals(rcvUser)) {
						//나한테 보낸 귓속말이라면..
						message = sndUser + "님께서 보낸 귓속말: " + rcvContent;
					} else {
						continue;
					}
				} else {
					message = "[" + sndUser + "] "+ contents;					
				}
				
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
