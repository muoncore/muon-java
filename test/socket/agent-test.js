"use strict";

var Agent = require('../../muon/socket/keep-alive-agent.js');
var assert = require('assert');
var expect = require('expect.js');
require('sexylog');
var bichannel = require('../../muon/infrastructure/channel.js');


describe("Agent class test:", function () {

    this.timeout(11000);

    it("agent acts as handler between two channels", function (done) {

        var msg = {text: 'agent smith'};

        var upstream = bichannel.create("upstream");
        var downstream = bichannel.create("downstream");

        var agent = new Agent(upstream.rightConnection(), downstream.leftConnection(), 'rpc', 100);

        upstream.leftConnection().listen(function (message) {
            console.log('***** upstream message recevied:' + JSON.stringify(message));
            console.dir(message);
            if (!message.channel_op) {
                assert.equal(msg.text, message.text);
                done();
            }
            //expect(message.channel_op).to.be(undefined);
            //done();
        });

        downstream.rightConnection().listen(function (message) {
            console.log('**************** downstream received: ' + JSON.stringify(message));
            downstream.rightSend(message);
        });

        // send message async after pings keep alive
        setTimeout(function () {
            upstream.leftSend(msg);
        }, 1000);


    });

    it("agent sends keep alive pings", function (done) {

        var upstream = bichannel.create("upstream");
        var downstream = bichannel.create("downstream");
        var protocol = 'rpc';
        var agent = new Agent(upstream.rightConnection(), downstream.leftConnection(), protocol, 10);

        var keepAlivePingCount = 0;
        downstream.rightConnection().listen(function (message) {
            keepAlivePingCount++;

            //assert.equal(protocol, message.protocol);
            if (keepAlivePingCount == 3) done();
        });

    });


    it("agent delays keep alive pings if event received", function (done) {
        var doneOnce = asyncAssert(done);
        var upstream = bichannel.create("upstream");
        var downstream = bichannel.create("downstream");
        var protocol = 'rpc';
        var agent = new Agent(upstream.rightConnection(), downstream.leftConnection(), protocol, 50);


        //  SEND 10 message (once every 10ms, then wait for keep alive)  /
        var messagesSent = 0;
        var backgroundSender = setInterval(function () {
            upstream.leftConnection().send({text: 'this is a test event!'});
            messagesSent++;
            if (messagesSent == 10) {
                clearInterval(backgroundSender);
            }
        }, 10);

        var pingReceived = 0;
        var messagesReceived = 0;
        downstream.rightConnection().listen(function (message) {
            //console.log('******************** message: ', message);
            messagesReceived++;
            if (message.step == 'keep-alive') pingReceived++;
            if (message.text) assert.equal('this is a test event!', message.text);
            doneOnce(messagesReceived > 10 && pingReceived > 0 && pingReceived < 5);
        });

    });

    it("two agents keep each other alive", function (done) {
        var doneOnce = asyncAssert(done);
        var protocol = 'rpc';
        var clientUpstream = bichannel.create("client-upstream");
        var clientDownstream = bichannel.create("client-downstream");
        var serverUpstream = bichannel.create("server-upstream");
        var serverDownstream = bichannel.create("server-downstream");

        function MockTransport(clientConnection, serverConnection) {
            var clientKeepAliveMessages = 0;
            var serverKeepAliveMessages = 0;
            clientConnection.listen(function (msg) {
                logger.info('mock trasnport client listener:' + JSON.stringify(msg));
                clientKeepAliveMessages++;
                serverConnection.send(msg);
            });
            serverConnection.listen(function (msg) {
                logger.info('mock trasnport server listener:' + JSON.stringify(msg));
                serverKeepAliveMessages++;
                clientConnection.send(msg);

                doneOnce(serverKeepAliveMessages > 10 && clientKeepAliveMessages > 10);
            });
        };

        var protocol = 'rpc';
        var clintAgent = new Agent(clientUpstream.rightConnection(), clientDownstream.leftConnection(), protocol, 10);
        var serverAgent = new Agent(serverUpstream.rightConnection(), serverDownstream.leftConnection(), protocol, 10);
        var mockTransport = new MockTransport(clientDownstream.rightConnection(), serverDownstream.rightConnection());

    });


    it("pings do not leak upstream of agents", function (done) {
        var testMsg = {text: 'this is not a ping'};
        var doneOnce = asyncAssert(done);
        var protocol = 'rpc';
        var clientUpstream = bichannel.create("client-upstream");
        var clientDownstream = bichannel.create("client-downstream");
        var serverUpstream = bichannel.create("server-upstream");
        var serverDownstream = bichannel.create("server-downstream");

        var clientKeepAliveMessages = 0;
        var serverKeepAliveMessages = 0;

        function MockTransport(clientConnection, serverConnection) {

            clientConnection.listen(function (msg) {
                //console.log('mock trasnport client fwd msg:' + JSON.stringify(msg));
                clientKeepAliveMessages++;
                serverConnection.send(msg);
            });

            serverConnection.listen(function (msg) {
                //console.log('mock transport server fwd msg:' + JSON.stringify(msg));
                serverKeepAliveMessages++;
                clientConnection.send(msg);
                //doneOnce(serverKeepAliveMessages > 10 && clientKeepAliveMessages > 10);
            });
        };

        var protocol = 'rpc';
        var clintAgent = new Agent(clientUpstream.rightConnection(), clientDownstream.leftConnection(), protocol, 10);
        var serverAgent = new Agent(serverUpstream.rightConnection(), serverDownstream.leftConnection(), protocol, 10);
        var mockTransport = new MockTransport(clientDownstream.rightConnection(), serverDownstream.rightConnection());


        var msgReceived = false;
        serverUpstream.leftConnection().listen(function (msg) {
            console.log('* server ******************************************** msg: ' + JSON.stringify(msg));
            console.log('* server ******************************************** serverKeepAliveMessages=' + serverKeepAliveMessages);
            console.log('* server ******************************************** clientKeepAliveMessages=' + clientKeepAliveMessages);
            console.log('* server ******************************************** msgReceived=' + msgReceived);
            expect(msg.text).to.be(testMsg.text);
            setTimeout(function () {
                serverUpstream.leftSend(msg);
            }, 100);

        });
        console.log('********************************************** sending message');
        setTimeout(function () {
            clientUpstream.leftConnection().send(testMsg);
        }, 100);


        clientUpstream.leftConnection().listen(function (msg) {
            console.log('* client ******************************************** msg: ' + JSON.stringify(msg));
            expect(msg.text).to.be(testMsg.text);
            done();
        });
    });

    it("agent with no service keep alive response shuts down channels", function (done) {
        var doneOnce = asyncAssert(done);

        var upstream = bichannel.create("upstream");
        var downstream = bichannel.create("downstream");
        var protocol = 'rpc';
        var agent = new Agent(upstream.rightConnection(),downstream.leftConnection(), protocol, 500);

        var keepAlivePingCount = 0;
        var shutdownMessage = 0;
        downstream.rightConnection().listen(function (message) {
            logger.trace(JSON.stringify(message));
            if (message.step == 'keep-alive') keepAlivePingCount++;
            if (message.channel_op == 'closed') shutdownMessage++;
            logger.trace('keep-alive=' + keepAlivePingCount + ' closed=' + shutdownMessage);
            doneOnce(keepAlivePingCount >= 3 && shutdownMessage == 1);
        });

        // we've got to make sure the agent has at least one keep-alive ping first as it will not shutdown unless
        // it's already made a connection

        // send message async after pings keep alive
        setTimeout(function () {
            downstream.rightSend({step: 'keep-alive'});
        }, 500);
    });

});


function asyncAssert(done) {
    var calledDone = false;

    function callDoneOnce() {
        if (calledDone) return;
        calledDone = true;
        done();
    }

    // returns a function that will only call done() once which can happen in async tests such as this
    return function (bool) {
        //console.log('doneonce(bool=' + bool + ')');
        if (bool) {
            callDoneOnce();
        }
    }
}
