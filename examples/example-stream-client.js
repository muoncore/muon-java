var muoncore = require('../muon/api/muoncore.js');


//
var amqpurl = "amqp://muon:microservices@localhost";
//var amqpurl = 'amqp://guest:guest@conciens.mooo.com';

logger.info('starting muon...');
muon = muoncore.create("test-client", amqpurl);


setTimeout(function() {
    var then = new Date().getTime()
    console.log("Starting request!")

    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()
    subscribe()

    
    // var promise = muon.request('rpc://muon-dev-tools/echo', {"search": "red"});
    //
    // promise.then(function (event) {
    //     var now = new Date().getTime()
    //     console.log("Latency is " + (now - then))
    //     process.exit(0);
    // });


}, 6000)


function subscribe() {
    muon.subscribe("stream://awesomeservicequery/ticktock", {},
        function(data) {
            logger.error("Data...")
            console.dir(data)
        },
        function(error) {
            logger.error("Errored...")
            console.dir(error)
        },
        function() {
            logger.warn("COMPLETED STREAM")
        }
    )
}