package stopWait;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _CHAT_APP{
		byte[] capp_totlen;
		byte capp_type;
		byte capp_unused;
		byte[] capp_data;
		
		public _CHAT_APP() {
			this.capp_totlen = new byte[2];
			this.capp_type = 0x00;
			this.capp_unused = 0x00;
			this.capp_data = null;
			}
	}

	_CHAT_APP m_sHeader = new _CHAT_APP();
	public static ByteBuffer buffer = ByteBuffer.allocate(1024); 
	
	public ChatAppLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 2; i++) {
			m_sHeader.capp_totlen[i] = (byte) 0x00;
		}
	}

	public byte[] ObjToByte(_CHAT_APP Header, byte[] data, int length) { //������ ���� data�� ���� layer�� header�� ��ġ�� �Լ�
		byte[] buf = new byte[length + 4];//ChatAppHeader�� ũ�� == 4
		byte[] totalLength = Header.capp_totlen;
		byte[] type = intToByte(Header.capp_type);
		byte[] unused = intToByte(Header.capp_unused);

		buf[0] = totalLength[0];
		buf[1] = totalLength[1];
		buf[2] = type[0];
		buf[3] = unused[0];

		for (int i = 0; i < length; i++)
			buf[4 + i] = data[i];

		return buf; //����ϰ� �����Ͱ� ������ �迭
	}

	byte[] intToByte(int value) {
		byte[] temp = new byte[1];
		temp[0] = (byte) value;

		return temp;
	}
	byte[] intToByte2(int value) {
		byte[] temp = new byte[2];
		temp[1] = (byte) (value >> 8);
		temp[0] = (byte) value;

		return temp;
	}
	public static int byte2ToInt(byte[] input) {
        int s1 = input[1] & 0xFF;
        int s2 = input[0] & 0xFF;
    
        return ((s1 << 8) + (s2 << 0));
	}
	public void setHeader(byte[] input, int length, byte type) {//��� ���ϱ�.
		m_sHeader.capp_data = input;
		m_sHeader.capp_totlen = intToByte2(length);//���ڿ��� ��������..
		m_sHeader.capp_type = type;
	}
	public void sendFregmantionPieceToUnderLayer(byte[] bytes,int length) {
		this.GetUnderLayer().Send(bytes, length);//EthernetLayer�� send
		
	}
	public byte[] cutFrontDataByteArrayAsSize(byte[] input,int size) {//size��ŭ ����Ʈ�迭�� �պκ��� �ڸ���.
		byte[] data = new byte[(input.length)-size];
		for(int i = 0;i<((input.length)-size);i++) {
			data[i] = input[i+size];
		}
		return data;
	}
	
	public boolean Send(byte[] input, int length) {//input == data. length == data.length
		
		if(length <= 10) {//���ڿ��� ũ�Ⱑ 10����Ʈ ���϶� ����ȭ ���� ����.
			setHeader(input,length,(byte) 0x00);
			byte[] bytes = ObjToByte(m_sHeader, input, length); 
			sendFregmantionPieceToUnderLayer(bytes,length + 4);
		}
		else if(10 < length) {//���ڿ��� ũ�Ⱑ 10����Ʈ ���� ũ�� ����ȭ
			int FregmentationPieceNumber = (length/10)+1;//����ȭ �� Ƚ��. ������ ������ ���� 
			for(int i = 0;i<FregmentationPieceNumber;i++) {
				if(i == 0) {
					setHeader(input,length,(byte)0x01);//ù ������ ������� �������� ��ü���̿� type = 0x01�� �־��ش�.
					byte[] bytes = ObjToByte(m_sHeader, input, input.length);// input�� ����� ���� �����͸� bytes �迭�� �����Ѵ�.
					sendFregmantionPieceToUnderLayer(bytes,10 + 4); //bytes�� �� layer�� ������.(bytes�� ũ��� 10(������) + 4(���))
					input = cutFrontDataByteArrayAsSize(input, 10);//input(������ ���� data)�� �� �κ��� 10(������ ũ��)��ŭ �ڸ���. 
				}
				else if((0 < i) && (i < FregmentationPieceNumber-1)) {// �߰� ������ ������� �������� ��ü���̿� type = 0x02�� �־��ش�.
					setHeader(input,length,(byte)0x02);
					byte[] bytes = ObjToByte(m_sHeader, input, input.length); 
					sendFregmantionPieceToUnderLayer(bytes,10 + 4);
					input = cutFrontDataByteArrayAsSize(input, 10);
				}
				else {//������ ������ ������� �������� ��ü���̿� type = 0x03�� �־��ش�.
					setHeader(input,length,(byte)0x03);
					byte[] bytes = ObjToByte(m_sHeader, input, input.length);
					sendFregmantionPieceToUnderLayer(bytes,input.length + 4);//(bytes�� ũ��� input.length(������) + 4(���))
				}
			}
		}
		return true;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		byte[] buf = new byte[length - 4];
		
		for (int i = 0; i < length-4; i++) { //����� ũ�� 4�� �� ������
			buf[i] = input[4+i];
		}
		return buf; //����� �����ϰ� �����͸� ���� �迭
	}
	public byte[] removePaddingOfinputArray(byte[] input, int sizeOfinputArray) {
		byte[] resizedInput = new byte[4+sizeOfinputArray]; //chap��� ũ�� + ������ũ��.		
		
		for(int i = 0; i < 4+sizeOfinputArray; i++) {
			resizedInput[i] = input[i];
		}
		return resizedInput;
	}
	public synchronized boolean Receive(byte[] input) {
		byte[] temp = new byte[2];
		for(int i = 0; i<2; i++) {
			temp[i] = input[i];
		}
		int totlen = byte2ToInt(temp);
		
		if(input[2] == 0x00) {//����ȭx
			byte[] data;
			input = removePaddingOfinputArray(input,totlen);//����ȭ ���� ��� input�迭���� �������� �迭���̴� totlen�̴�.
			data = RemoveCappHeader(input, input.length); //input���� ChatAppLayer�� header�� �и�.
			this.GetUpperLayer(0).Receive(data); //StopWaitDlg�� ������ ����
		}
		else if(input[2] == 0x01 || input[2] == 0x02) {//����ȭ ù��° ����,�ι�°����.
			byte[] bufData;
			input = removePaddingOfinputArray(input,10);//����ȭ ������ ���� : 4(���) + 10(������)
			bufData = RemoveCappHeader(input,input.length);//����� �����
			buffer.put(bufData);//���ۿ� �����͸� �״´�.
		}
		else {//input[2] == 0x03 �϶�. ������ ����ȭ �����϶�.
			byte[] bufData;
			bufData = RemoveCappHeader(input,input.length);//����� �����
			buffer.put(bufData);//���ۿ� �����͸� �״´�.
			
			byte[] data = new byte[totlen];//totlen(data�� ��ü����)��ŭ�� �迭�� ����.  
			for(int i = 0; i < totlen; i++) {
			data[i] = buffer.get(i);//���ݱ��� ���� ������������ �ϳ��� ��ģ��.
			}
			this.GetUpperLayer(0).Receive(data);
			buffer.clear();//�ٸ� �޽����� ���� �� �ֵ��� ���۸� ����ش�.
		}
		return true;
		
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
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;

	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

	//setEnet ������.

}
