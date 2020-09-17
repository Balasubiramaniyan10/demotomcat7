package com.freewinesearcher.batch;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.freewinesearcher.common.Dbutil;

public class DeployNewVersion {

	public static void Deploy(){
		String command = "start C:\\deploynewversion.bat";
		try{
			//Process p = Runtime.getRuntime().exec(command);
			Process p = Runtime.getRuntime().exec("cmd.exe /c "+command);
		} catch (Exception e){
			Dbutil.logger.error("Could not start batch file to deploy.",e);
		}
	}

	public static void RestoreLastKnownGood(){
		String command = "start C:\\restorelastknowngood.bat";
		try{
			Process p = Runtime.getRuntime().exec("notepad.exe");
		} catch (Exception e){
			Dbutil.logger.error("Could not start batch file to restore lastknowngood.",e);
		}

	}


	public static void runBatchFile(String filename){
		Runtime r=Runtime.getRuntime();
		Process p=null;
		try
		{
			p = r.exec(new String[]{"cmd","/c","start C:/temp/test.bat"});
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line=null;

			while((line=input.readLine()) != null) {
				System.out.println(line);
			}
			System.out.println("Exit Value = " + p.waitFor());//Method waitFor() will make the current thread to wait until the external program finish and return the exit value to the waited thread.

		}

		catch(Exception e){
			System.out.println("error==="+e.getMessage());
			e.printStackTrace();
		}
	}



}
