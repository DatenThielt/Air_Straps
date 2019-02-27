package com.daten.AirStraps.world;

import com.daten.AirStraps.AirStraps;
import com.daten.AirStraps.items.IStrapItem;
import com.daten.AirStraps.items.ItemBasicStrap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = AirStraps.MODID)
public class renderHighlights
{

    @SubscribeEvent
    static void onRender(RenderWorldLastEvent event)
    {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            World world = player.getEntityWorld();

            if (player.getHeldItemMainhand().getItem() instanceof IStrapItem)
            {
                ItemBasicStrap myItem = (ItemBasicStrap) player.getHeldItemMainhand().getItem();
                RayTraceResult tracer = player.rayTrace(myItem.BlockRange, 1.0F);

                if(tracer != null)
                {
                    BlockPos pos = tracer.getBlockPos();

                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(3.0F);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);

                    Block state = world.getBlockState(pos).getBlock();

                    double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
                    double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
                    double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

                    RenderGlobal.drawSelectionBoundingBox(state.FULL_BLOCK_AABB.offset(-x, -y, -z).offset(pos).grow(0.002), 1.0F, 1.0F, 1.0F, 0.4F);

                    GlStateManager.depthMask(true);
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                }
            }

    }
}
