var bichannel = require('../../muon/infrastructure/channel.js');
var handler = require('../../muon/infrastructure/handler.js');
var assert = require('assert');
var expect = require('expect.js');
require('sexylog');
var csp = require("js-csp");

describe("Handler test:", function () {

    this.timeout(4000);


      after(function() {
            //bi-channel.closeAll();
      });

    it("handler between two channels", function (done) {

            var msg = {count: 0, audit: []};

              var upstream = bichannel.create("upstream");
              var downstream = bichannel.create("downstream");

               var testHandler = handler.create('test');


              // OUTGOING/DOWNSTREAM event handling protocol logic
               testHandler.outgoing(function(message, forward, back) {
                    message.count++;
                    message.audit.push('testHandler.outgoing()');
                    forward(message);
               });

             // OUTGOING/DOWNSTREAM event handling protocol logic
              testHandler.incoming(function(message, forward, back) {
                     message.count++;
                     message.audit.push('testHandler.incoming()');
                     forward(message);
              });

                upstream.rightHandler(testHandler);
                downstream.leftHandler(testHandler);

                upstream.leftSend(msg);


                upstream.leftConnection().listen(function(message) {
                        console.log('***** upstream message returned:');
                        console.dir(message);
                        assert.equal(message.count, 2);
                        assert.equal(message.audit[0], 'testHandler.outgoing()');
                        assert.equal(message.audit[1], 'testHandler.incoming()');
                        done();
                });


                downstream.rightConnection().listen(function(message){
                    downstream.rightSend(message);
                });

    });


    it("single channel handler routes inbound message to callback and return response outbound", function (done) {

            var msg = {count: 0, audit: [], routingKey: 'server'};



               var testHandler = handler.create('test');
              // OUTGOING/DOWNSTREAM protocol logic
               testHandler.outgoing(function(message, forward, back, route) {
                    message.count++;
                    message.audit.push('testHandler.outgoing()');
                    forward(message);
               });

             // INCOMING/UPSTREAM eprotocol logic
              testHandler.incoming(function(message, forward, back, route) {
                     message.count++;
                     message.audit.push('testHandler.incoming()');
                     route(message, message.routingKey);
              });

              var serverEndpoint = function(message, respond) {
                    message.count++;
                    message.audit.push('serverEndpoint()');
                    respond(message);
              }

              testHandler.register(serverEndpoint, msg.routingKey);

                var downstream = bichannel.create("downstream");
                downstream.leftHandler(testHandler);

                downstream.rightConnection().listen(function(message) {
                        console.log('***** upstream message returned:');
                        console.dir(message);
                        assert.equal(message.count, 3);
                        assert.equal(message.audit[0], 'testHandler.incoming()');
                        assert.equal(message.audit[1], 'serverEndpoint()');
                        assert.equal(message.audit[2], 'testHandler.outgoing()');
                        done();
                });

                downstream.rightSend(msg);
    });


    it("single channel handler routes outbound message to callback and returns response inbound", function (done) {

            var msg = {count: 0, audit: [], routingKey: 'server'};



               var testHandler = handler.create('test');
              // OUTGOING/DOWNSTREAM protocol logic
               testHandler.outgoing(function(message, forward, back, route) {
                    message.count++;
                    message.audit.push('testHandler.outgoing()');
                    route(message, message.routingKey);

               });

             // INCOMING/UPSTREAM eprotocol logic
              testHandler.incoming(function(message, forward, back, route) {
                     message.count++;
                     message.audit.push('testHandler.incoming()');
                     forward(message);
              });

              var serverEndpoint = function(message, respond) {
                    message.count++;
                    message.audit.push('serverEndpoint()');
                    respond(message);
              }

              testHandler.register(serverEndpoint, msg.routingKey);

                var upstream = bichannel.create("upstream");
                upstream.rightHandler(testHandler);

                upstream.leftConnection().listen(function(message) {
                        console.log('***** upstream message returned:');
                        console.dir(message);
                        assert.equal(message.count, 3);
                        assert.equal(message.audit[0], 'testHandler.outgoing()');
                        assert.equal(message.audit[1], 'serverEndpoint()');
                        assert.equal(message.audit[2], 'testHandler.incoming()');
                        done();
                });

                upstream.leftSend(msg);
    });


});
