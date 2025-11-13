package com.daten.AirStraps.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ItemBasicStrap extends Item implements IStrapItem
{
    public float blockRange = 2.0F;
    private Block selectedBlock;
    private BlockState selectedBlockState;
    private ItemStack selectedItemStack = ItemStack.EMPTY;

    public ItemBasicStrap(float blockRange, int durability)
    {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(durability > 0 ? durability : 0)
        );
        this.blockRange = blockRange;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        // Perform ray trace
        BlockHitResult rayTraceResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        BlockPos hitPos = rayTraceResult.getBlockPos();
        BlockState blockHit = level.getBlockState(hitPos);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        // Check for shift right click (sneaking)
        if (player.isCrouching())
        {
            // If we shift-click a block we want to use that as the selected item
            if (rayTraceResult.getType() == HitResult.Type.BLOCK)
            {
                // Set the block as the block user wants to use
                selectedBlock = blockHit.getBlock();

                // Store the block's state so we know what to place down
                selectedBlockState = blockHit;

                // Get the item stack for this block
                selectedItemStack = selectedBlock.getCloneItemStack(level, hitPos, blockHit);

                // Send the player a message telling them what kind of block they have selected
                player.sendSystemMessage(Component.literal("New AirBlock Selected: " +
                    selectedBlock.getName().getString()));
            }
        }
        else
        {
            // If we have not selected a block yet, let the user know
            if (selectedBlock == null)
            {
                player.sendSystemMessage(Component.literal(
                    "No block selected, Shift+Right click a block to set it as an AirBlock."));
                return InteractionResultHolder.pass(stack);
            }

            // Perform ray trace for air placement
            BlockHitResult airTrace = player.pick(blockRange, 1.0F, false);
            BlockPos placePos = airTrace.getBlockPos();
            BlockState stateAtPos = level.getBlockState(placePos);

            // If we have a block, make sure we are looking at air or replaceable block
            if (airTrace.getType() == HitResult.Type.MISS ||
                stateAtPos.canBeReplaced())
            {
                // Check if there are no entities at the placement position
                AABB checkBox = new AABB(placePos);
                if (level.getEntitiesOfClass(LivingEntity.class, checkBox).isEmpty())
                {
                    // Check if player has the block in inventory
                    if (hasItemInInventory(player, selectedItemStack))
                    {
                        // Place the block with the desired block state
                        level.setBlock(placePos, selectedBlockState, 3);

                        // Damage the strap (only if it has durability)
                        if (stack.isDamageableItem()) {
                            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                        }

                        // Decrease the inventory
                        if (!player.getAbilities().instabuild) {
                            removeItemFromInventory(player, selectedItemStack);
                        }

                        return InteractionResultHolder.success(stack);
                    }
                }
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    private boolean hasItemInInventory(Player player, ItemStack itemStack)
    {
        if (itemStack.isEmpty()) return false;

        for (ItemStack invStack : player.getInventory().items) {
            if (ItemStack.isSameItemSameTags(invStack, itemStack)) {
                return true;
            }
        }
        return false;
    }

    private void removeItemFromInventory(Player player, ItemStack itemStack)
    {
        if (itemStack.isEmpty()) return;

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack invStack = player.getInventory().items.get(i);
            if (ItemStack.isSameItemSameTags(invStack, itemStack)) {
                invStack.shrink(1);
                break;
            }
        }
    }

    public static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluidMode) {
        double reach = player.blockInteractionRange();
        return level.clip(new ClipContext(
                player.getEyePosition(1.0F),
                player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(reach)),
                ClipContext.Block.OUTLINE,
                fluidMode,
                player
        ));
    }
}
