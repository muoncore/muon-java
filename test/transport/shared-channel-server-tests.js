
var assert = require('assert');
var expect = require('expect.js');
var sinon = require("sinon")
var transportclient = require("../../muon/transport/transport-client")
var bichannel = require("../../muon/infrastructure/channel")
var messages = require("../../muon/domain/messages")
require('sexylog');

describe("shared channel server:", function () {

    this.timeout(8000);

    it("when openChannel, return a new shared channel connection", function () {
        var serverStacks = {} 

        var sharedChannel = require("../../muon/transport/shared-channel-server").openSharedServerChannel(serverStacks)

        assert.notEqual(sharedChannel, null)
    })
    
    it("on the connection, when a message is received, calls openChannel on serverStacks and routes message", function (done) {
        var serverStacksApi = { openChannel: function (protocolName) {} };
        var serverStacks = sinon.mock(serverStacksApi);
        var channel = bichannel.create("server-stacks-channel");

        serverStacks.expects("openChannel").once().withArgs("fake-proto").returns(channel.leftConnection());

        channel.rightConnection().listen(function(msg) {

            assert.equal(msg.protocol, "fake-proto")
            assert.equal(msg.step, "fake-step")
            serverStacks.verify()
            done()
        })

        var sharedChannel = require("../../muon/transport/shared-channel-server").openSharedServerChannel(serverStacksApi)

        sharedChannel.send(messages.muonMessage({
            channelId: "1234",
            message: messages.muonMessage({}, "sourceService", "targetService", "fake-proto", "fake-step")
        }, "sourceService", "targetService", "shared-channel", "message"))

    })

    it("on the connection, when 2 messages received for same channelId, will look for a channelId X. If found, will routes messages", function (done) {
        var serverStacksApi = { openChannel: function (protocolName) {} };
        var serverStacks = sinon.mock(serverStacksApi);
        var channel = bichannel.create("server-stacks-channel");

        serverStacks.expects("openChannel").once().withArgs("fake-proto").returns(channel.leftConnection());

        channel.rightConnection().listen(function(msg) {

        })

        var sharedChannel = require("../../muon/transport/shared-channel-server").openSharedServerChannel(serverStacksApi)

        sharedChannel.send(messages.muonMessage({
            channelId: "1234",
            message: messages.muonMessage({}, "sourceService", "targetService", "fake-proto", "fake-step")
        }, "sourceService", "targetService", "shared-channel", "message"))

        sharedChannel.send(messages.muonMessage({
            channelId: "1234",
            message: messages.muonMessage({}, "sourceService", "targetService", "fake-proto", "fake-step")
        }, "sourceService", "targetService", "shared-channel", "message"))

        setTimeout(function() {
            serverStacks.verify()
            done()
        }, 1000)
    })

    it("when message is sent from server stack channel, shared channel message generated with appropriate channelId", function (done) {
        var serverStacksApi = { openChannel: function (protocolName) {} };
        var channel = bichannel.create("server-stacks-channel");
        var serverStacks = sinon.stub(serverStacksApi, "openChannel").returns(channel.leftConnection());;

        var sharedChannel = require("../../muon/transport/shared-channel-server").openSharedServerChannel(serverStacksApi)

        sharedChannel.listen(function(msg) {
            assert(msg)
            //protocol
            //payload
            
            done()
        })

        sharedChannel.send(messages.muonMessage({
            channelId: "1234",
            message: messages.muonMessage({}, "sourceService", "targetService", "fake-proto", "fake-step")
        }, "sourceService", "targetService", "shared-channel", "message"))

        setTimeout(function () {
            channel.rightConnection().send("HJELLO")
        }, 200)
    })
});
