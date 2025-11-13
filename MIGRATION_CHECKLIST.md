# Air_Straps Migration Checklist: 1.12.2 → NeoForge 1.21.1

## PHASE 1: PROJECT SETUP & BUILD CONFIGURATION

### Step 1: Update Java Version
- [ ] Install Java 17 or newer
- [ ] Update IDE to Java 17+
- [ ] Update system JAVA_HOME environment variable
- [ ] Verify: `java -version` shows 17+

### Step 2: Update Gradle Configuration
**File:** `build.gradle`
- [ ] Replace entire buildscript section with NeoForge repos
- [ ] Change ForgeGradle from 2.3-SNAPSHOT to version 6.x+
- [ ] Replace `apply plugin: 'net.minecraftforge.gradle.forge'` with `plugins { ... }`
- [ ] Update minecraft version to 1.21.1
- [ ] Change mappings system (add Parchment or use MojangMappings)
- [ ] Update Java sourceCompatibility/targetCompatibility to 17
- [ ] Update repository URLs to NeoForge Maven repos
- [ ] Test compilation: `./gradlew clean build`

### Step 3: Update Gradle Wrapper
- [ ] Update gradle-wrapper.jar to Gradle 8.x
- [ ] Update gradle-wrapper.properties to gradle-8.8 (or similar)
- [ ] Clear Gradle cache: `rm -rf .gradle`
- [ ] Test: `./gradlew --version` should show Gradle 8.x

### Step 4: Verify Project Setup
- [ ] Re-import project in IDE
- [ ] Clear IDE caches (Eclipse/IntelliJ)
- [ ] Run `./gradlew genEclipseRuns` or equivalent
- [ ] Project should have no IDE errors

---

## PHASE 2: MOD METADATA & RESOURCES

### Step 5: Create mods.toml
**New file:** `src/main/resources/META-INF/mods.toml`
- [ ] Create META-INF directory
- [ ] Create mods.toml with mod metadata
- [ ] Set correct modId: "airstraps"
- [ ] Set correct displayName: "Air Straps"
- [ ] Add NeoForge dependency
- [ ] Add Minecraft dependency for 1.21.1

### Step 6: Remove Old Metadata
**File:** `src/main/resources/mcmod.info`
- [ ] Delete mcmod.info entirely

### Step 7: Update pack.mcmeta
**File:** `src/main/resources/pack.mcmeta`
- [ ] Change pack_format from 3 to 15
- [ ] Update description if needed
- [ ] Verify JSON is valid

### Step 8: Create Language File
**New file:** `src/main/resources/assets/airstraps/lang/en_us.json`
- [ ] Create lang directory structure
- [ ] Add item translation keys for all items
- [ ] Format: "item.airstraps.itemStrap": "Basic Strap"

### Step 9: Fix Texture Paths
**File:** `src/main/resources/assets/airstraps/models/item/basic_strap.json`
- [ ] Update texture layer0 path from `airstraps:items/basic_strap` to `airstraps:item/basic_strap`
- [ ] Ensure texture PNG exists at correct path
- [ ] Verify: `src/main/resources/assets/airstraps/textures/item/basic_strap.png`

---

## PHASE 3: MAIN MOD CLASS

### Step 10: Refactor AirStraps.java
**File:** `src/main/java/com/daten/AirStraps/AirStraps.java`
- [ ] Remove @Mod class annotation entirely
- [ ] Remove @EventHandler methods (preInit, init)
- [ ] Keep MODID, NAME, VERSION constants
- [ ] Add DeferredRegister<Item> ITEMS field
- [ ] Add RegistryObject for each item (BASIC_STRAP, etc.)
- [ ] Create separate event handler class if needed

```java
public class AirStraps {
    public static final String MODID = "airstraps";
    public static final String NAME = "Air Straps";
    public static final String VERSION = "1.0";
    
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    
    public static final RegistryObject<Item> BASIC_STRAP = 
        ITEMS.register("basic_strap", () -> new ItemBasicStrap(...));
    // More items...
}
```

### Step 11: Add Event Handlers (if needed)
**New or Modified:** `EventHandler.java` or similar
- [ ] Create @Mod.EventBusSubscriber class if lifecycle events needed
- [ ] Use FMLModSetupEvent instead of FMLPreInitializationEvent
- [ ] Use FMLClientSetupEvent for client-side setup
- [ ] Specify bus parameter: `bus = Mod.EventBusSubscriber.Bus.MOD`

---

## PHASE 4: ITEM REGISTRATION

### Step 12: Update RegistryHandler.java
**File:** `src/main/java/com/daten/AirStraps/init/RegistryHandler.java`
- [ ] DELETE ENTIRE FILE (no longer needed with DeferredRegister)

OR (if keeping for other registries):
- [ ] Replace @EventBusSubscriber with @Mod.EventBusSubscriber
- [ ] Add `bus = Mod.EventBusSubscriber.Bus.MOD` parameter
- [ ] Replace RegistryEvent.Register<Item> pattern with DeferredRegister

