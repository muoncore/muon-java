var ServerStacks = require("../../muon/api/server-stacks");
var url = require("url");
var RSVP = require('rsvp');
var TransportClient = require("../transport/transport-client")

module.exports.build = function (config) {
    logger.info('builder.build() config=' + JSON.stringify(config));
    var serverStacks = new ServerStacks(config.serviceName);
    var transport;
    
    var infrastructure = {
        config: config,
        discovery: '',
        serverStacks: serverStacks,
        shutdown: function () {
            if (transport) transport.shutdown();
        },
        getTransport: function () {
            var retryMs = 100;
            var timeoutMs = 5000;
            var promise = new RSVP.Promise(function (resolve, reject) {
                var attempts = 0;
                var interval = setInterval(function () {

                    attempts++;
                    if (transport) {
                        logger.trace('getTransport() returning transport after attempts=' + attempts);
                        clearInterval(interval);
                        resolve(transport);  //TODo, wrap with transport client/ shared client.
                    }
                    var finalAttemptNum = timeoutMs / retryMs; // keep retrying every $retryMs until $timeoutMs
                    if (attempts > finalAttemptNum) {
                        clearInterval(interval);
                        logger.error('infrasrtructure unable to get transport handle after 5s, giving up and rejecting getTransport() promise');
                        reject(new Error('unable to find transport after 5s'));
                    }
                }, retryMs);
            }.bind(this));
            return promise;
        }
    }

    try {
        var AmqpDiscovery = require('../discovery/' + config.discoveryProtocol() + '/discovery.js');
        infrastructure.discovery = new AmqpDiscovery(config.discovery_url);
    } catch (err) {
        logger.error('unable to find discovery component for url: ""' + config.discovery_url + '""');
        logger.error(err.stack);
        throw new Error('unable to find discovery component for url: ""' + config.discovery_url + '""');
    }

    try {
        var amqpTransport = require('../transport/' + config.transportProtocol() + '/transport.js');
        var muonPromise = amqpTransport.create(config.serviceName, config.transport_url, serverStacks, infrastructure.discovery);
        muonPromise.then(function (transportObj) {
            logger.debug('[*** INFRASTRUCTURE:BOOTSTRAP ***] TRANSPORT CREATED SUCCESS');
            transport = TransportClient.create(transportObj, infrastructure);
        });
    } catch (err) {
        logger.error('unable to find transport component for url: ""' + config.transport_url + '""');
        logger.error(err.stack);
        throw new Error('unable to find transport component for url: ""' + config.transport_url + '""');
    }

    return infrastructure;
}

module.exports.config = function (serviceName, transportUrl, discoveryUrl) {
    if (!discoveryUrl) discoveryUrl = transportUrl;
    var config = {
        serviceName: serviceName,
        discovery_url: discoveryUrl,
        transport_url: transportUrl,
        transportProtocol: function () {
            return url.parse(transportUrl).protocol.split(':')[0];
        },
        discoveryProtocol: function () {
            return url.parse(discoveryUrl).protocol.split(':')[0];
        }
    };
    return config;
}
