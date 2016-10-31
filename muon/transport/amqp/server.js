//var amqp = require('../../../muon/transport/amqp/amqp-api.js');
var RSVP = require('rsvp');
var bichannel = require('../../../muon/infrastructure/channel.js');
var helper = require('./transport-helper.js');
require('sexylog');
var messages = require('../../domain/messages.js');
var MuonSocketAgent = require("../../socket/keep-alive-agent")
var errCallback;

exports.connect = function (serviceName, amqpApi, serverStacks, discovery) {

    try {

        logger.debug("[*** TRANSPORT:SERVER:BOOTSTRAP ***] server stack of service '" + serviceName + "' connecting to muon...");
        var serviceQueueName = helper.serviceNegotiationQueueName(serviceName);

        logger.info("[*** TRANSPORT:SERVER:HANDSHAKE ***] muon service '" + serviceName + "' listening for negotiation messages on amqp queue '%s'", serviceQueueName);
        var amqpQueue = amqpApi.inbound(serviceQueueName);
        amqpQueue.listen(function (msg) {
            logger.warn("[*** TRANSPORT:SERVER:HANDSHAKE ***]  received negotiation message.headers=%s", JSON.stringify(msg.headers));
            var serverStackChannel = serverStacks.openChannel(msg.headers.protocol);
            var transportChannel = bichannel.create(msg.headers.protocol + "-transport");

            // new MuonSocketAgent(serverStackChannel, transportChannel.leftConnection(), msg.headers.protocol, 1000);

            initMuonClientServerSocket(amqpApi, msg.headers.server_listen_q, msg.headers.server_reply_q, serverStackChannel);
            var replyHeaders = helper.handshakeAcceptHeaders();
            amqpApi.outbound(msg.headers.server_reply_q).send({headers: replyHeaders, data: {}});
            logger.info("[*** TRANSPORT:SERVER:HANDSAKE ***]  handshake confirmation sent to queue " + msg.headers.server_reply_q);
        });
    } catch (err) {
        logger.error(err.stack);
        if (errCallback) errCallback(err);
    }

};

exports.onError = function (callback) {
    errCallback = callback;
};

function initMuonClientServerSocket(amqpApi, listen_queue, send_queue, serverStackChannel) {

    amqpApi.inbound(listen_queue).listen(function (message) {
        logger.debug("[*** TRANSPORT:SERVER:INBOUND ***]  received inbound message: %s", JSON.stringify(message));
        var muonMessage = message.data;
        try {
            messages.validate(muonMessage);
            logger.info("Sending message on channel " + JSON.stringify(muonMessage));
            serverStackChannel.send(muonMessage);
        } catch (e) {
            logger.info("CLOSING CHANNEL .... " + JSON.stringify(e))
            serverStackChannel.close()
        }
    });

    serverStackChannel.listen(function (message) {
        logger.trace('[*** TRANSPORT:SERVER:OUTBOUND ***] message: ' + JSON.stringify(message));
        if (message.channel_op == 'closed') {
            // close muon socket
            logger.warn("[*** TRANSPORT:SERVER:OUTBOUND ***]  message channel_op=closed received. deleteing temp amqp queues for muon socket");
            logger.trace('deleting send_queue/listen_queue=' + send_queue + '/' + listen_queue);
            amqpApi.delete(listen_queue);
            amqpApi.delete(send_queue);
            muonSocketOpen = false;
            return;
        } else {
            logger.debug("[*** TRANSPORT:SERVER:OUTBOUND ***]  handling outbound muon event to queue " + send_queue + ": %s", JSON.stringify(message));
            amqpApi.outbound(send_queue).send({headers: {"content_type": "application/json"}, data: message});
        }
    });
}
