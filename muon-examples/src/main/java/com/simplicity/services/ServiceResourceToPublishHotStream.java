package com.simplicity.services;

public class ServiceResourceToPublishHotStream {

//    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {
//
//        final OldMuon muon = new OldMuon(
//                new AmqpDiscovery("amqp://localhost:5672"));
//
//        muon.setServiceIdentifer("resourcePublisher");
//        new AmqpTransportExtension("amqp://localhost:5672").extend(muon);
//        muon.start();
//
//        final Broadcaster<Map> stream = Broadcaster.create();
//
//        muon.onQuery("/data", Map.class, new MuonService.MuonQueryListener<Map>() {
//            @Override
//            public MuonFuture onQuery(MuonResourceEvent queryEvent) {
//
//                Map<String, String> data = new HashMap<String, String>();
//
//                stream.accept(data);
//
//                return MuonFutures.immediately(data);
//            }
//        });
//
//        muon.streamSource("/livedata", Map.class, stream);
//    }
}
