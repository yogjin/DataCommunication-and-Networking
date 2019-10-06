package stopWait;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

public class FileAppLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	public String filePath;
	public String fileName;
	public File file;
	ArrayList<Point> arrayList = new ArrayList<Point>();
	
	static class Point implements Comparable<Point>{//1448바이트가 넘는 파일받을때 단편화 조각을 객체배열로 Sorting하기 위해 만듦.
	    private byte[] data;
	    private Integer order;
	 
	    public Point(byte[] DataPiece, int byte4) {
	        this.data = DataPiece;
	        this.order = byte4;
	    }
	 
	    public byte[] getData() {
	        return data;
	    }
	 
	    public Integer getOrder() {
	        return order;
	    }

		@Override
		public int compareTo(Point o) {
			if (this.order < o.getOrder()) {
	            return -1;
	        } else if (this.order > o.getOrder()) {
	            return 1;
	        }
	        return 0;

		}
	 
	}
	private class _FAPP_HEADER {
		byte[] fapp_totlen;
		byte[] fapp_type;
		byte fapp_msg_type;
		byte fapp_unused;
		byte[] fapp_seq_num;

		public _FAPP_HEADER() {
			this.fapp_totlen = new byte[4];
			this.fapp_type = new byte[2];
			this.fapp_msg_type = 0x00;
			this.fapp_unused = 0x00;
			this.fapp_seq_num = new byte[4];
		}
	}

	_FAPP_HEADER m_sHeader = new _FAPP_HEADER();

	public FileAppLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 4; i++) {
			m_sHeader.fapp_totlen[i] = (byte) 0x00;
		}
		for(int i = 0; i < 2; i++) {
			m_sHeader.fapp_type[i] = (byte) 0x00;
		}
			m_sHeader.fapp_msg_type = (byte) 0x00;
			m_sHeader.fapp_unused =  (byte) 0x00;
		for(int i = 0; i < 4; i++) {
			m_sHeader.fapp_seq_num[i] = (byte) 0x00;
		}
	}

	public static ByteBuffer buffer;

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
	
	byte[] intToByte4(int value) {
		byte[] temp = new byte[4];
		temp[3] = (byte) (value >> 24);
		temp[2] = (byte) (value >> 16);
		temp[1] = (byte) (value >> 8);
		temp[0] = (byte) value;

		return temp;
	}
	
	public static int byte2ToInt(byte[] input) {
		int s1 = input[1] & 0xFF;
		int s2 = input[0] & 0xFF;

		return ((s1 << 8) + (s2 << 0));
	}
	public static int byte4ToInt(byte[] input) {
		int s1 = input[3] & 0xFF;
		int s2 = input[2] & 0xFF;
		int s3 = input[1] & 0xFF;
		int s4 = input[0] & 0xFF;

		return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
	}

	public byte[] ObjToByte(_FAPP_HEADER Header, byte[] data, int length) { // 위에서 받은 data와 현재 layer의 header를 합치는 함수
		byte[] buf = new byte[length + 12];// FileAppHeader의 크기 == 12
		byte[] totalLength = Header.fapp_totlen;
		byte[] type = Header.fapp_type;
		byte[] msg_type = intToByte(Header.fapp_msg_type);
		byte[] unused = intToByte(Header.fapp_unused);
		byte[] seq_num = Header.fapp_seq_num;
			
		buf[0] = totalLength[0];
		buf[1] = totalLength[1];
		buf[2] = totalLength[2];
		buf[3] = totalLength[3];
		buf[4] = type[0];
		buf[5] = type[1];
		buf[6] = msg_type[0];
		buf[7] = unused[0];
		buf[8] = seq_num[0];
		buf[9] = seq_num[1];
		buf[10] = seq_num[2];
		buf[11] = seq_num[3];
		
		for (int i = 0; i < length; i++)
			buf[12 + i] = data[i];

		return buf; // 헤더하고 데이터가 합쳐진 배열
	}

	public void setHeader(int length, byte[] type, byte msg_type, byte[] seq_number) {// 프레임
		
		m_sHeader.fapp_totlen = intToByte4(length);// 문자열의 길이저장..
		m_sHeader.fapp_type = type;
		m_sHeader.fapp_msg_type = msg_type;
		m_sHeader.fapp_seq_num = seq_number;
	}

	public void sendFregmentionPieceToUnderLayer(byte[] bytes, int length) {
		this.GetUnderLayer().Send(bytes, length);// EthernetLayer에 send

	}

	public byte[] cutFrontDataByteArrayAsSize(byte[] input, int size) {// size만큼 바이트배열의 앞부분을 자른다.
		byte[] data = new byte[(input.length) - size];
		for (int i = 0; i < ((input.length) - size); i++) {
			data[i] = input[i + size];
		}
		return data;
	}
	
	public byte[] headerType(int fregmentationNumber){
		byte[] type = new byte[2];
		
		if(fregmentationNumber == 0) {//단편화 안했을때.
			type[0] = 0x00;
			type[1] = 0x00;
		}
		else if(fregmentationNumber == 1) {
			type[0] = 0x00;
			type[1] = 0x01;
		}
		else if(fregmentationNumber == 2) {
			type[0] = 0x00;
			type[1] = 0x02;
		}
		else if(fregmentationNumber == 3) {
			type[0] = 0x00;
			type[1] = 0x03;
		}

		return type;
	}
	
	public boolean Send(byte[] input, int length, byte[] fileName) {// input == data. length == data.length

		Send_Thread thread = new Send_Thread(input,length,fileName,this.GetUpperLayer(0),this.GetUnderLayer());
				
		Thread obj = new Thread(thread);
		obj.start();
		
		return false;
	}
	
	public byte[] RemoveFappHeader(byte[] input, int length) {
		byte[] buf = new byte[length - 12];

		for (int i = 0; i < length - 12; i++) { // 헤더의 크기 12을 뺀 수까지
			buf[i] = input[12 + i];
		}
		return buf; // 헤더를 제거하고 데이터만 남은 배열
	}

	
	public byte[] getSeqNumByte(byte[] input) {
		byte[] seqNum = new byte[4];
		seqNum[0] = input[8];
		seqNum[1] = input[9];
		seqNum[2] = input[10];
		seqNum[3] = input[11];
		
		return seqNum;
	}
	public byte[] getFileAppName(byte[] input) {//파일이름 얻기. 이름에는 NULL없다고 생각함..
		int fileNameLength = 0;
		for(int i = 0; input[i+12] != 0; i++) {//아스키코드에서 0이면 NULL
			fileNameLength++;
		}
		byte[] fileAppName = new byte[fileNameLength];
		for(int i = 0; i < fileNameLength; i++) {//아스키코드에서 0이면 NULL
			fileAppName[i] = input[i+12];
		}
		return fileAppName;
	}
	public byte[] getFileOnlyData(byte[] input,int totlen) {//packet이 60이하일때만 생기는 padding자르기. 
		
		byte[] fileAppData = new byte[totlen];
		for(int i = 0; i < totlen; i++) {
			fileAppData[i] = input[i+12];
		}
		return fileAppData;
	}
	public byte[] getFileAppData(byte[] input) {//packet이 60이상일때 data얻기
		byte[] fileAppData = new byte[input.length-12];
		for(int i=0;i < input.length-12;i++) {
			fileAppData[i] = input[i+12];
		}
		return fileAppData;
	}
	public int count = 0;
	public synchronized boolean Receive(byte[] input) {
		byte[] totlenByte = new byte[4];
		for (int i = 0; i < 4; i++) {
			totlenByte[i] = input[i];
		}
		int totlen = byte4ToInt(totlenByte);
		
		int FregmentationPieceNumber = (totlen / 1448)+1+1;// 단편화 할 횟수. FregNum == 0일때는 파일명 보내므로 + 1 더해줌.
		int progress = 0;
		
		if(input[4] == 0x00 && input[5] == 0x00) {// 단편화x
			if(input[6] == 0x00) {//파일명,확장자일떄.
				
				fileName =  new String(getFileAppName(input));//받은 input에서 Data(byte[])만 뽑아낸다. String으로.
				filePath = "./" + fileName;//파일만들어 지는 경로설정.//fileName에는 파일이름과 확장자까지 포함.
				file = new File(filePath);// 생성할 파일의 경로 및 파일명 으로 File 객체 생성
				
				progress = 50;
				this.GetUpperLayer(0).progressBar_value(progress);
			}
			if(input[6] == 0x01) {//파일데이터일때.
				try {
					FileOutputStream fos = new FileOutputStream(file);// FileOutputStream으로 해당 파일을 생성한다. 존재하는 파일일 경우 덮어쓰기함.
					try {
						if(totlen < 60) {
							fos.write(getFileOnlyData(input,totlen));
						}
						else {
							fos.write(getFileAppData(input));//파일(os)에 데이터를 기록
						}
						fos.close();
						progress = 100;
						this.GetUpperLayer(0).progressBar_value(progress);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(input[4] == 0x00 && input[5] == 0x01) {//단편화 첫조각.
			//progressBar
			count++;
			progress = (int) (count*100/FregmentationPieceNumber);
			this.GetUpperLayer(0).progressBar_value(progress);
			
			fileName =  new String(getFileAppName(input));//Data(byte[])를 String으로.
			filePath = "./" + fileName;//파일만들어 지는 경로설정.//fileName에는 파일이름과 확장자까지 포함.
			file = new File(filePath);// 생성할 파일의 경로 및 파일명 으로 File 객체 생성 
			//buffer = ByteBuffer.allocate(totlen);//나중에 정렬후 쓰는 buffer
		}
		else if(input[4] == 0x00 && input[5] == 0x02) {
			//progressBar
			count++;
			progress = (int) (count*100/FregmentationPieceNumber);
			this.GetUpperLayer(0).progressBar_value(progress);
			
			arrayList.add(new Point(getFileAppData(input),byte4ToInt(getSeqNumByte(input))));//(file의 data),(int형 seq_num)
			
		}
		else if(input[4] == 0x00 && input[5] == 0x03) {
			if(getFileAppData(input).length < 60) {
				arrayList.add(new Point(getFileOnlyData(input,getFileAppData(input).length),byte4ToInt(getSeqNumByte(input))));
			}
			else {
				arrayList.add(new Point(getFileAppData(input),byte4ToInt(getSeqNumByte(input))));
			}
//			int lastFileSize = totlen - (byte4ToInt(getSeqNumByte(input))-1)*1448;//마지막 파일조각의 크기 : 파일전체크기 - (조각순서-1)*1448bytes
			Collections.sort(arrayList);//순서대로(내림차순) 정렬.
			try {
				FileOutputStream fos = new FileOutputStream(file);// FileOutputStream으로 해당 파일을 생성한다. 존재하는 파일일 경우 덮어쓰기함.
				try {
					for(Point o : arrayList) {
					fos.write(o.getData());//파일(os)에 데이터를 기록:정렬된것 순서대로 집어넣음
					}
					arrayList.clear();
					fos.close();
					//progressBar
					progress = 100;
					this.GetUpperLayer(0).progressBar_value(progress);
					count = 0;//ProgressBar 나타내는 카운트 초기화.
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	public void progressBar_value(int progress) {}
	
	class Send_Thread implements Runnable{
		
		byte[] input;
		int length;
		byte[] fileName;
		BaseLayer UpperLayer;
		BaseLayer UnderLayer;
		byte[] bytes;
		int progress;
		int FregmentationPieceNumber;
		
		public Send_Thread(byte[] input, int length, byte[] fileName,BaseLayer m_UpperLayer, BaseLayer m_UnderLayer) {
			this.input = input;
			this.length = length;
			this.fileName = fileName;
			this.UpperLayer = m_UpperLayer;
			this.UnderLayer = m_UnderLayer;
		}
		
		public void run() {
			if (length <= 1448) {// 파일의 크기가 1448바이트 이하라서 단편화 하지 않음.
				progress = 0;
				/*****첫번째 패킷*****/
				setHeader(length, headerType(0),(byte) 0x00,intToByte4(0));
				bytes = ObjToByte(m_sHeader, fileName, fileName.length);//첫 조각에는  파일명이 들어감.(파일이름)
				UnderLayer.setHeaderTypeToFile();//이더넷헤더의 타입을 0x2090으로
				UnderLayer.Send(bytes, bytes.length);// EthernetLayer에 send
				progress = 50;
				UpperLayer.progressBar_value(progress);
				/*****두번째 패킷*****/
				setHeader(length, headerType(0),(byte) 0x01,intToByte4(1));//두번째 조각에는  파일의 데이터가 들어감.(파일이름)
				bytes = ObjToByte(m_sHeader, input, length);
				UnderLayer.setHeaderTypeToFile();//이더넷헤더의 타입을 0x2090으로
				UnderLayer.Send(bytes, bytes.length);
				progress = 100;
				UpperLayer.progressBar_value(progress);
				progress = 0;
			} 
			else if (1448 < length) {// 파일의 크기가 1448바이트 보다 크면 단편화
				FregmentationPieceNumber = (length / 1448)+1+1;// 단편화 할 횟수. FregNum == 0일때는 파일명 보내므로 + 1 더해줌.
				for (int i = 0; i < FregmentationPieceNumber; i++) {
					if (i == 0) {
						setHeader(length,headerType(1),(byte) 0x00,intToByte4(i));// 첫 조각의 헤더에는 데이터의 전체길이와 type = 0x00을 넣어준다.
						bytes = ObjToByte(m_sHeader, fileName, fileName.length);// input에 헤더를 붙인 데이터를 bytes 배열에 저장한다.
						//progress bar 구현.
						progress = (int) (i*100/FregmentationPieceNumber);
						UpperLayer.progressBar_value(progress);
						UnderLayer.setHeaderTypeToFile();//이더넷헤더의 타입을 0x2090으로
						sendFregmentionPieceToUnderLayer(bytes, bytes.length); // bytes를 밑 layer로 보낸다.
					} 
					else if ((0 < i) && (i < FregmentationPieceNumber - 1)) {// 중간 조각의 헤더에는 데이터의 전체길이와 type = 0x01을 넣어준다.
						setHeader(length,headerType(2),(byte) 0x01,intToByte4(i));
						bytes = ObjToByte(m_sHeader, input, 1448);//Header(12) + data의 앞 1448바이트
						//progress bar 구현.
						progress = (int) (i*100/FregmentationPieceNumber);
						UpperLayer.progressBar_value(progress);
						UnderLayer.setHeaderTypeToFile();//이더넷헤더의 타입을 0x2090으로
						sendFregmentionPieceToUnderLayer(bytes, bytes.length);
						input = cutFrontDataByteArrayAsSize(input, 1448);//중간 조각데이터의 크기 == 1448
						System.out.println(i+"번째 조각 보내는중 ..");
					} 
					else {// 마지막 조각의 헤더에는 데이터의 전체길이와 type = 0x02을 넣어준다.
						setHeader(length,headerType(3),(byte) 0x01,intToByte4(i));
						bytes = ObjToByte(m_sHeader, input, input.length);
						UnderLayer.setHeaderTypeToFile();//이더넷헤더의 타입을 0x2090으로
						sendFregmentionPieceToUnderLayer(bytes, bytes.length);// (bytes의 크기는 input.length(데이터) + 12(헤더))
						progress = 100;
						UpperLayer.progressBar_value(progress);
						progress = 0;
					}
				}
			}
		}
	}

	@Override
	public void setHeaderTypeToChat() {
		// TODO Auto-generated method stub
		
	}

	public void setHeaderTypeToFile() {
		// TODO Auto-generated method stub
		
	}
}