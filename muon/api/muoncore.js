var channel = require('../infrastructure/channel.js');
require('sexylog');

var ServerStacks = require("../../muon/api/server-stacks");

var MuonSocketAgent = require('../socket/keep-alive-agent');
var channel = require("../infrastructure/channel")


exports.create = function (serviceName, transportUrl, discoveryUrl, tags) {
    var builder = require("../infrastructure/builder");
    var config = builder.config(serviceName, transportUrl, discoveryUrl);

    var infrastructure = new builder.build(config);

    return this.api(serviceName, infrastructure, tags)
}

exports.channel = function () {
    return channel;
}

exports.ServerStacks = ServerStacks
exports.MuonSocketAgent = MuonSocketAgent
exports.Messages = require("../domain/messages")

exports.api = function (serviceName, infrastructure, tags) {

    var rpc = require('../protocol/rpc');
    var introspection = require('../protocol/introspection');
    var streaming = require('../protocol/streaming/streaming');
    var events = require('../protocol/event');

    var rpcApi = rpc.getApi(serviceName, infrastructure);
    var introspectionApi = introspection.getApi(serviceName, infrastructure);
    var streamingApi = streaming.getApi(serviceName, infrastructure);
    var eventApi = events.getApi(serviceName, infrastructure);

    introspectionApi.protocols([rpcApi]);
    infrastructure.serverStacks.addProtocol(rpcApi);
    infrastructure.serverStacks.addProtocol(introspectionApi);
    infrastructure.serverStacks.addProtocol(streamingApi);

    logger.info("[*** INFRASTRUCTURE:BOOTSTRAP ***] advertising service '" + serviceName + "' on muon discovery");
    //logger.error('amqpApi=' + JSON.stringify(amqpApi));
    //console.dir(amqpApi);

    if (!tags) {
        tags = ["node", serviceName]
    }

    infrastructure.discovery.advertiseLocalService({
        identifier: serviceName,
        tags: tags,
        codecs: ["application/json"],
        //TODO, more intelligent geenration of connection urls by asking the transports
        connectionUrls: [infrastructure.config.transport_url]
    });

    return {
        infrastructure: function () {
            return infrastructure
        },
        transportClient: function () {
            return infrastructure.transport
        },
        discovery: function () {
            return infrastructure.discovery
        },
        shutdown: function () {
            logger.warn("Shutting down muon!");
            infrastructure.shutdown();
        },
        request: function (remoteServiceUrl, data, clientCallback) {
            return rpcApi.request(remoteServiceUrl, data, clientCallback);
        },
        handle: function (endpoint, callback) {
            rpcApi.handle(endpoint, callback);
        },
        emit: function(event) {
            return eventApi.emit(event)
        },
        introspect: function (remoteName, callback) {
            return introspectionApi.introspect(remoteName, callback);
        },
        replay: function (remoteurl, config, callback, errorCallback, completeCallback) {
            return streamingApi.replay(remoteurl, config, callback, errorCallback, completeCallback);
        },
        subscribe: function (remoteurl, params, callback, errorCallback, completeCallback) {
            return streamingApi.subscribe(remoteurl, params, callback, errorCallback, completeCallback);
        }
    };
}
