package com.cafe24.network.chat.util;

/**
 * 프로토콜 정의 및
 * 유틸 내용 작성
 * 
 * @author jgseo@naver.com
 * 
 */
public class NetUtil {
	
	/**
	 * 프로토콜 구분 문자 (:)
	 */
	public static final String PROTOCOL_DIV = ":";
	
	/**
	 * PTC_DIV_SIZE
	 *  : 패킷 내용 종류의 수
	 */
	public static final int PTC_DIV_SIZE = 3;
	
	/**
	 * 패킷 구분 정의
	 * |	0: 일반 메시지
	 * |	1: 최초 채팅방 입장
	 * |	2: 나가기
	 */
	public static final String PTC_DIV_BASIC 	= "0";
	public static final String PTC_DIV_JOIN 	= "1";
	public static final String PTC_DIV_QUIT 	= "2";
	
	/**
	 * 채팅방 나가기 : quit
	 */
	public static final String PTC_QUIT = "quit";
	
	/**
	 * 서버 로그
	 * @param log
	 */
	public static void svrlog(String log) {
		System.out.println("[server#"+ Thread.currentThread().getId() + "] " + log);
	}
	
	/**
	 * 클라이언트 로그
	 * @param log
	 */
	public static void cntlog(String log) {
		System.out.println("[client] " + log);
	}
	
	/**
	 * 
	 * @param div	메시지 구분
	 * |
	 * |	0: 일반 메시지
	 * |	1: 최초 채팅방 입장
	 * |	2: 나가기
	 * |
	 * @param nickName	유저 네임
	 * @param content	메시지 내용
	 * @return	String 배열의 패킷
	 */
	public static String[] makePacket(String div, String nickName, String content) {
		String[] packet = new String[PTC_DIV_SIZE];
		packet[0] = div;
		packet[1] = nickName;
		packet[2] = content;
		
		return packet;
	}
}
