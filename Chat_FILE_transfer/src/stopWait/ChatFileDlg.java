package stopWait;

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;//���� �޽��� ����� ���� import
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import org.jnetpcap.PcapIf;

public class ChatFileDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextField ChattingWrite;
	private JTextField FilePath;
	
	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcAddress;
	JTextArea dstAddress;

	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton Chat_send_Button;
	JButton File_send_Button;
	
	JProgressBar progressBar;
	
	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;

	String Text;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* ************************** "1": Layer ���� *************************** */

		m_LayerMgr.AddLayer(new NILayer("NI"));
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(new FileAppLayer("File"));
		m_LayerMgr.AddLayer(new ChatFileDlg("GUI"));
		
		m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *File ( *GUI ) *Chat ( *GUI ) ) ");//m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *File ( *GUI ) *Chat ( *GUI ) ) ");
		
	}
	public String ByteToStr(byte[] hardwareAddress) {
        // TODO Auto-generated method stub
           final StringBuilder buf = new StringBuilder();
           for(byte b : hardwareAddress) {
              if(buf.length() != 0) {
                 buf.append('-');
              }
              if(b>=0&&b<16) {
                 buf.append('0');
              }
              buf.append(Integer.toHexString((b<0)?b+256:b).toUpperCase());
           }
           return buf.toString();
     }

	public ChatFileDlg(String pName) {
		pLayerName = pName;

		setTitle("StopWaitProtocol");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ä��",
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
		
//		JScrollPane scrollPane = new JScrollPane(ChattingArea);
//		contentPane.add(scrollPane);
		
		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 18);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "����",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(384, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel fileSendingPanel = new JPanel();
		fileSendingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "��������",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		fileSendingPanel.setBounds(10, 282, 360, 94);
		contentPane.add(fileSendingPanel);
		fileSendingPanel.setLayout(null);
		
		/*
		 
		 */
		NICComboBox = new JComboBox<String>();
		NICComboBox.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		NICComboBox.setBounds(10, 54, 170, 20);
		//((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterList(); //��¥�� NILayer�����Ҷ� �����ڿ��� SetAdapterList()ȣ������.
		List<PcapIf> adapterList= ((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList;
		for(int i = 0;i < adapterList.size();i++) {//adapterList�� �����ŭ �޺��ڽ��� index�߰�.
		NICComboBox.addItem(adapterList.get(i).getDescription());
		
		}
		settingPanel.add(NICComboBox);
		
		
		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);

		lblsrc = new JLabel("���� �ּ�");
		lblsrc.setBounds(10, 75, 170, 20);
		settingPanel.add(lblsrc);

		srcAddress = new JTextArea();
		srcAddress.setBounds(2, 2, 170, 20);
		NICComboBox.addActionListener(new ActionListener() {//NICComboBox������ ���Ҷ� ���� �����ؼ� ����.
			public void actionPerformed(ActionEvent e) {
				int index = NICComboBox.getSelectedIndex();//NICComboBox�� ������ index�� ������
				String macAddress = null;
				//System.out.println("hello");
				try {
					macAddress=ByteToStr(((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.get(index).getHardwareAddress());//hardwareaddress�� str���� �ٲ㼭 mac�ּ� ����.
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            srcAddress.setText(macAddress);//���ּ� ���
	            adapterNumber=index;//����͹�ȣ ����.
			}
		});
		
		sourceAddressPanel.add(srcAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("������ �ּ�");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstAddress = new JTextArea();
		dstAddress.setBounds(2, 2, 168, 20);
		destinationAddressPanel.add(dstAddress);// dst address

		Setting_Button = new JButton("����");// setting
		Setting_Button.setBounds(44, 270, 100, 27);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting
		
		
		JLabel label = new JLabel("NIC ����");
		label.setBounds(10, 29, 190, 20);
		settingPanel.add(label);

		setVisible(true);

		Chat_send_Button = new JButton("����");
		Chat_send_Button.setBounds(270, 230, 80, 34);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);
		
		FilePath = new JTextField();
		FilePath.setBounds(10, 22, 257, 25);
		fileSendingPanel.add(FilePath);
		
		
		Button button = new Button("����...");
		button.setBounds(273, 22, 78, 25);
		fileSendingPanel.add(button);
		
		File_send_Button = new JButton("����");
		File_send_Button.setBounds(273, 57, 78, 25);
		fileSendingPanel.add(File_send_Button);
		
		File_send_Button.addActionListener(new setAddressListener());
		
		button.addActionListener(new ActionListener() {//����... ��������.
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				File file = chooser.getSelectedFile();
				String filename = file.getAbsolutePath();
				FilePath.setText(filename);
				FilePath.setEnabled(false);
			}
		});

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 59, 257, 23);
		progressBar.setStringPainted(true);// true�� �����ϸ� ���� �����Ȳ�� %�� ǥ����.
		fileSendingPanel.add(progressBar);
		
	}
	
	public void progressBar_value(int progress) {
		
		
		progressBar.setValue(progress);
      
        if(progress == 100) {//progress�� 100�̸� â ���� 0���� �ٲٱ�.
        	JOptionPane.showMessageDialog(null, "���� ���� �Ϸ�");
        	progressBar.setValue(0);
        }
    }// progress_start()��
	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			/*
			 * 
			 * ���� Setting ��ư�� Send ��ư�� ���� �� �ൿ
			 * 
			 * Setting ��ư ���� �� SocketLayer���� ��Ʈ ����
			 * 
			 */
			
			/* ************************** "2": Setting ��ư�� ������ ���� ����ó��  *************************** */
			if(e.getSource() == Setting_Button) {
					if(Setting_Button.getText() == "Reset") { //������ �ٽ� ������ �� 
						srcAddress.setText(null);
						dstAddress.setText(null); //�ּҵ��� text�� �������� ����
						dstAddress.setEnabled(true);
						srcAddress.setEnabled(true);//�ٽ� �ּҸ� set�Ҽ��ְ� true�� ����
						
						Setting_Button.setText("Setting"); //reset button�� �ٽ� setting button����
					} else {
					
						String Ssrc = srcAddress.getText(); //setting���� �Է¶��� text�� getText()�� �޾Ƽ� String�� ���� 
						String Sdst = dstAddress.getText();
						
						((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(Ssrc);
						((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetDstAddress(Sdst);//mac�ּ� ����.
						
						((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(adapterNumber); //
						
						Setting_Button.setText("Reset");
						dstAddress.setEnabled(false);
						srcAddress.setEnabled(false);
					}
				}
			/* ************************** "3": ä���� Send ��ư�� ������ ���� ����ó��  *************************** */
				if(e.getSource() == Chat_send_Button) {
					if(Setting_Button.getText() == "Reset") { //1.Setting Button�� Reset���� Ȯ��
						
						String input = ChattingWrite.getText();
						ChattingArea.append("[SEND]: "+ input + "\n"); //2.ChattingWrite�� ���� Text�� Chatting area�� �����ش�.
						//setText() replaces all the text from the existing one and append() adds your new value to existing one
						byte[] inputbytes = input.getBytes(); 
						// 3. String�� Byte�������� �����ؼ�  ChatAppLayer�� Sendȣ���ؼ�  ������.
						((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).Send(inputbytes, inputbytes.length);
					}
					else {//4. �ּҰ��� ������ "�ּҼ�������" MessageDialog�� ����.
						JOptionPane.showMessageDialog(null, "�ּ� ���� ����", "����", JOptionPane.ERROR_MESSAGE);

					}
				}
			
			/* ************************** "4": ������ Send ��ư�� ������ ���� ����ó��  *************************** */
				if(e.getSource() == File_send_Button) {
					if(Setting_Button.getText() == "Reset") { //1.Setting Button�� Reset���� Ȯ��
						
						String filePath = FilePath.getText();//���ϰ��
						String fileName = getFileName(filePath);//�����̸�
						int fileSize = getFileSize(filePath);//���ϻ�����
						System.out.println("����ũ��: "+fileSize);
						byte[] inputbytes = new byte[fileSize];//������ ũ�⿡ �´� byteArray ����.
						
						FileInputStream fis = null;
						try {
							fis = new FileInputStream(filePath);
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							fis.read(inputbytes);//������ byteArray�� �ٲ㼭 inputbytes�� ����.
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							fis.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						// 3. File�� Byte�������� �����ؼ�  FileAppLayer�� Sendȣ���ؼ�  ������.
						((FileAppLayer) m_LayerMgr.GetLayer("File")).Send(inputbytes, inputbytes.length, stringToByteArray(fileName));
					}
					else {//4. �ּҰ��� ������ "�ּҼ�������" MessageDialog�� ����.
						JOptionPane.showMessageDialog(null, "�ּ� ���� ����", "����", JOptionPane.ERROR_MESSAGE);

					}
				}
		}
	}
	
	
	
	public byte[] stringToByteArray(String fileName) {
		byte[] fileNameToByteArray = fileName.getBytes();
		return fileNameToByteArray;
	}
	public int getFileSize(String filePath) {
		long getSize = 0;
		int size = 0;
		File file = new File(filePath);
		if(file.exists()) {
			getSize = file.length();
			size = (int) getSize;
		}
		return size;
	}
	public String getFileName(String filePath) {
		
		String[] fileName = filePath.split("\\\\");
		String file = null;
		for (int i = 0; i < fileName.length; i++) {
			if(i == fileName.length-1) {
				file = fileName[i];
			}
		}
		return file;
	}
	public boolean Receive(byte[] input) {	
/*
 * 	���� ä�� ȭ�鿡 ä�� �����ֱ�
 * 
 */
		// �ּҼ���
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
     * ���̳ʸ� ����Ʈ �迭�� ��Ʈ������ ��ȯ
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
     * ���̳ʸ� ����Ʈ�� ��Ʈ������ ��ȯ
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
	@Override
	public void setHeaderTypeToChat() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setHeaderTypeToFile() {
		// TODO Auto-generated method stub
		
	}
}