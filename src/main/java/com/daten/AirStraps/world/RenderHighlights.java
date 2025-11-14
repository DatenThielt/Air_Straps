package com.daten.AirStraps.world;

import com.daten.AirStraps.AirStraps;
import com.daten.AirStraps.items.IStrapItem;
import com.daten.AirStraps.items.ItemBasicStrap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = AirStraps.MODID, value = Dist.CLIENT)
public class RenderHighlights
{
    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        Level level = player.level();

        if (player.getMainHandItem().getItem() instanceof IStrapItem)
        {
            ItemBasicStrap strapItem = (ItemBasicStrap) player.getMainHandItem().getItem();
            BlockHitResult hitResult = (BlockHitResult) player.pick(strapItem.blockRange, event.getPartialTick().getGameTimeDeltaTicks(), false);

            if (hitResult != null)
            {
                BlockPos pos = hitResult.getBlockPos();
                VoxelShape shape = level.getBlockState(pos).getShape(level, pos);

                if (!shape.isEmpty())
                {
                    PoseStack poseStack = event.getPoseStack();
                    poseStack.pushPose();

                    // Get camera position
                    double camX = mc.gameRenderer.getMainCamera().getPosition().x;
                    double camY = mc.gameRenderer.getMainCamera().getPosition().y;
                    double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

                    // Translate to block position relative to camera
                    poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);

                    // Get the bounding box from the shape
                    AABB aabb = shape.bounds();

                    // Render the outline using renderLineBox
                    VertexConsumer vertexConsumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
                    LevelRenderer.renderLineBox(
                        poseStack,
                        vertexConsumer,
                        aabb.minX, aabb.minY, aabb.minZ,
                        aabb.maxX, aabb.maxY, aabb.maxZ,
                        1.0F, 1.0F, 1.0F, 0.4F
                    );

                    poseStack.popPose();
                }
            }
        }
    }
}
