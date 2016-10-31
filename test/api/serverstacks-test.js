var ServerStacks = require('../../muon/api/server-stacks.js');
var handler = require('../../muon/infrastructure/handler.js');
var assert = require('assert');

describe("serverStacks test:", function () {


    it("does the server stack thing", function (done) {
        var serverStacks = new ServerStacks('test-server');

        var message = 'this is a test message';


        var stubProtocol = {

            name: function () {
                return 'stub'
            },
            protocolHandler: function () {
                return {
                    server: function () {
                        var protocolHandler = handler.create('server', {});
                        protocolHandler.outgoing(function (data, accept, reject, route) {
                            console.log('server outgoing data: ' + data);
                        });
                        protocolHandler.incoming(function (data, accept, reject, route) {
                            console.log('server incoming data: ' + data);
                            reject(data);
                        });
                        return protocolHandler;
                    },
                    client: function () {
                        var protocolHandler = handler.create('client', {});
                        protocolHandler.outgoing(function (data, accept, reject, route) {
                            console.log('client outgoing data: ' + data);
                        });
                        protocolHandler.incoming(function (data, accept, reject, route) {
                            console.log('client incoming data: ' + data);
                        });
                        return protocolHandler;
                    }
                }
            }
        }

        serverStacks.addProtocol(stubProtocol);

        var channel = serverStacks.openChannel('stub');

        channel.send(message);

        channel.listen(function (data) {
            if (data.protocol != 'muon' && data.step != 'ChannelShutdown') {
                assert.equal(message, data, 'expected simple test message ""' + message + '" but instead received ' + JSON.stringify(data));
                done();
            }

        });


    });


    it("responds success to shared-channel", function (done) {
        var serverStacks = new ServerStacks('test-server');
        var channel = serverStacks.openChannel('shared-channel');
        assert(channel);
        done();
    });

    it("handles missing protocol gracefully", function (done) {
        var serverStacks = new ServerStacks('test-server');
        var channel = serverStacks.openChannel('non-existant-protocol');
        assert.equal(null, channel);
        done();
    });


});
