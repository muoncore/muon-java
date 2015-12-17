package com.simplicity.services;

public class ServicePublishColdStream {

//    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {
//
//        final OldMuon muon = new OldMuon(
//                new AmqpDiscovery("amqp://localhost"));
//
//        muon.setServiceIdentifer("cl");
//        new AmqpTransportExtension("amqp://localhost").extend(muon);
//        muon.start();
//
//        muon.streamSource("/counter", Long.class, new MuonStreamGenerator<Long>() {
//            @Override
//            public Publisher<Long> generatePublisher(Map<String, String> parameters) {
//                long max = Long.parseLong(parameters.get("max"));
//                return Streams.range(0, max);
//            }
//        });
//
//        muon.streamSource("/countersimple", Long.class, Streams.range(0, 100));
//    }
}
