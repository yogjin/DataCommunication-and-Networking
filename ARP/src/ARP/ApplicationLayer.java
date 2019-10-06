package ARP;

import java.util.ArrayList;

public class ApplicationLayer implements BaseLayer{//Application층
	public int nUpperLayerCount = 0;//?
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	/*
	 * 자료구조 알게되면 추가. 알아서 설정해야하나?
	 *_APPLICATION_HEADER m_sHeader = new _APPLICATION_HEADER();
	 */
	
	public ApplicationLayer(String pName) {// 생성자
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}
	
	public void ResetHeader() {
		
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
