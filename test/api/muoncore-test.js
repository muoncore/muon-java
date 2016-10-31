var muoncore = require('../../muon/api/muoncore.js');
var assert = require('assert');
var expect = require('expect.js');


describe("Muon core API test:", function () {


    this.timeout(30000);
    var serviceName = "example-service";
    var amqpurl = process.env.MUON_URL || "amqp://muon:microservices@localhost";

    var muon
    var muon2

    before(function () {

    });

    after(function () {
        //if (muon) muon.shutdown();
        //if (muon2) muon2.shutdown();
    });

    afterEach(function () {
        // if (muon) muon.shutdown()
        // if (muon2) muon2.shutdown()
    });

    it("full stack request/response for rpc message", function (done) {

        console.log("MUONURL " + amqpurl)

        muon = muoncore.create(serviceName, amqpurl);
        muon.handle('/tennis', function (event, respond) {
            logger.warn('*****  muon://service/tennis: muoncore-test.js *************************************************');
            logger.warn('rpc://service/tennis server responding to event=' + JSON.stringify(event));
            respond("pong");
        });

        muon2 = muoncore.create("example-client", amqpurl);

        var promise = muon2.request('rpc://example-service/tennis', "ping");

        promise.then(function (response) {
            logger.warn("rpc://example-client server response received! response=" + JSON.stringify(response));
            logger.warn("muon promise.then() asserting response...");
            logger.info("Response is " + JSON.stringify(response))
            assert(response, "request response is undefined");
            assert.equal(response.body, "pong", "expected 'pong' but was " + response.body)
            done();
        }, function (err) {
            logger.error("muon promise.then() error!\n" + err.stack);
            done(err);
        }).catch(function (error) {
            logger.error("muoncore-test.js promise.then() error!:\n" + error.stack);
            done(error);

        });
    });


    it("rpc returns 404 message for invalid resource", function (done) {


        console.log('***************************************************************************************************************');
        console.log('*** rpc returns 404 message for invalid resource');
        console.log('***************************************************************************************************************');

        muon = muoncore.create(serviceName, amqpurl);
        muon.handle('/tennis', function (event, respond) {
            logger.warn('*****  rpc://service/tennis: muoncore-test.js *************************************************');
            logger.warn('rpc://service/tennis server responding to event.id=' + event.id);
            respond("pong");
        });

        muon2 = muoncore.create("example-client", amqpurl);

        var promise = muon2.request('rpc://example-service/blah', "ping");

        promise.then(function (event) {
            logger.warn("rpc://example-client server response received! event.id=" + event.id);
            logger.warn("rpc://example-client server response received! event=" + JSON.stringify(event));
            logger.warn("muon promise.then() asserting response...");
            assert(event, "request event is undefined");
            assert.equal(event.status, "404", "expected '404' response message from muon://example-service/blah");
            console.log('***************************************************************************************************************');
            console.log('*** done');
            console.log('***************************************************************************************************************');
            if (event.status === '404') {
                done();
            } else {
                done(new Error('expected 404'));
            }
        }, function (err) {
            logger.error("muon promise.then() error!!!!!");
            console.log('***************************************************************************************************************');
            console.log('*** done');
            console.log('***************************************************************************************************************');
            done(err);
        }).catch(function (error) {
            logger.error("muoncore-test.js promise.then() error!!!!!: " + error);
            console.log('***************************************************************************************************************');
            console.log('*** done');
            console.log('***************************************************************************************************************');
            done(err);

        });
    });


    it("transport returns failure message for invalid server name", function (done) {


        console.log('***************************************************************************************************************');
        console.log('*** transport returns failure message for invalid server name');
        console.log('***************************************************************************************************************');
        muon = muoncore.create("example-client", amqpurl);

        var promise = muon.request('rpc://invalid-service/blah', "ping");

        promise.then(function (event) {
            logger.warn("rpc://example-client server response received! event.id=" + event.id);
            logger.warn("rpc://example-client server response received! event=" + JSON.stringify(event));
            logger.warn("muon promise.then() asserting response...");
            assert(event, "request event is undefined");
            assert.equal(event.status, "noserver", "expected 'noserver' response message from muon://invalid-service/blah");


            console.log('***************************************************************************************************************');
            console.log('*** done');
            console.log('***************************************************************************************************************');
            if (event.status === 'noserver') {
                done();
            } else {
                done(new Error('expected noserver error'));
            }

        }, function (err) {
            logger.error("muon promise.then() error!!!!!");
            done(err);
        }).catch(function (error) {
            logger.error(error);
            done(error);

        });
    });


    it("rpc returns timeout message for non replying resource", function (done) {
        console.log('***************************************************************************************************************');
        console.log('*** rpc returns timeout message for non replying resource');
        console.log('***************************************************************************************************************');
        muon = muoncore.create(serviceName, amqpurl);
        muon.handle('/tennis', function (event, respond) {
            logger.warn('*****  rpc://service/tennis: muoncore-test.js *************************************************');
            logger.warn('rpc://service/tennis server not responding');
            //respond("pong");
        });

        muon2 = muoncore.create("example-client", amqpurl);

        var promise = muon2.request('rpc://example-service/tennis', "ping");

        promise.then(function (event) {
            logger.warn("rpc://example-client server response received! event.id=" + event.id);
            logger.warn("rpc://example-client server response received! event=" + JSON.stringify(event));
            logger.warn("muon promise.then() asserting response...");
            assert(event, "request event is undefined");
            assert.equal(event.error.status, "timeout", "expected 'timeout' message from calling rpc://example-service/tennis");

            console.log('***************************************************************************************************************');
            console.log('*** done');
            console.log('***************************************************************************************************************');
            if (event.error && event.error.status === 'timeout') {
                done();
            } else {
                done(new Error('timeout exceeded'));
            }

        }, function (err) {
            logger.error(err);
            done(err);
        }).catch(function (error) {
            logger.error(error);
            done(error);

        });
    });


    it("remote introspection request", function (done) {
        var doneOnce = asyncAssert(done);
        muon = muoncore.create('awesome-service', amqpurl);
        muon.handle('/some-endpoint', function (event, respond) {
            logger.warn('*****  muon://awesome-service/some-endpoint: called *************************************************');
            logger.warn('rpc://awesome-service/some-endpoint server responding to event=' + JSON.stringify(event));
            respond("I'm awesome, awesome, awesome, awesome");
        });

        muon2 = muoncore.create("awesome-client", amqpurl);

        var promise = muon2.introspect('awesome-service', function (response) {
            logger.trace(response);
            assert(response, "introspect response is undefined");
            doneOnce(response.protocols[0].protocolScheme, 'rpc');
        });

    });


    it("handles missing transport component error graefully", function () {

        expect(function () {
            muoncore.create(serviceName, 'http://blah/blah/blah', amqpurl)
        }).to.throwException(/unable to find transport component for url/);

    });


    it("handles missing discovery component error graefully", function () {

        expect(function () {
            muoncore.create(serviceName, amqpurl, 'http://blah/blah/blah')
        }).to.throwException(/unable to find discovery component for url/);

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
