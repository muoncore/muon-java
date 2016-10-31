
var assert = require('assert');
var expect = require('expect.js');
var messageHelper = require('../../../muon/domain/messages.js');
require('sexylog');

describe("amqp api test:", function () {

    this.timeout(8000);

      after(function() {

      });

    it("send and receive arbitrary number of messages", function (done) {
            var url = process.env.MUON_URL || "amqp://muon:microservices@localhost";
            var numMessages = 50;
            var messageCount = 0;
            var amqp = require('../../../muon/transport/amqp/amqp-api.js');
            var amqpConnect = amqp.connect(url);

             amqpConnect.then(function (amqpApi) {

                   var payload = {text: "amqp_api_test_message"};

                   console.log('waiting for message');
                   amqpApi.inbound('api_test_queue').listen(function(message) {
                       console.log('message received: ' + JSON.stringify(message));
                       assert.equal(message.data.text, payload.text);
                       messageCount++;
                       if (messageCount == numMessages) {
                            done();
                       }

                   });
                  console.log('sending payload');
                  for (var i = 0 ; i < numMessages ; i++) {
                      setTimeout(function() {
                        payload.id = i;
                        var message = {
                            data: payload,
                            headers: {
                                protocol: 'rpc'
                            }
                        }
                        amqpApi.outbound('api_test_queue').send(message);
                      }, 10);


                  }

            }, function (err) {
                console.log("muon promise.then() error!!!!!");
                throw new Error('error in return amqp-api promise');
            }).catch(function(error) {
               console.log("amqp-api-test.js connection.then() error!!!!!: " + error);
                throw new Error('error in return muon promise in amqp-api-test-test.js', error);
                assert.ok(false);
            });

    });


    it("sending invalid message schema throws exception on channel", function (done) {
            var url = process.env.MUON_URL || "amqp://muon:microservices@localhost";
            var payload = {id: 'A', text: "a_sample_test_message"};

            var invalidMessage = {
                payload: payload,
                headers: {
                    protocol: 'rpc'
                }
            }
            var amqp = require('../../../muon/transport/amqp/amqp-api.js');
            var amqpConnect = amqp.connect(url);
             amqpConnect.then(function (amqpApi) {
                  amqpApi.inbound('api_test_queue').listen(function(message) { });
                  var channel = amqpApi.outbound('api_test_queue');
                  channel.onError(function(err) {
                      assert.ok(err);
                      done();
                  });
                  channel.send(invalidMessage);
            });

    });


     it("invalid amqp url string", function (done) {
            var amqp = require('../../../muon/transport/amqp/amqp-api.js');
            var amqpConnect = amqp.connect('blah');

             amqpConnect.then(function (amqpApi) {
                console.log("muon amqpConnect.then() status ok");
            }, function (err) {
                console.log("muon amqpConnect.then() " + err);
                var errString = 'Error: invalid ampq url: blah';
                expect(err.toString()).to.contain(errString);
                done();
            }).catch(function(err) {
                console.log(err);
                done(err);
            });
     });


      it("invalid amqp url auth", function (done) {
            var amqp = require('../../../muon/transport/amqp/amqp-api.js');
            var amqpConnect = amqp.connect('amqp://bob:password@localhost');

             amqpConnect.then(function (amqpApi) {
                console.log("muon amqpConnect.then() status ok");
            }, function (err) {
                console.log("muon amqpConnect.then() " + err);
                var errString = 'Handshake terminated by server: 403 (ACCESS-REFUSED) with message';
                expect(err.toString()).to.contain(errString);
                done();
            }).catch(function(err) {
                console.log(err);
                done(err);
            });
      });

/* amqp connect  behaviour seems to vary depending on network type
      it("invalid amqp url host", function (done) {
          var amqp = require('../../../muon/transport/amqp/amqp-api.js');
            var amqpConnect = amqp.connect('amqp://bob:password@lolcathost');

             amqpConnect.then(function (amqpApi) {
                console.log("muon amqpConnect.then() status ok");
            }, function (err) {
                console.log("muon amqpConnect.then() " + err);
                var errString = 'Error: getaddrinfo ENOTFOUND lolcathost lolcathost:5672';
                expect(err.toString()).to.contain(errString);
                done();
            }).catch(function(err) {
                console.log(err);
                done(err);
            });
      });
      */


      it("invalid amqp url port", function (done) {
            var amqp = require('../../../muon/transport/amqp/amqp-api.js');
            var amqpConnect = amqp.connect('amqp://bob:password@localhost:60606');

             amqpConnect.then(function (amqpApi) {
                console.log("muon amqpConnect.then() status ok");
            }, function (err) {
                console.log("muon amqpConnect.then() " + err);
                var errString = 'Error: connect ECONNREFUSED 127.0.0.1:60606';
                expect(err.toString()).to.contain(errString);
                done();
            }).catch(function(err) {
                console.log(err);
                done(err);
            });
      });
});
