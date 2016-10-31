
var assert = require('assert');
var expect = require('expect.js');
var sinon = require("sinon")
var transportclient = require("../../muon/transport/transport-client")
var bichannel = require("../../muon/infrastructure/channel")
var messages = require("../../muon/domain/messages")
require('sexylog');

describe("transport-client:", function () {

    this.timeout(8000);
    
    var infra = {
        config: {
            serviceName: "local-service",
            sharedChannelTimeout:1000,
            sharedChannelCheck: 100
        }
    }

    /**
     * on shutdown virtual channel, propogate the shutdown message to the server
     *
     * on shutdown recieved from transport on the transport channel, send shutdown to all virtual channels.
     *
     * on openChannel, establish a transport channel, return a virtual channel
     * on openChannel to same place (ie, done twice), only return a virtual channel
     *
     */

    it("on openChannel, establish a transport channel, return a virtual channel", function () {
        var transportApi = { openChannel: function (remoteService, protocolName) {} };
        var transport = sinon.mock(transportApi);

        transport.expects("openChannel").once().returns(bichannel.create("transportchannel").leftConnection());

        var transclient = transportclient.create(transportApi, infra)

        var returnedChannel = transclient.openChannel("simples", "rpc")

        assert(returnedChannel)
        transport.verify();
    })

    it("on openChannel to same place (ie, done twice), only return a virtual channel", function () {
        var transportApi = { openChannel: function (remoteService, protocolName) {} };
        var transport = sinon.mock(transportApi);

        transport.expects("openChannel").once().returns(bichannel.create("transportchannel").leftConnection());

        var transclient = transportclient.create(transportApi, infra)

        var returnedChannel = transclient.openChannel("simples", "rpc")
        var returnedChannel2 = transclient.openChannel("simples", "streaming")
        var returnedChannel3 = transclient.openChannel("simples", "fake")
        var returnedChannel4 = transclient.openChannel("simples", "other")

        assert.notEqual(returnedChannel, returnedChannel2)
        transport.verify();
    })

    it("on shutdown virtual channel, propogate the shutdown message to the server", function (done) {
        var transportApi = { openChannel: function (remoteService, protocolName) {} };
        var transport = sinon.mock(transportApi);

        var transportChannel = bichannel.create("transportchannel")
        transportChannel.rightConnection().listen(function(msg) {
            logger.info("Got message " + JSON.stringify(msg))
            var payload = messages.decode(msg.payload)
            logger.info("Got message " + JSON.stringify(payload))
            assert.equal(payload.message.channel_op, "closed")
            done()
        })

        transport.expects("openChannel").once().returns(transportChannel.leftConnection());

        var transclient = transportclient.create(transportApi, infra)

        var returnedChannel = transclient.openChannel("simples", "rpc")

        returnedChannel.close()
    })

    it("on shutdown received from transport on the transport channel, send shutdown to all virtual channels", function (done) {
        var transportApi = { openChannel: function (remoteService, protocolName) {} };
        var transport = sinon.mock(transportApi);

        var transportChannel = bichannel.create("transportchannel")

        transport.expects("openChannel").once().returns(transportChannel.leftConnection());

        var transclient = transportclient.create(transportApi, infra)

        transclient.openChannel("simples", "rpc").listen(function(message) {
            assert.equal(message.channel_op, "closed")
            done()
        })

        transportChannel.rightConnection().send(messages.shutdownMessage())
    })

    it("if no virtual channels open for X seconds, close the transport channel", function (done) {
        var transportApi = { openChannel: function (remoteService, protocolName) {} };
        var transport = sinon.mock(transportApi);

        var transportChannel = bichannel.create("transportchannel")

        transportChannel.rightConnection().listen(function(msg) {
            console.log("Transport got message")
            if (msg.channel_op == "closed") done()
        })

        transport.expects("openChannel").once().returns(transportChannel.leftConnection());

        var transclient = transportclient.create(transportApi, infra)

        var virtchannel = transclient.openChannel("simples", "rpc");
        virtchannel.send(messages.shutdownMessage())
    })

    it("open channel to one service, open channel to second at the same time", function (done) {
        var transportApi = { openChannel: function (remoteService, protocolName) {} };
        var transport = sinon.mock(transportApi);

        var transportChannel = bichannel.create("simples")
        transportChannel.rightConnection().listen(function(msg) {
            var payload = messages.decode(msg.payload)
            console.log("Transport 1 got message " + JSON.stringify(payload))
            if (payload.message) {
                if (payload.message.step == "step2") {
                    done()
                }
            }
        })
        var transportChannel2 = bichannel.create("simples2")
        transportChannel2.rightConnection().listen(function(msg) {
            var payload = messages.decode(msg.payload)
            console.log("Transport 2 got message " + JSON.stringify(payload))
        })

        transport.expects("openChannel").twice()
            .onFirstCall().returns(transportChannel.leftConnection())
            .onSecondCall().returns(transportChannel2.leftConnection());

        var transclient = transportclient.create(transportApi, infra)

        var virtchannel = transclient.openChannel("simples", "rpc");
        virtchannel.send(messages.muonMessage({}, "sourceService", "simples", "protocol", "step"))
        setTimeout(function(){
            var virtchannel2 = transclient.openChannel("simples2", "rpc");
            virtchannel2.send(messages.muonMessage({}, "sourceService", "simples2", "protocol", "step1"))
            virtchannel2.send(messages.shutdownMessage())

            setTimeout(function(){
                virtchannel.send(messages.muonMessage({}, "sourceService", "simples2", "protocol", "step2"))
            }, 500);
        }, 500)
    })
});