### Step 13: Update StrapItems.java
**File:** `src/main/java/com/daten/AirStraps/init/StrapItems.java`
- [ ] Delete or refactor class
- [ ] Option A: Reference directly from AirStraps.BASIC_STRAP
- [ ] Option B: Keep @ObjectHolder but note it's now filled by DeferredRegister
- [ ] Remove any setRegistryName calls

---

## PHASE 5: ITEM IMPLEMENTATION

### Step 14: Update ItemBasicStrap Constructor
**File:** `src/main/java/com/daten/AirStraps/items/ItemBasicStrap.java`
- [ ] Change constructor signature (remove unlocalizedName parameter)
- [ ] Replace all setter calls with Item.Properties builder
- [ ] Remove: `setNoRepair()` (not available or replaced)
- [ ] Replace: `setMaxStackSize(1)` → `.stacksTo(1)` in Properties
- [ ] Replace: `setMaxDamage(durability)` → `.durability(durability)` in Properties
- [ ] Replace: `setRegistryName()` (handled by DeferredRegister)
- [ ] Replace: `setCreativeTab()` (handled via tabs or properties)
- [ ] Replace: `setUnlocalizedName()` → use JSON language files
- [ ] Store BlockRange as instance field

```java
public ItemBasicStrap(float blockRange, int durability) {
    super(new Item.Properties()
        .durability(durability)
        .stacksTo(1));
    this.BlockRange = blockRange;
}
```

### Step 15: Update Item Right-Click Method
**File:** `src/main/java/com/daten/AirStraps/items/ItemBasicStrap.java`
- [ ] Rename method: `onItemRightClick()` → `use()`
- [ ] Change parameters: `(World, EntityPlayer, EnumHand)` → `(Level, Player, InteractionHand)`
- [ ] Change return type: `ActionResult<ItemStack>` → `InteractionResultHolder<ItemStack>`
- [ ] Replace `worldIn` with `level`
- [ ] Replace `playerIn` with `player`
- [ ] Update all method calls within the function

### Step 16: Update Ray Tracing in ItemBasicStrap
**File:** `src/main/java/com/daten/AirStraps/items/ItemBasicStrap.java`
- [ ] Replace: `playerIn.rayTrace(BlockRange, 1.0F)` → `player.pick(BlockRange, 1.0F, false)`
- [ ] Ensure HitResult import is correct: `net.minecraft.world.phys.HitResult`
- [ ] Update result type references

### Step 17: Update World/Block State API in ItemBasicStrap
**File:** `src/main/java/com/daten/AirStraps/items/ItemBasicStrap.java`
- [ ] Replace `World` import with `net.minecraft.world.level.Level`
- [ ] Keep IBlockState → BlockState references the same (mostly compatible)
- [ ] Update: `worldIn.getBlockState()` → `level.getBlockState()`
- [ ] Update: `worldIn.setBlockState()` → `level.setBlock()`

### Step 18: Update Item Damage/Inventory in ItemBasicStrap
**File:** `src/main/java/com/daten/AirStraps/items/ItemBasicStrap.java`
- [ ] Replace: `stack.damageItem(20, playerIn)` → `stack.hurtAndBreak(20, player, p -> {})`
- [ ] Replace: `playerIn.inventory` → `player.containerMenu` or `player.getInventory()`
- [ ] Update inventory method calls:
  - [ ] `findSlotMatchingUnusedItem()` → check if method exists, may need refactor
  - [ ] `decrStackSize()` → check new API

### Step 19: Update Entity Imports in ItemBasicStrap
**File:** `src/main/java/com/daten/AirStraps/items/ItemBasicStrap.java`
- [ ] Replace: `EntityPlayer` → `Player` (net.minecraft.world.entity.player.Player)
- [ ] Replace: `EntityLivingBase` → `LivingEntity`
- [ ] Replace: `CreativeTabs.MISC` → New creative tab system (check properties or use fallback)

### Step 20: Compile Check ItemBasicStrap
- [ ] Run `./gradlew build` and verify no compilation errors in ItemBasicStrap
- [ ] Fix any remaining import errors

---

## PHASE 6: RENDERING & EVENTS

### Step 21: Delete ModelRegistryHandler
**File:** `src/main/java/com/daten/AirStraps/init/ModelRegistryHandler.java`
- [ ] DELETE ENTIRE FILE (model registration is now automatic from JSON)

### Step 22: Update renderHighlights.java
**File:** `src/main/java/com/daten/AirStraps/world/renderHighlights.java`
- [ ] Update @Mod.EventBusSubscriber annotation:
  - [ ] Change `Side.CLIENT` → `value = Dist.CLIENT`
  - [ ] Add `modid = AirStraps.MODID`
  - [ ] Add `bus = Mod.EventBusSubscriber.Bus.FORGE`
- [ ] Update event: `RenderWorldLastEvent` → `RenderLevelLastEvent`
- [ ] Replace class imports:
  - [ ] `EntityPlayerSP` → `LocalPlayer`
  - [ ] Add import: `net.minecraft.client.player.LocalPlayer`
- [ ] Replace player accessor:
  - [ ] `Minecraft.getMinecraft().player` → `Minecraft.getInstance().player`
