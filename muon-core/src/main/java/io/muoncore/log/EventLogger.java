package io.muoncore.log;

import io.muoncore.Muon;
import io.muoncore.ServiceDescriptor;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.resource.MuonResourceEventBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventLogger {
	private static PrintWriter							pw = null;
	private static Muon									muon = null;
	private static String								photon = null;
	private static ThLogger								thLogger = null;
	private static BlockingQueue<Map<String,Object>>	queue =
			new LinkedBlockingQueue<Map<String,Object>>();
	// allEvents has been used for REPL live debugging from Clojure
//	public  static LinkedList<Map<String,Object>>		allEvents =
//			new LinkedList<Map<String,Object>>();

	static class ThLogger extends Thread {
		@Override
		public void run() {
			Map<String, Object> event;
			while(true) {
				try {
					event = queue.take();
					if(photon != null) {
						Map<String,Object> payload = new HashMap<String, Object>();
						payload.put("service-id", "muon://" + muon.getServiceIdentifer());
						payload.put("local-id", UUID.randomUUID());
						payload.put("server-timestamp", System.currentTimeMillis());
						//			Map<String,Object> provenance = new HashMap<String,Object>();
						//			provenance.put("service-id", value);
						//			provenance.put("local-id", value);
						//			payload.put("provenance", provenance);
						MuonMessageEvent ev = (MuonMessageEvent)event.get("payload");
						payload.put("stream-name", "eventlog");
						payload.put("payload", ev.toString());
						if(ev.getDecodedContent() != null) {
							payload.put("payload-decoded", ev.getDecodedContent().toString());
						}
						if(ev.getBinaryEncodedContent() != null) {
							payload.put("payload-decoded", ev.getBinaryEncodedContent().toString());
						}
						MuonResourceEventBuilder evb = MuonResourceEventBuilder.event(payload);
						evb.withUri("muon://photon/events");
						muon.command("muon://photon/events",
								evb.build(),
								Map.class);
						pw.println(photon.toString() + "|" +
								event.get("channel") + "|" +
								event.get("payload"));
						pw.flush();
					}
				} catch (Exception e) {
					pw.println(e.getMessage());
					e.printStackTrace(pw);
					pw.flush();
				}				
			}
		}
	}

	static class ThCrawl extends Thread {
		@Override
		public void run() {
			try {
				while(true) {
					pw.println("Crawling...");
					pw.flush();

					List<ServiceDescriptor> availableServices = muon.discoverServices();
					Iterator<ServiceDescriptor> itServices = availableServices.iterator();
					while(itServices.hasNext()) {
						ServiceDescriptor s = itServices.next();
						List<String> tags = s.getTags();
						if(tags.contains("photon")) {
							photon = "muon://" + s.getIdentifier();
						}
					}

					pw.println("Number of services: " + availableServices.size());
					pw.flush();

					itServices = availableServices.iterator();
					while(itServices.hasNext()) {
						ServiceDescriptor s = itServices.next();
						List<String> tags = s.getTags();
						pw.print(s.getIdentifier());
						pw.println(tags.toString());
						// pw.println(s.getStreamConnectionUrls().toString());
						pw.flush();
					}

					try {
						long waiting = 600000;
						if(photon == null) {
							waiting = 10000;
						}
						sleep(600000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			catch(Exception e) {
				pw.println(e.getMessage());
				e.printStackTrace(pw);
				pw.flush();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void initialise(Muon m) {
		muon = m;
		photon = null;
		
		try {
			File f = File.createTempFile("event", ".log");
			pw = new PrintWriter(f);
			System.out.println("Logging events at " + f.getAbsolutePath());
			pw.println("Starting...");
			pw.flush();

			new ThCrawl().start();
			if(thLogger == null) {
				thLogger = new ThLogger();
				thLogger.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void logEvent(String queueName, MuonMessageEvent event) {
		Map<String,Object> ev = new HashMap<String,Object>();
		
		pw.println("Logging event...");
		pw.flush();
		if(event != null /* &&
				event.getDecodedContent().toString() != null &&
				!event.getDecodedContent().toString().equals("") */)
		{
			ev.put("channel", queueName);
			ev.put("payload", event);
		
			pw.println("!!!!!!!!!!       SUCCESS       !!!!!!!!!!!!!!");
			queue.offer(ev);
//			allEvents.add(ev);
		}
	}
}
