var bichannel = require('../../../muon/infrastructure/channel.js');
var client = require('../../../muon/transport/amqp/client.js');
var server = require('../../../muon/transport/amqp/server.js');
var assert = require('assert');
var expect = require('expect.js');
var uuid = require('node-uuid');
var messages = require('../../../muon/domain/messages.js');
var AmqpDiscovery = require("../../../muon/discovery/amqp/discovery");
var amqp = require('../../../muon/transport/amqp/amqp-api.js');
var AmqpDiscovery = require('../../../muon/discovery/amqp/discovery.js');

var url = process.env.MUON_URL || "amqp://muon:microservices@localhost";
var amqpApi;
var discovery;


describe("muon client/server transport test: ", function () {

    var discovery = new AmqpDiscovery(url);

    this.timeout(15000);

    beforeEach(function () {

    });

    afterEach(function () {

    });

    before(function (done) {
        discovery = new AmqpDiscovery(url);
        amqp.connect(url).then(function (api) {
            logger.info('****************************** AMQP CONNECTED IN TEST **********************************');
            amqpApi = api;
            done();
        });
    });

    after(function () {
        //shutdown nicely
    });

    it("client server negotiate handshake and exchange rpc message", function (done) {

        var serverName = 'server17';
        var clientName = 'client17';

        var serverChannel = bichannel.create("server-stacks");
        var mockServerStacks = {
            openChannel: function () {
                return serverChannel.rightConnection();
            }
        };


        discovery.advertiseLocalService({
            identifier: serverName,
            tags: ["node", "test", serverName],
            codecs: ["application/json"],
            connectionUrls: [url]
        });

        serverChannel.leftConnection().listen(function (event) {
            logger.warn('********** client_server-test.js serverChannel.leftConnection().listen() event.id=' + event.id);
            var payload = messages.decode(event.payload);
            assert.equal(payload.body, 'PING');

            logger.warn('********** client_server-test.js serverChannel.leftConnection().listen() reply with PONG');
            var rpcResponseMsg = {
                url: 'rpc://client1/reply',
                body: "PONG",
                content_type: 'text/plain'
            }
            var reply = messages.muonMessage(rpcResponseMsg, clientName, 'client1', 'rpc', "request.made");
            messages.validate(reply);
            serverChannel.leftConnection().send(reply);
        });


        server.connect(serverName, amqpApi, mockServerStacks, discovery);
        // now create a muon client socket to connect to server1:
        console.log('creating muon client..');
        var muonClientChannel = client.connect(serverName, "rpc", amqpApi, discovery);
        muonClientChannel.listen(function (event) {
            console.log('********** client_server-test.js muonClientChannel.listen() event received: ');
            //console.dir(event);
            var responseData = messages.decode(event.payload, 'application/json');
            console.dir(responseData);
            assert.equal(responseData.body, 'PONG');
            done();
        });
        console.log('sending muon event via client..');
        var rpcMsg = {
            url: 'rpc://client17/ping',
            body: "PING",
            content_type: 'text/plain'
        }
        var event = messages.muonMessage(rpcMsg, clientName, 'server17', 'rpc', "request.made");
        muonClientChannel.send(event);

    });

    it("client server negotiate handshake and exchange string message", function (done) {

        var serverName = 'server2';
        var clientName = 'client2';


        discovery.advertiseLocalService({
            identifier: serverName,
            tags: ["node", "test", serverName],
            codecs: ["application/json"],
            connectionUrls: [url]
        });

        var serverChannel = bichannel.create("server-stacks");
        var mockServerStacks = {
            openChannel: function () {
                return serverChannel.rightConnection();
            }
        }

        serverChannel.leftConnection().listen(function (event) {
            logger.warn('********** client_server-test.js serverChannel.leftConnection().listen() event.id=' + event.id);
            var payload = messages.decode(event.payload);
            assert.equal(payload, 'PING');

            logger.warn('********** client_server-test.js serverChannel.leftConnection().listen() reply with PONG');

            var reply = messages.muonMessage('PONG', clientName, 'client1', 'rpc', "request.made");
            messages.validate(reply);
            serverChannel.leftConnection().send(reply);
        });

        server.connect(serverName, amqpApi, mockServerStacks, discovery);
        // now create a muon client socket to connect to server1:
        console.log('creating muon client..');
        var muonClientChannel = client.connect(serverName, "rpc", amqpApi, discovery);
        muonClientChannel.listen(function (event) {
            console.log('********** client_server-test.js muonClientChannel.listen() event received!');
            var responseData = messages.decode(event.payload);
            assert.equal(responseData, 'PONG');
            done();
        });
        console.log('sending muon event via client..');

        var event = messages.muonMessage("PING", clientName, 'server1', 'rpc', "request.made");
        muonClientChannel.send(event);

    });

    it("when discovery cache populated, will rapidly connect and exchange messages", function (done) {

        var serverName = 'server2';
        var clientName = 'client2';

        discovery.advertiseLocalService({
            identifier: serverName,
            tags: ["node", "test", serverName],
            codecs: ["application/json"],
            connectionUrls: [url]
        });

        var serverChannel = bichannel.create("server-stacks");
        var mockServerStacks = {
            openChannel: function () {
                return serverChannel.rightConnection();
            }
        }

        serverChannel.leftConnection().listen(function (event) {
            logger.warn('********** client_server-test.js serverChannel.leftConnection().listen() event.id=' + event.id);
            var payload = messages.decode(event.payload);
            assert.equal(payload, 'PING');

            logger.warn('********** client_server-test.js serverChannel.leftConnection().listen() reply with PONG');

            var reply = messages.muonMessage('PONG', clientName, 'client1', 'rpc', "request.made");
            messages.validate(reply);
            serverChannel.leftConnection().send(reply);
        });

        server.connect(serverName, amqpApi, mockServerStacks, discovery);
        // now create a muon client socket to connect to server1:
        setTimeout(function() {
            var then = new Date().getTime()
            console.log('creating muon client..');

            var muonClientChannel = client.connect(serverName, "rpc", amqpApi, discovery);
            muonClientChannel.listen(function (event) {
                console.log('********** client_server-test.js muonClientChannel.listen() event received!');
                var now = new Date().getTime()
                var handshakeTime = now - then
                assert(handshakeTime < 500, "Handshake took too long, " + handshakeTime)
                console.log("Handshake is quick - " + handshakeTime)
                done();
            });
            console.log('sending muon event via client..');

            var event = messages.muonMessage("PING", clientName, 'server1', 'rpc', "request.made");
            muonClientChannel.send(event);
        }, 4000);
    });
});
