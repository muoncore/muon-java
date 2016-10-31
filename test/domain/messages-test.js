var muoncore = require('../../muon/api/muoncore.js');
var assert = require('assert');
var expect = require('expect.js');
var messages = require('../../muon/domain/messages.js');


describe("test messages:", function () {


    it("create muon message with valid headers", function (done) {
           //todo change to muon message
          var msg = messages.muonMessage("PING", 'testclient', 'testserver', 'rpc', "request.made");
          console.log('valid message: ');
          console.dir(msg);
          assert.equal(msg.origin_service, 'testclient');
          assert.equal(msg.target_service, 'testserver');
          done();
    });




    it("copy message", function (done) {
          var msg =  messages.muonMessage("PING", 'testclient', 'testserver',  'rpc', "request.made");
          var messageCopy = messages.copy(msg);
          assert.deepEqual(msg, messageCopy);
          done();
    });

    it("encode/decode object payload", function (done) {
         var object = {text: "hello, world!"};
          var payload =  messages.encode(object);
          var decoded = messages.decode(payload, 'application/json');
          assert.deepEqual(object, decoded);
          done();
    });

    it("encode/decode string payload", function (done) {
         var object = "hello, world!";
          var payload =  messages.encode(object);
          var decoded = messages.decode(payload);
          assert.equal(object, decoded);
          done();
    });



    it("encode/decode muon message json payload", function (done) {
         var rpcMsg = {
            url: 'rpc://client1/ping',
            body: "PING",
            content_type: 'text/plain'
        }
        var muonMessage = messages.muonMessage(rpcMsg, 'client1', 'server1',  'rpc',"request.made");

          var decoded = messages.decode(muonMessage.payload, 'application/json');
          assert.deepEqual(rpcMsg, decoded);
          done();
    });

    it("encode/decode muon message string payload", function (done) {
        var muonMessage = messages.muonMessage('PING', 'client1', 'server1',  'rpc', "request.made");
          var decoded = messages.decode(muonMessage.payload, 'text/plain');
          assert.equal('PING', decoded);
          done();
    });


    it("encode/decode buffer message payload", function (done) {
         var rpcMsg = {
            url: 'rpc://client1/ping',
            body: new Buffer("PING"),
            content_type: 'text/plain'
        };
        var muonMessage = messages.muonMessage(rpcMsg, 'client1', 'server1',  'rpc', "request.made");
          var decoded = messages.decode(muonMessage.payload);
          console.dir(decoded);
          assert.equal('PING', new Buffer(decoded.body).toString());
          done();
    });

    it("buffer/unbuffer", function (done) {
         var buffer = new Buffer('PING');

         console.log('buffer.toJSON(): ' + buffer.toJSON());
         console.log('buffer2.toJSON().stringify(): ' + JSON.stringify(buffer.toJSON()));
         console.log('buffer.toJSON().data: ' + buffer.toJSON().data);

         var json = buffer.toJSON();
         var jsonData  = json.data;

         assert.equal('PING', buffer.toString());
         assert.equal('PING', new Buffer(jsonData).toString());
         done();
    });


    it("buffer/unbuffer with nested buffer in object", function (done) {

         var buffer = new Buffer('PING');

         console.log('buffer.toJSON(): ' + buffer.toJSON());
         console.log('buffer2.toJSON().stringify(): ' + JSON.stringify(buffer.toJSON()));
         console.log('buffer.toJSON().data: ' + buffer.toJSON().data);

         var json = buffer.toJSON();
         var jsonData  = json.data;

         assert.equal('PING', buffer.toString());
         assert.equal('PING', new Buffer(jsonData).toString());
         done();
    });




    it("create resource 404 failure message", function (done) {
          var msg =  messages.muonMessage("PING", 'testclient', 'testserver',  'rpc', "request.made");
          var returnMessage = messages.serverFailure(msg, 'request', '404', 'resource not found /ping');
          //console.log('***** messages-test.js ********************************');
          //console.dir(returnMessage);
          assert.equal(returnMessage.origin_service, 'testserver', 'expected return message to swap source/target service');
          assert.equal(returnMessage.target_service, 'testclient', 'expected return message to swap target/source service');
          assert.equal(returnMessage.status, 'failure', 'expected return message to have 404 status');
          assert.equal(returnMessage.payload.status, '404', 'expected return message to have 404 status');
          assert.equal(returnMessage.provenance_id, msg.id, 'expected return message provenance_id to have same msg.id');
          done();
    });

    it("create server discovery failure message", function (done) {
          var msg =  messages.muonMessage("PING", 'testclient', 'testserver',  'rpc', "request.made");
          var returnMessage =  messages.clientFailure(msg, 'request', 'noserver', 'service "testserver" not found ', "request.made");
          //console.log('***** messages-test.js ********************************');
          //console.dir(returnMessage);
          assert.equal(returnMessage.origin_service, 'testclient', 'expected return message to swap source/target service');
          assert.equal(returnMessage.target_service, 'testserver', 'expected return message to swap target/source service');
          assert.equal(returnMessage.status, 'failure', 'expected return message to have 404 status');
          assert.equal(returnMessage.payload.status, 'noserver', 'expected return message to have 404 status');
          assert.equal(returnMessage.provenance_id, msg.id, 'expected return message provenance_id to have same msg.id');
          done();
    });




    it("test message validation", function (done) {
          var msg =     {
                id: '696f4064-2cc5-44c2-a4dd-6c61bdb1e799',
                created: new Date(),
                provenance_id: '696f4064-2cc5-44c2-a4dd-6c61bdb1e799',
                step: 'request.made',
                protocol: 'request',
                event_source: arguments.callee.caller.name,
                target_service: 'server1',
                origin_service: 'client1',
                channel_op: 'normal',
                content_type: 'application/json',
                payload: 'PING'
          };

          var response = messages.validate(msg);
          assert(response);
          done();
    });


    it("shutdown message is valid", function (done) {
        var msg =messages.shutdownMessage();
          var response = messages.validate(msg);
          assert(response);
          done();
    });

    it("decodes empty array payload", function (done) {
        var outcome = messages.decode([], 'application/json');
          assert.equal('', outcome);
          done();
    });


    it("decodes empty object payload", function (done) {
        var outcome = messages.decode({}, 'application/json');
          assert.equal('', outcome);
          done();
    });



    it("creating message with invalid headers throws exception", function (done) {
          try {
            var msg = messages.muonMessage("PING", 'testclient',  'rpc', '', '');
          }
          catch(err) {
            //logger.error(err);
            //logger.error(err.stack);
            expect(err).not.to.be(undefined);
            expect(err.message).to.contain('Error! problem validating muon message schema');
          }
          done();
    });


});
