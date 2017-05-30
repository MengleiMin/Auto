package Manage;

import java.awt.Canvas;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;



public class Main_download {
	protected Shell shell;
	private Label image_Mid;

	private Label label_Information;
	private Label label_manage;

	private Label label_device;
	private Label label_password;
	private Label label_appName;
	private Label label_current;
	private Label appname;
	private Label current;

	static StyledText Text_infomation;
	private Button btn_start;
	private Button btn_stop;
	private Button btn_add;
	private Button btn_delete;

	private Text text_name;
	private Text text_password;

	Boolean timerflag = false;
	Timer timer = new Timer(true);
	Server server = new Server();
	//AeSimpleSHA1 sha1;
	private FileWriter log_writer;

	String working_directory;
	String web_base;
	String java_path;
	String outpath;
	String outpathbase;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Main_download window = new Main_download();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() throws InterruptedException, IOException {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() throws InterruptedException, IOException {
		working_directory = System.getProperty("user.dir").replace('\\', '/');
		web_base = "http://localhost:9999/myAuto/Database/";
		java_path = System.getenv("JAVA_HOME").replace('\\', '/') + "bin/";
		log_writer = new FileWriter(working_directory + "/resource/myapp/log_download.txt", true);
		server.main();

		shell = new Shell();
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				server.run = false;
				System.out.println("Close Window!");
			}

		});
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setImage(SWTResourceManager.getImage(working_directory + "/resource/images/manager.png"));
		shell.setSize(488, 451);
		shell.setText("Manager");
		shell.setLocation(438, 110);
		init();
		displayApp();
	}

	public void Timertask_start() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							if (download()) {
								// jump to send part
								displayApp();
								// setDisplay("Send file to authenticated
								// devices!\n");
								log(log_writer);
								//server.main();
								//Thread.sleep(5000);
								server.sendFile();

							} else {// later delete
								// server.sendFile();

							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		};
		timer = new Timer(true);
		timer.schedule(task, 1, 10000);
	}

	public boolean download() throws IOException, InterruptedException {
		// System.out.println("no Exist info.properties file!");
		URL url = new URL(web_base + "info.properties");
		if (exists(url)) {
			Properties p = new Properties();
			InputStream info = url.openStream();
			p.load(info);
			// System.out.println("Exist info.properties file!");
			String download_version = p.getProperty("version");
			URL url1 = new URL(web_base + p.getProperty("name") + "_" + download_version + ".jar");
			if (exists(url1)) {
				outpathbase = working_directory + "/resource/myapp/";
				String jarname = p.getProperty("name") + "_" + download_version;
				outpath = outpathbase + jarname + ".jar";
				File file = new File(outpath);
				if (file.exists()) {
					// setDisplay("This version application has been
					// downloaded!\n");
				} else {
					downloadFileFromUrl(url, url1, p);
					if (jar_verify(jarname + ".jar")) {
						setDisplay(jarname + " verify successful!\n");
						return true;
					} else {
						setDisplay("Verify failure! File has been corrupted!\n");
						// delete jar file
						delete_file(p);
					}
				}
			} else {
				System.out.println("No this version application exist!");
			}

		} else {
			System.out.println("Not any properties file exist!");
		}
		return false;
	}

	public boolean jar_verify(String jarname) throws IOException, InterruptedException {
		java_path = System.getenv("JAVA_HOME").replace('\\', '/') + "bin/";
		System.out.println(java_path);
		System.out.println(working_directory + "/resource/myapp/" + jarname);
		ProcessBuilder pb = new ProcessBuilder(java_path + "jarsigner", "-verify", "-certs",
				working_directory + "/resource/myapp/" + jarname);
		Process p = pb.start();
		p.waitFor();
		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			if (line.contains("“——È÷§")) {
				return true;
			}
		}
		return false;
	}

	public static boolean exists(URL URLName) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			// HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con = (HttpURLConnection) URLName.openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void downloadFileFromUrl(URL url, URL url1, Properties p) throws IOException, InterruptedException {
		String jarname = p.getProperty("name") + "_" + p.getProperty("version");
		InputStream injar = null;
		OutputStream outjar = null;
		injar = url1.openStream();
		outjar = new BufferedOutputStream(new FileOutputStream(outpath));
		byte[] buffer = new byte[2048];
		for (;;) {
			int nBytes = injar.read(buffer);
			if (nBytes <= 0)
				break;
			outjar.write(buffer, 0, nBytes);
		}
		if (injar != null) {
			injar.close();
		}
		if (outjar != null) {
			outjar.flush();
			outjar.close();
			setDisplay("Download " + jarname + " successful!\n");
		}
		injar = url.openStream();
		outjar = new BufferedOutputStream(new FileOutputStream(outpathbase + "/info.properties"));
		buffer = new byte[2048];
		for (;;) {
			int nBytes = injar.read(buffer);
			if (nBytes <= 0)
				break;
			outjar.write(buffer, 0, nBytes);
		}
		if (injar != null) {
			injar.close();
		}
		if (outjar != null) {
			outjar.flush();
			outjar.close();
		}
	}

	private void delete_file(Properties p) throws InterruptedException {
		String jarname = p.getProperty("name") + "_" + p.getProperty("version");
		File file = new File(working_directory + "/resource/myapp/" + jarname + ".jar");
		System.out.println(file.toString());
		if (file.delete()) {
			setDisplay(jarname + " has been deleted!\n");
		} else {
			setDisplay(jarname + " is being deleted!\n");
			setDisplay("Delete operation is failed!\n\n");
		}

		file = new File(working_directory + "/resource/myapp/info.properties");
		System.out.println(file.toString());
		if (file.delete()) {
			setDisplay("Related file has been deleted!\n\n");
		} else {
			setDisplay("Related file is being deleted!\n");
			setDisplay("Delete operation is failed!\n\n");
		}
	}

	private void addDevice() throws IOException, NoSuchAlgorithmException {
		String device = text_name.getText();
		String password = text_password.getText();
		String password_SHA1 = AeSimpleSHA1.SHA1(password);
		if (device != "" && password != "") {
			try {
				File device_group = new File(working_directory + "/resource/secure/device_group.properties");
				Properties p = new Properties();
				InputStream group = new FileInputStream(device_group);// reader
				p.load(group);
				if (!p.containsKey(device)) {
					p.setProperty(device, password_SHA1);
					PrintWriter writer = new PrintWriter(new BufferedWriter// writer
					(new FileWriter(working_directory + "/resource/secure/device_group.properties")));
					p.store(writer, null);
					writer.close();
					setDisplay("Add trust device success!\n");

				} else {
					setDisplay("This device has existed!\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			setDisplay("Fails! Input should not be null!\n");
		}
	}

	private void deleteDevice() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String device = text_name.getText();
		String password = text_password.getText();
		String password_SHA1 = AeSimpleSHA1.SHA1(password);
		if (device != "" && password != "") {
			try {
				File device_group = new File(working_directory + "/resource/secure/device_group.properties");
				Properties p = new Properties();
				InputStream group = new FileInputStream(device_group);// reader
				p.load(group);
				if (p.containsKey(device)) {
					// System.out.println(p.getProperty(device));
					if (p.getProperty(device).equals(password_SHA1)) {
						p.remove(device, password_SHA1);
						PrintWriter writer = new PrintWriter(new BufferedWriter// writer
						(new FileWriter(working_directory + "/resource/secure/device_group.properties")));
						p.store(writer, null);
						writer.close();
						setDisplay("Delete trust device success!\n");
					} else {
						setDisplay("This device doesn't match this value!\n");
					}
				} else {
					setDisplay("No exist this device!\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			setDisplay("Fails! Input should not be null!\n");
		}
	}

	private void displayApp() throws IOException {
		File file = new File(working_directory + "/resource/myapp/info.properties");
		if (file.exists()) {
			Properties p = new Properties();
			InputStream info = new FileInputStream(file);// reader
			p.load(info);
			appname.getParent().layout();
			appname.setText(p.getProperty("name"));
			current.getParent().layout();
			current.setText(p.getProperty("version"));
		} else {
			appname.getParent().layout();
			appname.setText(" null");
			current.getParent().layout();
			current.setText(" null");
		}

	}

	private void log(FileWriter log_writer) throws IOException {
		File file = new File(working_directory + "/resource/myapp/info.properties");
		Properties p = new Properties();
		InputStream info = new FileInputStream(file);// reader
		p.load(info);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// System.out.println( );
		log_writer.write("Release Time: " + p.getProperty("timestamp") + "\r\n");
		log_writer.write("Application Name:" + p.getProperty("name") + "\r\n");
		log_writer.write("Version Number: " + p.getProperty("version") + "\r\n");
		log_writer.write("Download Time: " + sdf.format(cal.getTime()) + "\r\n");
		log_writer.write("\r\n");
		log_writer.write("\r\n");
		log_writer.flush();
	}

	private void setDisplay(String str) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Text_infomation.append(sdf.format(cal.getTime()) + ":  ");
				Text_infomation.append(str);
			}
		});
	}

	public void init() {
		btn_start = new Button(shell, SWT.NONE);
		btn_start.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				btn_start.setEnabled(false);
				btn_stop.setEnabled(true);
				Timertask_start();
				timerflag = true;
			}
		});
		btn_start.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		btn_start.setBounds(10, 198, 45, 27);
		btn_start.setText("Start");

		btn_stop = new Button(shell, SWT.NONE);
		btn_stop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btn_stop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (timerflag) {
					btn_start.setEnabled(true);
					btn_stop.setEnabled(false);
					timerflag = false;
					timer.cancel();
					timer.purge();
				}
			}
		});
		btn_stop.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		btn_stop.setBounds(10, 239, 45, 27);
		btn_stop.setText("Stop");
		btn_stop.setEnabled(false);

		btn_add = new Button(shell, SWT.NONE);
		btn_add.setText("Add");
		btn_add.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					addDevice();
				} catch (IOException | NoSuchAlgorithmException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btn_add.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		btn_add.setBounds(10, 357, 51, 27);

		btn_delete = new Button(shell, SWT.NONE);
		btn_delete.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					deleteDevice();
				} catch (NoSuchAlgorithmException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btn_delete.setText("Delete");
		btn_delete.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		btn_delete.setBounds(77, 357, 51, 27);

		image_Mid = new Label(shell, SWT.NONE);
		image_Mid.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		image_Mid.setBounds(118, 0, 221, 84);
		image_Mid.setImage(
				SWTResourceManager.getImage(working_directory + "/resource/images/DHU.PNG"));

		Text_infomation = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		Text_infomation.setForeground(SWTResourceManager.getColor(105, 105, 105));
		Text_infomation.setFont(SWTResourceManager.getFont("Times New Roman", 10, SWT.BOLD));
		Text_infomation.setBounds(61, 163, 401, 147);

		label_device = new Label(shell, SWT.NONE);
		label_device.setText("Device :");
		label_device.setForeground(SWTResourceManager.getColor(128, 128, 128));
		label_device.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		label_device.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_device.setAlignment(SWT.CENTER);
		label_device.setBounds(131, 362, 63, 22);

		label_password = new Label(shell, SWT.NONE);
		label_password.setText("Password :");
		label_password.setForeground(SWTResourceManager.getColor(128, 128, 128));
		label_password.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		label_password.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_password.setAlignment(SWT.CENTER);
		label_password.setBounds(296, 362, 73, 22);

		text_name = new Text(shell, SWT.BORDER);
		text_name.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.NORMAL));
		text_name.setBounds(200, 360, 79, 23);

		text_password = new Text(shell, SWT.BORDER);
		text_password.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.NORMAL));
		text_password.setBounds(375, 360, 79, 23);

		label_Information = new Label(shell, SWT.NONE);
		label_Information.setText("Information Display :");
		label_Information.setForeground(SWTResourceManager.getColor(233, 150, 122));
		label_Information.setFont(SWTResourceManager.getFont("Times New Roman", 13, SWT.BOLD));
		label_Information.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_Information.setAlignment(SWT.CENTER);
		label_Information.setBounds(10, 107, 159, 27);

		label_appName = new Label(shell, SWT.NONE);
		label_appName.setText("App name:");
		label_appName.setForeground(SWTResourceManager.getColor(128, 128, 128));
		label_appName.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		label_appName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_appName.setBounds(98, 140, 73, 17);

		label_current = new Label(shell, SWT.NONE);
		label_current.setText("Current :");
		label_current.setForeground(SWTResourceManager.getColor(128, 128, 128));
		label_current.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		label_current.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_current.setAlignment(SWT.CENTER);
		label_current.setBounds(275, 140, 63, 21);

		label_manage = new Label(shell, SWT.NONE);
		label_manage.setText("Device Management :");
		label_manage.setForeground(SWTResourceManager.getColor(233, 150, 122));
		label_manage.setFont(SWTResourceManager.getFont("Times New Roman", 13, SWT.BOLD));
		label_manage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_manage.setAlignment(SWT.CENTER);
		label_manage.setBounds(10, 324, 159, 27);

		appname = new Label(shell, SWT.NONE);
		appname.setForeground(SWTResourceManager.getColor(233, 150, 122));
		appname.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		appname.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		appname.setBounds(177, 139, 92, 21);

		current = new Label(shell, SWT.NONE);
		current.setForeground(SWTResourceManager.getColor(233, 150, 122));
		current.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		current.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		current.setBounds(344, 139, 79, 21);
	}
}


