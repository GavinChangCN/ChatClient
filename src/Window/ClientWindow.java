package Window;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import JqueryUI.JqueryButton;

@SuppressWarnings("serial")
public class ClientWindow extends JFrame{

	JPanel mainJPanel ;
	JTextArea chatArea ;
	JScrollPane scrollPane ;
	JTextArea inputText ;
	JqueryButton submit ;
	JqueryButton exitChat ;
	ServerSocket server ;
	Socket socket ;
	InputStream in ;
	OutputStream out ;
	String hostIp = "" ;
	String hostname = "" ;
	ImageIcon backgroundImage ;
	JLabel backLabel ;
	boolean receive = true ;
	public static final String IP_ADDRESS = "localhost" ;
	public static final int PORT = 9999 ;
	public static final int DISCONNECT_SERVER = 0 ;
	public static final int SEND_CONTENT = 1 ;
	
	public ClientWindow() {
		//---------------------------------添加控件操作------------------------------------
		Font font = new Font( "微软雅黑" , 1 , 12 ) ;
		mainJPanel = new JPanel() ;
		
		backgroundImage = new ImageIcon( "images/background.jpg" ) ;
		backLabel = new JLabel( backgroundImage ) ;
		backLabel.setBounds( 0 , 0 , backgroundImage.getIconWidth() , backgroundImage.getIconHeight() );
		this.getLayeredPane().add( backLabel , new Integer( Integer.MIN_VALUE ) ) ;
		
		JPanel jP = (JPanel)this.getContentPane() ;
		jP.setOpaque( false );
		
		submit = new JqueryButton() ;
		submit.setText("发送");
		submit.setFont(font) ;
		submit.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				receive = false ;
				sendContent();
				receive = true ;
			}
		});
		
		exitChat = new JqueryButton() ;
		exitChat.setText("离开") ;
		exitChat.setFont(font) ;
		exitChat.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if( exitChat.getText().equals( "离开" ) ){
					disConnectServer() ;					
				}else {
					connectServer() ;
				}
			}
		});
		
		Color color = new Color( 225 , 225 , 225 ) ;
		
		chatArea = new JTextArea() ;
		chatArea.setFont(font) ;
		chatArea.setEditable( false ) ;
		chatArea.setLineWrap( true );
		scrollPane = new JScrollPane( chatArea ) ;
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.transferFocusDownCycle() ;
		scrollPane.setOpaque( false );
		scrollPane.getViewport().setOpaque( false );
		chatArea.setBackground( color );
		
		inputText = new JTextArea() ;
		inputText.setBackground( color );
		inputText.setFont(font);
		inputText.setLineWrap( true );
		inputText.addKeyListener( new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				int code = e.getKeyCode() ;
				if( code == 13 ) {
					receive = false ;
					sendContent() ;
					receive = true ;					
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mainJPanel.setLayout( null ) ;
		mainJPanel.add( scrollPane ) ;
		mainJPanel.add( inputText ) ;
		mainJPanel.add( submit ) ;
		mainJPanel.add( exitChat ) ;
		
		//---------------------------------窗口布局操作------------------------------------
		this.add( mainJPanel ) ;
		this.addComponentListener( new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				connectServer() ;
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				autoLayout() ;
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				disConnectServer() ;
			}
		});
		mainJPanel.setOpaque( false );
		this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ) ;
		this.setFont(font) ;
		this.setTitle("史上最简陋的聊天工具Beta V1.0");
		this.setSize( 400 , 600 ) ;
		this.setLocationRelativeTo(null) ;
		this.setVisible( true ) ;
		receiveContent() ;
	}
	//---------------------------------业务操作------------------------------------
	
	/**
	 * @Description: 自动完成窗体布局
	 * @param    
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015年3月12日
	 */
	public void autoLayout() {
		scrollPane.setBounds( 15 , 15 , mainJPanel.getWidth()-30 , mainJPanel.getHeight() - 95 ) ;
		inputText.setBounds( scrollPane.getX() , scrollPane.getY()+scrollPane.getHeight() + 20 , scrollPane.getWidth() - 140 , 40 );
		submit.setBounds( inputText.getX() + inputText.getWidth() + 10 , inputText.getY() + 5 , 60 , 30 );
		exitChat.setBounds( submit.getX() + submit.getWidth() + 10 , submit.getY() , 60 , 30 ) ;
		if( socket == null || socket.isClosed() ){
			exitChat.setText("加入") ;
			submit.setEnabled( false ) ;
			inputText.setEnabled( false ) ;
		}else {
			exitChat.setText("离开") ;
			submit.setEnabled( true ) ;
			inputText.setEnabled( true ) ;
		}
	}
	
	
	/**
	 * @Description: 窗体显示警告
	 * @param @param message   
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015年3月12日
	 */
	public void showMessage ( String message ) {
		JOptionPane.showMessageDialog( this , message );
	}
	
	/**
	 * @Description: 显示聊天框文本信息
	 * @param @param content   
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015年3月12日
	 */
	public void setContent ( String content ) {
		String text = this.chatArea.getText() ;
		if (text.isEmpty()) {
			this.chatArea.setText( content ) ;
		}else {			
			this.chatArea.setText( text + "\r\n" + content ) ;
		}
	}
	
	/**
	 * @Description: 连接聊天服务器
	 * @param    
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015年3月12日
	 */
	public void connectServer() {
		try {
			socket = new Socket( IP_ADDRESS , PORT ) ;
			InetAddress addr = InetAddress.getLocalHost() ;
			hostIp = addr.getHostAddress().toString() ;
			hostname = addr.getHostName().toString() ;
			in = socket.getInputStream() ;
			out = socket.getOutputStream() ;
			showMessage( "服务器连接成功！\n"+"本机IP地址："+hostIp+"\n本机名称："+hostname );
		} catch (UnknownHostException unknownHostException) {
			unknownHostException.printStackTrace() ;
			showMessage( "找不到目标主机！\n" + unknownHostException.getMessage());
		} catch (IOException IOe) {
			IOe.printStackTrace() ;
			showMessage( "远程连接失败！\n" + IOe.getMessage() );
		}
		autoLayout();
	}
	
	/**
	 * @Description: 断开聊天服务器连接
	 * @param    
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015年3月12日
	 */
	public void disConnectServer() {
		try {
			out.write( DISCONNECT_SERVER );
			String myInfo = hostname+ "-" + hostIp ;
			byte[] data = myInfo.getBytes();
			out.write( data.length );
			out.write( data );
			out.flush() ;
			showMessage( "已断开与服务器的连接！" );
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		socket = null ;
		autoLayout() ;
	}
	
	/**
	 * @Description: 发送聊天内容到服务器
	 * @param    
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015年3月12日
	 */
	public void sendContent() {
		if (inputText.getText().equals("")) {
			showMessage("请输入消息内容再点击发送！\n        "
					+ "-我是小尾巴，据说15字有惊喜哦！");
		}else {
			if( socket != null && !socket.isClosed() ) {
				try {
					out.write( SEND_CONTENT );
					String content = inputText.getText() ;
					String allInfo = hostname + "-" + hostIp 
							+ "（" + new Date().toLocaleString() + "）：\n" + content ; 
					byte data[] = allInfo.getBytes() ;
					out.write( data.length );
					out.write( data );
					out.flush() ;
					inputText.setText("");
				} catch (Exception e) {
					e.printStackTrace() ;
					showMessage( "消息发送失败！\n" + e.getMessage() );
				}
			}	
		}
	}
	
	/**
	 * @Description: 接受服务器转发的聊天内容
	 * @param    
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015年3月12日
	 */
	public void receiveContent() {
		new Thread( new Runnable() {
			public void run() {
				while ( receive ) {
					System.out.println("进程成功开启！");
					if( socket != null && !socket.isClosed() ) {
						try {
							byte data[] = new byte[ in.read() ] ;
							in.read(data) ;
							String content = new String( data ).trim() ;
							setContent( content ) ;
							Thread.sleep( 2000 );
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					System.out.println("进程成功结束！");
				}
			}
		}).start() ;
	}
}
