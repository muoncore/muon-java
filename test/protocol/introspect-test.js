var bichannel = require('../../muon/infrastructure/channel.js');
var introspection = require('../../muon/protocol/introspection.js');
var assert = require('assert');
var expect = require('expect.js');
var messages = require('../../muon/domain/messages.js');


describe("test introspection client protocol:", function () {

    var clientName = 'client';
     var serverName = 'server';

    it("can request introspection", function (done) {
         var introApi = introspection.getApi('client');

         var clientApiChannel = bichannel.create("clientapi");
         var clientTransportChannel = bichannel.create("client-transport");

         var introClientProtocol = introApi.protocolHandler().client(serverName);
         clientApiChannel.rightHandler(introClientProtocol);
         clientTransportChannel.leftHandler(introClientProtocol);

        var rpcClientRequest = {
            url: 'introspection://server/',
        }
        var muonMessage = messages.muonMessage(rpcClientRequest, clientName, 'client',  'introspection', "introspection.request");
        clientApiChannel.leftSend(muonMessage);


        clientTransportChannel.rightConnection().listen(function(msg) {
                          console.log("introspect-test.js response: ");
                          console.dir(msg);
                          assert.equal(msg.protocol, 'introspect');
                          assert.equal(msg.step, 'introspectionRequested', 'message protocol step expected to be "introspectionRequested"');
                          done();
        });

    });

});
