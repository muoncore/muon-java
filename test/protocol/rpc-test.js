var bichannel = require('../../muon/infrastructure/channel.js');
var rpc = require('../../muon/protocol/rpc.js');
var assert = require('assert');
var expect = require('expect.js');
var messages = require('../../muon/domain/messages.js');
//var eep = require('eep');


describe("test rpc protocol:", function () {

    this.timeout(8000);

    var requestText = 'Hello, world!';
    var responseText = 'Goodbye, world!';
    var clientName = 'client';
    var serverName = 'server';
    var requestUrl = 'rpc://server/endpoint';

    it("rpc server api inbound/outbound handler happy path", function (done) {
        var rpcApi = rpc.getApi('server');

        rpcApi.handle('/endpoint', function (request, respond) {
            console.log('rpcApi.handle() called');
            logger.info("request is " + JSON.stringify(request))
            assert.equal(requestText, request.body);
            respond(responseText);
        });

        var serverApiChannel = bichannel.create("serverapi");
        var serverTransportChannel = bichannel.create("server-transport");

        var rpcServerProtocol = rpcApi.protocolHandler().server(serverApiChannel.leftConnection());
        serverApiChannel.rightHandler(rpcServerProtocol);
        serverTransportChannel.leftHandler(rpcServerProtocol);

        var rpcClientRequest = {
            body: messages.encode(requestText),
            url: requestUrl,
            content_type: 'text/plain',

        }
        var muonMessage = messages.muonMessage(rpcClientRequest, clientName, 'server', 'rpc', "response.sent");
        serverTransportChannel.rightSend(muonMessage);

        var closeMsg = 0;
        serverTransportChannel.rightConnection().listen(function (msg) {
            console.log('***** test message received: ' + JSON.stringify(msg));
            //console.dir(msg);
            if (msg.channel_op == 'closed') {
                closeMsg++;
                if (closeMsg >= 1) done(); // here we ensure a shutdown message is sent at elast once before passing test
            } else {
                var response = messages.decode(msg.payload, msg.content_type);
                var responseBody = messages.decode(response.body, msg.content_type)
                assert.equal(responseText, responseBody);
            }
        });

    });


    it("rpc client-server happy path", function (done) {
        var rpcApi = rpc.getApi('server');

        rpcApi.handle('/endpoint', function (request, respond) {
            console.log('rpcApi.handle() called');
            logger.info("request is " + JSON.stringify(request))
            assert.equal(requestText, request.body);
            respond(responseText);
        });

        var serverApiChannel = bichannel.create("serverapi");
        var serverTransportChannel = bichannel.create("server-transport");

        var rpcServerProtocol = rpcApi.protocolHandler().server();
        serverApiChannel.rightHandler(rpcServerProtocol);
        serverTransportChannel.leftHandler(rpcServerProtocol);


        var rpcClientProtocol = rpcApi.protocolHandler().client(requestUrl);
        var clientApiChannel = bichannel.create("cleintapi");
        var clientTransportChannel = bichannel.create("client-transport");

        clientApiChannel.rightHandler(rpcClientProtocol);
        clientTransportChannel.leftHandler(rpcClientProtocol);


        clientTransportChannel.rightConnection().listen(function (msg) {
            serverTransportChannel.rightSend(msg);
        });


        serverTransportChannel.rightConnection().listen(function (msg) {
            if (msg.channel_op == 'closed') return;
            var response = messages.decode(msg.payload, msg.content_type);
            var responseBody = messages.decode(response.body, response.content_type)
            assert.equal(responseText, responseBody);
            clientTransportChannel.rightSend(msg);
        });


        clientApiChannel.leftConnection().listen(function (msg) {
            console.log('****************************** client response:');
            console.dir(msg);
            done();
        });


        clientApiChannel.leftSend(requestText);


    });


    it("rpc serverside protocol with two endpoint handlers", function (done) {
        var rpcApi = rpc.getApi('server');

        var calls = {
            endpoint1: 0,
            endpoint2: 0,
        };

        var callDone = function () {
            logger.rainbow('callDone() returned calls: ' + JSON.stringify(calls));
            if (calls.endpoint1 == 1 && calls.endpoint2 == 1) {
                done();
            }
        }

        var rpcRequest1 = {
            body: messages.encode('blah1 text'),
            url: 'rpc://server/endpoint1',
            content_type: 'text/plain'
        }

        var rpcRequest2 = {
            body: messages.encode('blah2 text'),
            url: 'rpc://server/endpoint2',
            content_type: 'text/plain'
        }

        rpcApi.handle('/endpoint1', function (request, response) {
            console.log('rpcApi.handle(/endpoint1) called');
            assert.equal('blah1 text', request.body);
            response('reply1');
        });

        rpcApi.handle('/endpoint2', function (request, response) {
            console.log('rpcApi.handle(/endpoint2) called');
            assert.equal('blah2 text', request.body);
            response('reply2');
        });

        var serverTransportChannel1 = bichannel.create("server1-transport");
        var rpcServerProtocol = rpcApi.protocolHandler().server();
        serverTransportChannel1.leftHandler(rpcServerProtocol);
        var muonMessage1 = messages.muonMessage(rpcRequest1, clientName, 'server', 'rpc', "response.sent");
        serverTransportChannel1.rightSend(muonMessage1);

        serverTransportChannel1.rightConnection().listen(function (msg) {
            console.log('****** serverTransportChannel1.rightConnection().listen() ');
            if (msg.channel_op == 'closed') return;
            var rpcResponse = messages.decode(msg.payload, msg.content_type);
            var responseBody = messages.decode(rpcResponse.body, rpcResponse.content_type)
            console.dir(rpcResponse);
            assert.equal(responseBody, 'reply1');
            calls.endpoint1++;
            callDone();
        });

        var serverTransportChannel2 = bichannel.create("server2-transport");
        var rpcServerProtocol = rpcApi.protocolHandler().server();
        serverTransportChannel2.leftHandler(rpcServerProtocol);
        var muonMessage2 = messages.muonMessage(rpcRequest2, clientName, 'server', 'rpc', "response.sent");
        serverTransportChannel2.rightSend(muonMessage2);

        serverTransportChannel2.rightConnection().listen(function (msg) {
            console.log('****** serverTransportChannel2.rightConnection().listen() ');
            if (msg.channel_op == 'closed') return;
            var rpcResponse = messages.decode(msg.payload, msg.content_type);
            var responseBody = messages.decode(rpcResponse.body, rpcResponse.content_type)
            console.dir(rpcResponse);
            assert.equal(responseBody, 'reply2');
            calls.endpoint2++;
            callDone();
        });

    });


});
