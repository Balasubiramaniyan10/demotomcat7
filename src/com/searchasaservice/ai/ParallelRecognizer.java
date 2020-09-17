package com.searchasaservice.ai;

import com.freewinesearcher.common.Dbutil;

public class ParallelRecognizer implements Runnable{
	int parallellism=1;
	int thisthread=0;
	Recognizer rec;
	public boolean problem=false;

	
	public ParallelRecognizer(Recognizer rec,int threads) {
		super();
		this.rec=rec;
		parallellism=threads;
		rec.restart=false;
		
		
	}
	
	public void startAnalyses(){
		ThreadGroup tg=new ThreadGroup("Recognizer");
		for (int i=0;i<parallellism;i++){
			thisthread=i;
			new Thread(tg,this).start();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Dbutil.logger.error("Interupted!!!",e);
			}
		}
		while (tg.activeCount()>0){
			try {
				Thread.sleep(60000);
				Dbutil.logger.info(tg.activeCount()+" threads active");
			} catch (InterruptedException e) {
				Dbutil.logger.error("Interupted!!!",e);
			} 
		}
		Dbutil.logger.info(tg.activeCount()+" threads active");
		
	}


	@Override
	public void run() {
		try {
			rec.clone().getMatches(0, parallellism, thisthread);
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ",e);
			problem=true;
		}
		
	}

	
	

}
