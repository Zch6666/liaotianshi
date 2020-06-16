package UI;
 
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
 
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
 
public class ChatRoomClientFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7909370607161820239L;
	
	private JPanel panel;
	private JPanel northPanel;
	private JPanel southPanel;
	private JTextField port;
	private JTextField IP;
	private JTextField userName;
	private JTextArea userlist;
	private JTextArea chatRecords;
	private JScrollPane leftPanel;
	private JScrollPane rightPanel;
	private JSplitPane  centerPanel;
	private JTextField message;
	private JButton link;
	private JButton discon;
	private JButton send;
	private JPanel Jpanel;
	private JMenuBar bar;
	private JMenu file;
	private JMenuItem save;
	private JMenu format;
	private JMenuItem font;
	private JMenuItem foreGround;
	private JMenuItem backGround;
	private Font myfont;
	
	private boolean isConnect = false;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private ClientThread ct;
	//private ArrayList<String> users = new ArrayList<String>();
	private String[] userstr;
	private int usersLength = 0;
 
 
	public ChatRoomClientFrame(){
		
		initServer();
		
		link.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(userName.getText().trim().equals("")){
					JOptionPane.showMessageDialog(null, "用户名不能为空","错误",JOptionPane.ERROR_MESSAGE);
				}else{
				
					try {
						socket = new Socket(IP.getText(),Integer.parseInt(port.getText()));
						dos = new DataOutputStream(socket.getOutputStream());
						dis = new DataInputStream(socket.getInputStream());
						
						if(socket!=null){
							port.setEditable(false);
							IP.setEditable(false);
							userName.setEditable(false);
							link.setEnabled(false);
							discon.setEnabled(true);
							
							ct = new ClientThread();
							ct.start();
							
							if(isConnect==false){
								dos.writeUTF(userName.getText()+" 进入了聊天室");
								isConnect=true;
							}
						}	
					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "服务器未开启","错误",JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					} 
					
				}
			}
		});
		
		discon.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(isConnect==true){
					isConnect=false;
					dos.writeUTF(userName.getText()+" 离开了聊天室");
					userlist.setText("");
					userstr=null;
					usersLength=0;
					dos.close();
					dis.close();
					socket.close();
					chatRecords.append("\n");
					chatRecords.append("你离开了聊天室");
					chatRecords.setCaretPosition(chatRecords.getDocument().getLength());
					port.setEditable(true);
					IP.setEditable(true);
					userName.setEditable(true);
					link.setEnabled(true);
					discon.setEnabled(false);
					ct.stop();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		send.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		
		message.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		
		font.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				MyFontChooser mf = new MyFontChooser(chatRecords.getFont());
				 int returnValue = mf.showFontDialog(ChatRoomClientFrame.this);  
	                // 如果按下的是确定按钮  
                if (returnValue == MyFontChooser.APPROVE_OPTION) {  
                    // 获取选择的字体  
                    Font font = mf.getSelectFont();  
                    // 将字体设置到JTextArea中  
                    chatRecords.setFont(font);  
                }
			}
		});
		
		save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog saveFile = new FileDialog(ChatRoomClientFrame.this, "保存文件...", FileDialog.SAVE);
				saveFile.setVisible(true);
				String filePath = saveFile.getDirectory() + saveFile.getFile();
			
				try {
					FileOutputStream fos = new FileOutputStream(filePath);
					fos.write(chatRecords.getText().getBytes());
					fos.close();
					
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		backGround.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color bc = JColorChooser.showDialog(ChatRoomClientFrame.this, "选择颜色", Color.BLACK);
				chatRecords.setBackground(bc);
			}
		});
		
		foreGround.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color fc = JColorChooser.showDialog(ChatRoomClientFrame.this, "选择颜色", Color.BLACK);
				chatRecords.setForeground(fc);
			}
		});
 
		initFrame();
	}
	
	private void initServer() {
		Jpanel = new JPanel();
		Jpanel.setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		northPanel = new JPanel();
		northPanel.setBorder(new TitledBorder("连接信息"));
		
		bar = new JMenuBar();
		file = new JMenu("记录");
		save = new JMenuItem("保存");
		format = new JMenu("格式");
		font = new JMenuItem("字体");
		foreGround = new JMenuItem("前景色");
		backGround = new JMenuItem("背景色");
		file.add(save);
		format.add(font);
		format.add(foreGround);
		format.add(backGround);
		bar.add(file);
		bar.add(format);
		Jpanel.add(bar,BorderLayout.NORTH);
		
		port = new JTextField(8);
		port.setText("521");
		IP = new JTextField(8);
		IP.setText("192.1.1.1");
		userName = new JTextField(8);
		link= new JButton("启动");
		discon= new JButton("停止");
		discon.setEnabled(false);
		northPanel.add(new JLabel("端口号"));
		northPanel.add(port);
		northPanel.add(new JLabel("服务器IP"));
		northPanel.add(IP);
		northPanel.add(new JLabel("用户名"));
		northPanel.add(userName);
		northPanel.add(link);
		northPanel.add(discon);
		panel.add(northPanel,BorderLayout.NORTH);
		
		userlist = new JTextArea();
		userlist.setLineWrap(true);
		userlist.setEditable(false);
		leftPanel =new JScrollPane(userlist);
		leftPanel.setBorder(new TitledBorder("在线用户"));
		panel.add(leftPanel,BorderLayout.EAST);
		
		chatRecords = new JTextArea();
		chatRecords.setLineWrap(true);
		chatRecords.setEditable(false);
		rightPanel =new JScrollPane(chatRecords);
		rightPanel.setBorder(new TitledBorder("聊天信息"));
		centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);  
		centerPanel.setDividerLocation(100);  
		panel.add(centerPanel,BorderLayout.CENTER);
		
		southPanel = new JPanel();
		southPanel.setBorder(new TitledBorder("写消息"));
		message = new JTextField(50);
		send = new JButton("发送");
		southPanel.add(message);
		southPanel.add(send);
		panel.add(southPanel, BorderLayout.SOUTH);
		Jpanel.add(panel,BorderLayout.CENTER);
		this.add(Jpanel);
	}
 
	private void initFrame(){
		this.setTitle("MyTestChatRoom");
		this.setSize(640,480);
		this.setLocationRelativeTo(null);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.getImage("img/logo.jpg");
		this.setIconImage(img);
		this.setResizable(false);
		opening();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		closing();
	}
 
	public class ClientThread extends Thread{
 
		@Override
		public void run() {
			String message=null;
			while(true){
				try {
					message = dis.readUTF();				
					if(message.contains("@在线用户列表@")){
						userstr = message.split(" ");
						if(userstr.length!=usersLength){
							userlist.setText("");
							for(int i=0;i<userstr.length-1;i++){
							   userlist.append(userstr[i]);
							   userlist.append("\n");
							}
							usersLength = userstr.length;
						}
						
					}else{
						chatRecords.append("\n");
						chatRecords.append(message);
						chatRecords.setCaretPosition(chatRecords.getDocument().getLength());
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					if(isConnect==true)
						JOptionPane.showMessageDialog(null, "服务器断开连接","错误",JOptionPane.ERROR_MESSAGE);
					link.setEnabled(true);
					discon.setEnabled(false);
					isConnect = false;
					port.setEditable(true);
					IP.setEditable(true);
					userName.setEditable(true);
					userlist.setText("");
					userstr=null;
					usersLength=0;
					try {
						dis.close();
						dos.close();
						socket.close();
						ct.stop();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		}
	}
 
	public void send(){
		if(isConnect==false){
			JOptionPane.showMessageDialog(null, "未连接服务器，无法发送消息","错误",JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(message.getText().trim().equals("")){
			JOptionPane.showMessageDialog(null, "消息不能为空","错误",JOptionPane.ERROR_MESSAGE);
			return;
		}
		String str = message.getText();	
		sendMessage(str);	
	}
 
	public void sendMessage(String str){
		try {
			dos.writeUTF(userName.getText()+"： "+str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		message.setText("");
	}
 
	public void opening(){
		Properties p = new Properties();
 
		try {
			if(new File("src/FontClient.properties").exists()){
				p.load(new FileReader("src/FontClient.properties"));
				myfont = new Font(p.getProperty("FontName"),Integer.parseInt(p.getProperty("FontStyle")),Integer.parseInt(p.getProperty("FontSize")));
				chatRecords.setFont(myfont);
				chatRecords.setForeground(new Color(Integer.parseInt(p.getProperty("foreColor"))));
				chatRecords.setBackground(new Color(Integer.parseInt(p.getProperty("backColor"))));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closing(){
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(isConnect == true){
					try {
						dos.writeUTF(userName.getText()+" 离开了聊天室");
						dis.close();
						dos.close();
						socket.close();
						ct.stop();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				Properties size = new Properties();
				size.setProperty("FontName", ChatRoomClientFrame.this.chatRecords.getFont().getFamily());
				size.setProperty("FontStyle", ChatRoomClientFrame.this.chatRecords.getFont().getStyle()+"");
				size.setProperty("FontSize", ChatRoomClientFrame.this.chatRecords.getFont().getSize()+"");
				size.setProperty("foreColor", ChatRoomClientFrame.this.chatRecords.getForeground().getRGB()+"");
				size.setProperty("backColor", ChatRoomClientFrame.this.chatRecords.getBackground().getRGB()+"");
				try {
					FileWriter fr = new FileWriter("src/FontClient.properties");
					size.store(fr, "FontClient Info");
					fr.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}
}
