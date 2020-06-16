package UI;
 
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
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
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
 
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
 
 
public class ChatRoomServerFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6582389471265865971L;
	
	private JPanel panel;
	private JPanel northPanel;
	private JPanel southPanel;
	private JTextField IP;
	private JTextField port;
	private JTextArea userlist;
	private JTextArea chatRecords;
	private JScrollPane leftPanel;
	private JScrollPane rightPanel;
	private JSplitPane  centerPanel;
	private JTextField message;
	private JButton start;
	private JButton stop;
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
	private Vector<Socket> lists=new Vector<Socket>(); 
	
	private ServerSocket serverSocket;
	private ServerThread st;
	private DataOutputStream dos;
	private DataInputStream dis;
	private ArrayList<String> users = new ArrayList<String>();
	private int usersLength = 0;
	private UserList ul;
	private String usermessage=null;
	
	private boolean isStart = false;
 
	public ChatRoomServerFrame(){
	
		initServer();
		
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
					try {
						if(port.getText().trim().equals("")){
							JOptionPane.showMessageDialog(null, "端口号不能为空","错误",JOptionPane.ERROR_MESSAGE);
							return;
						}
						if(isStart==false){
							serverSocket = new ServerSocket(Integer.parseInt(port.getText()));
							chatRecords.append("\n");
							chatRecords.append("服务器已开启");
							st = new ServerThread(serverSocket);
							st.start();
							port.setEditable(false);
							start.setEnabled(false);
							stop.setEnabled(true);
							isStart = true;
							ul = new UserList();
							ul.start();
						}
					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		});
 
		stop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					chatRecords.append("\n");
					chatRecords.append("服务器已断开");
					chatRecords.setCaretPosition(chatRecords.getDocument().getLength());
					if(dis != null){
						dis.close();
					}
					if(dos != null){
						dos.close();
					}
					users.removeAll(users);
					userlist.setText("");
					usersLength=0;
					serverSocket.close();
					port.setEditable(true);
					start.setEnabled(true);
					stop.setEnabled(false);
					isStart = false;
					ul.stop();
					st.stop();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		send.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(lists.size()==0){
					JOptionPane.showMessageDialog(null, "当前没有用户在线","错误",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(message.getText().trim().equals("")){
					JOptionPane.showMessageDialog(null, "消息不能为空","错误",JOptionPane.ERROR_MESSAGE);
					return;
				}
				String line="服务器："+message.getText();
				sendMessage(line);
				
				message.setText("");	
			}
		});
		
		message.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(lists.size()==0){
					JOptionPane.showMessageDialog(null, "当前没有用户在线","错误",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(message.getText().trim().equals("")){
					JOptionPane.showMessageDialog(null, "消息不能为空","错误",JOptionPane.ERROR_MESSAGE);
					return;
				}
				String line="服务器："+message.getText();
				sendMessage(line);
				
				message.setText("");	
				
			}
		});
		
		font.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				MyFontChooser mf = new MyFontChooser(chatRecords.getFont());
				 int returnValue = mf.showFontDialog(ChatRoomServerFrame.this);  
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
				FileDialog saveFile = new FileDialog(ChatRoomServerFrame.this, "保存文件...", FileDialog.SAVE);
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
				Color bc = JColorChooser.showDialog(ChatRoomServerFrame.this, "选择颜色", Color.BLACK);
				chatRecords.setBackground(bc);
			}
		});
		
		foreGround.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color fc = JColorChooser.showDialog(ChatRoomServerFrame.this, "选择颜色", Color.BLACK);
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
		northPanel.setBorder(new TitledBorder("配置信息"));
		IP = new JTextField(15);
		String ipAddress = null;
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IP.setText(ipAddress);
		IP.setEditable(false);
		port = new JTextField(15);
		port.setText("8088");
		start = new JButton("启动");
		stop = new JButton("停止");
		stop.setEnabled(false);
		northPanel.add(new JLabel("本地地址"));
		northPanel.add(IP);
		northPanel.add(new JLabel("端口号"));
		northPanel.add(port);
		northPanel.add(start);
		northPanel.add(stop);
		
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
		panel.add(northPanel,BorderLayout.NORTH);
		
		userlist = new JTextArea();
		userlist.setEditable(false);
		leftPanel =new JScrollPane(userlist);
		leftPanel.setBorder(new TitledBorder("在线用户"));
		panel.add(leftPanel,BorderLayout.EAST);
		
		chatRecords = new JTextArea();
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
		Jpanel.add(panel, BorderLayout.CENTER);
		this.add(Jpanel);
	}
 
	private void initFrame(){
		
		this.setTitle("聊天室-服务器");
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
	
	private class MSThread implements Runnable{
		
		private Socket socket;
		private DataInputStream dis ;
		
		public MSThread(Socket socket) {
			
			try {
				this.socket = socket;
				dis = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			
			String message=null;
			try {
				while(true){
					message = dis.readUTF();
					if(message.contains("进入了聊天室")&&!message.contains("："))
						users.add(message.split(" ")[0]);
					if(message.contains("离开了聊天室")&&!message.contains("：")){
						users.remove(message.split(" ")[0]);
						sendMessage(message);
						dis.close();
						socket.close();
						lists.remove(socket);
					}else{
						sendMessage(message);
					}
				}
			} catch (IOException e) {
				lists.remove(socket);
				e.printStackTrace();
			}
		}
	}
 
	public void sendMessage(String str){
		try {
			for(int i=0;i<lists.size();i++){
				dos = new DataOutputStream(lists.get(i).getOutputStream());
				dos.writeUTF(str);
			} 
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(str);
		chatRecords.append("\n");
		chatRecords.append(str);
		chatRecords.setCaretPosition(chatRecords.getDocument().getLength());
	}
	
	public void sendUserMessage(String str){
		try {
			for(int i=0;i<lists.size();i++){
				dos = new DataOutputStream(lists.get(i).getOutputStream());
				dos.writeUTF(str);
			} 
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	private class ServerThread extends Thread{
		private ServerSocket serverSocket;
		
		public ServerThread(ServerSocket serverSocket){
			this.serverSocket = serverSocket;
		}
		
		@Override
		public void run() {
			while(true)
			{
				Socket soket=null;
				try {
					soket=serverSocket.accept();
					new Thread(new MSThread(soket)).start();
					lists.add(soket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
 
	public void opening(){
		Properties p = new Properties();
 
		try {
			if(new File("src/FontServer.properties").exists()){
				p.load(new FileReader("src/FontServer.properties"));
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
				Properties size = new Properties();
				size.setProperty("FontName", ChatRoomServerFrame.this.chatRecords.getFont().getFamily());
				size.setProperty("FontStyle", ChatRoomServerFrame.this.chatRecords.getFont().getStyle()+"");
				size.setProperty("FontSize", ChatRoomServerFrame.this.chatRecords.getFont().getSize()+"");
				size.setProperty("foreColor", ChatRoomServerFrame.this.chatRecords.getForeground().getRGB()+"");
				size.setProperty("backColor", ChatRoomServerFrame.this.chatRecords.getBackground().getRGB()+"");
				try {
					FileWriter fr = new FileWriter("src/FontServer.properties");
					size.store(fr, "FontServer Info");
					fr.close();
					if(isStart == true){
						if(dis != null){
							dis.close();
						}
						if(dos != null){
						 dos.close();
						}
						serverSocket.close();
						ul.stop();
						st.stop();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}
 
	public class UserList extends Thread{
 
		@Override
		public void run() {
			while(true){
				if(users.size()!=usersLength){
					userlist.setText("");
					usermessage="";
					for(String s : users){
					   userlist.append(s);
					   userlist.append("\n");
					   usermessage=usermessage+s+" ";
					}
					usersLength = users.size();
					sendUserMessage(usermessage+"@在线用户列表@");
				}
			}
		}
		
	}
}