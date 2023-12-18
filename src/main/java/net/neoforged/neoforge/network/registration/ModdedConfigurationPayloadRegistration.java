package net.neoforged.neoforge.network.registration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;

/**
 * Registration for a custom packet payload.
 * This type holds the negotiated preferredVersion of the payload to use, and the handler for it.
 *
 * @param <T> The type of payload.
 */
public record ModdedConfigurationPayloadRegistration<T extends CustomPacketPayload>(
        ResourceLocation id,
        Class<T> type,
        IConfigurationPayloadHandler<T> handler,
        FriendlyByteBuf.Reader<T> reader
) {
}
