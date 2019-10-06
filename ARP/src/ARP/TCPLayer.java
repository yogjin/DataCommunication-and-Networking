package ARP;

import java.util.ArrayList;

public class TCPLayer implements BaseLayer{//Transport층
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private class _TCP_HEADER {
		_TCP_PORT tcp_srcport;
		_TCP_PORT tcp_dstport;
		byte[] tcp_seq;
		byte[] tcp_ack;
		byte[] tcp_offset;
		byte[] tcp_flag;
		byte[] tcp_window;
		byte[] tcp_cksum;
		byte[] tcp_urgptr;
		byte[] tcp_padding;
		byte[] tcp_data;
		
		public _TCP_HEADER() {//tcp header의 자료구조
			this.tcp_dstport = new _TCP_PORT();
			this.tcp_srcport = new _TCP_PORT();
			this.tcp_seq = new byte[4];
			this.tcp_ack = new byte[4];
			this.tcp_offset = new byte[1];
			this.tcp_flag = new byte[1];
			this.tcp_window = new byte[2];
			this.tcp_cksum = new byte[2];
			this.tcp_urgptr = new byte[2];
			this.tcp_padding = new byte[4];
			this.tcp_data = null;
		}
	}

	private class _TCP_PORT {
		private byte[] port = new byte[2];

		private _TCP_PORT() {
			this.port[0] = (byte) 0x00;
			this.port[1] = (byte) 0x00;
		}
	}

	_TCP_HEADER m_sHeader = new _TCP_HEADER();

	public TCPLayer(String pName) {// 생성자
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}
	public void ResetHeader() {//실제로 쓰이는 것을 reset. tcp는 port만 사용
		for (int i = 0; i < 2; i++) {
			m_sHeader.tcp_dstport.port[i] = (byte) 0x00;
			m_sHeader.tcp_srcport.port[i] = (byte) 0x00;
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
