package org.apm.start;

import org.apm.comm.annotation.StatisticsTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@StatisticsTime("say")
public class Say {
	
	static Logger logger = LoggerFactory.getLogger(App.class);
	
	public void say(int x){
		for(int i=0;i<x;i++) {
			System.out.println(i);
		}
		
	}
	
	
	
	public static void main(String[] args) {
	}

}
