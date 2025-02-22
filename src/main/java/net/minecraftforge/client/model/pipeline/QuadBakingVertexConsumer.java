/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.model.pipeline;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BakedQuadInjection;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Vertex consumer that outputs {@linkplain BakedQuad baked quads}.
 * <p>
 * This consumer accepts data in {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#BLOCK} and is not picky about
 * ordering or missing elements, but will not automatically populate missing data (color will be black, for example).
 */
public class QuadBakingVertexConsumer implements VertexConsumer, IForgeVertexConsumer
{
    private final Map<VertexFormatElement, Integer> ELEMENT_OFFSETS = Util.make(new IdentityHashMap<>(), map -> {
        int i = 0;
        for (var element : DefaultVertexFormat.BLOCK.getElements())
            map.put(element, DefaultVertexFormat.BLOCK.getOffset(i++) / 4); // Int offset
    });
    private static final int QUAD_DATA_SIZE = IQuadTransformer.STRIDE * 4;

    private final Consumer<BakedQuad> quadConsumer;

    int vertexIndex = 0;
    private int[] quadData = new int[QUAD_DATA_SIZE];

    private int tintIndex;
    private Direction direction = Direction.DOWN;
    private TextureAtlasSprite sprite = UnitTextureAtlasSprite.INSTANCE;
    private boolean shade;
    private boolean hasAmbientOcclusion;

    public QuadBakingVertexConsumer(Consumer<BakedQuad> quadConsumer)
    {
        this.quadConsumer = quadConsumer;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z)
    {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
        quadData[offset] = Float.floatToRawIntBits((float) x);
        quadData[offset + 1] = Float.floatToRawIntBits((float) y);
        quadData[offset + 2] = Float.floatToRawIntBits((float) z);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z)
    {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.NORMAL;
        quadData[offset] = ((int) (x * 127.0f) & 0xFF) |
                           (((int) (y * 127.0f) & 0xFF) << 8) |
                           (((int) (z * 127.0f) & 0xFF) << 16);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a)
    {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.COLOR;
        quadData[offset] = ((a & 0xFF) << 24) |
                           ((b & 0xFF) << 16) |
                           ((g & 0xFF) << 8) |
                           (r & 0xFF);
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v)
    {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
        quadData[offset] = Float.floatToRawIntBits(u);
        quadData[offset + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v)
    {
        if (IQuadTransformer.UV1 >= 0) // Vanilla doesn't support this, but it may be added by a 3rd party
        {
            int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.UV1;
            quadData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
        }
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v)
    {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.UV2;
        quadData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
        return this;
    }

    @Override
    public VertexConsumer misc(VertexFormatElement element, int... rawData)
    {
        Integer baseOffset = ELEMENT_OFFSETS.get(element);
        if (baseOffset != null)
        {
            int offset = vertexIndex * IQuadTransformer.STRIDE + baseOffset;
            System.arraycopy(rawData, 0, quadData, offset, rawData.length);
        }
        return this;
    }

    @Override
    public void endVertex()
    {
        if (++vertexIndex != 4)
            return;
        // We have a full quad, pass it to the consumer and reset
        quadConsumer.accept(BakedQuadInjection.withAo(quadData, tintIndex, direction, sprite, shade, hasAmbientOcclusion));
        vertexIndex = 0;
        quadData = new int[QUAD_DATA_SIZE];
    }

    @Override
    public void defaultColor(int r, int g, int b, int a)
    {
    }

    @Override
    public void unsetDefaultColor()
    {
    }

    public void setTintIndex(int tintIndex)
    {
        this.tintIndex = tintIndex;
    }

    public void setDirection(Direction direction)
    {
        this.direction = direction;
    }

    public void setSprite(TextureAtlasSprite sprite)
    {
        this.sprite = sprite;
    }

    public void setShade(boolean shade)
    {
        this.shade = shade;
    }

    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion)
    {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    public static class Buffered extends QuadBakingVertexConsumer
    {
        private final BakedQuad[] output;

        public Buffered()
        {
            this(new BakedQuad[1]);
        }

        private Buffered(BakedQuad[] output)
        {
            super(q -> output[0] = q);
            this.output = output;
        }

        public BakedQuad getQuad()
        {
            var quad = Preconditions.checkNotNull(output[0], "No quad has been emitted. Vertices in buffer: " + vertexIndex);
            output[0] = null;
            return quad;
        }
    }
}
