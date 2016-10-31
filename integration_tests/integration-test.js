var muoncore = require('../muon/api/muoncore.js');
var assert = require('assert');
var expect = require('expect.js');




describe("Muon core integration test:", function () {


    this.timeout(1000 * 60 * 60 * 24); //24 hours
    var serviceName = "amqp-resource-test-service";
    var amqpurl = process.env.MUON_URL || "amqp://muon:microservices@localhost";


    before(function () {

    });

    after(function() {

    });



    it("test amqp resoruces are released", function (done) {


        var muonServer = muoncore.create(serviceName, amqpurl);
        muonServer.handle('/ello', function (event, respond) {
            logger.warn('*****  muon://' + serviceName + '/ello: integration-test.js *************************************************');
            logger.warn('responding to incoming rpc message=' + JSON.stringify(event));
            respond("Hello, world");
        });

        var muonClient = muoncore.create("integration-client", amqpurl);

        setInterval(function() {
          var promise = muonClient.request('rpc://' + serviceName +'/ello', "ping");

          promise.then(function (response) {
              logger.warn("rpc://integration-client server response received! response=" + JSON.stringify(response));
              logger.info("Response is " + JSON.stringify(response))
              assert(response, "request response is undefined");
              assert.equal(response.body, "Hello, world", "expected 'Hello, world' but was " + response.body)

          }, function (err) {
              logger.error("muon promise.then() error!\n" + err.stack);
              done(err);
          }).catch(function(error) {
              logger.error("muoncore-test.js promise.then() error!:\n" + error.stack);
              done(error);

          });

        }, 15000);




    });



});
