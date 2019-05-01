package com.cafe24.network.chat.client;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import com.cafe24.network.chat.util.NetUtil;

public class ChatClientApp {
	
	private static final String SERVER_IP 	= 	"192.168.1.9";
	private static final int 	SERVER_PORT = 	7000; 

	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);
		
		while( true ) {
			
			NetUtil.cntlog("대화명을 입력하세요.");
			NetUtil.cntlog(">>> ");
			name = scanner.nextLine();
			
			if (name.isEmpty() == false ) {
				break;
			}
			
			NetUtil.cntlog("대화명은 한글자 이상 입력해야 합니다.\n");
		}
		
		Socket socket = null;
		
		try {
			
			//1. 소켓 만들고
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			NetUtil.cntlog("connected");
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//3. join 성공
		//
		scanner.close();

		new ChatWindow(name, socket).show();
	}
	
}
