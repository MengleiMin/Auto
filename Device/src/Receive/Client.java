package Receive;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import Install.Installer;
import Receive.AeSimpleSHA1;

public class Client implements Runnable {
	static String pathToStore = null;
	final static String keyStoreFileName = "sslclientkeys";
	final static String trustStoreFileName = "sslclienttrust";
	final static String passwd = "123456";

	final static String deviceName = "Device1";
	final static String devicePassword = "123456";

	public static String FILE_TO_RECEIVED = null;
	public final static int FILE_SIZE = 6022386;
	final static int port = 8000;

	private SSLSocket sslsocket = null;
	String working_directory;

	public boolean auth;
	static boolean debug = false;

	private String java_path;
	public String jarname;

	static receive_main receive = new receive_main();
	static Installer install = new Installer();
	boolean connect = false;

	// BufferedReader input;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			init();
			conn();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void main() throws IOException, InterruptedException {

	}

	public void init() throws IOException, InterruptedException {
		working_directory = System.getProperty("user.dir").replace('\\', '/');
		pathToStore = working_directory + "/resource/secure/";
		java_path = System.getenv("JAVA_HOME").replace('\\', '/') + "bin/";
		// receive = new receive_main();
		String keyStoreFile = pathToStore + keyStoreFileName;
		String trustStoreFile = pathToStore + trustStoreFileName;
		// dis = ((Display1) receive).Display1();
		System.setProperty("javax.net.ssl.keyStore", keyStoreFile);
		System.setProperty("javax.net.ssl.keyStorePassword", passwd);
		System.setProperty("javax.net.ssl.trustStore", trustStoreFile);
		System.setProperty("javax.net.ssl.trustStorePassword", passwd);
		if (debug) {
			System.setProperty("javax.net.debug", "all");
		}
	}

	public void conn() throws UnknownHostException, IOException, InterruptedException {
		Runnable connTask = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				while (true) {
					if (!connect) {
						try {
							sslsocket = (SSLSocket) socketFactory.createSocket("localhost", port);
							auth = false;
							if (sslsocket.isConnected()) {
								connect = true;
								displayconnect("Connect server successful!\n");
								Authenticate();
							} else {
								sslsocket.close();
								displayconnect("Connect server fail!\n\n");
							}
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		Thread connThread = new Thread(connTask);
		connThread.start();
	}

	public void Authenticate() throws Exception {
		// get client socket outputstream
		PrintWriter output = new PrintWriter(new OutputStreamWriter(sslsocket.getOutputStream()), true);
		// send username and password to server via stream
		output.println(deviceName);
		String deviceNPassword_sha1 = AeSimpleSHA1.SHA1(devicePassword);
		output.println(deviceNPassword_sha1);
		System.out.println(deviceNPassword_sha1);
		output.flush();

		BufferedReader input = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
		String response = input.readLine();
		if (response != null) {
			System.out.println("response:" + response);
			jarname = input.readLine();
			System.out.println("input:" + jarname);
			FILE_TO_RECEIVED = working_directory + "/resource/myapp/" + jarname;
			System.out.println("FILE_TO_RECEIVED:auth " + FILE_TO_RECEIVED);
			if (response.equals("OK")) {
				auth = true;
				displayconnect("Authenticate success\n");
				receive_file(jarname);
			} else {
				System.out.println("Client:Authenticate fail!");
				displayconnect("Authenticate fail!\n\n");
				output.close();
				input.close();
				sslsocket.close();
				connect = false;
			}
		}
		connect = false;
	}

	public void receive_file(String jarname) throws Exception {
		File updated = new File(FILE_TO_RECEIVED);
		if (updated.exists()) {
			updated.delete();
		}
		//updated.createNewFile();
		System.out.println("bbbb");
		FileOutputStream file = new FileOutputStream(updated);
		DataOutputStream fileout = new DataOutputStream(file);
		if(!sslsocket.isConnected()){
			System.out.println("not connect!");
			sslsocket.close();
			displayconnect("socket disconnects!\n\n");
			connect = false;
			return;
		}
		DataInputStream data_in = new DataInputStream(sslsocket.getInputStream());
		System.out.println("bbb");
		long len = 0;
		long count = 0;
		//connect = false;
		long sum = data_in.readLong();
		System.out.println("bbbss");
		while (true) {
			byte y = data_in.readByte();
			int x = data_in.readInt();
			byte[] buf = new byte[500];
			int offset = 0;
			while (offset < x) {
				len = data_in.read(buf, offset, x - offset);
				offset += len;
			}
			count += x;
			//System.out.println("current:" + x + " Total:" + count);
			fileout.write(buf, 0, x);
			fileout.flush();
			if (y == 2) {
				fileout.flush();
				fileout.close();
				file.close();
				break;
			}
		}
		// System.out.println(updated.);
		String str = jarname.substring(0, jarname.length() - 4);
		System.out.println("cc:" + str);
		displayconnect("Download " + str + " successful!\n");
		if (jar_verify(jarname)) {
			displayconnect("File verification success\n\n");
			connect = false;
			// copyProperties();
			createProperties();
			install.run();
			displayApp();

		} else {
			displayconnect("File verification fails\n\n");
			delete_file();
			connect = false;
		}
	}

	public boolean jar_verify(String jarname) throws IOException, InterruptedException {
		// java_path = System.getenv("JAVA_HOME").replace('\\', '/') + "bin/";
		ProcessBuilder pb = new ProcessBuilder(java_path + "jarsigner", "-verify", "-certs",
				working_directory + "/resource/myapp/" + jarname);
		Process p = pb.start();
		p.waitFor();
		InputStream is = p.getInputStream();
		InputStream err = p.getErrorStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			if (line.contains("ÒÑÑéÖ¤")) {
				return true;
			}
		}
		return false;
	}

	public void createProperties() throws IOException {
		String path = working_directory + "/resource/myapp/info.properties";
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		System.out.println("Properties file not found!");
		System.out.println("Create a null properties file!");
		try {
			PrintWriter writer = new PrintWriter(path, "UTF-8");
			int index = jarname.indexOf('_');
			writer.println("name=" + jarname.substring(0, index));
			System.out.println(jarname.substring(0, index));
			writer.println("version=" + jarname.substring(index + 1, jarname.length() - 4));
			System.out.println(jarname.substring(index + 1, jarname.length() - 4));
			writer.close();
		} catch (IOException e) {
			// do something
			System.out.println("Properties file create fails!");
		}
	}

	public void delete_file() throws InterruptedException {
		File file = new File(working_directory + "/resource/myapp/" + jarname);
		if (file.delete()) {
			displayconnect("Related file have been deleted!\n\n");
		}
	}

	

	public void displayApp() {
		receive.appname.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				File file = new File(working_directory + "/resource/myapp/info.properties");
				Properties p = new Properties();
				InputStream info;
				try {
					info = new FileInputStream(file);
					p.load(info);
					receive.appname.getParent().layout();
					receive.appname.setText(p.getProperty("name"));
					receive.current.getParent().layout();
					receive.current.setText(p.getProperty("version"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // reader

			}
		});
	}

	public void displayconnect(String str) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				receive.Text_connect.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Calendar cal = Calendar.getInstance();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						receive.Text_connect.append(sdf.format(cal.getTime()) + ":  ");
						receive.Text_connect.append(str);
					}
				});
			}

		}).start();
	}

}
