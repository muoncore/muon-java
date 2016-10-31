var client = require('./client.js');
var server = require('./server.js');
var bichannel = require('../../infrastructure/channel.js');

var MuonSocketAgent = require('../../socket/keep-alive-agent');
var RSVP = require('rsvp');

exports.create = function (localServiceName, url, serverStacks, discovery) {

    logger.info('[*** TRANSPORT:BOOTSTRAP ***] creating new MUON AMQP Transport connection with url=' + url);

    var upstreamCallback;

    var transportErrCallback = function (err) {
        logger.error('[*** TRANSPORT:ERROR ***] ' + err);
        if (upstreamCallback) {
            upstreamCallback(err);
        }
    }

    var promise = new RSVP.Promise(function (resolve, reject) {
        var amqp = require('../../../muon/transport/amqp/amqp-api.js');
        amqp.connect(url).then(function (amqpApi) {
            server.connect(localServiceName, amqpApi, serverStacks, discovery);
            server.onError(transportErrCallback);
            var transport = {
                openChannel: function (remoteServiceName, protocolName) {

                    logger.debug('[*** TRANSPORT:OPEN-CONNECTION ***] opening muon connection to remote service ""' + remoteServiceName + '""');
                    var transportConnection = client.connect(remoteServiceName, protocolName, amqpApi, discovery);
                    var transportChannel = wrap(transportConnection);
                    logger.trace('[*** TRANSPORT:OPEN-CONNECTION ***] got amqp client channel handle');
                    var upstreamClientChannel = bichannel.create(localServiceName + "upstream-transport-client");
                    logger.trace('[*** TRANSPORT:OPEN-CONNECTION ***] created upstream cleint channel');
                    var clientKeepAliveAgent = new MuonSocketAgent(upstreamClientChannel.rightConnection(), transportChannel.leftConnection(), protocolName, 1000);
                    logger.trace('[*** TRANSPORT:OPEN-CONNECTION ***] created keep-alive agent');
                    client.onError(transportErrCallback);
                    logger.trace('[*** TRANSPORT:OPEN-CONNECTION ***] returning upstream cleint channel handle');
                    return upstreamClientChannel.leftConnection();

                },
                onError: function (cb) {
                    upstreamCallback = cb;
                },
                shutdown: function () {
                    amqpApi.shutdown();
                    serverStacks.shutdown();
                    discovery.shutdown();
                }

            }
            resolve(transport);
        }, function (err) {
            logger.error("[*** TRANSPORT:OPEN-CONNECTION ***] tranport promise.then() error!\n" + err.stack);
        }).catch(function (error) {
            logger.error("[*** TRANSPORT:OPEN-CONNECTION ***] ERROR!:\n" + error.stack);
        });
    });

    return promise;

}


function wrap(connection) {

    var wrapperChannel = bichannel.create(connection.name() + "-wrapper");

    wrapperChannel.rightConnection().listen(function (msg) {
        connection.send(msg);
    });

    connection.listen(function (msg) {
        try {
            wrapperChannel.rightConnection().send(msg);
        } catch (err) {
            logger.warn('error sending message on csp channel ' + connection);
            logger.warn(err.stack);
        }

    });

    return wrapperChannel;
}
