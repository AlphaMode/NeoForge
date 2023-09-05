package net.minecraftforge.network.simple;

import java.util.function.IntSupplier;

/**
 * An abstraction for login network packets.
 *
 * @see SimpleChannel#simpleLoginMessageBuilder(Class, int)
 */
public interface SimpleLoginMessage extends SimpleMessage, IntSupplier {
    /**
     * {@return the index of this packet}
     */
    int getLoginIndex();

    /**
     * Sets the index of this packet.
     *
     * @param loginIndex the index of the packet
     */
    void setLoginIndex(int loginIndex);

    @Override
    default int getAsInt() {
        return getLoginIndex();
    }
}
