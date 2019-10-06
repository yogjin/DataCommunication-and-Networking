package stopWait;

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public boolean ack = true;

	private class _ETHERNET_ADDR {
		private byte[] addr = new byte[6];

		private _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}
	}

	private class _ETHERNET_Frame {
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;

		public _ETHERNET_Frame() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	_ETHERNET_Frame m_sHeader = new _ETHERNET_Frame();

	public EthernetLayer(String pName) {// 생성자
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 5; i++) {
			m_sHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
		m_sHeader.enet_type[0] = (byte) 0x00;
		m_sHeader.enet_type[1] = (byte) 0x00;
	}

	public void setHeaderTypeToData() {
		m_sHeader.enet_type[0] = (byte) 0x00;
		m_sHeader.enet_type[1] = (byte) 0x01;
	}

	public void setHeaderTypeToACK() {
		m_sHeader.enet_type[0] = (byte) 0x00;
		m_sHeader.enet_type[1] = (byte) 0x02;
	}

	public boolean typeIsData(byte[] bytes) {
		if ((bytes[12] == (byte) 0x00) && (bytes[13] == (byte) 0x01)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean typeIsAck(byte[] bytes) {
		if ((bytes[12] == (byte) 0x00) && (bytes[13] == (byte) 0x02)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean Send(byte[] input, int length) {
		byte[] bytes = ObjToByte(m_sHeader, input, length); // ChatAppLayer로 부터 받은 메시지( 3. String을 Byte형식으로 변경해서
															// ChatAppLayer에 Send호출해서 보낸다.)에 헤더를 더해서

		while (ack == false) {// ack를 true로 초기화 했으므로 맨 처음 조각은 통과 .
			try {
				Thread.sleep(1000); //sleep 해서 ack가 변경될 수 있도록 함.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.GetUnderLayer().Send(bytes, length + 14);// 14:frame header의 크기
		ack = false;//ack를 받기 전까지 위의 while에서 busy waiting을 하기위해 ack를 false로 바꿔줌.

		return true;
	}

	public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) { // 위에서 받은 data와 현재 layer의 header를 합치는 함수
		setHeaderTypeToData();
		byte[] buf = new byte[length + 14];// ChatAppHeader의 크기 == 14
		byte[] dstaddr = new byte[6];
		byte[] srcaddr = new byte[6];
		byte[] type = new byte[2];
		for (int i = 0; i < 6; i++) {
			dstaddr[i] = Header.enet_dstaddr.addr[i];
			srcaddr[i] = Header.enet_srcaddr.addr[i];
		}
		for (int i = 0; i < 2; i++) {
			type[i] = Header.enet_type[i];
		}
		buf[0] = dstaddr[0];
		buf[1] = dstaddr[1];
		buf[2] = dstaddr[2];
		buf[3] = dstaddr[3];
		buf[4] = dstaddr[4];
		buf[5] = dstaddr[5];
		buf[6] = srcaddr[0];
		buf[7] = srcaddr[1];
		buf[8] = srcaddr[2];
		buf[9] = srcaddr[3];
		buf[10] = srcaddr[4];
		buf[11] = srcaddr[5];
		buf[12] = type[0];
		buf[13] = type[1];

		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf; // 헤더하고 데이터가 합쳐진 배열
	}

	public byte[] makeAckFrame(_ETHERNET_Frame Header) {
		setHeaderTypeToACK();
		byte[] buf = new byte[14];// ChatAppHeader의 크기 == 14
		byte[] dstaddr = new byte[6];
		byte[] srcaddr = new byte[6];
		byte[] type = new byte[2];
		for (int i = 0; i < 6; i++) {
			dstaddr[i] = Header.enet_dstaddr.addr[i];
			srcaddr[i] = Header.enet_srcaddr.addr[i];
		}
		for (int i = 0; i < 2; i++) {
			type[i] = Header.enet_type[i];
		}
		buf[0] = dstaddr[0];
		buf[1] = dstaddr[1];
		buf[2] = dstaddr[2];
		buf[3] = dstaddr[3];
		buf[4] = dstaddr[4];
		buf[5] = dstaddr[5];
		buf[6] = srcaddr[0];
		buf[7] = srcaddr[1];
		buf[8] = srcaddr[2];
		buf[9] = srcaddr[3];
		buf[10] = srcaddr[4];
		buf[11] = srcaddr[5];
		buf[12] = type[0];
		buf[13] = type[1];

		return buf;
	}

	public boolean Receive(byte[] input) {// 수신 : input은 상대방이 나한테 준 패킷. input[0]~[5]에 dstaddr이 있다.이게 내 srcaddr과 같아야함.

		Receive_Thread thread = new Receive_Thread(input, this.GetUpperLayer(0), this.GetUnderLayer());// 패킷 수신 시 패킷 처리를
																										// 위한 runnable
																										// 클래스 생성
		Thread obj = new Thread(thread);// Thread 생성
		obj.start();// Thread 시작

		return false;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		byte[] buf = new byte[length - 14];

		for (int i = 0; i < length - 14; i++) { // 헤더의 크기 14을 뺀 수까지
			buf[i] = input[14 + i];
		}

		return buf; // 헤더를 제거하고 데이터만 남은 배열
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		// TODO Auto-generated method stub
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

	public void SetEnetSrcAddress(String srcMacAddress) {// StopWaitDlg에서 호출해서 mac주소를 저장함. string -> byte[]로 변환.
		// TODO Auto-generated method stub
		byte[] srcaddr = ConvertMacAddressToByteArray(srcMacAddress);
		m_sHeader.enet_srcaddr.addr = srcaddr;
	}

	public void SetEnetDstAddress(String dstMacAddress) {
		// TODO Auto-generated method stub
		byte[] dstaddr = ConvertMacAddressToByteArray(dstMacAddress);
		m_sHeader.enet_dstaddr.addr = dstaddr;
	}

	public byte[] ConvertMacAddressToByteArray(String macAddress) {
		String[] macAddressParts = macAddress.split("-");

		// convert hex string to byte values
		byte[] macAddressBytes = new byte[6];
		for (int i = 0; i < 6; i++) {
			Integer hex = Integer.parseInt(macAddressParts[i], 16);
			macAddressBytes[i] = hex.byteValue();
		}
		return macAddressBytes;
	}

	class Receive_Thread implements Runnable {
		byte[] input;
		BaseLayer UpperLayer;
		BaseLayer UnderLayer;

		public Receive_Thread(byte[] input, BaseLayer m_UpperLayer, BaseLayer m_UnderLayer) {
			this.input = input;
			UpperLayer = m_UpperLayer;
			UnderLayer = m_UnderLayer;
		}

		public void run() {
//			while (true) {
				if (typeIsData(input) == true) {//Data type
					byte[] data;
					byte[] srcaddr = new byte[6];
					for (int i = 0; i < 6; i++) {
						srcaddr[i] = m_sHeader.enet_srcaddr.addr[i];
					}
					int broadcastcount = 0;
					// 브로드캐스트 주소일 경우
					for (int i = 0; i < 6; i++) {
						if (input[i] == (byte) 0xFF) {
							broadcastcount++;
						}
					}
					if (broadcastcount == 6) {
						data = RemoveCappHeader(input, input.length); // input에서 ChatAppLayer의 header를 분리.
						this.UpperLayer.Receive(data); // StopWaitDlg로 데이터 보냄
						byte[] ack = makeAckFrame(m_sHeader);
						this.UnderLayer.Send(ack, 14);
					}

					for (int i = 0; i < 6; i++) { // 목적지 Ethernet 주소가자신의 Ethernet 주소일경우 검사하고 chatapplayer로 전달.
						if (input[i] != srcaddr[i]) {// input[0]~input[6]에 dstaddr의 주소가 있다.
							//thread 때문에 return false가 사라짐.//나중에 broadcast처럼 count같은 변수 추가해서  처리
						}
					}
					data = RemoveCappHeader(input, input.length); // input에서 EthernetLayer의 header를 분리.
					this.UpperLayer.Receive(data); // ChatAppLayer로 데이터 보냄
					byte[] ack = makeAckFrame(m_sHeader);
					this.UnderLayer.Send(ack, 14);
				} else if (typeIsAck(input) == true) {// Ack type
					ack = true;
				}
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}
	}
}
