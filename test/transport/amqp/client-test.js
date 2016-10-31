var bichannel = require('../../../muon/infrastructure/channel.js');
var client = require('../../../muon/transport/amqp/client.js');
var server = require('../../../muon/transport/amqp/server.js');
var assert = require('assert');
var expect = require('expect.js');
var uuid = require('node-uuid');
var messages = require('../../../muon/domain/messages.js');
var amqp = require('../../../muon/transport/amqp/amqp-api.js');
var amqplib = require('amqplib/callback_api');

var AmqpDiscovery = require("../../../muon/discovery/amqp/discovery");

describe("client test:", function () {

    this.timeout(20000);

    it("client discovery error handled gracefully", function (done) {

        var serverName = 'serverabc123';
        var url = process.env.MUON_URL || "amqp://muon:microservices@localhost";
        var discovery = new AmqpDiscovery(url);
        var fakeAmqpApi = {};

        var muonClientChannel = client.connect(serverName, 'rpc', fakeAmqpApi, discovery);

        muonClientChannel.listen(function (msg) {
            expect(msg.status).to.contain('failure');
            var payload = messages.decode(msg.payload);
            expect(payload.status).to.contain('noserver');
            done();
        });
    });


      it("client deletes muon socket queues on channel_op equals closed message", function (done) {

        var serverName = 'serverabc123';
        var url = process.env.MUON_URL || "amqp://muon:microservices@localhost";

        var deleteCalled = 0;
        var discovery = {
            discoverServices: function(cb) {
                var services = {find: function() {return {identifier: serverName}}};
                cb(services);
            },
            serviceList: [{identifier: serverName}]
        }
        var amqpApi = {
          delete: function(queueName) {
              deleteCalled++;
              expect(queueName).to.contain(serverName);
              if (deleteCalled == 2) done(); // it's been called twice
          },
          outbound: function() {
            return {send: function(){}};
          },
          inbound: function() {
            return {listen: function(cb) {cb({headers: {handshake: 'accepted'}})}}
          }
        };

        var muonClientChannel = client.connect(serverName, 'rpc', amqpApi, discovery);

        muonClientChannel.send(messages.shutdownMessage());



      });

});