- [ ] Replace world accessor:
  - [ ] `player.getEntityWorld()` → `player.level`
- [ ] Replace GlStateManager calls with RenderSystem:
  - [ ] `GlStateManager.enableBlend()` → `RenderSystem.enableBlend()`
  - [ ] `GlStateManager.tryBlendFuncSeparate()` → `RenderSystem.defaultBlendFunc()`
  - [ ] `GlStateManager.glLineWidth()` → `RenderSystem.lineWidth()`
  - [ ] `GlStateManager.disableTexture2D()` → `RenderSystem.disableTexture()`
  - [ ] `GlStateManager.depthMask()` → `RenderSystem.depthMask()`
- [ ] Replace drawing call:
  - [ ] `RenderGlobal.drawSelectionBoundingBox()` → implement custom rendering with `PoseStack` or `LevelRenderer.renderVoxelShape()`
- [ ] Add `PoseStack poseStack = event.getPoseStack()` if needed
- [ ] Add null checks for player and level

### Step 23: Update Loot.java
**File:** `src/main/java/com/daten/AirStraps/Loot.java`
- [ ] Update @Mod.EventBusSubscriber:
  - [ ] Add `bus = Mod.EventBusSubscriber.Bus.FORGE`
- [ ] Update LootTableLoadEvent method signature if needed
- [ ] Replace LootPool constructor call:
  - [ ] OLD: `new LootPool(new LootEntry[0], ...)`
  - [ ] NEW: `LootPool.lootPool().add(...).build()`
- [ ] Replace LootEntryItem with LootItem.lootTableItem()
- [ ] Replace RandomValueRange with IntProviders
- [ ] Update method calls (addEntry, addPool, etc.)

OR (Recommended):
- [ ] Delete Loot.java entirely
- [ ] Create datagen LootTableProvider instead
- [ ] Use JSON-based loot table definition

### Step 24: Compile Check All Classes
- [ ] Run `./gradlew build`
- [ ] Fix all compilation errors
- [ ] Check for warnings and address them

---

## PHASE 7: TESTING

### Step 25: Run in Development Environment
- [ ] Start game from IDE (run configuration should exist)
- [ ] Check console for errors/warnings
- [ ] Verify mod loads (check logs for "Air Straps")

### Step 26: Creative Mode Testing
- [ ] Check creative inventory for items
- [ ] Verify all strap items appear (Basic, Iron, Gold, Diamond, Invincible)
- [ ] Check item names display correctly
- [ ] Verify item textures are visible

### Step 27: Item Functionality Testing
- [ ] Right-click to select a block (should show chat message)
- [ ] Right-click in air to place selected block
- [ ] Verify block is placed correctly
- [ ] Check item durability decreases
- [ ] Test all durability levels (basic=65, iron=128, etc.)

### Step 28: Rendering Testing
- [ ] Hold strap item and look at block
- [ ] Verify highlight box renders correctly
- [ ] Check rendering doesn't cause crashes
- [ ] Verify rendering works in different lighting

### Step 29: Loot Testing
- [ ] Travel to Nether
- [ ] Find Nether Bridge chests
- [ ] Check if Basic Strap drops in loot (should appear ~20/3 rolls)
- [ ] Verify no loot table errors in console

### Step 30: Performance Testing
- [ ] Run game for several minutes
- [ ] Monitor for crashes or lag
- [ ] Check CPU/memory usage is normal
- [ ] Look for any warning messages in logs

---

## FINAL CLEANUP

### Step 31: Remove Deprecated Code
- [ ] Remove any unused imports
- [ ] Remove @Deprecated method references
- [ ] Clean up commented-out code
- [ ] Review code style consistency

### Step 32: Documentation
- [ ] Update mod version if needed
- [ ] Update CHANGELOG
- [ ] Document any breaking changes
- [ ] Update README with NeoForge 1.21.1 info

### Step 33: Verification
- [ ] Final build: `./gradlew clean build`
- [ ] Generate JAR: `./gradlew jar`
- [ ] Test JAR loading in Minecraft
- [ ] Verify mod features work as expected

---

## ROLLBACK REFERENCE

If something goes wrong, you can rollback:

1. Undo last git commit: `git reset --hard HEAD~1`
2. Clear Gradle cache: `rm -rf .gradle`
3. Reimport project in IDE
4. Run: `./gradlew clean build`

Or restore from backup/previous branch.

---

## ESTIMATED TIME

- **Phase 1 (Setup):** 30 min
- **Phase 2 (Resources):** 20 min
- **Phase 3-4 (Mod Class):** 30 min
- **Phase 5 (Items):** 45 min
- **Phase 6 (Rendering):** 45 min
- **Phase 7 (Testing):** 30 min

**Total:** ~3.5-4 hours for methodical execution

---

## SUCCESS CRITERIA

All of the following should be true:

- Project builds without errors: ✓
- Mod loads without crashes: ✓
- Items appear in creative menu: ✓
- All items are usable: ✓
- Block placement works: ✓
- Durability system works: ✓
- Rendering/highlights visible: ✓
- Loot drops work: ✓
- No console errors/warnings: ✓
- Performance is acceptable: ✓

