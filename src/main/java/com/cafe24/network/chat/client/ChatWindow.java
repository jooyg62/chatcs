package com.cafe24.network.chat.client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import com.cafe24.network.chat.util.NetUtil;

/**
 * 패킷 정의
 * |--------------------------------------|
 * | 첫접속(0/1/2):이름:내용
 * |______________________________________|
 *
 */

public class ChatWindow {
	
	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	
	String 			nickName 	= null;
	
	Socket 			socket 	= null;
	PrintWriter 	pw 		= null;
	
	public ChatWindow(String name, Socket socket) {
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		
		this.nickName = name;
		this.socket = socket;
		
		try {
			this.pw = new PrintWriter( new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			
			//첫 접속 메세지를 보냄
			String[] joinPacket = NetUtil.makePacket(NetUtil.PTC_DIV_JOIN, name, NetUtil.base64Encoding(name+"님이 입장 하였습니다"));
			String packetString = String.join(NetUtil.PROTOCOL_DIV, joinPacket);
			pw.println(packetString); 
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private void finish() {
		try {
			if(socket != null && socket.isClosed() == false) {
				//socket 정리
				socket.close();				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(".........");
		System.exit(0);
	}

	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				sendMessage();
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if(keyCode == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
			
		});

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();
		
		//thread 생성
		ChatWindowReceiveThread chatWindowReceiveThread = new ChatWindowReceiveThread(socket, textArea, this.nickName);
		chatWindowReceiveThread.start();
	}
	
	private void sendMessage() {
		String message = textField.getText();
		
		if("".equals(message)) {
			return;
		}
		
		String ptc_div = " ";
		
		if(NetUtil.PTC_QUIT.equals(message)) {
			// 나가기 div
			ptc_div = NetUtil.PTC_DIV_QUIT;
		} else if(isWhisper(message)) {
			//귓속말
			ptc_div = NetUtil.PTC_DIV_WHISPER;
			
			String[] splits = message.split(" ");
			
			message = whisperMsgFormat(splits);
			
		} else {
			// 일반 div
			ptc_div = NetUtil.PTC_DIV_BASIC;
		} 
		
		if(NetUtil.PTC_DIV_QUIT.equals(ptc_div)) {
			//나가기: 프로세스 종료
			System.exit(0);
		}
		
		String[] packet = NetUtil.makePacket(ptc_div, nickName, NetUtil.base64Encoding(message));			
		String packetString = String.join(NetUtil.PROTOCOL_DIV, packet);
		
		pw.println(packetString);
		
		textField.setText("");
		textField.requestFocus();
		
	}
	
	public boolean isWhisper(String message) {
		boolean result = false;
		
		String[] tokens = message.split(" ");
		
		if("/w".equals(tokens[0]) && tokens.length >= 3) {
			result = true;
		}
		
		return result;
	}
	
	public String whisperMsgFormat(String... str) {
		String one = str[0];
		String two = str[1];
		StringBuffer threeBuffer = new StringBuffer();
		
		String result = "";
		
		for(int i=0; i< str.length; i++) {
			if(!(i==0 || i==1)) {
				threeBuffer.append(str[i]);
				threeBuffer.append(" ");				
			}
		}
		
		result = one + " " + two + " " + NetUtil.base64Encoding(threeBuffer.toString());
		
		return result;
	}
	
}
