package com.autoexample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class Auto {
	static Timer timer = new Timer(true);
	// String install_end;

	public static void main(String[] args) throws IOException, ParseException {
		InputStream in = Auto.class.getResourceAsStream("info.properties");
		Properties p = new Properties();
		if (in != null) {
			p.load(in);
		} else {
			// Properties file not found!
			System.out.println("Properties file not found!");
		}
		String name = p.getProperty("name");
		String vendor = p.getProperty("vendor");
		String location = p.getProperty("location");
		String license = p.getProperty("license");
		String developer = p.getProperty("developer");
		String version = p.getProperty("version");
		String release_time = p.getProperty("timestamp");

		System.out.println("Application  information========================");
		System.out.println("Application name: " + name);
		System.out.println("Vendor: " + vendor);
		System.out.println("Location: " + location);
		System.out.println("License: " + license);
		System.out.println("Developer: " + developer);
		System.out.println("Version: " + version);
		System.out.println("Release time: " + release_time);

		System.out.println("");
		System.out.println("Update Progress=================================");
		System.out.println("Update Start");

		try {
			System.out.println("Loading...");
			Thread.sleep(1000);
			System.out.println("Compeleting 25%");
			Thread.sleep(500);
			System.out.println("Compeleting 50%");
			Thread.sleep(500);
			System.out.println("Compeleting 75%");
			Thread.sleep(500);
			System.out.println("Compeleting 100%");
			Thread.sleep(500);
			System.out.println("Install End!");

			Calendar cal1 = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String install_end = sdf.format(cal1.getTime());
			System.out.println("Install End Time: " + install_end);
			// Date date1 = sdf.parse(install_end);
			run(p, cal1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void run(Properties p, Calendar cal1) {
		String jarname = p.getProperty("name") + "_"+p.getProperty("version");
		int i =0;
		while (i!=20) {
			// Do your task
			Calendar cal2 = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String runningTime = sdf.format(cal2.getTime());
			//System.out.println(runningTime);
			
			long diff = cal2.getTime().getTime() - cal1.getTime().getTime();
			
			if(diff!=0){
				System.out.println(jarname + " have been running " + (float)diff/(1000)+" Seconds");
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			i++;
		}
	}
}
