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
	
	static class Point implements Comparable<Point>{//1448����Ʈ�� �Ѵ� ���Ϲ����� ����ȭ ������ ��ü�迭�� Sorting�ϱ� ���� ����.
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

	public byte[] ObjToByte(_FAPP_HEADER Header, byte[] data, int length) { // ������ ���� data�� ���� layer�� header�� ��ġ�� �Լ�
		byte[] buf = new byte[length + 12];// FileAppHeader�� ũ�� == 12
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

		return buf; // ����ϰ� �����Ͱ� ������ �迭
	}

	public void setHeader(int length, byte[] type, byte msg_type, byte[] seq_number) {// ������
		
		m_sHeader.fapp_totlen = intToByte4(length);// ���ڿ��� ��������..
		m_sHeader.fapp_type = type;
		m_sHeader.fapp_msg_type = msg_type;
		m_sHeader.fapp_seq_num = seq_number;
	}

	public void sendFregmentionPieceToUnderLayer(byte[] bytes, int length) {
		this.GetUnderLayer().Send(bytes, length);// EthernetLayer�� send

	}

	public byte[] cutFrontDataByteArrayAsSize(byte[] input, int size) {// size��ŭ ����Ʈ�迭�� �պκ��� �ڸ���.
		byte[] data = new byte[(input.length) - size];
		for (int i = 0; i < ((input.length) - size); i++) {
			data[i] = input[i + size];
		}
		return data;
	}
	
	public byte[] headerType(int fregmentationNumber){
		byte[] type = new byte[2];
		
		if(fregmentationNumber == 0) {//����ȭ ��������.
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

		for (int i = 0; i < length - 12; i++) { // ����� ũ�� 12�� �� ������
			buf[i] = input[12 + i];
		}
		return buf; // ����� �����ϰ� �����͸� ���� �迭
	}

	
	public byte[] getSeqNumByte(byte[] input) {
		byte[] seqNum = new byte[4];
		seqNum[0] = input[8];
		seqNum[1] = input[9];
		seqNum[2] = input[10];
		seqNum[3] = input[11];
		
		return seqNum;
	}
	public byte[] getFileAppName(byte[] input) {//�����̸� ���. �̸����� NULL���ٰ� ������..
		int fileNameLength = 0;
		for(int i = 0; input[i+12] != 0; i++) {//�ƽ�Ű�ڵ忡�� 0�̸� NULL
			fileNameLength++;
		}
		byte[] fileAppName = new byte[fileNameLength];
		for(int i = 0; i < fileNameLength; i++) {//�ƽ�Ű�ڵ忡�� 0�̸� NULL
			fileAppName[i] = input[i+12];
		}
		return fileAppName;
	}
	public byte[] getFileOnlyData(byte[] input,int totlen) {//packet�� 60�����϶��� ����� padding�ڸ���. 
		
		byte[] fileAppData = new byte[totlen];
		for(int i = 0; i < totlen; i++) {
			fileAppData[i] = input[i+12];
		}
		return fileAppData;
	}
	public byte[] getFileAppData(byte[] input) {//packet�� 60�̻��϶� data���
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
		
		int FregmentationPieceNumber = (totlen / 1448)+1+1;// ����ȭ �� Ƚ��. FregNum == 0�϶��� ���ϸ� �����Ƿ� + 1 ������.
		int progress = 0;
		
		if(input[4] == 0x00 && input[5] == 0x00) {// ����ȭx
			if(input[6] == 0x00) {//���ϸ�,Ȯ�����ϋ�.
				
				fileName =  new String(getFileAppName(input));//���� input���� Data(byte[])�� �̾Ƴ���. String����.
				filePath = "./" + fileName;//���ϸ���� ���� ��μ���.//fileName���� �����̸��� Ȯ���ڱ��� ����.
				file = new File(filePath);// ������ ������ ��� �� ���ϸ� ���� File ��ü ����
				
				progress = 50;
				this.GetUpperLayer(0).progressBar_value(progress);
			}
			if(input[6] == 0x01) {//���ϵ������϶�.
				try {
					FileOutputStream fos = new FileOutputStream(file);// FileOutputStream���� �ش� ������ �����Ѵ�. �����ϴ� ������ ��� �������.
					try {
						if(totlen < 60) {
							fos.write(getFileOnlyData(input,totlen));
						}
						else {
							fos.write(getFileAppData(input));//����(os)�� �����͸� ���
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
		else if(input[4] == 0x00 && input[5] == 0x01) {//����ȭ ù����.
			//progressBar
			count++;
			progress = (int) (count*100/FregmentationPieceNumber);
			this.GetUpperLayer(0).progressBar_value(progress);
			
			fileName =  new String(getFileAppName(input));//Data(byte[])�� String����.
			filePath = "./" + fileName;//���ϸ���� ���� ��μ���.//fileName���� �����̸��� Ȯ���ڱ��� ����.
			file = new File(filePath);// ������ ������ ��� �� ���ϸ� ���� File ��ü ���� 
			//buffer = ByteBuffer.allocate(totlen);//���߿� ������ ���� buffer
		}
		else if(input[4] == 0x00 && input[5] == 0x02) {
			//progressBar
			count++;
			progress = (int) (count*100/FregmentationPieceNumber);
			this.GetUpperLayer(0).progressBar_value(progress);
			
			arrayList.add(new Point(getFileAppData(input),byte4ToInt(getSeqNumByte(input))));//(file�� data),(int�� seq_num)
			
		}
		else if(input[4] == 0x00 && input[5] == 0x03) {
			if(getFileAppData(input).length < 60) {
				arrayList.add(new Point(getFileOnlyData(input,getFileAppData(input).length),byte4ToInt(getSeqNumByte(input))));
			}
			else {
				arrayList.add(new Point(getFileAppData(input),byte4ToInt(getSeqNumByte(input))));
			}
//			int lastFileSize = totlen - (byte4ToInt(getSeqNumByte(input))-1)*1448;//������ ���������� ũ�� : ������üũ�� - (��������-1)*1448bytes
			Collections.sort(arrayList);//�������(��������) ����.
			try {
				FileOutputStream fos = new FileOutputStream(file);// FileOutputStream���� �ش� ������ �����Ѵ�. �����ϴ� ������ ��� �������.
				try {
					for(Point o : arrayList) {
					fos.write(o.getData());//����(os)�� �����͸� ���:���ĵȰ� ������� �������
					}
					arrayList.clear();
					fos.close();
					//progressBar
					progress = 100;
					this.GetUpperLayer(0).progressBar_value(progress);
					count = 0;//ProgressBar ��Ÿ���� ī��Ʈ �ʱ�ȭ.
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
			if (length <= 1448) {// ������ ũ�Ⱑ 1448����Ʈ ���϶� ����ȭ ���� ����.
				progress = 0;
				/*****ù��° ��Ŷ*****/
				setHeader(length, headerType(0),(byte) 0x00,intToByte4(0));
				bytes = ObjToByte(m_sHeader, fileName, fileName.length);//ù ��������  ���ϸ��� ��.(�����̸�)
				UnderLayer.setHeaderTypeToFile();//�̴�������� Ÿ���� 0x2090����
				UnderLayer.Send(bytes, bytes.length);// EthernetLayer�� send
				progress = 50;
				UpperLayer.progressBar_value(progress);
				/*****�ι�° ��Ŷ*****/
				setHeader(length, headerType(0),(byte) 0x01,intToByte4(1));//�ι�° ��������  ������ �����Ͱ� ��.(�����̸�)
				bytes = ObjToByte(m_sHeader, input, length);
				UnderLayer.setHeaderTypeToFile();//�̴�������� Ÿ���� 0x2090����
				UnderLayer.Send(bytes, bytes.length);
				progress = 100;
				UpperLayer.progressBar_value(progress);
				progress = 0;
			} 
			else if (1448 < length) {// ������ ũ�Ⱑ 1448����Ʈ ���� ũ�� ����ȭ
				FregmentationPieceNumber = (length / 1448)+1+1;// ����ȭ �� Ƚ��. FregNum == 0�϶��� ���ϸ� �����Ƿ� + 1 ������.
				for (int i = 0; i < FregmentationPieceNumber; i++) {
					if (i == 0) {
						setHeader(length,headerType(1),(byte) 0x00,intToByte4(i));// ù ������ ������� �������� ��ü���̿� type = 0x00�� �־��ش�.
						bytes = ObjToByte(m_sHeader, fileName, fileName.length);// input�� ����� ���� �����͸� bytes �迭�� �����Ѵ�.
						//progress bar ����.
						progress = (int) (i*100/FregmentationPieceNumber);
						UpperLayer.progressBar_value(progress);
						UnderLayer.setHeaderTypeToFile();//�̴�������� Ÿ���� 0x2090����
						sendFregmentionPieceToUnderLayer(bytes, bytes.length); // bytes�� �� layer�� ������.
					} 
					else if ((0 < i) && (i < FregmentationPieceNumber - 1)) {// �߰� ������ ������� �������� ��ü���̿� type = 0x01�� �־��ش�.
						setHeader(length,headerType(2),(byte) 0x01,intToByte4(i));
						bytes = ObjToByte(m_sHeader, input, 1448);//Header(12) + data�� �� 1448����Ʈ
						//progress bar ����.
						progress = (int) (i*100/FregmentationPieceNumber);
						UpperLayer.progressBar_value(progress);
						UnderLayer.setHeaderTypeToFile();//�̴�������� Ÿ���� 0x2090����
						sendFregmentionPieceToUnderLayer(bytes, bytes.length);
						input = cutFrontDataByteArrayAsSize(input, 1448);//�߰� ������������ ũ�� == 1448
						System.out.println(i+"��° ���� �������� ..");
					} 
					else {// ������ ������ ������� �������� ��ü���̿� type = 0x02�� �־��ش�.
						setHeader(length,headerType(3),(byte) 0x01,intToByte4(i));
						bytes = ObjToByte(m_sHeader, input, input.length);
						UnderLayer.setHeaderTypeToFile();//�̴�������� Ÿ���� 0x2090����
						sendFregmentionPieceToUnderLayer(bytes, bytes.length);// (bytes�� ũ��� input.length(������) + 12(���))
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