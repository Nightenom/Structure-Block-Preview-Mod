package com.ldtteam.sbpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.ldtteam.sbpm.RenderUtils.BuiltBuffer;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;

public class RenderBuffers implements IRenderTypeBuffer
{
    private static final List<RenderType> worldRenderingOrder = new ArrayList<>();
    private Map<RenderType, BufferBuilder> startedBuffers = new HashMap<>(32);
    private Map<RenderType, BuiltBuffer> builtBuffers = new HashMap<>(32);
    private List<BuiltBuffer> sortedBuffers;

    static
    {
        worldRenderingOrder.add(RenderType.getSolid());
        worldRenderingOrder.add(RenderType.getCutoutMipped());
        worldRenderingOrder.add(RenderType.getCutout());
        worldRenderingOrder.add(RenderType.getEntitySolid(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        worldRenderingOrder.add(RenderType.getEntityCutout(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        worldRenderingOrder.add(RenderType.getEntityCutoutNoCull(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        worldRenderingOrder.add(RenderType.getEntitySmoothCutout(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        worldRenderingOrder.add(RenderType.getEntitySolid(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        worldRenderingOrder.add(Atlases.getSolidBlockType());
        worldRenderingOrder.add(Atlases.getCutoutBlockType());
        worldRenderingOrder.add(Atlases.getBedType());
        worldRenderingOrder.add(Atlases.getShulkerBoxType());
        worldRenderingOrder.add(Atlases.getSignType());
        worldRenderingOrder.add(Atlases.getChestType());
        worldRenderingOrder.add(Atlases.getTranslucentBlockType());
        worldRenderingOrder.add(Atlases.getBannerType());
        worldRenderingOrder.add(Atlases.getShieldType());
        worldRenderingOrder.add(RenderType.getGlint());
        worldRenderingOrder.add(RenderType.getEntityGlint());
        worldRenderingOrder.add(RenderType.getWaterMask());
        worldRenderingOrder.add(RenderType.getLines());
        worldRenderingOrder.add(RenderType.getTranslucent());
    }

    public RenderBuffers()
    {
    }

    public IVertexBuilder getBuffer(final RenderType renderType)
    {
        if (!startedBuffers.containsKey(renderType))
        {
            startedBuffers.put(renderType, RenderUtils.createAndBeginBuffer(renderType));
        }

        return startedBuffers.get(renderType);
    }

    public void finish()
    {
        for (final RenderType renderType : startedBuffers.keySet())
        {
            if (startedBuffers.containsKey(renderType))
            {
                builtBuffers.put(renderType, RenderUtils.finishBuffer(startedBuffers.get(renderType), renderType));
            }
        }
        startedBuffers = null;
    }

    public void finish(final RenderType renderType)
    {
        if (startedBuffers.containsKey(renderType))
        {
            final BufferBuilder finishingBuilder = startedBuffers.remove(renderType);
            builtBuffers.put(renderType, RenderUtils.finishBuffer(finishingBuilder, renderType));
        }
    }

    public void sortUsingWorldOrder()
    {
        startedBuffers = null;
        final List<BuiltBuffer> newSortedBuffers = new ArrayList<>(builtBuffers.size());

        for (final RenderType renderType : worldRenderingOrder)
        {
            newSortedBuffers.add(builtBuffers.remove(renderType));
        }
        newSortedBuffers.addAll(builtBuffers.values());

        builtBuffers = null;
        sortedBuffers = newSortedBuffers.stream().filter(buffer -> buffer != null).collect(Collectors.toList());
    }

    public void render()
    {
        if (sortedBuffers != null)
        {
            sortedBuffers.forEach(RenderUtils::drawBuiltBuffer);
        }
    }
}
