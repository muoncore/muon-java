
var _ = require("underscore")
var uuid = require("node-uuid")
var messages = require("../domain/messages")
var bichannel = require("../infrastructure/channel")
var zip = require("../transport/zip")

module.exports.openSharedServerChannel = function(serverStacks) {

    var protocolChannels = {}

    var channel = bichannel.create("shared-channel")
    var zipchannel = bichannel.create("shared-channel")
    zip.connectAndZip(zipchannel.leftConnection(), channel.rightConnection())

    channel.leftConnection().listen(function(message) {
        if (message.protocol == "shared-channel") {

            var sharedChannelMessage = messages.decode(message.payload)
            var sharedChannelId = sharedChannelMessage.channelId
            var protocolMessage = sharedChannelMessage.message

            var protoChannel = protocolChannels[sharedChannelId]
            
            if (protoChannel == null) {
                logger.debug("Opening new protocol channel for shared channelId " + sharedChannelId)
                protoChannel = serverStacks.openChannel(protocolMessage.protocol)
                protocolChannels[sharedChannelId] = protoChannel
                if (!protoChannel) {
                    logger.warn("Server stacks has returned a null channel for " + JSON.stringify(protocolMessage))
                    return
                }
                protoChannel.listen(function(msg) {
                    logger.debug("Received new mnessage from protocol")

                    var msg = messages.muonMessage({
                        channelId: sharedChannelId,
                        message: msg
                    }, message.origin_service, message.targetService, "shared-channel", "message")

                    channel.leftConnection().send(msg)
                })
            }
            logger.debug("Routing shared channel message onto channel " + sharedChannelId)
            protoChannel.send(protocolMessage)
            /*
            TODO - channel shutdown behaviour. how to handle?
             */
        }
    })
    
    return zipchannel.rightConnection();
}
