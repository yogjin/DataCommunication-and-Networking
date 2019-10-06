package stopWait;

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();


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

	public void setHeaderTypeToChat() {
		m_sHeader.enet_type[0] = (byte) 0x20;
		m_sHeader.enet_type[1] = (byte) 0x80;
	}

	public void setHeaderTypeToFile() {
		m_sHeader.enet_type[0] = (byte) 0x20;
		m_sHeader.enet_type[1] = (byte) 0x90;
	}

	public boolean Send(byte[] input, int length) {
		byte[] bytes = ObjToByte(m_sHeader, input, length); 
		
		this.GetUnderLayer().Send(bytes, length + 14);// 14:frame header의 크기
		return true;
	}

	public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) { // 위에서 받은 data와 현재 layer의 header를 합치는 함수
		
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


	public boolean Receive(byte[] input) {// 수신 : input은 상대방이 나한테 준 패킷. input[0]~[5]에 dstaddr이 있다.이게 내 srcaddr과 같아야함.
			//chat,file구현한 뒤 헤더에 있는 타입을 보고 2080이면 chat으로, 2090이면 file로 보내줌.
			byte[] data;
			byte[] srcaddr = new byte[6];
			for (int i = 0; i < 6; i++) {
				srcaddr[i] = m_sHeader.enet_srcaddr.addr[i];
			}

			for (int i = 0; i < 6; i++) { // 목적지 Ethernet 주소가자신의 Ethernet 주소일경우 검사하고 chatapplayer로 전달.
				if (input[i] != srcaddr[i]) {// input[0]~input[6]에는 dstaddr의 주소가 있다.
					return false;
				}
			}
			if(input[12] == (byte)0x20 && input[13] == (byte)0x80) {// 채팅이면
				data = RemoveCappHeader(input, input.length); //input에서 EthernetLayer의 header를 분리.
				this.GetUpperLayer(1).Receive(data); //ChatAppLayer로 데이터 보냄
			}
			else if(input[12] == (byte)0x20 && input[13] == (byte)0x90) {// 파일이면 
				data = RemoveCappHeader(input, input.length); //input에서 EthernetLayer의 header를 분리.
				this.GetUpperLayer(0).Receive(data); //FileAppLayer로 데이터 보냄
			}
			return true;
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
	public void progressBar_value(int progress) {}
}
