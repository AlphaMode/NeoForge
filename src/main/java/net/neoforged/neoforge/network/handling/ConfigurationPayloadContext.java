/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import java.util.Optional;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.player.Player;

/**
 * The context that is passed to a replyHandler for a payload that arrives during the configuration phase of the connection.
 *
 * @param replyHandler               A reply replyHandler that can be used to send a reply to the player.
 * @param packetHandler         The packet replyHandler that can be used to immediately process other packets.
 * @param taskCompletedHandler  The task completed replyHandler that can be used to indicate that a configuration task has been completed.
 * @param workHandler           A work replyHandler that can be used to schedule work to be done on the main thread.
 * @param flow                  The flow of the packet.
 * @param channelHandlerContext The channel replyHandler context.
 * @param player                The player of the payload.
 * @implNote The {@link #player()} will always be empty, because no player is available during the configuration phase.
 */
public record ConfigurationPayloadContext(
        IReplyHandler replyHandler,
        IPacketHandler packetHandler,
        ITaskCompletedHandler taskCompletedHandler,
        ISynchronizedWorkHandler workHandler,
        PacketFlow flow,
        ChannelHandlerContext channelHandlerContext,
        Optional<Player> player) implements IPayloadContext {
    @Override
    public ConnectionProtocol protocol() {
        return ConnectionProtocol.CONFIGURATION;
    }
}
