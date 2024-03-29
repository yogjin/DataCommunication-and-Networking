package stopWait;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;//오류 메시지 출력을 위해 import
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.jnetpcap.PcapIf;

public class StopWaitDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextField ChattingWrite;

	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcAddress;
	JTextArea dstAddress;

	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton Chat_send_Button;

	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;

	String Text;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* ************************** "1": Layer 연결 *************************** */

		m_LayerMgr.AddLayer(new NILayer("NI"));
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(new StopWaitDlg("GUI"));
		
		m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *Chat ( *GUI ) ) ");
		
	}
	public String ByteToStr(byte[] hardwareAddress) {
        // TODO Auto-generated method stub
           final StringBuilder buf = new StringBuilder();
           for(byte b : hardwareAddress) {
              if(buf.length() != 0) {
                 buf.append('-');
              }
              if(b>=0&&b<16) {////////////????????????????????????다른코드알아보기
                 buf.append('0');
              }
              buf.append(Integer.toHexString((b<0)?b+256:b).toUpperCase());
           }
           return buf.toString();
     }

	public StopWaitDlg(String pName) {
		pLayerName = pName;

		setTitle("StopWaitProtocol");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ChattingArea = new JTextArea();
		ChattingArea.setEditable(false);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(384, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		/*
		 
		 */
		NICComboBox = new JComboBox<String>();
		NICComboBox.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		NICComboBox.setBounds(10, 54, 170, 20);
		//((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterList(); //어짜피 NILayer생성할때 생성자에서 SetAdapterList()호출해줌.
		List<PcapIf> adapterList= ((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList;
		for(int i = 0;i < adapterList.size();i++) {//adapterList의 사이즈만큼 콤보박스의 index추가.
		NICComboBox.addItem(adapterList.get(i).getDescription());
		
		}
		settingPanel.add(NICComboBox);
		
		
		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);

		lblsrc = new JLabel("\uC2DC\uC791 \uC8FC\uC18C");
		lblsrc.setBounds(10, 75, 170, 20);
		settingPanel.add(lblsrc);

		srcAddress = new JTextArea();
		srcAddress.setBounds(2, 2, 170, 20);
		NICComboBox.addActionListener(new ActionListener() {//NICComboBox선택이 변할때 마다 반응해서 동작.
			public void actionPerformed(ActionEvent e) {
				int index = NICComboBox.getSelectedIndex();//NICComboBox가 선택한 index를 가져옴
				String macAddress = null;
				//System.out.println("hello");
				try {
					macAddress=ByteToStr(((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.get(index).getHardwareAddress());//hardwareaddress를 str으로 바꿔서 mac주소 저장.
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            srcAddress.setText(macAddress);//맥주소 출력
	            adapterNumber=index;//어답터번호 저장.
			}
		});
		
		sourceAddressPanel.add(srcAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("\uBAA9\uC801\uC9C0 \uC8FC\uC18C");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstAddress = new JTextArea();
		dstAddress.setBounds(2, 2, 168, 20);
		destinationAddressPanel.add(dstAddress);// dst address

		Setting_Button = new JButton("\uC124\uC815");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting
		
		
		JLabel label = new JLabel("NIC 선택");
		label.setBounds(10, 29, 190, 20);
		settingPanel.add(label);

		Chat_send_Button = new JButton("\uC804\uC1A1");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button

		setVisible(true);

	}

	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			/*
			 * 
			 * 과제 Setting 버튼과 Send 버튼을 누를 시 행동
			 * 
			 * Setting 버튼 누를 시 SocketLayer에서 포트 설정
			 * 
			 */
			
			/* ************************** "2": Setting 버튼이 눌렸을 때의 동작처리  *************************** */
			if(e.getSource() == Setting_Button) {
					if(Setting_Button.getText() == "Reset") { //리셋을 다시 눌렀을 때 
						srcAddress.setText(null);
						dstAddress.setText(null); //주소들의 text를 공백으로 변경
						dstAddress.setEnabled(true);
						srcAddress.setEnabled(true);//다시 주소를 set할수있게 true로 변경
						
						Setting_Button.setText("Setting"); //reset button을 다시 setting button으로
					} else {
					
						String Ssrc = srcAddress.getText(); //setting에서 입력란의 text를 getText()로 받아서 String에 저장 
						String Sdst = dstAddress.getText();
						
						((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(Ssrc);
						((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetDstAddress(Sdst);//mac주소 설정.
						
						((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(adapterNumber); //
						
						Setting_Button.setText("Reset");
						dstAddress.setEnabled(false);
						srcAddress.setEnabled(false);
					}
				}
			/* ************************** "3": Send 버튼이 눌렸을 때의 동작처리  *************************** */
				if(e.getSource() == Chat_send_Button) {
					if(Setting_Button.getText() == "Reset") { //1.Setting Button이 Reset인지 확인
						
						String input = ChattingWrite.getText();
						ChattingArea.append("[SEND]: "+ input + "\n"); //2.ChattingWrite에 적은 Text를 Chatting area에 보여준다.
						//setText() replaces all the text from the existing one and append() adds your new value to existing one
						byte[] inputbytes = input.getBytes(); 
						// 3. String을 Byte형식으로 변경해서  ChatAppLayer에 Send호출해서  보낸다.
						((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).Send(inputbytes, inputbytes.length);
					}
					else {//4. 주소값이 없으면 "주소설정오류" MessageDialog를 띄운다.
						JOptionPane.showMessageDialog(null, "주소 설정 오류", "오류", JOptionPane.ERROR_MESSAGE);

					}
				}
				
		}
	}

	public boolean Receive(byte[] input) {	
/*
 * 	과제 채팅 화면에 채팅 보여주기
 * 
 */
		// 주소설정
		String message;
		try {
			message = new String(input,"MS949");
			ChattingArea.append("[RECV]: "+ message  + "\n");
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		} 
	
		return true;
	}
	
	public static String convertBinaryStringToString(String string){
	    StringBuilder sb = new StringBuilder();
	    char[] chars = string.replaceAll("\\s", "").toCharArray();
	    int [] mapping = {1,2,4,8,16,32,64,128};

	    for (int j = 0; j < chars.length; j+=8) {
	        int idx = 0;
	        int sum = 0;
	        for (int i = 7; i>= 0; i--) {
	            if (chars[i+j] == '1') {
	                sum += mapping[idx];
	            }
	            idx++;
	        }
	        System.out.println(sum);//debug
	        sb.append(Character.toChars(sum));
	    }
	    return sb.toString();
	}
	/**
     * 바이너리 바이트 배열을 스트링으로 변환
     * 
     * @param b
     * @return
     */
    public static String byteArrayToBinaryString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            sb.append(byteToBinaryString(b[i]));
        }
        return sb.toString();
    }
    
    /**
     * 바이너리 바이트를 스트링으로 변환
     * 
     * @param n
     * @return
     */
    public static String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
}