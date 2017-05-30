package MainPackage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
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
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.StyledText;

public class MainScreen {
	protected Shell shell;
	Label image_Mid;
	Label name;
	Label name_content;
	Label version;
	Label version_content;
	Label release;
	Label releaseTime_content;
	Label note;
	Label label_information;

	static StyledText Text_infomation;
	Button btn_upload;

	private FileWriter log_writer;
	String working_directory;
	String java_path;
	static String web_base;

	public static void main(String[] args) {
		try {
			MainScreen window = new MainScreen();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open() throws URISyntaxException, IOException {
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

	protected void createContents() throws URISyntaxException, IOException {
		working_directory = System.getProperty("user.dir").replace('\\', '/');
		web_base = "D:/Tomcat-project/tomcat/webapps/myAuto/Database";
		log_writer = new FileWriter(working_directory + "/resource/log_upload.txt", true);
		java_path = System.getenv("JAVA_HOME").replace('\\', '/') + "bin/";

		shell = new Shell();
		shell.setToolTipText("");
		shell.setSize(453, 451);
		shell.setText("Adminitrator");
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setImage(SWTResourceManager.getImage(working_directory + "/resource/images/admin1.png"));
		shell.setLocation(0, 110);
		getAppInfo();
	}

	public void uploadFile(Properties p) throws InterruptedException {
		boolean done = false;
		String selected = null;
		String appName;
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		String[] filterExt = { "*.jar" };
		dialog.setText("Select an archive");
		dialog.setFilterExtensions(filterExt);
		dialog.setFilterPath("D:/");
		while (!done) {
			selected = dialog.open();
			appName = dialog.getFileName();
			if (selected == null) {
				done = true;
			} else {
				selected = selected.replace("\\", "/");
				Path source = Paths.get(selected);
				Path dest = Paths.get(web_base);
				// System.out.println("selected file:" + selected);
				File fullDest = new File(dest + "/" + appName);

				if (fullDest.exists() && !fullDest.isDirectory()) {
					// do something
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING);
					messageBox.setText("Warning");
					messageBox.setMessage("Files existed! Please re-select!");
					messageBox.open();
				} else {
					String newNumber = appName.substring(appName.indexOf("_") + 1, appName.length() - 4);
					String oldNumber = p.getProperty("version");
					if (newNumber.compareTo(oldNumber) > 0 || oldNumber.compareTo("null") == 0) {
						try {
							setDisplay("Choose file successful, Start to sign!\n");
							if (jarSign(source)) {
								setDisplay(appName.substring(0,appName.length()-4) + " signature success!\n");
								Files.copy(source, dest.resolve(source.getFileName()));
								done = true;
								setDisplay("Upload " + appName.substring(0,appName.length()-4) + " successfully!\n\n");
								MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
								messageBox.setText("Info");
								messageBox.setMessage("Upload " +  appName.substring(0,appName.length()-4)+ " successfully!");
								messageBox.open();
								copyPrepertiesFromServer(newNumber, p);
								InputStream info = new FileInputStream(web_base + "/info.properties");
								p.load(info);
								refreshContent(p);
								log(log_writer, p, selected);
							} else {
								setDisplay( appName.substring(0,appName.length()-4) + " signature fails!\n");
								setDisplay("Upload " +  appName.substring(0,appName.length()-4) + " failure!\n\n");
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else {
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING);
						messageBox.setText("Warning");
						messageBox
								.setMessage("Uploaded version is older than current! \n             Please re-select!");
						messageBox.open();
					}

				}
			}
		}
	}

	public void createProperties(String ur) {
		System.out.println("Properties file not found!Create a null properties file!");
		try {
			PrintWriter writer = new PrintWriter(ur, "UTF-8");
			writer.println("name=null");
			writer.println("vendor=null");
			writer.println("location=null");
			writer.println("license=null");
			writer.println("developer=null");
			writer.println("version=null");
			writer.println("timestamp=null");
			writer.close();
		} catch (IOException e) {
			// do something
			System.out.println("Properties file create fails!");
		}
	}

	public static void copyPrepertiesFromServer(String newNumber, Properties p) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			String Url = "jar:http://localhost:9999/myAuto/Database/" + "CommonApp_" + newNumber
					+ ".jar!/com/autoexample/info.properties";
			URL url = new URL(Url);
			JarURLConnection conn = (JarURLConnection) url.openConnection();
			JarFile jarfile = conn.getJarFile();
			JarEntry jarEntry = conn.getJarEntry();
			in = new BufferedInputStream(jarfile.getInputStream(jarEntry));
			out = new BufferedOutputStream(new FileOutputStream(web_base + "/info.properties"));
			byte[] buffer = new byte[2048];
			for (;;) {
				int nBytes = in.read(buffer);
				if (nBytes <= 0)
					break;
				out.write(buffer, 0, nBytes);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	public void log(FileWriter log_writer, Properties p, String selected) throws IOException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// System.out.println( );
		log_writer.write("Source: " + selected + "\r\n");
		log_writer.write("Release Time: " + p.getProperty("timestamp") + "\r\n");
		log_writer.write("Application Name:" + p.getProperty("name") + "\r\n");
		log_writer.write("Version Number: " + p.getProperty("version") + "\r\n");
		log_writer.write("Upload Time: " + sdf.format(cal.getTime()) + "\r\n");
		log_writer.write("\r\n");
		log_writer.write("\r\n");
		log_writer.flush();
		// log_writer.close();

	}

	public void refreshContent(Properties p) {
		name_content.getParent().layout();
		name_content.setText(p.getProperty("name"));

		version_content.getParent().layout();
		version_content.setText(p.getProperty("version"));

		releaseTime_content.setText(p.getProperty("timestamp"));
		releaseTime_content.getParent().layout();
	}

	public boolean jarSign(Path path) throws IOException, InterruptedException {
		String str = path.toString().replace('\\', '/');
		// System.out.println(str);
		ProcessBuilder pb = new ProcessBuilder(java_path + "jarsigner", "-keystore", java_path + "MyKeystore",
				"-storepass", "123456", str, "mykey");
		Process p = pb.start();
		p.waitFor();
		InputStream is = p.getInputStream();
		InputStream err = p.getErrorStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			if (line.contains("ÒÑÇ©Ãû")) {
				return true;
			}
		}
		isr = new InputStreamReader(err);
		br = new BufferedReader(isr);
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		return false;
	}

	public void getAppInfo() throws FileNotFoundException {
		File file = new File(web_base + "/info.properties");
		InputStream info;
		Properties p = new Properties();

		if (file.exists()) {
			info = new FileInputStream(web_base + "/info.properties");
			if (info != null) {
				try {
					p.load(info);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TODO close the stream
			}
		} else {
			// Properties file not found!
			createProperties(web_base + "/info.properties");
			info = new FileInputStream(web_base + "/info.properties");
			if (info != null) {
				try {
					p.load(info);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TODO close the stream
			}
		}
		setlayoutContent(p);
	}

	public void setlayoutContent(Properties p) {
		image_Mid = new Label(shell, SWT.NONE);
		image_Mid.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		image_Mid.setBounds(107, -11, 252, 103);
		image_Mid.setImage(SWTResourceManager.getImage(working_directory + "/resource/images/DHU.PNG"));

		name = new Label(shell, SWT.RIGHT);
		name.setAlignment(SWT.LEFT);
		name.setForeground(SWTResourceManager.getColor(128, 128, 128));
		name.setBackground(SWTResourceManager.getColor(255, 255, 255));
		name.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		name.setText("App Name: ");
		name.setBounds(10, 109, 78, 23);

		name_content = new Label(shell, SWT.NONE);
		name_content.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		name_content.setForeground(SWTResourceManager.getColor(250, 128, 114));
		name_content.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		name_content.setBounds(92, 109, 90, 19);
		name_content.setText(p.getProperty("name"));

		version = new Label(shell, SWT.SHADOW_IN | SWT.RIGHT);
		version.setAlignment(SWT.LEFT);
		version.setForeground(SWTResourceManager.getColor(128, 128, 128));
		version.setBackground(SWTResourceManager.getColor(255, 255, 255));
		version.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		version.setBounds(188, 109, 61, 23);
		version.setText("Current: ");

		version_content = new Label(shell, SWT.NONE);
		version_content.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		version_content.setForeground(SWTResourceManager.getColor(250, 128, 114));
		version_content.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		version_content.setBounds(255, 109, 30, 17);
		version_content.setText(p.getProperty("version"));

		releaseTime_content = new Label(shell, SWT.NONE);
		releaseTime_content.setAlignment(SWT.CENTER);
		releaseTime_content.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		releaseTime_content.setForeground(SWTResourceManager.getColor(250, 128, 114));
		releaseTime_content.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		releaseTime_content.setBounds(350, 109, 78, 17);
		releaseTime_content.setText(p.getProperty("timestamp"));

		note = new Label(shell, SWT.NONE);
		note.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		note.setForeground(SWTResourceManager.getColor(128, 128, 128));
		note.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD | SWT.ITALIC));
		note.setAlignment(SWT.CENTER);
		note.setBounds(46, 174, 362, 23);
		note.setText("NOTE: Please upload the newer version than current!");

		btn_upload = new Button(shell, SWT.NONE);
		btn_upload.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		btn_upload.setBounds(188, 138, 61, 23);
		btn_upload.setText("Upload");

		label_information = new Label(shell, SWT.NONE);
		label_information.setText("Information Display :");
		label_information.setForeground(SWTResourceManager.getColor(233, 150, 122));
		label_information.setFont(SWTResourceManager.getFont("Times New Roman", 13, SWT.BOLD));
		label_information.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_information.setBounds(10, 194, 159, 23);

		Text_infomation = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		Text_infomation.setForeground(SWTResourceManager.getColor(105, 105, 105));
		Text_infomation.setFont(SWTResourceManager.getFont("Times New Roman", 10, SWT.BOLD));
		Text_infomation.setBounds(10, 223, 415, 179);

		release = new Label(shell, SWT.SHADOW_IN | SWT.RIGHT);
		release.setText("Release:");
		release.setForeground(SWTResourceManager.getColor(128, 128, 128));
		release.setFont(SWTResourceManager.getFont("Times New Roman", 11, SWT.BOLD));
		release.setBackground(SWTResourceManager.getColor(255, 255, 255));
		release.setAlignment(SWT.LEFT);
		release.setBounds(291, 109, 68, 23);
		btn_upload.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					uploadFile(p);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
	}

	private void setDisplay(String str) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Text_infomation.append(sdf.format(cal.getTime()) + ":  ");
		Text_infomation.append(str);
	}
}
