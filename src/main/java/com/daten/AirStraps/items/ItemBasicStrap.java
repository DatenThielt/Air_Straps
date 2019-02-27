package com.daten.AirStraps.items;

import com.daten.AirStraps.AirStraps;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

public class ItemBasicStrap extends Item implements IStrapItem
{
    public float BlockRange = 2;
    private static Logger logger;
    private Block selectedBlock;
    private IBlockState selectedBlockState;
    private ItemStack selectedItemStack;

    public ItemBasicStrap(String unlocalizedName, String registryName, float blockRange, int durability)
    {
        super();

        BlockRange = blockRange;
        setNoRepair();
        setMaxStackSize(1);
        setMaxDamage(durability);
        setRegistryName(registryName);
        setCreativeTab(CreativeTabs.MISC);
        setUnlocalizedName(AirStraps.MODID + "." + unlocalizedName);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        RayTraceResult rayTraceResult = playerIn.rayTrace(BlockRange, 1.0F);
        IBlockState blockHit = worldIn.getBlockState(rayTraceResult.getBlockPos());
        ItemStack stack = playerIn.getHeldItemMainhand();

        if (worldIn.isRemote) return super.onItemRightClick(worldIn, playerIn, handIn);

        //Check for shift right click aka sneaking click
        if(playerIn.isSneaking())
        {
            //If we shift-click a block we want to use that as the selected item.
            if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                //Set the block as the block user wants to use,
                selectedBlock = blockHit.getBlock();

                //Store the blocks state so we know what to place down
                selectedBlockState = blockHit;

                selectedItemStack = selectedBlock.getPickBlock(selectedBlockState,rayTraceResult,worldIn,rayTraceResult.getBlockPos(),playerIn);

                //send the player a message telling them what kind of block they have selected.
                if(selectedBlockState.getProperties().containsKey(BlockStone.VARIANT))
                    playerIn.sendMessage(new TextComponentString("New AirBlock Selected: "+selectedBlockState.getValue(BlockStone.VARIANT).getName()));
                else
                    playerIn.sendMessage(new TextComponentString("New AirBlock Selected: "+selectedBlockState.getBlock().getLocalizedName()));

            }
        }
        else
        {
            //If we have not selected a block yet, let the user know
            if(selectedBlock == null)
            {
                playerIn.sendMessage(new TextComponentString("No block selected, Shift+Right click a block to set it as an AirBlock."));
                return super.onItemRightClick(worldIn, playerIn, handIn);
            }

            //If we have a block,  make sure we are looking at the air, and that its not a mob.
            if (rayTraceResult.typeOfHit == RayTraceResult.Type.MISS && worldIn.getEntitiesWithinAABB(EntityLivingBase.class, Block.FULL_BLOCK_AABB.offset(rayTraceResult.getBlockPos())).isEmpty())
            {
                //If the block we are trying to replace is replaceable, and we have the block in inventory,  Place away
                if (blockHit.getBlock().isReplaceable(worldIn, rayTraceResult.getBlockPos()) && playerIn.inventory.hasItemStack(selectedItemStack))
                {
                    //Place the box with the desired block state,  Damage the Strap
                    worldIn.setBlockState(rayTraceResult.getBlockPos(), selectedBlockState);
                    stack.damageItem(20,playerIn);

                    //Decrease the inventory
                    int toFind = playerIn.inventory.findSlotMatchingUnusedItem(selectedItemStack);
                    playerIn.inventory.decrStackSize(toFind,1);
                }
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}

