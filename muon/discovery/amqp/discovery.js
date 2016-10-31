var _ = require("underscore");
var AmqpConnection = require('./infra/amqp-connection.js');
var Broadcast = require('./infra/amqp-broadcast.js');
require('sexylog');

var AmqpDiscovery = function (url, frequency) {

    if (!frequency) frequency = 3000; //broadcast frequency ms
    this.cacheFillTime = 5500;
    this.frequency = frequency;
    this.descriptors = [];
    this.callbacks = [];

    var _this = this;

    logger.debug("[*** DISCOVERY:BOOTSTRAP ***] AMQP Discovery is booting using URL " + url);
    _this.discoveryInitiated = false;
    _this.connection = new AmqpConnection(url);
    _this.url = url;

    _this.connection.connect(function () {
        logger.info("[*** DISCOVERY:BOOTSTRAP ***] AMQP Discovery is ready!!");
        setTimeout(function() {
            logger.debug("Discovery is now Initialised and has a full cache. Discovery queries will now return")
            _this.discoveryInitiated = true;
        }, _this.cacheFillTime)
        _this.broadcast = new Broadcast(_this.connection);
        listenToServices(_this)
        startAnnouncements(_this)
    });

    _this.serviceList = []

    _this.addFoundService = function(svc) {
        if (_.findWhere(_this.serviceList, {"identifier": svc.identifier})) return
        _this.serviceList.push(svc);
    }

    this.discoveredServices = {
        find: function (name) {
            logger.trace("DISCOVERY:AMQP - Searching for service " + name + " in list " + JSON.stringify(_this.serviceList))
            return _this.serviceList.find(function(svc) {
                return svc.identifier == name
            })
        },
        findServiceWithTags: function (tags) {
            return _this.serviceList.find(function(svc) {
                var matchingTags = svc.tags.filter(function(tag) {
                    return tags.indexOf(tag) >= 0
                })
                return matchingTags.length == tags.length
            })
        },
        serviceList: _this.serviceList
    };
};


AmqpDiscovery.prototype.advertiseLocalService = function (serviceDescriptor) {
    this.descriptors.push(serviceDescriptor);
};
AmqpDiscovery.prototype.clearAnnouncements = function () {
    this.descriptors = [];
};

AmqpDiscovery.prototype.discoverServices = function (callback) {
    var _this = this
    setTimeout(function () {
        if (_this.discoveryInitiated) {
            callback(_this.discoveredServices);
        } else {
            var interval = setInterval(function() {
                if (_this.discoveryInitiated) {
                    clearInterval(interval)
                    callback(_this.discoveredServices);
                }
            }, 100)
        }
    }, 0);
};

AmqpDiscovery.prototype.close = function () {
    logger.debug("[*** DISCOVERY:SHUTDOWN ***] closing connections...");
    this.connection.close();
};

AmqpDiscovery.prototype.shutdown = function () {
    logger.debug("[*** DISCOVERY:SHUTDOWN ***] shutting down connections...");
    this.connection.close();
};

function listenToServices(discovery) {
    discovery.broadcast.listenOnBroadcast("discovery", function (event, message) {
        try {
            discovery.addFoundService(message);
        } catch (err) {
            logger.warn("[*** DISCOVERY ***] Had issues parsing discovery response");
            logger.warn(err);
        }
    });
}

function startAnnouncements(discovery) {
    var waitInterval = setInterval(function () {
        if (typeof discovery.broadcast !== 'undefined') {
            clearInterval(waitInterval);

            _.each(discovery.descriptors, function (it) {
                discovery.broadcast.emit(
                    {
                        name: "discovery",
                        payload: it
                    });
            });

            setInterval(function () {
                _.each(discovery.descriptors, function (response) {
                    var discMsg = {
                        name: "discovery",
                        payload: response
                    };
                    logger.debug('[*** DISCOVERY ***] broadcasting discovery services: ' + JSON.stringify(discMsg));
                    discovery.broadcast.emit(discMsg);
                });
            }, discovery.frequency);
        }
    }, 0);
}

module.exports = AmqpDiscovery;
