/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import javax.annotation.Nullable;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

public interface IServerCommonPacketListenerExtension {

    void send(CustomPacketPayload packetPayload);

    void send(CustomPacketPayload packetPayload, @Nullable PacketSendListener listener);

    void disconnect(Component p_294116_);

    Connection getConnection();

    ReentrantBlockableEventLoop<?> getMainThreadEventLoop();
}
