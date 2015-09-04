package io.muoncore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.MuonClient.MuonResult;
import io.muoncore.MuonService;
import io.muoncore.MuonStreamGenerator;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.future.MuonFuture;
import io.muoncore.future.MuonFutures;
import io.muoncore.transport.resource.MuonResourceEvent;
import io.muoncore.transport.resource.MuonResourceEventBuilder;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author sergio
 *
 */
public class MuonTestCase {
	private Muon 			m, c;
	private String			uuid;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() throws Exception {
		uuid = UUID.randomUUID().toString();
		Discovery d = new AmqpDiscovery("amqp://muon:microservices@localhost");
		AmqpTransportExtension ate = new AmqpTransportExtension("amqp://muon:microservices@localhost");
		m = new Muon(d);
		m.setServiceIdentifer("test-" + uuid);
		m.addTag("server");
		ate.extend(m);
		m.start();
		
		m.onCommand("/post-endpoint", Map.class,
				new MuonService.MuonCommand() {
					@Override
					public MuonFuture onCommand(MuonResourceEvent queryEvent) {
						System.out.println("onCommand");
						
						Map res = new HashMap();
						Map resource = (Map) queryEvent.getDecodedContent();
						
						System.out.println(resource);
						
						res.put("val", (Double) resource.get("val") + 1.0);
						
						return MuonFutures.immediately(res);
					}
		});
		m.streamSource("/stream-endpoint", Map.class,
				new MuonStreamGenerator<Map>() {
					@Override
					public Publisher<Map> generatePublisher(
							Map<String, String> parameters) {
						return new Publisher<Map>() {
							@Override
							public void subscribe(Subscriber<? super Map> s) {
								Map elem = new HashMap();
								for(double i=1.0;i<6.0;i=i+1) {
									elem.put("val", i);
									s.onNext(elem);
								}
								s.onComplete();
							}
						};
					}
		});
		
		d = new AmqpDiscovery("amqp://muon:microservices@localhost");
		ate = new AmqpTransportExtension("amqp://muon:microservices@localhost");
		c = new Muon(d);
		c.setServiceIdentifer("test-" + uuid + "-client");
		c.addTag("client");
		ate.extend(c);
		c.start();
		
		Thread.sleep(10000);
	}
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Test
//	public void onePost() throws InterruptedException, ExecutionException {
//		Map resource = new HashMap();
//		resource.put("val", 1.0);
//
//		MuonResourceEventBuilder mre = MuonResourceEventBuilder.event(resource);
//		mre.withUri("muon://test-" + uuid + "/post-endpoint");
//		MuonFuture mf = c.command("muon://test-" + uuid + "/post-endpoint", mre.build(), Map.class);
//		System.out.println(mf.get().getClass());
//		MuonResult result = (MuonResult) mf.get();
//		Map mResult = (Map) result.getResponseEvent().getDecodedContent();
//
//		assertEquals(mResult.get("val"), 2.0);
//	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void manyPosts() throws InterruptedException, ExecutionException {
		for(int i=0;i<10;i++) {
			System.out.println("----------------------------------------------------------------------------------");

			Map resource = new HashMap();
			resource.put("val", 1.0);

			MuonResourceEventBuilder mre = MuonResourceEventBuilder.event(resource);
			mre.withUri("muon://test-" + uuid + "/post-endpoint");
			MuonFuture mf = c.command("muon://test-" + uuid + "/post-endpoint", mre.build(), Map.class);
			MuonResult result = (MuonResult) mf.get();
			if (!result.isSuccess()) {
				System.out.println("Not working");
			}
			Map mResult = (Map) result.getResponseEvent().getDecodedContent();
			System.out.println(" Return is " + mResult);

			assertEquals(mResult.get("val"), 2.0);
			System.out.println("===========================================================================================");
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void streamTest() throws URISyntaxException, InterruptedException {
		BlockingQueue	bq;
		Map				endToken;
		
		bq = new LinkedBlockingQueue();
		endToken = new HashMap();
		endToken.put("end", "ok");
		
		c.subscribe("muon://test-" + uuid + "/stream-endpoint",
				Map.class, new Subscriber() {
					@Override
					public void onSubscribe(Subscription s) {
						System.out.println("Subscribed!");
					}

					@Override
					public void onNext(Object t) {
						try {
							bq.put(t);
						} catch (InterruptedException e) {
							fail(e.getMessage());
						}
					}

					@Override
					public void onError(Throwable t) {
						fail(t.getMessage());
					}

					@Override
					public void onComplete() {
						try {
							bq.put(endToken);
						} catch (InterruptedException e) {
							fail(e.getMessage());
						}
					}
		});
		
		Map elem = (Map) bq.take();
		double i = 1.0;
		while(elem.get("end") == null || !elem.get("end").equals("ok")) {
			assertEquals(elem.get("val"), i);
			i = i + 1.0;
		}
	}
}
