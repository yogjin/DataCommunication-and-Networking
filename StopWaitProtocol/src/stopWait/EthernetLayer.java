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

	public EthernetLayer(String pName) {// ������
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
		byte[] bytes = ObjToByte(m_sHeader, input, length); // ChatAppLayer�� ���� ���� �޽���( 3. String�� Byte�������� �����ؼ�
															// ChatAppLayer�� Sendȣ���ؼ� ������.)�� ����� ���ؼ�

		while (ack == false) {// ack�� true�� �ʱ�ȭ �����Ƿ� �� ó�� ������ ��� .
			try {
				Thread.sleep(1000); //sleep �ؼ� ack�� ����� �� �ֵ��� ��.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.GetUnderLayer().Send(bytes, length + 14);// 14:frame header�� ũ��
		ack = false;//ack�� �ޱ� ������ ���� while���� busy waiting�� �ϱ����� ack�� false�� �ٲ���.

		return true;
	}

	public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) { // ������ ���� data�� ���� layer�� header�� ��ġ�� �Լ�
		setHeaderTypeToData();
		byte[] buf = new byte[length + 14];// ChatAppHeader�� ũ�� == 14
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

		return buf; // ����ϰ� �����Ͱ� ������ �迭
	}

	public byte[] makeAckFrame(_ETHERNET_Frame Header) {
		setHeaderTypeToACK();
		byte[] buf = new byte[14];// ChatAppHeader�� ũ�� == 14
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

	public boolean Receive(byte[] input) {// ���� : input�� ������ ������ �� ��Ŷ. input[0]~[5]�� dstaddr�� �ִ�.�̰� �� srcaddr�� ���ƾ���.

		Receive_Thread thread = new Receive_Thread(input, this.GetUpperLayer(0), this.GetUnderLayer());// ��Ŷ ���� �� ��Ŷ ó����
																										// ���� runnable
																										// Ŭ���� ����
		Thread obj = new Thread(thread);// Thread ����
		obj.start();// Thread ����

		return false;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		byte[] buf = new byte[length - 14];

		for (int i = 0; i < length - 14; i++) { // ����� ũ�� 14�� �� ������
			buf[i] = input[14 + i];
		}

		return buf; // ����� �����ϰ� �����͸� ���� �迭
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

	public void SetEnetSrcAddress(String srcMacAddress) {// StopWaitDlg���� ȣ���ؼ� mac�ּҸ� ������. string -> byte[]�� ��ȯ.
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
					// ��ε�ĳ��Ʈ �ּ��� ���
					for (int i = 0; i < 6; i++) {
						if (input[i] == (byte) 0xFF) {
							broadcastcount++;
						}
					}
					if (broadcastcount == 6) {
						data = RemoveCappHeader(input, input.length); // input���� ChatAppLayer�� header�� �и�.
						this.UpperLayer.Receive(data); // StopWaitDlg�� ������ ����
						byte[] ack = makeAckFrame(m_sHeader);
						this.UnderLayer.Send(ack, 14);
					}

					for (int i = 0; i < 6; i++) { // ������ Ethernet �ּҰ��ڽ��� Ethernet �ּ��ϰ�� �˻��ϰ� chatapplayer�� ����.
						if (input[i] != srcaddr[i]) {// input[0]~input[6]�� dstaddr�� �ּҰ� �ִ�.
							//thread ������ return false�� �����.//���߿� broadcastó�� count���� ���� �߰��ؼ�  ó��
						}
					}
					data = RemoveCappHeader(input, input.length); // input���� EthernetLayer�� header�� �и�.
					this.UpperLayer.Receive(data); // ChatAppLayer�� ������ ����
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
