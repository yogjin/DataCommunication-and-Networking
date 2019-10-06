package ARP;

import java.util.ArrayList;

public class ARPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;//?
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private class _ARP_HEADER {//ARP의 header == 28bytes
		byte[] arp_hardtype;
		byte[] arp_prototype;
		byte[] arp_enet_addr_len;
		byte[] arp_ip_addr_len;
		byte[] arp_opcode;
		_ETHERNET_ADDR enet_srcaddr;
		_IP_ADDR ip_srcaddr;
		_ETHERNET_ADDR enet_dstaddr;
		_IP_ADDR ip_dstaddr;
		
		public _ARP_HEADER() {//ip header의 자료구조
			this.arp_hardtype = new byte[2];
			this.arp_prototype = new byte[2];
			this.arp_enet_addr_len = new byte[1];
			this.arp_ip_addr_len = new byte[1];
			this.arp_opcode = new byte[2];
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.ip_srcaddr = new _IP_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.ip_srcaddr = new _IP_ADDR();
		}
	}

	private class _IP_ADDR {//ip주소
		private byte[] addr = new byte[4];

		private _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}
	private class _ETHERNET_ADDR {//mac주소
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
	_ARP_HEADER m_sHeader = new _ARP_HEADER();

	public ARPLayer(String pName) {// 생성자
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}
	public void ResetHeader() {//실제로 쓰이는 것을 reset.
		m_sHeader.arp_hardtype[0] = (byte) 0x00;
		m_sHeader.arp_hardtype[1] = (byte) 0x01;//이더넷타입 == 1
		m_sHeader.arp_prototype[0] = (byte) 0x08;
		m_sHeader.arp_prototype[1] = (byte) 0x00;//ip타입 == 0x0800
		m_sHeader.arp_enet_addr_len[0] = (byte) 0x06;//mac주소 길이 == 6
		m_sHeader.arp_ip_addr_len[0] = (byte) 0x04;//ip주소 길이 == 4
		m_sHeader.arp_opcode[0] = (byte) 0x00;//opcode는 나중에 설정해줘야함. opcode == 1 -> 요청,  opcode == 2 -> 응답
		for (int i = 0; i < 4; i++) {
			m_sHeader.ip_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.ip_srcaddr.addr[i] = (byte) 0x00;
		}
		for (int i = 0; i < 5; i++) {
			m_sHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeaderTypeToChat() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeaderTypeToFile() {
		// TODO Auto-generated method stub
		
	}

}
