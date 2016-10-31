var _ = require("underscore");
var bichannel = require('../infrastructure/channel');
var rpcProtocol = require('../protocol/rpc.js');
var messages = require('../domain/messages.js');
var MuonSocketAgent = require('../socket/keep-alive-agent');
var sharedChannelServer = require('../transport/shared-channel-server');



var ServerStacks = function (serverName) {
   this.serverName = serverName;
   this.protocols = {};
};



ServerStacks.prototype.openChannel = function(protocolName) {
    logger.debug("[*** API ***] opening muon server stacks channel..." + protocolName);
    if (protocolName == "shared-channel") {
        return sharedChannelServer.openSharedServerChannel(this)
    } else {
        var serverStacksChannel = bichannel.create("serverstacks");
        var protocol = this.protocols[protocolName];
        if (!protocol) return null;
        var protocolServerHandler = protocol.protocolHandler().server();
        serverStacksChannel.leftHandler(protocolServerHandler);

        return serverStacksChannel.rightConnection();
    }
};




ServerStacks.prototype.addProtocol = function(protocolApi) {
      this.protocols[protocolApi.name()] = protocolApi;
}

ServerStacks.prototype.shutdown = function() {
    
}

module.exports = ServerStacks;
