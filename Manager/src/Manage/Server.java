package Manage;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server {
	final static String keyStoreFileName = "sslserverkeys";
	final static String trustStoreFileName = "sslservertrust";
	private static String pathToStore = null;
	private static String FILE_TO_SEND = null; // you may change this
	private static String key_passwd = "123456";

	final static int serverport = 8000;
	private SSLServerSocket serverSocket = null;
	private SSLSocket socket;

	private static boolean debug = false;
	public boolean auth = false;
	public boolean run;

	static Main_download download = new Main_download();

	String working_directory;
	String jarname;

	public void main() throws IOException, InterruptedException {
		init();
		accept();
	}

	public void init() throws IOException, InterruptedException {
		working_directory = System.getProperty("user.dir").replace('\\', '/');
		pathToStore = working_directory + "/resource/secure/";
		String keyStoreFile = pathToStore + keyStoreFileName;
		String trustStoreFile = pathToStore + trustStoreFileName;
		run = true;

		System.setProperty("javax.net.ssl.keyStore", keyStoreFile);
		System.setProperty("javax.net.ssl.keyStorePassword", key_passwd);
		System.setProperty("javax.net.ssl.trustStore", trustStoreFile);
		System.setProperty("javax.net.ssl.trustStorePassword", key_passwd);

		if (debug) {
			System.setProperty("javax.net.debug", "all");
		}
	}
	

	public void accept() throws IOException {
		Runnable serverTask = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory
						.getDefault();
				try {
					serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(serverport);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setDisplay("Server is listenting...\n");
				long i =0;
				while (run) {
					try {
						if (!run) {
							socket.close();
							System.out.println("Close server socket!");
							setDisplay("Close server socket!\n");
						}
						
						socket = (SSLSocket) serverSocket.accept();
						if(i!=0)setDisplay("Server is listenting...\n");
						i++;
						
						if (Authenticate()) {
							System.out.println("run:"+run);
							auth = true;
							System.out.println("auth"+auth);
							//sendFile();
							System.out.println("Server99:Authenticate success!");
							
						} else {
							System.out.println("Server:Authentication failed, you have no access to server!");
							setDisplay("Authentication failed!\n\n");
						}
						Thread.sleep(10000);
					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				System.out.println("out");
			}
		};
		Thread serverThread = new Thread(serverTask);
		serverThread.start();
	}

	public boolean Authenticate() throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

		File infofile = new File(working_directory  + "/resource/secure/device_group.properties");
		InputStream info = new FileInputStream(infofile);
		Properties p = new Properties();
		p.load(info);
		// get username and password
		String device = input.readLine();
		String password = input.readLine();
		
		File infofile1 = new File(working_directory + "/" + "resource/myapp/info.properties");
		if(!infofile1.exists()){
			jarname = "CommonApp_1.0.jar";
		}else{
			InputStream info1 = new FileInputStream(infofile1);
			Properties p1 = new Properties();
			p1.load(info1);
			float f = (float) (Float.parseFloat(p1.getProperty("version"))+1.0);
			System.out.println("float:"+f);
			jarname = p1.getProperty("name") + "_" + f + ".jar";
		}
		FILE_TO_SEND = working_directory + "/resource/myapp/" + jarname;
		System.out.println("send:"+FILE_TO_SEND);
		// verify identification
		if (p.getProperty(device) != null) {
			if (password.equals(p.getProperty(device))) {
				output.println("OK");
				output.println(jarname);
				output.flush();
				setDisplay("Authenticate success!\n");
				return true;
			}
		}
		output.println("NO");
		output.flush();
		input.close();
		socket.close();
		return false;
	}

	public void sendFile() throws IOException, InterruptedException {
		Runnable fileTask = new Runnable() {
			@Override
			public void run() {
				System.out.println("auyh:"+auth);
				if (auth) {
					try {
						setDisplay("Send file to authenticated devices!\n");
						OutputStream output = socket.getOutputStream();
						DataOutputStream data_out = new DataOutputStream(new BufferedOutputStream(output));
						File myjar = new File(FILE_TO_SEND);
						FileInputStream Sendfile = new FileInputStream(myjar);

						byte[] buf = new byte[500];
						ByteArrayOutputStream box = null;
						DataOutputStream out = new DataOutputStream(box = new ByteArrayOutputStream());

						long x;
						long len = 0;
						data_out.writeLong(Sendfile.available());
						data_out.flush();
						System.out.println("Sendfile:" + Sendfile.available());
						long count = 0;
						while (Sendfile.available() > 0) {
							box.reset();
							x = Sendfile.available();
							len = Sendfile.read(buf);
							if (x != len) {
								out.writeByte(1);
								out.writeInt((int) len);
								out.write(buf, 0, (int) len);
							} else {
								out.writeByte(2);
								out.writeInt((int) len);
								out.write(buf, 0, (int) len);
							}
							out.flush();
							byte[] Q = box.toByteArray();
							data_out.write(Q, 0, (int) (len + 5));
							data_out.flush();
							//System.out.println("Sendfile:" + Sendfile.available());
							count += len;
							// System.out.println("write byte:"+len+" Totcal:
							// "+count);
						}
						Sendfile.close();
						data_out.close();
						output.close();
						setDisplay("Send "+jarname.substring(0, jarname.length()-4)+" successful!\n\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		};
		Thread fileThread = new Thread(fileTask);
		fileThread.start();
	}

	public void setDisplay(String str) {
		download.Text_infomation.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				download.Text_infomation.append(sdf.format(cal.getTime()) + ":  ");
				download.Text_infomation.append(str);
			}
		});
	}
}
