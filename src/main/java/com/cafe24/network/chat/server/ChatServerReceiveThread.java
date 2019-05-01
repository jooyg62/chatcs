package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import com.cafe24.network.chat.util.NetUtil;

public class ChatServerReceiveThread extends Thread {
	String nickName = "";
	
	private Socket socket;
	List<Writer> pwList;
	
	public ChatServerReceiveThread(Socket socket, List<Writer> pwList) {
		this.socket = socket;
		this.pwList = pwList;
	}
	
	@Override
	public void run() {
		PrintWriter pw = null;
		try {
			
			//4. IOStream 생성 (받아오기)
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			
			pw = new PrintWriter( new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			
			while(true) {
				//5. 데이터 읽기
				String data = br.readLine();
				
				if(data == null) {
					ChatServer.log("closed by client");
					break;
				}
				
				
				
				System.out.println("[server] received msg: " + data);
				
				String[] tokens = data.split(NetUtil.PROTOCOL_DIV);
				
				if("quit".equals(tokens[tokens.length-1])) {
					doQuit(pw);
					continue;
				}
				
				if("1".equals(tokens[0])) {
					//첫 접속일 경우
					doJoin(tokens[1], pw);
					continue;
				}
				/*
				 * ChatServer.log("received: "); for(String token : tokens ) {
				 * ChatServer.log(token); }
				 * 
				 */				
				//6. 데이터 쓰기
				doMessage(tokens[2]); //개행이 자동으로 붙어서 감.
				
			}
		
		} catch(SocketException e) {
			ChatServer.log("sudden closed by client");
			doQuit(pw);
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
	
	private void doJoin( String nickName, Writer writer ) {
		this.nickName = nickName;
		
		String data = nickName + "님이참여하였습니다.";
		broadcast( data );
		
		/* writer pool에 저장*/
		addWriter( writer );
		
		//ack
		((PrintWriter) writer).println( "join:ok" );
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void addWriter( Writer writer ) {
		synchronized( pwList ) {
			pwList.add( writer );
		}
	}
	
	private void doMessage( String message ) {
		// 프로토콜 만들기
		String[] strArr = new String[NetUtil.PTC_DIV_SIZE-1];
		strArr[0] = "0";
		strArr[1] = nickName;
		strArr[2] = message;
		
		String data = String.join(NetUtil.PROTOCOL_DIV, strArr);
		
		synchronized( pwList ) {
			
			for(Writer writer : pwList ) {
				PrintWriter printWriter = (PrintWriter) writer;
				printWriter.println( data );
				printWriter.flush();
			}
		}
		
	}
	
	private void broadcast( String message ) {
		
		// 프로토콜 만들기
				String[] strArr = new String[NetUtil.PTC_DIV_SIZE-1];
				strArr[0] = "0";
				strArr[1] = nickName;
				strArr[2] = message;
				
				String data = String.join(NetUtil.PROTOCOL_DIV, strArr);
		
		synchronized( pwList ) {
			
			for(Writer writer : pwList ) {
				PrintWriter printWriter = (PrintWriter) writer;
				printWriter.println( data );
				printWriter.flush();
			}
		}
	}
	
	private void doQuit( Writer writer ) {
		removeWriter( writer );
		
		String data = nickName + "님이퇴장하였습니다.";
		broadcast( data );
	}
	
	private void removeWriter( Writer writer ) {
		for(int i=0; i < pwList.size(); i++) {
			if(pwList.get(i) == writer) {
				pwList.remove(i);
				break;
			}
		}
	}

}
