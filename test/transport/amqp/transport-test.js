var amqpTransport = require('../../../muon/transport/amqp/transport.js');
var assert = require('assert');
var expect = require('expect.js');
var messages = require('../../../muon/domain/messages.js');
var bichannel = require('../../../muon/infrastructure/channel.js');
var builder = require("../../../muon/infrastructure/builder");
var AmqpDiscovery = require("../../../muon/discovery/amqp/discovery");


describe("muon transport test: ", function () {

    this.timeout(10000);

    after(function () {
        //bi-channel.closeAll();
    });

    it("client server negotiate handshake", function (done) {
        var server = 'transport-test-server';
        var url = "amqp://muon:microservices@localhost";

        var event = messages.muonMessage("PING", 'testclient', 'server', 'test', "test.request");

        var fakeServerStackChannel = bichannel.create("fake-serverstacks");
        var fakeServerStacks = {
            openChannel: function (msg) {
                console.log("SERVER STACKS OPEN!")
                return fakeServerStackChannel.rightConnection();
            }
        }
        var discovery = new AmqpDiscovery(url);
        discovery.advertiseLocalService({
            identifier: server,
            tags: [],
            codecs: ["application/json"],
            //TODO, more intelligent geenration of connection urls by asking the transports
            connectionUrls: [url]
        })
        var transportPromise = amqpTransport.create(server, url, fakeServerStacks, discovery);

        transportPromise.then(function (muonTransport) {
            console.log('********************* transportPromise.then(): getting channel handle');
            var transportChannel = muonTransport.openChannel(server, 'rpc');
            console.log("STARTING TO LISTENT")
            console.log('********************* sending event on transport channel: ');
            transportChannel.send(event);
            console.log('test: wait for response from remote service ' + server);
            try {
                fakeServerStackChannel.leftConnection().listen(function (event) {
                    console.log('********** transport.js transportChannel.listen() event received ' + JSON.stringify(event));
                    var payload = messages.decode(event.payload);
                    console.log('test: typeof event.payload: ' + (typeof event.payload));
                    assert.equal(payload, 'PING');
                    done();
                }, function (err) {
                    logger.error(err.stack);
                });
            } catch (e) {
                console.log("AN ERROR WAS THROWN")
                console.dir(e)
            }

        });


    });


});
