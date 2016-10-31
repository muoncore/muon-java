"use strict";

var Handler = require('../../muon/infrastructure/handler-class.js');
var assert = require('assert');
var expect = require('expect.js');
require('sexylog');
var bichannel = require('../../muon/infrastructure/channel.js');


describe("Handler class test:", function () {


      this.timeout(4000);


      after(function() {

      });


    it("handler between two channels", function (done) {

            var msg = {count: 0, audit: []};

              var upstream = bichannel.create("upstream");
              var downstream = bichannel.create("downstream");

               class TestHandler extends Handler {

                 outgoingFunction(message, forward, back, route) {
                   message.count++;
                   message.audit.push('testHandler.outgoing()');
                   forward(message);
                 }

                 incomingFunction(message, forward, back, route) {
                   message.count++;
                   message.audit.push('testHandler.incoming()');
                   forward(message);
                 }
               }

               var testHandler = new TestHandler('test');

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

    it("outgoing handler between two channels sends back", function (done) {

            var msg = {count: 0, audit: []};

              var upstream = bichannel.create("upstream");
              var downstream = bichannel.create("downstream");

               class TestHandler extends Handler {

                 outgoingFunction(message, forward, back, route) {
                   message.count++;
                   message.audit.push('testHandler.outgoing()');
                   back(message);
                 }

                 incomingFunction(message, forward, back, route) {
                   message.count++;
                   message.audit.push('testHandler.incoming()');
                   forward(message);
                 }
               }

               var testHandler = new TestHandler('test');

                upstream.rightHandler(testHandler);
                downstream.leftHandler(testHandler);

                upstream.leftSend(msg);

                upstream.leftConnection().listen(function(message) {
                        console.log('***** upstream message returned:');
                        console.dir(message);
                        assert.equal(message.count, 1);
                        assert.equal(message.audit[0], 'testHandler.outgoing()');
                        done();
                });


                //downstream.rightConnection().listen(function(message){
                //    downstream.rightSend(message);
                //});

    });


    it("incoming handler between two channels sends back", function (done) {

            var msg = {count: 0, audit: []};

              var upstream = bichannel.create("upstream");
              var downstream = bichannel.create("downstream");

               class TestHandler extends Handler {

                 outgoingFunction(message, forward, back, route) {
                   message.count++;
                   message.audit.push('testHandler.outgoing()');
                   forward(message);
                 }

                 incomingFunction(message, forward, back, route) {
                   message.count++;
                   message.audit.push('testHandler.incoming()');
                   back(message);
                 }
               }

               var testHandler = new TestHandler('test');

                upstream.rightHandler(testHandler);
                downstream.leftHandler(testHandler);

                upstream.leftSend(msg);

                var downstreamCalls = 0;
                downstream.rightConnection().listen(function(message) {
                    downstreamCalls++;
                    if (downstreamCalls == 1) {
                        downstream.rightSend(message);
                    } else {
                      console.log('***** downstream message returned:');
                      console.dir(message);
                      assert.equal(message.count, 2);
                      assert.equal(message.audit[0], 'testHandler.outgoing()');
                      assert.equal(message.audit[1], 'testHandler.incoming()');
                      done();
                    }

                });

    });

    it("single channel handler routes inbound message to callback and return response outbound", function (done) {

            var msg = {count: 0, audit: [], routingKey: 'server'};

              class TestHandler extends Handler {

                outgoingFunction(message, forward, back, route) {
                  message.count++;
                  message.audit.push('testHandler.outgoing()');
                  forward(message);
                }

                incomingFunction(message, forward, back, route) {
                  message.count++;
                  message.audit.push('testHandler.incoming()');
                  route(message, message.routingKey);
                }
              }

              var testHandler = new TestHandler('test');

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


              class TestHandler extends Handler {

                outgoingFunction(message, forward, back, route) {
                  message.count++;
                  message.audit.push('testHandler.outgoing()');
                  route(message, message.routingKey);
                }

                incomingFunction(message, forward, back, route) {
                  message.count++;
                  message.audit.push('testHandler.incoming()');
                  forward(message);
                }
              }

              var testHandler = new TestHandler('test');

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
