var bichannel = require('../../../muon/infrastructure/channel');
var api = require('../../../muon/protocol/streaming/client-simple-api');
var assert = require('assert');
var expect = require('expect.js');

describe("streaming client simple api", function () {

    it("when onSubscribe is called, subscription.request called", function (done) {
        
        var subscriber = api.subscriber(function(data) {

        }, function(error) {

        })

        subscriber.onSubscribe({
            cancel: function() {

            },
            request: function(amount) {
                done()
            }
        })
    });

    it("when onNext is called, dataCallback called", function (done) {
        var dat = 1234

        var subscriber = api.subscriber(function(data) {
            assert.equal(dat, data)
            done()
        }, function(error) {

        })

        subscriber.onNext(dat)
    });

    it("when onError is called, errorCallback called", function (done) {
        var dat = 1234

        var subscriber = api.subscriber(function(data) {

        }, function(error) {
            assert.equal(dat, error)
            done()
        })

        subscriber.onError(dat)
    });

    it("when onComplete is called, invoke completeCallback", function (done) {
        var dat = 1234

        var subscriber = api.subscriber(function(data) {

        }, function(error) {

        }, function() {
            done()
        })

        subscriber.onSubscribe({
            request: function(){},
            cancel: function() {
            }
        })
        subscriber.onComplete()
    });

    it("when cancel is invoked on control, subscription.cancel is called", function (done) {
        var dat = 1234

        var subscriber = api.subscriber(function(data) {

        }, function(error) {

        }, function() {

        })

        subscriber.onSubscribe({
            request: function(){},
            cancel: function() {
                done()
            }
        })
        subscriber.control.cancel()
    });

    it("when onNext is called 10 times, request(10) more data from subscription", function (done) {
        var dat = 1234

        var subscriber = api.subscriber(function(data) {
        }, function(error) {

        })

        var requestCalled = 0
        subscriber.onSubscribe({
            request: function(){
                requestCalled++
                if (requestCalled == 2) {
                    done()
                }
            }
        })
        subscriber.onNext(dat)
        subscriber.onNext(dat)
        subscriber.onNext(dat)
        subscriber.onNext(dat)
        subscriber.onNext(dat)
        subscriber.onNext(dat)
        subscriber.onNext(dat)
        subscriber.onNext(dat)
        subscriber.onNext(dat)
        subscriber.onNext(dat)
    });
});
