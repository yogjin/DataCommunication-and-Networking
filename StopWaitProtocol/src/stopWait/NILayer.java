package stopWait;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

import stopWait.BaseLayer;

public class NILayer implements BaseLayer{
	
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	int m_iNumAdapter;//네트워크 어뎁터 인덱스
	public Pcap m_AdapterObject;//네트워크 어뎁터 객체
	public PcapIf device;//네트워크 인터페이스 객체
	public List<PcapIf> m_pAdapterList;//네트워크 인터페이스 목록
	StringBuilder errbuf = new StringBuilder();//에러 버퍼
	
	/*--------------생성자 -------------- */
	public NILayer(String pName) {
		//super(pName)
		pLayerName = pName;
		
		m_pAdapterList = new ArrayList<PcapIf>();//네트워크 인터페이스 목록을 동적 할당
		m_iNumAdapter = 0;//네트워크 어뎁터 인덱스을 0으로 초기화
		SetAdapterList();//네트워크 어뎁터 목록 가져오기 함수 호출
	}
	
	
	public void SetAdapterList() {//현재 컴퓨터에 존재하는 모든 네트워크 어뎁터 목록 가져오기
		int r = Pcap.findAllDevs(m_pAdapterList, errbuf);//네트워크 어뎁터가 하나도 존재하지 않을 경우 에러 처리
		if(r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
			System.err.printf("Can't read list of devices, error is %s",errbuf.toString());
			return;
		}
	}
	public void SetAdapterNumber(int iNum) {
		m_iNumAdapter = iNum;//선택된 네트워크 어뎁터 인덱스로 변수 초기화
		PacketStartDriver();//패킷 드라이버 시작 함수(네트워크 어뎁터 객체 open)
		Receive();//패킷 수신 함수.
	}
	public void PacketStartDriver() {
		int snaplen = 64 * 1024; //Capture all packets, no truncation//패킷 캡처 길이
		int flags = Pcap.MODE_PROMISCUOUS;//capture all packets//] 패킷 캡처 플래그 (PROMISCUOUS; 모든 패킷)
		int timeout = 10 * 1000;//10seconds in millisecond//패킷 캡처 시간 (설정 시간 동안 패킷이 수신되지 않은 경우 에러버퍼에 입력)		
		//Pcap 동작에 필요한 기본 설정을 위한 변수들
		m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(),snaplen,flags,timeout,errbuf);//선택된 네트워크 어뎁터 및 설정된 옵션에 맞춰진 pcap 작동 시작
	}
	public boolean Send(byte[] input, int length) {
		ByteBuffer buf = ByteBuffer.wrap(input);//상위레이어로부터 전달받은 데이터를 바이트 버퍼에 담음
		if(m_AdapterObject.sendPacket(buf) != Pcap.OK) {//네트워크 어뎁터의 sendPacket()함수를 통해 데이터 전송
			System.err.println(m_AdapterObject.getErr());//패킷 전송이 실패한 경우 에러메시지 출력 및 false 반환
			return false;
		}
		return true;// 패킷 전송이 성공한 경우 true 반환
	}
	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(m_AdapterObject,this.GetUpperLayer(0));//패킷 수신 시 패킷 처리를 위한 runnable 클래스 생성
		Thread obj = new Thread(thread);//Thread 생성
		obj.start();//Thread 시작
		
		return false;
	}
	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if(p_UnderLayer == null) 
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if(nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if(pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if(pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++,pUpperLayer);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		// TODO Auto-generated method stub
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

}


class Receive_Thread implements Runnable {
	byte[] data;
	Pcap AdapterObject;
	BaseLayer UpperLayer;

	public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {	/* 생성자   */ //Pcap 처리에 필요한 네트워크 어뎁터 및 상위 레이어 객체 초기화
		// TODO Auto-generated constructor stub

		AdapterObject = m_AdapterObject;
		UpperLayer = m_UpperLayer;
	}

	@Override
	public void run() {												/* 실제실행  */
		while (true) {
			PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>(){//패킷 수신을 위한 라이브러리 함수 (PcapPacketHandler)
				public void nextPacket(PcapPacket packet, String user) {
					data = packet.getByteArray(0,packet.size());//수신된 패킷의 데이터(바이트 배열)와 패킷 크기를 알아냄
					UpperLayer.Receive(data);//수신된 데이터를 상위 레이어로 전달
					
				}
			};
			
			AdapterObject.loop(100000, jpacketHandler,"");
		}//네트워크 어뎁터에서 PcapPacketHandler를 무한 반복ㄴ
	}
}
