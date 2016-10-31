var muoncore = require('../muon/api/muoncore.js');


//
var amqpurl = "amqp://muon:microservices@192.168.99.100";
//var amqpurl = 'amqp://guest:guest@conciens.mooo.com';

logger.info('starting muon...');
muon = muoncore.create("test-client", amqpurl);


setTimeout(function() {
    var then = new Date().getTime()
    console.log("Starting request!")
    
    muon.replay("something",
        {"stream-type":"hot-cold"},
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
    
    
    // var promise = muon.request('rpc://muon-dev-tools/echo', {"search": "red"});
    //
    // promise.then(function (event) {
    //     var now = new Date().getTime()
    //     console.log("Latency is " + (now - then))
    //     process.exit(0);
    // });


}, 6000)
