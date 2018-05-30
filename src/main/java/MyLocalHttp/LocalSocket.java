package MyLocalHttp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;

import javax.swing.text.AbstractDocument.BranchElement;

import org.apache.log4j.Logger;

import MyServerHttp.MyServerSocket;
import sun.misc.BASE64Decoder;

public class LocalSocket extends Thread{
	Socket accept = null;
	Socket socket = null;
	static	ServerSocket serverSocket;
	static long i = 0;
	public static Logger logger = Logger.getLogger(LocalSocket.class);
    static	String local_port;
    static	String local_address;
    static	String server_port;
    static	String server_address;
    static	String net_port;
    static	String net_address;

    public LocalSocket(Socket accept) {
		this.accept = accept;
	}
	public static void main(String[] args)  {
		ResourceBundle bundle = ResourceBundle.getBundle("Http");
		 local_port = bundle.getString("local_port");
		 local_address = bundle.getString("local_address");
		 server_port = bundle.getString("server_port");
		 server_address = bundle.getString("server_address");
		// net_port = bundle.getString("net_port");	
		 net_address = bundle.getString("net_address");
	
		
		try {
			logger.info("本地端口："+ local_port);
			logger.info("代理地址："+ server_address);
			logger.info("代理端口："+ server_port);
			serverSocket = new ServerSocket(Integer.parseInt(local_port));
			logger.info("本地端口开启成功!");
			while (true) {
				Socket accept = serverSocket.accept();
				LocalSocket aa = new LocalSocket(accept);
				aa.start();
			}
			
		} catch (IOException e) {
			logger.error("本地端口开启失败!");
			logger.error("失败原因："+e.getMessage());
			e.printStackTrace();
		}
	
        
	}
	@Override
	public void run() {
		connect();
	}

	public void connect() {
		try {
			logger.debug("第" + ++i + "次链接");
			socket = new Socket(server_address, Integer.parseInt(server_port));
			long t3 = System.currentTimeMillis();
			communcate(accept.getInputStream(), accept.getOutputStream(), socket);
			long t4 = System.currentTimeMillis();
			//System.out.println("时间：" + (t4 - t3));
			socket.close();
			accept.close();
		} catch (IOException e) {
			logger.error("外网链接失败!");
			logger.error("失败原因：" + e.getMessage());
			e.printStackTrace();
		}
	}

	public String communcate(InputStream in, OutputStream outputStream, Socket socket) throws IOException {
		this.socket = socket;
		// 转发请求
		long t = System.currentTimeMillis();
		OutputStream os = socket.getOutputStream();
		//BufferedInputStream inbur = new BufferedInputStream(in,1024*64);
		byte[] b = new byte[2048];
		int k = -20;
		while ((k = in.read(b)) != -1) {
            os.write(b, 0, k);
			// System.out.println(new String(b, 0, k));
			if (k < 2048) {
				break;
			}
		}
		os.flush();
		int y = 0;
		int num = 0;
		try {
			// 读取响应，写入输出流
			socket.setSoTimeout(150);
			InputStream is = socket.getInputStream();
			BufferedInputStream re = new BufferedInputStream(is,1024*8);
			BufferedOutputStream outputStream2 = new BufferedOutputStream(outputStream);
			// re.read(b);
			
			while (true) {
				y++;	
				//int available = re.available();	
				long t1 = System.currentTimeMillis();
				try {
					k = re.read(b);
				} catch (Exception e) {
					if (y==1) {
						num++;
						if (num>3) {
							break;
						}
						System.out.println("sleep");
						Thread.sleep(1000);
						y--;
						continue;
					}else {
						long t2 = System.currentTimeMillis();
						System.out.println("读取响应，写入输出流-1时间==" + y + ":" + (t2 - t1));	
						break;
					}
					
				}
					
					//int available2 = re.available();
					if (k == -1) {
						
					break;
				}
					outputStream2.write(b, 0, k);			
			//	System.out.println(y + "==" + k+"available="+available+"available2="+available2);				
			}
			
			
			outputStream2.flush();
			long t3 = System.currentTimeMillis();
			System.out.println("时间总==" + y + ":" + (t3 - t));

		} catch (Exception e) {
			MyServerSocket.logger.error(e.getMessage() + "==="  + "===" + y + "===" + k);
			e.printStackTrace();
		}

		return null;
	}

}
