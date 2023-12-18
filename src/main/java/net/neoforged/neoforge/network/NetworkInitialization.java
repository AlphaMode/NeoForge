/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.event.RegisterPacketHandlerEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.payload.*;
import net.neoforged.neoforge.network.registration.registrar.IPayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;

@Mod.EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ApiStatus.Internal
public class NetworkInitialization {
    
    @SubscribeEvent
    public static void register(final RegisterPacketHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(NeoForgeVersion.MOD_ID)
                                                    .versioned(NeoForgeVersion.getSpec())
                                                    .optional();
        registrar
                .configuration(
                        FrozenRegistrySyncStartPayload.ID,
                        FrozenRegistrySyncStartPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle)
                )
                .configuration(
                        FrozenRegistryPayload.ID,
                        FrozenRegistryPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle)
                )
                .configuration(
                        FrozenRegistrySyncCompletedPayload.ID,
                        FrozenRegistrySyncCompletedPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle)
                                            .server(ServerPayloadHandler.getInstance()::handle)
                )
                .configuration(
                        TierSortingRegistryPayload.ID,
                        TierSortingRegistryPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle)
                )
                .configuration(
                        TierSortingRegistrySyncCompletePayload.ID,
                        TierSortingRegistrySyncCompletePayload::new,
                        handlers -> handlers.server(ServerPayloadHandler.getInstance()::handle)
                )
                .configuration(
                        ConfigFilePayload.ID,
                        ConfigFilePayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle)
                );
    }

/*
    public static SimpleChannel getPlayChannel() {
        SimpleChannel playChannel = NetworkRegistry.ChannelBuilder.named(NetworkConstants.FML_PLAY_RESOURCE).clientAcceptedVersions(a -> true).serverAcceptedVersions(a -> true).networkProtocolVersion(() -> NetworkConstants.NETVERSION).simpleChannel();

        playChannel.messageBuilder(PlayMessages.SpawnEntity.class, 0).decoder(PlayMessages.SpawnEntity::decode).encoder(PlayMessages.SpawnEntity::encode).consumerMainThread(PlayMessages.SpawnEntity::handle).add();

        playChannel.messageBuilder(PlayMessages.OpenContainer.class, 1).decoder(PlayMessages.OpenContainer::decode).encoder(PlayMessages.OpenContainer::encode).consumerMainThread(PlayMessages.OpenContainer::handle).add();

        return playChannel;
    }*/
}
