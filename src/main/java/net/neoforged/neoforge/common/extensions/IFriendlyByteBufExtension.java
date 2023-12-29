/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Collection;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Additional helper methods for {@link FriendlyByteBuf}.
 */
public interface IFriendlyByteBufExtension {
    private FriendlyByteBuf self() {
        return (FriendlyByteBuf) this;
    }

    /**
     * Writes a FluidStack to the packet buffer, easy enough. If EMPTY, writes a FALSE.
     * This behavior provides parity with the ItemStack method in PacketBuffer.
     *
     * @param stack FluidStack to be written to the packet buffer.
     */
    default void writeFluidStack(FluidStack stack) {
        if (stack.isEmpty()) {
            self().writeBoolean(false);
        } else {
            self().writeBoolean(true);
            stack.writeToPacket(self());
        }
    }

    /**
     * Reads a FluidStack from this buffer.
     */
    default FluidStack readFluidStack() {
        return !self().readBoolean() ? FluidStack.EMPTY : FluidStack.readFromPacket(self());
    }

    /**
     * Writes the entries in the given set to the buffer, by first writing the count and then writing each entry.
     *
     * @param set    The set to write
     * @param writer The writer to use for writing each entry
     * @param <T>    The type of the entry
     * @implNote This is a convenience method for {@link FriendlyByteBuf#writeCollection(Collection, FriendlyByteBuf.Writer)}, where the callback can be a method on the entry type.
     */
    default <T> void writeObjectCollection(Collection<T> set, BiConsumer<T, FriendlyByteBuf> writer) {
        self().writeCollection(set, (buf, t) -> writer.accept(t, buf));
    }

    /**
     * Reads an {@link ItemStack} from the current buffer, but allows for a larger count than the vanilla method, using a variable length int instead of a byte.
     *
     * @return The read stack
     */
    default ItemStack readItemWithLargeCount() {
        if (!self().readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            Item item = self().readById(BuiltInRegistries.ITEM);
            int i = self().readVarInt();
            return net.neoforged.neoforge.attachment.AttachmentInternals.reconstructItemStack(item, i, self().readNbt());
        }
    }

    /**
     * Writes an {@link ItemStack} to the current buffer, but allows for a larger count than the vanilla method, using a variable length int instead of a byte.
     *
     * @param stack The stack to write
     * @return The buffer
     */
    default FriendlyByteBuf writeItemWithLargeCount(ItemStack stack) {
        if (stack.isEmpty()) {
            self().writeBoolean(false);
        } else {
            self().writeBoolean(true);
            Item item = stack.getItem();
            self().writeId(BuiltInRegistries.ITEM, item);
            self().writeVarInt(stack.getCount());
            CompoundTag compoundtag = new CompoundTag();
            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
                compoundtag = stack.getTag();
            }
            compoundtag = net.neoforged.neoforge.attachment.AttachmentInternals.addAttachmentsToTag(compoundtag, stack, false);

            self().writeNbt(compoundtag);
        }

        return self();
    }
}
