package Install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import Receive.receive_main;

public class Installer implements Runnable {
	static receive_main receive = new receive_main();
	public static final String SUN_JAVA_COMMAND = "sun.java.command";
	private String java_path;
	private FileWriter log_writer;
	String jarname;
	String working_directory;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			init();
			if (run_install()) {
				displayInstall(jarname + " install successful!\n");
				log(log_writer);
				restart();
			} else {
				displayInstall(jarname + " install failure!\n\n");
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init() throws IOException {
		working_directory = System.getProperty("user.dir").replace('\\', '/');
		java_path = System.getenv("JAVA_HOME").replace('\\', '/');
		log_writer = new FileWriter(working_directory + "/resource/secure/log_install.txt", true);
		String infopath = working_directory + "/resource/myapp/info.properties";
		Properties p = new Properties();
		InputStream info = new FileInputStream(infopath);// reader
		p.load(info);
		jarname = p.getProperty("name") + "_" + p.getProperty("version");
		clearInstall();
	}

	public boolean run_install() throws IOException {
		displayInstall("Prepare to install " + jarname + "\n");
		String jar = working_directory.replace("Program Files", "\"Program Files\"") + "/resource/myapp/" + jarname
				+ ".jar";
		System.out.println("install:" + jar);
		// ProcessBuilder pb = new ProcessBuilder(java_path,"-jar",jar);
		Process p = Runtime.getRuntime().exec("java -jar " + jar);
		try {
			InputStream is = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("Seconds")) {
					return true;
				}
				System.out.println(line);
				display_notime(line + "\n");
			}

			InputStream err = p.getErrorStream();
			isr = new InputStreamReader(err);
			br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void restart() throws InterruptedException, IOException {
		receive.appRun = false;
		Thread.sleep(5000);
		displayInstall("Restart device!\n");
		clearConnect();
		clearInstall();
		Thread.sleep(1000);
		//restartApplication();
		//System.exit(0);
		appRun();

	}

	public static void restartApplication() throws IOException, InterruptedException {
		
				// TODO Auto-generated method stub
				String separator = System.getProperty("file.separator");
				String classpath = System.getProperty("java.class.path");
				String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
				ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath,
						receive.getClass().getName());
				System.out.println(path);
				System.out.println(classpath);
				Process process;
				try {
					process = processBuilder.start();
					System.exit(0);
					/*InputStream is = process.getInputStream();
					InputStream err = process.getErrorStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					String line;

					while ((line = br.readLine()) != null) {
						System.out.println(line);
					}*/
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// process.waitFor();
				System.out.println(classpath);
		
	}

	public void appRun() throws IOException, InterruptedException {
		Runnable Task = new Runnable() {
			@Override
			public void run() {
				File file = new File(working_directory + "/resource/myapp/info.properties");
				if (file.exists()) {
					Properties p = new Properties();
					InputStream info;
					try {
						info = new FileInputStream(file);
						p.load(info);
						String jarname = p.getProperty("name") + "_" + p.getProperty("version");
						String jar = working_directory.replace("Program Files", "\"Program Files\"")
								+ "/resource/myapp/" + jarname + ".jar";
						Process proc = Runtime.getRuntime().exec("java -jar " + jar);
						System.out.println(jarname);
						InputStream is = proc.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr);
						String line;
						int i = 0;
						displayInstall(jarname + " is starting, Please wait!\n");
						while ((line = br.readLine()) != null) {
							// System.out.println(line);
							if (line.contains("Seconds")) {
								if (i == 0)
									displayInstall("Start timer!\n");
								displayInstall(line + "\n");
								i++;
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
					} // re
				} else {
					displayInstall("No application is Running!\n\n");
				}
			}
		};
		Thread runThread = new Thread(Task);
		runThread.start();
	}

	public void displayInstall(String str) {
		receive.Text_install.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				receive.Text_install.append(sdf.format(cal.getTime()) + ":  ");
				receive.Text_install.append(str);
			}
		});
	}

	public void display_notime(String str) {
		receive.Text_install.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				receive.Text_install.append(str);
			}
		});
	}

	private void log(FileWriter log_writer) throws IOException {
		File file = new File(working_directory + "/resource/myapp/info.properties");
		Properties p = new Properties();
		InputStream info = new FileInputStream(file);// reader
		p.load(info);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		log_writer.write("Application Name:" + p.getProperty("name") + "\r\n");
		log_writer.write("Version Number: " + p.getProperty("version") + "\r\n");
		log_writer.write("Install Time: " + sdf.format(cal.getTime()) + "\r\n");
		log_writer.write("\r\n");
		log_writer.write("\r\n");
		log_writer.flush();
	}
	
	public void clearConnect() {
		receive.Text_connect.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				receive.Text_connect.setText("");
				;
			}
		});
	}
	
	public void clearInstall() {
		receive.Text_install.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				receive.Text_install.setText("");
				;
			}
		});
	}
}
