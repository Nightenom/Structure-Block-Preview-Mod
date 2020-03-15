package com.ldtteam.sbpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.EditStructureScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class StructureDisplayer
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static BlockPos pos;
    private static String latestValidStructure = "";
    private static RenderBuffers renderBuffers;
    private static PlacementSettings placementSettings;

    private static String toDataAbsolutePath(final ResourceLocation rl)
    {
        return "/data/" + rl.getNamespace() + "/" + rl.getPath();
    }

    public static void onStructureBlockUpdate(StructureMode newMode)
    {
        if (Minecraft.getInstance().currentScreen instanceof EditStructureScreen)
        {
            final EditStructureScreen screen = (EditStructureScreen) Minecraft.getInstance().currentScreen;

            if (newMode == null)
            {
                newMode = screen.tileStructure.getMode();
            }

            if (newMode == StructureMode.LOAD)
            {
                final Template template;
                final String structurePath;
                pos = new BlockPos(Integer.parseInt(screen.posXEdit.getText()),
                    Integer.parseInt(screen.posYEdit.getText()),
                    Integer.parseInt(screen.posZEdit.getText())).add(screen.tileStructure.getPos());
                final PlacementSettings newPlacementSettings = (new PlacementSettings()).setMirror(screen.tileStructure.getMirror())
                    .setRotation(screen.tileStructure.getRotation())
                    .setIgnoreEntities(screen.tileStructure.ignoresEntities())
                    .setChunk(null);

                try
                {
                    ResourceLocation structureRL = new ResourceLocation(screen.nameEdit.getText());
                    structureRL = new ResourceLocation(structureRL.getNamespace(), "structures/" + structureRL.getPath() + ".nbt");
                    structurePath = toDataAbsolutePath(structureRL);
                }
                catch (final ResourceLocationException e)
                {
                    // unuseable name, dont care about this
                    return;
                }
                try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(structurePath))
                {
                    if (is == null)
                    {
                        return;
                    }

                    if (false && structurePath.equals(latestValidStructure) && placementSettings != null
                        && newPlacementSettings.getRotation() == placementSettings.getRotation()
                        && newPlacementSettings.getMirror() == placementSettings.getMirror()
                        && newPlacementSettings.getIgnoreEntities() == placementSettings.getIgnoreEntities())
                    {
                        return;
                    }
                    latestValidStructure = structurePath;
                    placementSettings = newPlacementSettings;

                    final CompoundNBT nbt = CompressedStreamTools.readCompressed(is);

                    if (!nbt.contains("DataVersion", 99))
                    {
                        nbt.putInt("DataVersion", 500);
                    }
                    template = new Template();
                    template.read(NBTUtil
                        .update(Minecraft.getInstance().getDataFixer(), DefaultTypeReferences.STRUCTURE, nbt, nbt.getInt("DataVersion")));

                    compileTemplate(template);
                }
                catch (final IOException e)
                {
                    // cannot open file
                    return;
                }
            }
            else
            {
                FakeWorld.INSTANCE.setBlocks(null);
                renderBuffers = null;
            }
        }
    }

    private static void compileTemplate(final Template template)
    {
        final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        final Random random = new Random();
        final MatrixStack matrixStack = new MatrixStack();
        final List<BlockInfo> blocks = Template
            .processBlockInfos(template, null, BlockPos.ZERO, placementSettings, placementSettings.func_227459_a_(template.blocks, pos));

        renderBuffers = new RenderBuffers();
        FakeWorld.INSTANCE.setBlocks(blocks);
        for (final RenderType renderType : RenderType.getBlockRenderTypes())
        {
            final IVertexBuilder buffer = renderBuffers.getBuffer(renderType);
            for (final BlockInfo blockInfo : blocks)
            {
                try
                {
                    final BlockState state = blockInfo.state.mirror(placementSettings.getMirror()).rotate(placementSettings.getRotation());
                    final BlockPos blockPos = blockInfo.pos;
                    final IFluidState fluidState = state.getFluidState();

                    matrixStack.push();
                    matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                    if (!fluidState.isEmpty() && RenderTypeLookup.canRenderInLayer(fluidState, renderType))
                    {
                        blockRendererDispatcher.renderFluid(blockPos, FakeWorld.INSTANCE, buffer, fluidState);
                    }

                    if (state.getRenderType() != BlockRenderType.INVISIBLE && RenderTypeLookup.canRenderInLayer(state, renderType))
                    {
                        blockRendererDispatcher
                            .renderModel(state, blockPos, FakeWorld.INSTANCE, matrixStack, buffer, true, random, EmptyModelData.INSTANCE);
                    }

                    matrixStack.pop();
                }
                catch (final ReportedException e)
                {
                    LOGGER.error("Error while trying to render template part: " + e.getMessage(), e.getCause());
                }
            }
        }

        if (!placementSettings.getIgnoreEntities())
        {
            Template.processEntityInfos(template, null, pos, placementSettings, template.entities).forEach(entityInfo -> {
                final ListNBT listnbt = new ListNBT();
                listnbt.add(DoubleNBT.valueOf(entityInfo.pos.x));
                listnbt.add(DoubleNBT.valueOf(entityInfo.pos.y));
                listnbt.add(DoubleNBT.valueOf(entityInfo.pos.z));
                entityInfo.nbt.put("Pos", listnbt);
                entityInfo.nbt.remove("UUIDMost");
                entityInfo.nbt.remove("UUIDLeast");
                final Entity entity = EntityType.func_220335_a(entityInfo.nbt, null, Function.identity());
                float yaw = entity.getMirroredYaw(placementSettings.getMirror());
                yaw += (entity.rotationYaw - entity.getRotatedYaw(placementSettings.getRotation()));
                entity.setLocationAndAngles(entityInfo.pos.x, entityInfo.pos.y, entityInfo.pos.z, yaw, entity.rotationPitch);
                Minecraft.getInstance()
                    .getRenderManager()
                    .renderEntityStatic(entity,
                        entity.getPosX(),
                        entity.getPosY(),
                        entity.getPosZ(),
                        MathHelper.lerp(0.0f, entity.prevRotationYaw, entity.rotationYaw),
                        0,
                        matrixStack,
                        renderBuffers,
                        200);
            });
        }

        blocks.forEach(bi -> {
            if (bi.nbt != null)
            {
                bi.nbt.putInt("x", bi.pos.getX());
                bi.nbt.putInt("y", bi.pos.getY());
                bi.nbt.putInt("z", bi.pos.getZ());
                final TileEntity tileEntity = TileEntity.create(bi.nbt);
                tileEntity.mirror(placementSettings.getMirror());
                tileEntity.rotate(placementSettings.getRotation());
                tileEntity.setWorldAndPos(FakeWorld.INSTANCE, tileEntity.getPos());
                final BlockPos tePos = tileEntity.getPos();
                matrixStack.push();
                matrixStack.translate(tePos.getX(), tePos.getY(), tePos.getZ());
                final TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
                if (renderer != null)
                {
                    renderer.render(tileEntity, 0.0f, matrixStack, renderBuffers, 15728880, OverlayTexture.NO_OVERLAY);
                }
                matrixStack.pop();
            }
        });

        renderBuffers.finish();
        renderBuffers.sortUsingWorldOrder();
    }

    @SubscribeEvent
    public static void onWorldRenderLast(final RenderWorldLastEvent event)
    {
        if (renderBuffers == null)
        {
            return;
        }

        final MatrixStack matrixStack = event.getMatrixStack();
        final Vec3d camera = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();

        RenderUtils.saveVanillaState();
        matrixStack.push();
        matrixStack.translate(pos.getX() - camera.getX(), pos.getY() - camera.getY(), pos.getZ() - camera.getZ());
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(matrixStack.getLast().getMatrix());
        renderBuffers.render();
        RenderSystem.popMatrix();
        matrixStack.pop();
        RenderUtils.loadVanillaState();
    }
}
