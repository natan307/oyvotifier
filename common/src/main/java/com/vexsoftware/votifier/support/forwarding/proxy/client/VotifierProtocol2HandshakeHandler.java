package com.vexsoftware.votifier.support.forwarding.proxy.client;

import com.vexsoftware.votifier.platform.VotifierPlugin;
import com.vexsoftware.votifier.model.Vote;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.CorruptedFrameException;

public class VotifierProtocol2HandshakeHandler extends SimpleChannelInboundHandler<String> {
    private final Vote toSend;
    private final VotifierResponseHandler responseHandler;
    private final VotifierPlugin oyvotifer;

    public VotifierProtocol2HandshakeHandler(Vote toSend, VotifierResponseHandler responseHandler, VotifierPlugin oyvotifer) {
        this.toSend = toSend;
        this.responseHandler = responseHandler;
        this.oyvotifer = oyvotifer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) {
        String[] handshakeContents = s.split(" ");
        if (handshakeContents.length != 3) {
            throw new CorruptedFrameException("Handshake is not valid.");
        }

        VoteRequest request = new VoteRequest(handshakeContents[2], toSend);
        if (oyvotifer.isDebug()) {
            oyvotifer.getPluginLogger().info("Sent request: " + request.toString());
        }
        ctx.writeAndFlush(request);
        ctx.pipeline().addLast(new VotifierProtocol2ResponseHandler(responseHandler));
        ctx.pipeline().remove(this);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        responseHandler.onFailure(cause);
        ctx.close();
    }
}
