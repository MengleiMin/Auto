package Receive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

public class receive_main implements Runnable {
	protected Shell shell;
	Label image_Mid;

	static Client client = new Client();;
	String working_directory;
	private Label label_install;
	public static StyledText Text_connect;
	public static StyledText Text_install;

	private Label label_Informatin;

	private Label label_name;
	static Label appname;
	private Label label_current;
	static Label current;
	public Thread runThread;
	public boolean appRun = true; 
	/**
	 * Launch the application.
	 */

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		try {
			receive_main window = new receive_main();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() throws UnknownHostException, IOException, InterruptedException {
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
	protected void createContents() throws UnknownHostException, IOException, InterruptedException {
		working_directory = System.getProperty("user.dir").replace('\\', '/');

		shell = new Shell();
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setImage(SWTResourceManager.getImage(working_directory + "/resource/images/device1.PNG"));
		shell.setSize(471, 451);
		shell.setText("Device");
		shell.setLocation(911,110);
		init();
		appRun();
		displayApp();
		client.run();
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
			appname.setText("  null");
			current.getParent().layout();
			current.setText("  null");
		}

	}

	public void appRun() throws IOException, InterruptedException{
		Runnable Task = new Runnable() {
			@Override
			public void run() {
				File file = new File(working_directory + "/resource/myapp/info.properties");
				if(file.exists()){
					Properties p = new Properties();
					InputStream info;
					try {
						info = new FileInputStream(file);
						p.load(info);
						String jarname = p.getProperty("name")+"_"+p.getProperty("version");
						String jar = working_directory.replace("Program Files", "\"Program Files\"") 
								+ "/resource/myapp/" + jarname+".jar";
						Process proc = Runtime.getRuntime().exec("java -jar "+jar);
						System.out.println("main:"+jarname);
						InputStream is = proc.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr);
						String line;
						int i = 0;
						setDisplay(jarname+" is starting, Please wait!\n");
						while ((line = br.readLine()) != null) {
							//System.out.println(line);
							if(line.contains("Seconds")){
								if(i==0) setDisplay("Start timer!\n");
								setDisplay(line+"\n");
								i++;
								if(i==8) return;
								if(!appRun) return;
							}
						}
						
						InputStream err = proc.getErrorStream();
						isr = new InputStreamReader(err);
						br = new BufferedReader(isr);
						while ((line = br.readLine()) != null) {
							System.out.println(line);
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}// re
					}else{
						setDisplay("No application is Running!\n\n");
					}
			}
		};
		runThread = new Thread(Task);
		runThread.start();
		return;
	}

	private void setDisplay(String str) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Text_install.append(sdf.format(cal.getTime()) + ":  ");
				Text_install.append(str);
			}
			
		});
	}

	public void init() {
		image_Mid = new Label(shell, SWT.NONE);
		image_Mid.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		image_Mid.setBounds(117, 0, 208, 80);
		image_Mid.setImage(
				SWTResourceManager.getImage(working_directory + "/resource/images/DHU.PNG"));

		label_install = new Label(shell, SWT.NONE);
		label_install.setText("Install informatin:");
		label_install.setForeground(SWTResourceManager.getColor(233, 150, 122));
		label_install.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		label_install.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_install.setAlignment(SWT.CENTER);
		label_install.setBounds(10, 267, 116, 17);

		Text_connect = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		Text_connect.setForeground(SWTResourceManager.getColor(105, 105, 105));
		Text_connect.setFont(SWTResourceManager.getFont("Times New Roman", 10, SWT.BOLD));
		Text_connect.setBounds(10, 153, 435, 108);
		
		Text_install = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		Text_install.setForeground(SWTResourceManager.getColor(105, 105, 105));
		Text_install.setFont(SWTResourceManager.getFont("Times New Roman", 10, SWT.BOLD));
		Text_install.setBounds(10, 290, 435, 112);

		label_Informatin = new Label(shell, SWT.NONE);
		label_Informatin.setText("Download informatin:");
		label_Informatin.setForeground(SWTResourceManager.getColor(233, 150, 122));
		label_Informatin.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		label_Informatin.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_Informatin.setAlignment(SWT.CENTER);
		label_Informatin.setBounds(10, 130, 145, 17);

		label_name = new Label(shell, SWT.NONE);
		label_name.setText("App name:");
		label_name.setForeground(SWTResourceManager.getColor(128, 128, 128));
		label_name.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		label_name.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_name.setBounds(86, 99, 73, 25);

		appname = new Label(shell, SWT.NONE);
		appname.setForeground(SWTResourceManager.getColor(233, 150, 122));
		appname.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		appname.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		appname.setBounds(165, 98, 92, 18);

		label_current = new Label(shell, SWT.NONE);
		label_current.setText("Current :");
		label_current.setForeground(SWTResourceManager.getColor(128, 128, 128));
		label_current.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		label_current.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_current.setAlignment(SWT.CENTER);
		label_current.setBounds(274, 99, 63, 18);

		current = new Label(shell, SWT.NONE);
		current.setForeground(SWTResourceManager.getColor(233, 150, 122));
		current.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		current.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		current.setBounds(343, 98, 58, 17);
	}
}
