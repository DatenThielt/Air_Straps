# Air_Straps 1.12.2 → NeoForge 1.21.1: Quick Reference Guide

## Files to Delete
- [ ] `ModelRegistryHandler.java` - Model registration is now automatic
- [ ] `src/main/resources/mcmod.info` - Replaced by mods.toml

## Files to Create
- [ ] `src/main/resources/META-INF/mods.toml` - New mod metadata format
- [ ] `src/main/resources/assets/airstraps/lang/en_us.json` - Language translations

## Files to Update
1. **build.gradle** - Complete rewrite for NeoForge + Java 17+
2. **AirStraps.java** - Remove @Mod annotation, add DeferredRegister
3. **RegistryHandler.java** - Replace RegistryEvent with DeferredRegister
4. **StrapItems.java** - Update to use RegistryObject wrappers
5. **ItemBasicStrap.java** - Update constructor, onItemRightClick() → use()
6. **Loot.java** - Update LootPool/LootEntry API or switch to Datagen
7. **renderHighlights.java** - GlStateManager → RenderSystem
8. **pack.mcmeta** - Update pack_format from 3 to 15
9. **basic_strap.json** - Update texture path from items/ to item/

## Critical Code Changes

### Before: AirStraps.java
```java
@Mod(modid = AirStraps.MODID, name = AirStraps.NAME, version = AirStraps.VERSION)
public class AirStraps {
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) { ... }
}
```

### After: AirStraps.java
```java
public class AirStraps {
    public static final String MODID = "airstraps";
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> BASIC_STRAP = 
        ITEMS.register("basic_strap", () -> new ItemBasicStrap(new Item.Properties()));
}
```

### Before: Item Constructor
```java
public ItemBasicStrap(String unlocalizedName, String registryName, float blockRange, int durability) {
    setNoRepair();
    setMaxStackSize(1);
    setMaxDamage(durability);
    setRegistryName(registryName);
    setCreativeTab(CreativeTabs.MISC);
    setUnlocalizedName(AirStraps.MODID + "." + unlocalizedName);
}
```

### After: Item Constructor
```java
public ItemBasicStrap(float blockRange, int durability) {
    super(new Item.Properties()
        .durability(durability)
        .stacksTo(1));
    this.BlockRange = blockRange;
}
```

### Before: Item Right-Click
```java
@Override
public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
    RayTraceResult rayTraceResult = playerIn.rayTrace(BlockRange, 1.0F);
    // ... logic
    stack.damageItem(20, playerIn);
    return super.onItemRightClick(worldIn, playerIn, handIn);
}
```

### After: Item Right-Click
```java
@Override
public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    HitResult rayTraceResult = player.pick(BlockRange, 1.0F, false);
    // ... logic
    stack.hurtAndBreak(20, player, (p) -> {});
    return InteractionResultHolder.success(player.getItemInHand(hand));
}
```

### Before: Event Handler
```java
@Mod.EventBusSubscriber(Side.CLIENT)
public class ModelRegistryHandler {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) { ... }
}
```

### After: Event Handler
```java
@Mod.EventBusSubscriber(modid = AirStraps.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EventHandler {
    @SubscribeEvent
    public static void onEvent(SomeEvent event) { ... }
}
```

### Before: Rendering
```java
GlStateManager.enableBlend();
GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, ...);
GlStateManager.glLineWidth(3.0F);
GlStateManager.disableTexture2D();
RenderGlobal.drawSelectionBoundingBox(...);
EntityPlayerSP player = Minecraft.getMinecraft().player;
World world = player.getEntityWorld();
```

### After: Rendering
```java
RenderSystem.enableBlend();
RenderSystem.defaultBlendFunc();
RenderSystem.lineWidth(3.0F);
RenderSystem.disableTexture();
// Custom rendering with PoseStack
LocalPlayer player = Minecraft.getInstance().player;
Level world = player.level;
```

## Method Name Changes (Complete List)

| Old | New |
|-----|-----|
| `ItemStack.damageItem()` | `ItemStack.hurtAndBreak()` |
| `Player.inventory` | `Player.containerMenu` |
| `World.setBlockState()` | `Level.setBlock()` |
| `EntityPlayer` | `Player` |
| `EntityPlayerSP` | `LocalPlayer` |
| `EntityLivingBase` | `LivingEntity` |
| `IBlockState` | `BlockState` |
| `onItemRightClick()` | `use()` or `useOn()` |
| `player.rayTrace()` | `player.pick()` |
| `RayTraceResult` | `HitResult` |
| `ActionResult<T>` | `InteractionResultHolder<T>` |
| `ActionResultType` | `InteractionResult` |
| `EnumHand` | `InteractionHand` |
| `player.getEntityWorld()` | `player.level` |
| `GlStateManager.*` | `RenderSystem.*` |
| `RenderGlobal.draw*` | Custom rendering / LevelRenderer |

## Environment Requirements

- **Java Version:** 17+ (was 1.8)
- **Gradle Version:** 8.x (was 2.14)
- **Minecraft:** 1.21.1
- **NeoForge:** 51.0.0+ for 1.21.1
- **ForgeGradle:** 6.x+ (was 2.3)

## New Files Required

### mods.toml
```toml
modLoader="javafxmod"
loaderVersion="[45,)"

[[mods]]
modId="airstraps"
version="${file.jarVersion}"
displayName="Air Straps"
description="Place blocks in the air with Air Straps!"
authors="Your Name"

[[dependencies.airstraps]]
modId="minecraft"
mandatory=true
versionRange="[1.21.1,1.22)"
side="BOTH"
```

### en_us.json (Language)
```json
{
    "item.airstraps.itemStrap": "Basic Strap",
    "item.airstraps.itemStrapIron": "Iron Strap",
    "item.airstraps.itemStrapGold": "Gold Strap",
    "item.airstraps.itemStrapDiamond": "Diamond Strap",
    "item.airstraps.itemStrapInvincible": "Invincible Strap"
}
```

## Testing Checklist

After upgrade, verify:
- [ ] Project compiles without errors
- [ ] Mod loads in game launcher
- [ ] Items appear in creative menu
- [ ] Item can be used (right-click)
- [ ] Block placement works
- [ ] Damage/durability system works
- [ ] Loot drops in Nether Bridge chests
- [ ] Rendering/highlights display correctly
- [ ] No crash logs on game load

## Common Errors & Solutions

| Error | Solution |
|-------|----------|
| `Cannot find symbol: class ModelRegistryEvent` | Delete ModelRegistryHandler, use JSON models only |
| `Cannot find symbol: RegistryEvent` | Use DeferredRegister instead |
| `setUnlocalizedName cannot resolve` | Use Item.Properties and JSON language files |
| `Cannot access onItemRightClick` | Replace with `use()` method |
| `World cannot be resolved` | Import `net.minecraft.world.level.Level` not `World` |
| `GlStateManager deprecated` | Use `RenderSystem` methods instead |

## References

- NeoForge Docs: https://docs.neoforged.net/
- Migration Guide: https://docs.neoforged.net/docs/1.21.x/migration/
- Datagen: https://docs.neoforged.net/docs/datagen/
- Maven Central: https://mvnrepository.com/

