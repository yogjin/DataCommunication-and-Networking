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

	public byte[] ObjToByte(_CHAT_APP Header, byte[] data, int length) { //위에서 받은 data와 현재 layer의 header를 합치는 함수
		byte[] buf = new byte[length + 4];//ChatAppHeader의 크기 == 4
		byte[] totalLength = Header.capp_totlen;
		byte[] type = intToByte(Header.capp_type);
		byte[] unused = intToByte(Header.capp_unused);

		buf[0] = totalLength[0];
		buf[1] = totalLength[1];
		buf[2] = type[0];
		buf[3] = unused[0];

		for (int i = 0; i < length; i++)
			buf[4 + i] = data[i];

		return buf; //헤더하고 데이터가 합쳐진 배열
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
	public void setHeader(byte[] input, int length, byte type) {//헤더 정하기.
		m_sHeader.capp_data = input;
		m_sHeader.capp_totlen = intToByte2(length);//문자열을 길이저장..
		m_sHeader.capp_type = type;
	}
	public void sendFregmantionPieceToUnderLayer(byte[] bytes,int length) {
		this.GetUnderLayer().Send(bytes, length);//EthernetLayer에 send
		
	}
	public byte[] cutFrontDataByteArrayAsSize(byte[] input,int size) {//size만큼 바이트배열의 앞부분을 자른다.
		byte[] data = new byte[(input.length)-size];
		for(int i = 0;i<((input.length)-size);i++) {
			data[i] = input[i+size];
		}
		return data;
	}
	
	public boolean Send(byte[] input, int length) {//input == data. length == data.length
		
		if(length <= 10) {//문자열의 크기가 10바이트 이하라서 단편화 하지 않음.
			setHeader(input,length,(byte) 0x00);
			byte[] bytes = ObjToByte(m_sHeader, input, length); 
			sendFregmantionPieceToUnderLayer(bytes,length + 4);
		}
		else if(10 < length) {//문자열의 크기가 10바이트 보다 크면 단편화
			int FregmentationPieceNumber = (length/10)+1;//단편화 할 횟수. 조각의 개수와 같다 
			for(int i = 0;i<FregmentationPieceNumber;i++) {
				if(i == 0) {
					setHeader(input,length,(byte)0x01);//첫 조각의 헤더에는 데이터의 전체길이와 type = 0x01을 넣어준다.
					byte[] bytes = ObjToByte(m_sHeader, input, input.length);// input에 헤더를 붙인 데이터를 bytes 배열에 저장한다.
					sendFregmantionPieceToUnderLayer(bytes,10 + 4); //bytes를 밑 layer로 보낸다.(bytes의 크기는 10(데이터) + 4(헤더))
					input = cutFrontDataByteArrayAsSize(input, 10);//input(위에서 받은 data)의 앞 부분을 10(조각의 크기)만큼 자른다. 
				}
				else if((0 < i) && (i < FregmentationPieceNumber-1)) {// 중간 조각의 헤더에는 데이터의 전체길이와 type = 0x02을 넣어준다.
					setHeader(input,length,(byte)0x02);
					byte[] bytes = ObjToByte(m_sHeader, input, input.length); 
					sendFregmantionPieceToUnderLayer(bytes,10 + 4);
					input = cutFrontDataByteArrayAsSize(input, 10);
				}
				else {//마지막 조각의 헤더에는 데이터의 전체길이와 type = 0x03을 넣어준다.
					setHeader(input,length,(byte)0x03);
					byte[] bytes = ObjToByte(m_sHeader, input, input.length);
					sendFregmantionPieceToUnderLayer(bytes,input.length + 4);//(bytes의 크기는 input.length(데이터) + 4(헤더))
				}
			}
		}
		return true;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		byte[] buf = new byte[length - 4];
		
		for (int i = 0; i < length-4; i++) { //헤더의 크기 4을 뺀 수까지
			buf[i] = input[4+i];
		}
		return buf; //헤더를 제거하고 데이터만 남은 배열
	}
	public byte[] removePaddingOfinputArray(byte[] input, int sizeOfinputArray) {
		byte[] resizedInput = new byte[4+sizeOfinputArray]; //chap헤더 크기 + 데이터크기.		
		
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
		
		if(input[2] == 0x00) {//단편화x
			byte[] data;
			input = removePaddingOfinputArray(input,totlen);//단편화 안한 경우 input배열에서 데이터의 배열길이는 totlen이다.
			data = RemoveCappHeader(input, input.length); //input에서 ChatAppLayer의 header를 분리.
			this.GetUpperLayer(0).Receive(data); //StopWaitDlg로 데이터 보냄
		}
		else if(input[2] == 0x01 || input[2] == 0x02) {//단편화 첫번째 조각,두번째조각.
			byte[] bufData;
			input = removePaddingOfinputArray(input,10);//단편화 조각의 길이 : 4(헤더) + 10(데이터)
			bufData = RemoveCappHeader(input,input.length);//헤더를 떼어낸다
			buffer.put(bufData);//버퍼에 데이터를 쌓는다.
		}
		else {//input[2] == 0x03 일때. 마지막 단편화 조각일때.
			byte[] bufData;
			bufData = RemoveCappHeader(input,input.length);//헤더를 떼어낸다
			buffer.put(bufData);//버퍼에 데이터를 쌓는다.
			
			byte[] data = new byte[totlen];//totlen(data의 전체길이)만큼의 배열을 생성.  
			for(int i = 0; i < totlen; i++) {
			data[i] = buffer.get(i);//지금까지 쌓은 데이터조각을 하나로 합친다.
			}
			this.GetUpperLayer(0).Receive(data);
			buffer.clear();//다른 메시지를 받을 수 있도록 버퍼를 비워준다.
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

	//setEnet 삭제함.

}
