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
 * 프로토콜 정의
 * |--------------------------------------|
 * | BASE64 : 첫접속(1/0) 이름 내용
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
			pw.println("1"+NetUtil.PROTOCOL_DIV+ name +NetUtil.PROTOCOL_DIV+ "connect"); //첫 접속 메세지를 보냄
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
		ChatWindowReceiveThread chatWindowReceiveThread = new ChatWindowReceiveThread(socket, textArea);
		chatWindowReceiveThread.start();
	}
	
	private void sendMessage() {
		String message = textField.getText();
		
		// 프로토콜 만들기
		String[] strArr = new String[NetUtil.PTC_DIV_SIZE-1];
		strArr[0] = "0";
		strArr[1] = nickName;
		strArr[2] = message;
		
		if("quit".equals(strArr[2])) {
			//나가기
			System.exit(0);
		}
		
		String data = String.join(NetUtil.PROTOCOL_DIV, strArr);
		
		System.out.println("client: send: " + data);
				
		pw.println(data); // PrintWriter : 서버에 전달할 프로토콜 정의해서 작성하기
		
		textField.setText("");
		textField.requestFocus();
		
		
	}
	
	public static void log(String log) {
		System.out.println("[server] " + log);
	}
}
