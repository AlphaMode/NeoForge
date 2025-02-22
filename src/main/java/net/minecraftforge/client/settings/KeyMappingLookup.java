/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.settings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KeyMappingLookup
{
    private static final EnumMap<KeyModifier, Map<InputConstants.Key, Collection<KeyMapping>>> map = new EnumMap<>(KeyModifier.class);
    static
    {
        for (KeyModifier modifier : KeyModifier.values())
        {
            map.put(modifier, new HashMap<>());
        }
    }

    /** Replaced by {@link #getAll(InputConstants.Key) getAll} */
    @Deprecated(forRemoval = true, since = "1.20.1")
    @Nullable
    public KeyMapping get(InputConstants.Key keyCode)
    {
        KeyModifier activeModifier = KeyModifier.getActiveModifier();
        if (!activeModifier.matches(keyCode))
        {
            KeyMapping binding = get(keyCode, activeModifier);
            if (binding != null)
            {
                return binding;
            }
        }
        return get(keyCode, KeyModifier.NONE);
    }

    @Nullable
    @Deprecated(forRemoval = true, since = "1.20.1")
    private KeyMapping get(InputConstants.Key keyCode, KeyModifier keyModifier)
    {
        Collection<KeyMapping> bindings = map.get(keyModifier).get(keyCode);
        if (bindings != null)
        {
            for (KeyMapping binding : bindings)
            {
                if (((IForgeKeyMapping) binding).isActiveAndMatches(keyCode))
                {
                    return binding;
                }
            }
        }
        return null;
    }

    /**
     * Returns all active keys associated with the given key code and the active
     * modifiers and conflict context.
     *
     * @param keyCode the key being pressed
     * @return the list of key mappings
     */
    public List<KeyMapping> getAll(InputConstants.Key keyCode)
    {
        List<KeyMapping> matchingBindings = new ArrayList<KeyMapping>();
        for (Map<InputConstants.Key, Collection<KeyMapping>> bindingsMap : map.values())
        {
            Collection<KeyMapping> bindings = bindingsMap.get(keyCode);
            if (bindings != null)
            {
                matchingBindings.addAll(bindings);
            }
        }
        return matchingBindings;
    }

    public void put(InputConstants.Key keyCode, KeyMapping keyBinding)
    {
        KeyModifier keyModifier = ((IForgeKeyMapping) keyBinding).getKeyModifier();
        Map<InputConstants.Key, Collection<KeyMapping>> bindingsMap = map.get(keyModifier);
        Collection<KeyMapping> bindingsForKey = bindingsMap.get(keyCode);
        if (bindingsForKey == null)
        {
            bindingsForKey = new ArrayList<KeyMapping>();
            bindingsMap.put(keyCode, bindingsForKey);
        }
        bindingsForKey.add(keyBinding);
    }

    public void remove(KeyMapping keyBinding)
    {
        KeyModifier keyModifier = ((IForgeKeyMapping) keyBinding).getKeyModifier();
        InputConstants.Key keyCode = ((IForgeKeyMapping) keyBinding).getKey();
        Map<InputConstants.Key, Collection<KeyMapping>> bindingsMap = map.get(keyModifier);
        Collection<KeyMapping> bindingsForKey = bindingsMap.get(keyCode);
        if (bindingsForKey != null)
        {
            bindingsForKey.remove(keyBinding);
            if (bindingsForKey.isEmpty())
            {
                bindingsMap.remove(keyCode);
            }
        }
    }

    public void clear()
    {
        for (Map<InputConstants.Key, Collection<KeyMapping>> bindings : map.values())
        {
            bindings.clear();
        }
    }
}
