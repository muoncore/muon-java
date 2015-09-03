package io.muoncore.test;

import static org.junit.Assert.assertEquals;
import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.MuonClient.MuonResult;
import io.muoncore.MuonService;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.future.MuonFuture;
import io.muoncore.future.MuonFutures;
import io.muoncore.transport.resource.MuonResourceEvent;
import io.muoncore.transport.resource.MuonResourceEventBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author sergio
 *
 */
public class MuonTestCase {
	private Muon 	m, c;
	private String	uuid;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() throws Exception {
		uuid = UUID.randomUUID().toString();
		Discovery d = new AmqpDiscovery("amqp://localhost");
		AmqpTransportExtension ate = new AmqpTransportExtension("amqp://localhost");
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
		
		d = new AmqpDiscovery("amqp://localhost");
		ate = new AmqpTransportExtension("amqp://localhost");
		c = new Muon(d);
		c.setServiceIdentifer("test-" + uuid + "-client");
		c.addTag("client");
		ate.extend(c);
		c.start();
		
		Thread.sleep(10000);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void onePost() throws InterruptedException, ExecutionException {
		Map resource = new HashMap();
		resource.put("val", 1.0);
		
		MuonResourceEventBuilder mre = MuonResourceEventBuilder.event(resource);
		mre.withUri("muon://test-" + uuid + "/post-endpoint");
		MuonFuture mf = c.command("muon://test-" + uuid + "/post-endpoint", mre.build(), Map.class);
		System.out.println(mf.get().getClass());
		MuonResult result = (MuonResult) mf.get();
		Map mResult = (Map) result.getResponseEvent().getDecodedContent();
		
		assertEquals(mResult.get("val"), 2.0);
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void manyPosts() throws InterruptedException, ExecutionException {
		for(int i=0;i<10;i++) {
			Map resource = new HashMap();
			resource.put("val", 1.0);

			MuonResourceEventBuilder mre = MuonResourceEventBuilder.event(resource);
			mre.withUri("muon://test-" + uuid + "/post-endpoint");
			MuonFuture mf = c.command("muon://test-" + uuid + "/post-endpoint", mre.build(), Map.class);
			MuonResult result = (MuonResult) mf.get();
			Map mResult = (Map) result.getResponseEvent().getDecodedContent();
			System.out.println(mResult);

			assertEquals(mResult.get("val"), 2.0);
		}
	}
}
