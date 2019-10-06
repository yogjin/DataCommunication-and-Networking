package ARP;

import java.util.ArrayList;

public class IPLayer implements BaseLayer{//NetWork층
	public int nUpperLayerCount = 0;//?
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private class _IP_HEADER {//ip header의 자료구조
		byte[] ip_verlen;
		byte[] ip_tos;
		byte[] ip_len;
		byte[] ip_id;
		byte[] ip_flagoff;
		byte[] ip_ttl;
		byte[] ip_proto;
		byte[] ip_cksum;
		_IP_ADDR ip_dstaddr;
		_IP_ADDR ip_srcaddr;
		byte[] ip_data;
		
		public _IP_HEADER() {
			this.ip_verlen = new byte[1];
			this.ip_tos = new byte[1];
			this.ip_len = new byte[2];
			this.ip_id = new byte[2];
			this.ip_flagoff = new byte[2];
			this.ip_ttl = new byte[1];
			this.ip_proto = new byte[1];
			this.ip_cksum = new byte[2];
			this.ip_dstaddr = new _IP_ADDR();
			this.ip_srcaddr = new _IP_ADDR();
			this.ip_data = null;
		}
	}

	private class _IP_ADDR {
		private byte[] addr = new byte[4];

		private _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}

	_IP_HEADER m_sHeader = new _IP_HEADER();

	public IPLayer(String pName) {// 생성자
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}
	public void ResetHeader() {//실제로 쓰이는 것을 reset. ip버전: 4, 시작주소 목적주소를 사용.
		for (int i = 0; i < 4; i++) {
			m_sHeader.ip_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.ip_srcaddr.addr[i] = (byte) 0x00;
		}
		m_sHeader.ip_verlen[0] = (byte) 0x04;//IPv4
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
