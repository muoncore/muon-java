var muoncore = require('../muon/api/muoncore.js');
var amqpurl = "amqp://muon:microservices@localhost";
var uuid = require('node-uuid');

var config = {
    discovery:{
        type:"amqp",
        url:amqpurl
    },
    transport:{
        type:"amqp",
        url:amqpurl
    }
};

logger.info('starting muon dev tools server...');
muon = muoncore.create("muon-node-test-examples", amqpurl);



muon.handle('/ping', function (event, respond) {
    logger.debug('rpc://muon-node-test-examples/ping responding to event.id=' + event.id);
    respond("pong");
});



muon.handle('/echo', function (event, respond) {
    logger.debug('rpc://muon-node-test-examples/echo responding to event.id=' + event.id);
    respond(event.body);
});



muon.handle('/type', function (event, respond) {
    logger.debug('rpc://muon-node-test-examples/type responding to event.id=' + event.id);
    respond(typeof event.body);
});



muon.handle('/random', function (event, respond) {
    logger.debug('rpc://muon-node-test-examples/random responding to event.id=' + event.id);
    var max = 99999;
    var min = 10000;
    var randomNumber = Math.floor(Math.random() * (max - min + 1)) + min;
    respond(randomNumber);
});



muon.handle('/uuid', function (event, respond) {
    logger.debug('rpc://muon-node-test-examples/uuid responding to event.id=' + event.id);
    respond(uuid.v4());
});




muon.handle('/json', function (event, respond) {
    logger.debug('rpc://muon-node-test-examples/json responding to event.id=' + event.id);
    respond({message: 'hello, world!', server_url: 'request://muon-dev-tools/json', echo: event.body});
});



muon.handle('/function', function (event, respond) {
    logger.debug('rpc://muon-node-test-examples/function responding to event.id=' + event.id);
    var func = new Function(event.body);
    var result = func();
    respond(result);
});
