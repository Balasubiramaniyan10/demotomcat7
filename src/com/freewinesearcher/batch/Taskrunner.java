package com.freewinesearcher.batch;

import java.sql.ResultSet;
import java.sql.Statement;

import com.freewinesearcher.batch.sitescrapers.SuckWineSearcher;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Winerating;

	
public class Taskrunner implements Runnable{
	public String task="";
	public String directory="";
	
	public void run() {
		if (task.equals("analyseRatedWines")){
			Winerating.refreshRatedWines();
		}
		if (task.equals("analyseKnownWines")){
			new Knownwines(0);
			
		}
		if (task.equals("analyseRedundantWineTerms")){
			Knownwines.analyseRedundantWineTerms();
		}
		if (task.equals("suckWineSearcher")){
			SuckWineSearcher sws=new SuckWineSearcher();
			sws.directory=directory;
			sws.run();
		}
		
		
	}
	
}
