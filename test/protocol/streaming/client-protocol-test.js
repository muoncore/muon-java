var bichannel = require('../../../muon/infrastructure/channel');
var protocol = require('../../../muon/protocol/streaming/client-protocol');
var messages = require("../../../muon/domain/messages")
var assert = require('assert');
var expect = require('expect.js');
require("mocha-sinon")

var targetService="tombola"
var localService="local"
var targetStream="theStream"
var args={}

describe("streaming client protocol", function () {

    it("sends subscribe when the protocol starts", function (done) {
        
        var subscription

        var transchannel = bichannel.create("transportchannel")

        var sub = {
            onSubscribe: function(theSub) {
            subscription = theSub
        }};

        var proto = protocol.create(sub,
            transchannel.leftConnection(),
            targetService,
            localService,
            targetStream,
            args
        )

        transchannel.rightConnection().listen(function(msg) {
            console.log("stream subscribe");
            console.dir(msg);
            assert.equal(msg.protocol, 'reactive-stream');
            assert.equal(msg.step, 'SubscriptionRequested');
            done();
        });
        
        proto.start()
    });

    it("on ACK, calls subscriber.onSubscribe", function (done) {
        var transchannel = bichannel.create("transportchannel")

        var sub = {
            onSubscribe: function(theSub) {
            done()
        }};

        var proto = protocol.create(sub,
            transchannel.leftConnection(),
            targetService,
            localService,
            targetStream,
            args
        )

        transchannel.rightConnection().send(
            messages.muonMessage(
                {},
                "tombola",
                "simples",
                "reactive-stream",
                "SubAck"
            )
        );
    });

    it("on NACK, calls sub.error", function (done) {
        var transchannel = bichannel.create("transportchannel")

        var sub = {
            onSubscribe: function(theSub) {

            },
            onError: function(error) {
                done()
            }
        };

        var proto = protocol.create(sub,
            transchannel.leftConnection(),
            targetService,
            localService,
            targetStream,
            args
        )

        transchannel.rightConnection().send(
            messages.muonMessage(
                {},
                "tombola",
                "simples",
                "reactive-stream",
                "SubNack"
            )
        );
    });
    //
    // it("on transport error, calls subscriber onSubscribe and sub.error", function (done) {
    //    
    //     assert.fail("Not done")
    // });

    it("send CANCEL when sub calls cancel on stream control", function (done) {

        var transchannel = bichannel.create("transportchannel")

        var sub = {
            onSubscribe: function(theSub) {
                theSub.cancel()
            }
        };

        var proto = protocol.create(sub,
            transchannel.leftConnection(),
            targetService,
            localService,
            targetStream,
            args
        )

        transchannel.rightConnection().listen(function(message) {
            if(message.step == "Cancelled") {
                done()
            }
        });

        transchannel.rightConnection().send(
            messages.muonMessage(
                {},
                "tombola",
                "simples",
                "reactive-stream",
                "SubAck"
        ));

    });

    it("sends REQUEST when the subscriber requests more data", function (done) {
        var transchannel = bichannel.create("transportchannel")

        var sub = {
            onSubscribe: function(theSub) {
                theSub.request(10)
            }
        };

        var proto = protocol.create(sub,
            transchannel.leftConnection(),
            targetService,
            localService,
            targetStream,
            args
        )

        transchannel.rightConnection().listen(function(message) {
            var data = messages.decode(message.payload)
            if(message.step == "DataRequested" && data.request == 10) {
                done()
            }
        });

        transchannel.rightConnection().send(
            messages.muonMessage(
                {},
                "tombola",
                "simples",
                "reactive-stream",
                "SubAck"
            ));
    });

    it("on DATA received, calls sub onNext", function (done) {
        var transchannel = bichannel.create("transportchannel")

        var sub = {
            onSubscribe: function(theSub) {

            },
            onNext: function(error) {
                done()
            }
        };

        var proto = protocol.create(sub,
            transchannel.leftConnection(),
            targetService,
            localService,
            targetStream,
            args
        )

        transchannel.rightConnection().send(
            messages.muonMessage(
                {},
                "tombola",
                "simples",
                "reactive-stream",
                "Data"
            )
        );
    });

    it("on COMPLETE, calls sub onComplete", function (done) {
        var transchannel = bichannel.create("transportchannel")

        var sub = {
            onSubscribe: function(theSub) {

            },
            onComplete: function(error) {
                done()
            }
        };

        var proto = protocol.create(sub,
            transchannel.leftConnection(),
            targetService,
            localService,
            targetStream,
            args
        )

        transchannel.rightConnection().send(
            messages.muonMessage(
                {},
                "tombola",
                "simples",
                "reactive-stream",
                "Completed"
            )
        );
    });

    it("on ERROR, calls sub.onError", function (done) {
        var transchannel = bichannel.create("transportchannel")

        var sub = {
            onSubscribe: function(theSub) {

            },
            onError: function(error) {
                done()
            }
        };

        var proto = protocol.create(sub,
            transchannel.leftConnection(),
            targetService,
            localService,
            targetStream,
            args
        )

        transchannel.rightConnection().send(
            messages.muonMessage(
                {},
                "tombola",
                "simples",
                "reactive-stream",
                "Errored"
            )
        );
    });

    //TODO, test the subscription generated for an ACK
});
