var bichannel = require('../../../muon/infrastructure/channel.js');
var server = require('../../../muon/transport/amqp/server.js');
var assert = require('assert');
var expect = require('expect.js');
var uuid = require('node-uuid');
var messages = require('../../../muon/domain/messages.js');
var AmqpDiscovery = require("../../../muon/discovery/amqp/discovery");
var helper = require('../../../muon/transport/amqp/transport-helper.js');

describe("muon transport server-test:", function () {


     beforeEach(function() {
        //
     });

      afterEach(function() {
            //shutdown nicely
      });

     before(function() {

     });

      after(function() {
            //shutdown nicely
      });

    it("server amqpapi connection errors handled gracefully", function (done) {

        var serverName = 'serverabc123';
        var clientName = 'clientabc123';
        var url = "amqp://";
        var discovery = new AmqpDiscovery(url);

        var errMsg = 'amqp api error';

        var fakeServerStackChannel = bichannel.create("fake-serverstacks");
        var fakeServerStacks = {
            openChannel: function() {
                return fakeServerStackChannel.rightConnection();
            }
        }


        server.onError(function(err) {
                   console.log('********** client_server-test.js muonClientChannel.onError() error received: ');
                   console.dir(err);
                   console.log(typeof err);
                   assert.ok(err);
                   assert.ok(err instanceof Error);
                   expect(err.toString()).to.contain(errMsg);
                   done();
        });

        var fakeAmqpApi = {
            inbound: function() {
              throw new Error(errMsg);
            },
            outbound: function() {
              throw new Error(errMsg);
            },
            url: function() {
              return 'http://fake.com/';
            }

        };
        server.connect(clientName, fakeAmqpApi, fakeServerStacks, discovery);

    });


    it("server deletes muon socket queues on channel_op equals closed message", function (done) {

      var serverName = 'serverabc123';
      var url = process.env.MUON_URL || "amqp://muon:microservices@localhost";

      var listen_q = serverName+ '.listen_to_me';
      var send_q = serverName+ '.send_to_me';

      var fakeServerStackChannel = bichannel.create("fake-serverstacks");
      var fakeServerStacks = {
          openChannel: function() {
              return fakeServerStackChannel.rightConnection();
          }
      }

      var deleteCalled = 0;
      var discovery = {
          discoverServices: function(cb) {
              var services = {find: function() {return {identifier: serverName}}};
              cb(services);
          },
          advertiseLocalService: function() {},
          serviceList: [{identifier: serverName}]
      }
      var amqpApi = {
        delete: function(queueName) {
            logger.trace('delete called for queuename='+ queueName);
            deleteCalled++;
            expect(queueName).to.contain(serverName);
            if (deleteCalled == 2) done(); // it's been called twice
        },
        outbound: function() {
          return {send: function(){}};
        },
        inbound: function(q) {

          var serviceQueueName = helper.serviceNegotiationQueueName(serverName);

          if (q == serviceQueueName) {
            return {
              listen: function(cb) {
                cb( {id: 'abc123', headers: helper.handshakeRequestHeaders('rpc', listen_q, send_q), data: {}} );
              }
            }
          } else {
            return {
              listen: function(cb) {
                //cb( {id: 'abc123', headers: {handshake: 'initiated'}, data: {}} );
              }
            }
          }


        },
        url: function() {return 'http://server/path'}
      };

      server.connect(serverName, amqpApi, fakeServerStacks, discovery);

      fakeServerStackChannel.leftSend(messages.shutdownMessage());

    });


});
