//
require("sexylog")

var Muon = require("../muon/api/muoncore");

var muon = Muon.create("my-tester", process.env.MUON_URL || "amqp://muon:microservices@localhost");

muon.infrastructure().getTransport().then(function(transport) {
    // var internalChannel = transport.openChannel("awesomeservicequery", "rpc");
    setInterval(function() {
        // var internalChannel2 = transport.openChannel("awesomeservicequery", "rpc");
        muon.emit({
            "event-type": "MyAwesomeEvent",
            "stream-name": "woot",
            "service-id": "testclient",
            payload: { "big": "boo boo"}
        }).then(function(resp) {
            console.log(JSON.stringify(resp))
        })

    }, 5)
})
//    