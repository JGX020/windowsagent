package org.hp.test.hostagent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Tail;
import org.hyperic.sigar.shell.ShellCommandExecException;
import org.hyperic.sigar.shell.ShellCommandUsageException;
import org.hyperic.sigar.win32.EventLog;
import org.hyperic.sigar.win32.EventLogNotification;
import org.hyperic.sigar.win32.EventLogRecord;
import org.hyperic.sigar.win32.EventLogThread;
import org.hyperic.sigar.win32.Win32Exception;


public class timeagent {
	public static void main( String[] args )
    {
    	 
    	  final String[] newargs=args;
    	
    	TimerTask task=new TimerTask(){
    		@Override
    		public void run(){
    			try {
					new CpuInfo().processCommand(newargs);
					 new Df().processCommand(newargs);
					 Tail tail = new Tail();
				        tail.parseArgs(newargs);

				        if (tail.files.size() == 0) {
				            tail.files.add(EventLog.SYSTEM);
				        }

				        for (int i=0; i<tail.files.size(); i++) {
				            String name = (String)tail.files.get(i);
				            tail(name, tail);

				            if (tail.follow) {
				                TailNotification notifier = new TailNotification();
				                EventLogThread thread = 
				                    EventLogThread.getInstance(name);
				                thread.add(notifier);
				                thread.doStart();
				            }
				        }

				        if (tail.follow) {
				            System.in.read();
				        }
				        new Ps().processCommand(newargs);
				        new Route().processCommand(newargs);
				} catch (ShellCommandUsageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ShellCommandExecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SigarException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	};
    	Timer timer=new Timer();
    	long delay=0;
    	long intevalPeriod =60*1000;
    	timer.scheduleAtFixedRate(task, delay, intevalPeriod);
        
    }
	private static void tail(String name, Tail tail) throws Win32Exception {
        EventLog log = new EventLog();
        log.open(name);
        int max = log.getNumberOfRecords();
        if (tail.number < max) {
            max = tail.number;
        }
        int last = log.getNewestRecord()+1;
        int first = last - max;

        for (int i=first; i<last; i++) {
            EventLogRecord record = log.read(i);
            System.out.println(record);
        }
        log.close();
    }

    private static class TailNotification implements EventLogNotification {
        public void handleNotification(EventLogRecord event) {
            System.out.println(event);
        }

        public boolean matches(EventLogRecord event) {
            return true;
        }
    }

}

