# Air_Straps: Minecraft Forge 1.12.2 → NeoForge 1.21.1 Upgrade Analysis

## Executive Summary
This analysis identifies **9 major areas** requiring significant refactoring to upgrade from Minecraft Forge 1.12.2 to NeoForge 1.21.1. The upgrade is substantial due to fundamental API changes in registration, event handling, item behavior, and resource loading.

---

## 1. MAIN MOD CLASS & ANNOTATIONS

### Current Code (1.12.2)
**File:** `AirStraps.java`
```java
@Mod(modid = AirStraps.MODID, name = AirStraps.NAME, version = AirStraps.VERSION)
public class AirStraps {
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) { ... }
    
    @EventHandler
    public void init(FMLInitializationEvent event) { ... }
}
```

### Breaking Changes
| Aspect | 1.12.2 Forge | NeoForge 1.21.1 | Impact |
|--------|-------------|-----------------|---------|
| **@Mod Annotation** | Class-level mod declaration | Should move to mods.toml | BREAKING |
| **@EventHandler** | FML lifecycle handlers | Replaced with different system | BREAKING |
| **Lifecycle Events** | FMLPreInitializationEvent, FMLInitializationEvent | ModLoadingContext/FMLModSetupEvent | BREAKING |
| **Event Bus** | Managed by Forge | Managed by NeoForge ModBus | BREAKING |
| **Logger Initialization** | event.getModLog() | ModLoadingContext.get().getActiveContainer() | BREAKING |

### Required Changes
1. Remove `@Mod` class annotation entirely
2. Create/update `src/main/resources/META-INF/mods.toml` file
3. Replace `@EventHandler` with `@Mod.EventBusSubscriber` or use ModBus directly
4. Replace `FMLPreInitializationEvent`/`FMLInitializationEvent` with:
   - `FMLModSetupEvent`
   - `FMLClientSetupEvent`
   - Or event-driven registration via DeferredRegister
5. Update logger initialization to use `ModLoadingContext`

### New Pattern (1.21.1)
```java
// AirStraps.java - Main class becomes simple or event-driven
public class AirStraps {
    public static final String MODID = "airstraps";
    // Static block with DeferredRegister for items
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
}

// Create separate handler class
@Mod.EventBusSubscriber(modid = AirStraps.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    @SubscribeEvent
    public static void onModSetup(FMLModSetupEvent event) { ... }
}
```

---

## 2. ITEM REGISTRATION SYSTEM

### Current Code (1.12.2)
**File:** `RegistryHandler.java`
```java
@EventBusSubscriber
public class RegistryHandler {
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        final Item[] items = {
            new ItemBasicStrap("itemStrap", "basic_strap", 2.0F, 65),
            // ... more items
        };
        event.getRegistry().registerAll(items);
    }
}
```

### Breaking Changes
| Aspect | 1.12.2 | NeoForge 1.21.1 | Impact |
|--------|--------|-----------------|---------|
| **RegistryEvent.Register** | Primary registration method | DEPRECATED/REMOVED | BREAKING |
| **event.getRegistry().registerAll()** | Batch registration | No longer available | BREAKING |
| **Item Constructor** | Simple: `new ItemBasicStrap(...)` | Complex builder pattern | BREAKING |
| **Registry Pattern** | Event-driven dynamic | DeferredRegister preferred | BREAKING |
| **setRegistryName()** | Called in constructor | Built into Item properties | BREAKING |
| **@EventBusSubscriber** | Works for item registration | Context changed (MOD vs FORGE bus) | BREAKING |

### Required Changes
1. Replace `RegistryEvent.Register<Item>` with `DeferredRegister<Item>`
2. Use supplier pattern for item registration
3. Item constructor becomes much more complex with properties
4. Remove individual `setRegistryName()` calls
5. Use Item.Properties builder pattern

### New Pattern (1.21.1)
```java
public class AirStraps {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    
    public static final RegistryObject<Item> BASIC_STRAP = ITEMS.register(
        "basic_strap", 
        () -> new ItemBasicStrap(new Item.Properties())
    );
    // Additional items with RegistryObject wrappers
}

// In main class constructor or static initializer:
bus.register(AirStraps.ITEMS);
```

### StrapItems.java Refactoring
**Current (1.12.2):**
```java
@ObjectHolder(AirStraps.MODID)
public class StrapItems {
    public static final Item BASIC_STRAP = null; // Filled by registry
}
```

**New (1.21.1):**
```java
// ObjectHolder still works, but DeferredRegister.RegistryObject is preferred
// OR directly access from registry:
public class StrapItems {
    public static Item BASIC_STRAP() {
        return ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(AirStraps.MODID, "basic_strap")
        );
    }
}
```

---

## 3. EVENT HANDLING

### Current Code (1.12.2)
**File:** `ModelRegistryHandler.java`, `Loot.java`, `renderHighlights.java`
```java
@Mod.EventBusSubscriber(Side.CLIENT)
public class ModelRegistryHandler {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) { ... }
}

@Mod.EventBusSubscriber(modid = AirStraps.MODID)
public class Loot {
    @SubscribeEvent
    public static void onLootTableLoad(@Nonnull LootTableLoadEvent evt) { ... }
}
```

### Breaking Changes
| Aspect | 1.12.2 | NeoForge 1.21.1 | Impact |
|--------|--------|-----------------|---------|
| **@Mod.EventBusSubscriber(Side.CLIENT)** | Marks client-side events | Use `Bus.MOD` or `Bus.FORGE` | BREAKING |
| **ModelRegistryEvent** | Fired for model registration | NO LONGER EXISTS | BREAKING |
| **@SubscribeEvent** | Basic event subscription | Works but needs Bus specification | MODERATE |
| **Event Parameters** | Direct access | May require getSource() calls | MODERATE |
| **LootTableLoadEvent** | Direct mutation API | Changed to builder pattern | BREAKING |

### Required Changes
1. Replace `@Mod.EventBusSubscriber(Side.CLIENT)` with `@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD, value = Dist.CLIENT)`
2. **ModelRegistryEvent is removed** - must use JSON model system instead
3. Specify event bus explicitly: `bus = Mod.EventBusSubscriber.Bus.FORGE` or `Bus.MOD`
4. Update LootTableLoadEvent handling to new API
5. Event method signatures may need adjustment for new event object structures

### New Pattern (1.21.1)
```java
@Mod.EventBusSubscriber(modid = AirStraps.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent evt) { ... }
}

@Mod.EventBusSubscriber(modid = AirStraps.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    // Note: ModelRegistryEvent REMOVED - no replacement needed
    // Models loaded automatically from JSON
}
```

---

## 4. ITEM IMPLEMENTATION (Block Placement Logic)

### Current Code (1.12.2)
**File:** `ItemBasicStrap.java`
```java
public ItemBasicStrap(String unlocalizedName, String registryName, float blockRange, int durability) {
    setNoRepair();
    setMaxStackSize(1);
    setMaxDamage(durability);
    setRegistryName(registryName);
    setCreativeTab(CreativeTabs.MISC);
    setUnlocalizedName(AirStraps.MODID + "." + unlocalizedName);
}

@Override
public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
    RayTraceResult rayTraceResult = playerIn.rayTrace(BlockRange, 1.0F);
    // Block placement logic...
}
```

### Breaking Changes
| Aspect | 1.12.2 | NeoForge 1.21.1 | Impact |
|--------|--------|-----------------|---------|
| **Constructor** | Simple with setters | Complex with Item.Properties builder | BREAKING |
| **setMaxDamage()** | Direct setter | Item.Properties.durability(n) | BREAKING |
| **setMaxStackSize()** | Direct setter | Item.Properties builder | BREAKING |
| **setCreativeTab()** | Direct setter | CreativeModeTabs (datadriven) | BREAKING |
| **setUnlocalizedName()** | Direct setter | Translation key via properties | BREAKING |
| **onItemRightClick()** | Main callback | Changed to `use()` | BREAKING |
| **RayTraceResult rayTrace()** | Player method | Moved to HitResult, different signature | BREAKING |
| **EnumHand** | Enum | Still exists but in different package | MINOR |
| **EntityPlayer** | Main player class | Now `Player` | MODERATE |
| **World** | Direct usage | Mostly same but with changes | MINOR |
| **IBlockState** | Primary state class | BlockState (similar but namespace changed) | MINOR |
| **getBlockState()** | World method | Same API | MINOR |
| **damageItem()** | Direct method | `hurtAndBreak()` | BREAKING |
| **inventory methods** | Player.inventory | Player.containerMenu | BREAKING |

### Required Changes
1. Convert constructor to use `Item.Properties` builder:
   - `new Item.Properties().durability(durability).stacksTo(1)`
2. Replace `setCreativeTab(CreativeTabs.MISC)` with tab registration
3. Replace `onItemRightClick()` with `use(UseOnContext)` or `use(Level, Player, InteractionHand)`
4. Update method signature returns (`ActionResultType` → `InteractionResult`)
5. Update `rayTrace()` call - now uses HitResult and different parameters
6. Replace `damageItem()` with `hurtAndBreak()`
7. Update player inventory methods: `playerIn.inventory` → `playerIn.containerMenu` / `playerIn.getInventory()`
8. Update entity/player class references and method names

### New Pattern (1.21.1)
```java
public ItemBasicStrap(float blockRange, int durability) {
    super(new Item.Properties()
        .durability(durability)
        .stacksTo(1)
    );
    this.BlockRange = blockRange;
}

@Override
public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide) {
        HitResult rayTraceResult = player.pick(BlockRange, 1.0F, false);
        BlockState blockHit = level.getBlockState(rayTraceResult.getBlockPos());
        ItemStack stack = player.getItemInHand(hand);
        
        // Block placement logic adjusted for new APIs...
        level.setBlock(rayTraceResult.getBlockPos(), selectedBlockState, 3);
        stack.hurtAndBreak(20, player, (p) -> {});
    }
    return InteractionResultHolder.success(player.getItemInHand(hand));
}
```

### Specific Method/Class Changes
```
Old → New
------
ItemStack.damageItem(amount, entity) → ItemStack.hurtAndBreak(amount, entity, callback)
Player.inventory → Player.containerMenu or getInventory()
World.setBlockState() → Level.setBlock() (World → Level)
EntityPlayer → Player
EntityLivingBase → LivingEntity
IBlockState → BlockState
onItemRightClick() → use() or useOn()
EnumHand → InteractionHand
ActionResult<ItemStack> → InteractionResultHolder<ItemStack>
ActionResultType → InteractionResult
EntityPlayerSP → LocalPlayer
RayTraceResult → HitResult (and API changes)
rayTraceResult.getBlockPos() → Still getBlockPos()
Block.FULL_BLOCK_AABB → Block.BLOCK_RENDERING_DISPATCH_HANDLER or use Shapes API
```

---

## 5. RESOURCE FILES & METADATA

### Current Files (1.12.2)
1. **mcmod.info** - JSON format for mod metadata
2. **pack.mcmeta** - Resource pack format descriptor

### Breaking Changes
| File | 1.12.2 Format | NeoForge 1.21.1 Format | Impact |
|------|---------------|------------------------|---------|
| **mcmod.info** | `[{...}]` JSON array | REMOVED | BREAKING |
| **Mod Metadata** | In mcmod.info | In `mods.toml` (TOML format) | BREAKING |
| **pack.mcmeta** | `pack_format: 3` | `pack_format: 15` (1.21.x) | BREAKING |
| **Texture Paths** | `airstraps:items/...` | Same but validation stricter | MINOR |
| **Model References** | Standard JSON | Same format but stricter validation | MINOR |

### Required Changes

#### Remove `mcmod.info`
- File can be deleted entirely

#### Create/Update `mods.toml`
**New file:** `src/main/resources/META-INF/mods.toml`
```toml
modLoader="javafxmod"
loaderVersion="[45,)"
license="[License file contents or URL]"

[[mods]]
modId="airstraps"
version="${file.jarVersion}"
displayName="Air Straps"
description="Place blocks in the air with Air Straps!"
displayURL=""
logoFile=""
credits="Original Author"
authors="Your Name"

[[dependencies.airstraps]]
    modId="forge"
    mandatory=true
    versionRange="[45,)"
    ordering="NONE"
    side="BOTH"

[[dependencies.airstraps]]
    modId="minecraft"
    mandatory=true
    versionRange="[1.21.1,1.22)"
    ordering="NONE"
    side="BOTH"
```

#### Update `pack.mcmeta`
```json
{
    "pack": {
        "description": "Air Straps resources",
        "pack_format": 15
    }
}
```

### Language Files
- 1.12.2: `en_US.lang` (flat file)
- 1.21.1: `en_us.json` (JSON file under `assets/airstraps/lang/`)

**New file:** `src/main/resources/assets/airstraps/lang/en_us.json`
```json
{
    "item.airstraps.itemStrap": "Basic Strap",
    "item.airstraps.itemStrapIron": "Iron Strap",
    "item.airstraps.itemStrapGold": "Gold Strap",
    "item.airstraps.itemStrapDiamond": "Diamond Strap",
    "item.airstraps.itemStrapInvincible": "Invincible Strap"
}
```

---

## 6. MODEL REGISTRATION SYSTEM

### Current Code (1.12.2)
**File:** `ModelRegistryHandler.java`
```java
@Mod.EventBusSubscriber(Side.CLIENT)
public class ModelRegistryHandler {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerModel(StrapItems.BASIC_STRAP);
    }
    
    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(
            item, 0, 
            new ModelResourceLocation(item.getRegistryName(), "inventory")
        );
    }
}
```

### Breaking Changes
| Aspect | 1.12.2 | NeoForge 1.21.1 | Impact |
|--------|--------|-----------------|---------|
| **ModelRegistryEvent** | Fired to register models | NO LONGER EXISTS | BREAKING |
| **ModelLoader.setCustomModelResourceLocation()** | Primary registration | REMOVED/DEPRECATED | BREAKING |
| **Model Registration** | Programmatic via event | Automatic from JSON only | BREAKING |
| **ModelResourceLocation** | Used for registration | May still exist but unused | BREAKING |

### Required Changes
1. **Delete entire ModelRegistryHandler class** - no longer needed
2. Ensure `basic_strap.json` exists at correct path with correct parent
3. All model registration is now automatic from JSON files

### JSON Model System (Automatic)
**File:** `src/main/resources/assets/airstraps/models/item/basic_strap.json`
```json
{
    "parent": "item/generated",
    "textures": {
        "layer0": "airstraps:item/basic_strap"
    }
}
```

**Texture File Path:**
`src/main/resources/assets/airstraps/textures/item/basic_strap.png`
(Note: Changed from `items/` to `item/`)

### Migration Steps
1. Update JSON model textures reference path from `airstraps:items/basic_strap` to `airstraps:item/basic_strap`
2. Move or rename texture from `textures/items/` to `textures/item/`
3. Delete `ModelRegistryHandler.java`
4. Delete model registration code from main class

---

## 7. LOOT TABLE SYSTEM

### Current Code (1.12.2)
**File:** `Loot.java`
```java
@Mod.EventBusSubscriber(modid = AirStraps.MODID)
public class Loot {
    @SubscribeEvent
    public static void onLootTableLoad(@Nonnull LootTableLoadEvent evt) {
        LootPool lp = new LootPool(
            new LootEntry[0], NO_CONDITIONS, 
            new RandomValueRange(1, 3), 
            new RandomValueRange(0, 0), 
            AirStraps.NAME
        );
        LootEntry basicStrap = new LootEntryItem(
            StrapItems.BASIC_STRAP, 20, 0, 
            new LootFunction[0], 
            new LootCondition[0], 
            "airstraps:basic_strap"
        );
        lp.addEntry(basicStrap);
        evt.getTable().addPool(lp);
    }
}
```

### Breaking Changes
| Aspect | 1.12.2 | NeoForge 1.21.1 | Impact |
|--------|--------|-----------------|---------|
| **LootPool Constructor** | Direct instantiation with arrays | Builder pattern required | BREAKING |
| **LootEntry/LootEntryItem** | Direct classes | Replaced with LootItem, etc. | BREAKING |
| **LootTableLoadEvent** | Allows runtime modification | Still exists but different API | MODERATE |
| **RandomValueRange** | Constructor parameter | Integrated into builders | BREAKING |
| **LootCondition[] arrays** | Direct initialization | Builder pattern | BREAKING |
| **Loot Table JSON** | Still used but deprecated in favor of datagen | Datagen preferred | MODERATE |

### Required Changes

#### Option A: Keep Event-Based (Simpler but Deprecated)
```java
@Mod.EventBusSubscriber(modid = AirStraps.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Loot {
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent evt) {
        if (evt.getName().equals(new ResourceLocation("minecraft", "chests/nether_bridge"))) {
            LootPool pool = LootPool.lootPool()
                .add(LootItem.lootTableItem(StrapItems.BASIC_STRAP.get())
                    .setWeight(20))
                .setRolls(IntProviders.constant(1))
                .build();
            evt.getTable().addPool(pool);
        }
    }
}
```

#### Option B: Use Datagen (Recommended, Modern)
Create loot table JSON files:
`src/main/resources/data/minecraft/loot_tables/chests/nether_bridge.json`

```json
{
    "pools": [
        {
            "rolls": 1,
            "entries": [
                {
                    "type": "minecraft:item",
                    "weight": 20,
                    "name": "airstraps:basic_strap"
                }
            ]
        }
    ]
}
```

**Recommended:** Use Datagen approach for 1.21.1
- Create `src/main/java/.../datagen/LootTableProvider.java`
- Use LootTableProvider to generate loot tables at build time
- Delete runtime Loot.java event handler

### API Changes for Event-Based Approach
```
Old → New
------
LootPool(LootEntry[], ...) → LootPool.lootPool().add(...).build()
new LootEntryItem(...) → LootItem.lootTableItem(item)
RandomValueRange(min, max) → IntProviders.constant() or IntProviders.uniform()
LootCondition[] → Builder pattern
evt.getTable().addPool() → Still same but different implementation
```

---

## 8. RENDERING & CLIENT UTILITIES

### Current Code (1.12.2)
**File:** `renderHighlights.java`
```java
@Mod.EventBusSubscriber(modid = AirStraps.MODID)
public class renderHighlights {
    @SubscribeEvent
    static void onRender(RenderWorldLastEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        World world = player.getEntityWorld();
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(...);
        GlStateManager.glLineWidth(3.0F);
        GlStateManager.disableTexture2D();
        // ... rendering code
    }
}
```

### Breaking Changes
| Aspect | 1.12.2 | NeoForge 1.21.1 | Impact |
|--------|--------|-----------------|---------|
| **EntityPlayerSP** | Client player class | Renamed to `LocalPlayer` | BREAKING |
| **Minecraft.getMinecraft().player** | Player access | Same but may be null | MINOR |
| **player.getEntityWorld()** | World getter | Renamed to `level` property | BREAKING |
| **GlStateManager** | Minecraft GL wrapper | MOSTLY DEPRECATED in favor of RenderSystem | BREAKING |
| **GlStateManager methods** | Various GL operations | Some removed/moved to RenderSystem | BREAKING |
| **RenderGlobal.drawSelectionBoundingBox()** | Static rendering method | Removed, use DebugRenderer or custom rendering | BREAKING |
| **Player positioning methods** | lastTickPosX, lastTickPosY, etc. | Different system with getEyePosition() | BREAKING |
| **Block.FULL_BLOCK_AABB** | Direct static field | Now use Shapes API or Block shape methods | BREAKING |

### Required Changes

1. Replace class/field names:
   - `EntityPlayerSP` → `LocalPlayer`
   - `player.getEntityWorld()` → `player.level`
   - `RenderGlobal` → Handle differently (no direct replacement)

2. Replace GlStateManager calls with RenderSystem:
   ```
   GlStateManager.enableBlend() → RenderSystem.enableBlend()
   GlStateManager.disableTexture2D() → RenderSystem.disableTexture()
   GlStateManager.depthMask() → RenderSystem.depthMask()
   GlStateManager.glLineWidth() → RenderSystem.lineWidth()
   ```

3. Replace drawing methods:
   - `RenderGlobal.drawSelectionBoundingBox()` removed
   - Use `LevelRenderer.renderVoxelShape()` or custom rendering with `PoseStack`

4. Update player position interpolation:
   ```
   Old: player.lastTickPosX + (player.posX - player.lastTickPosX) * ticks
   New: player.getEyePosition(partialTick) - simplified vector approach
   ```

### New Pattern (1.21.1)
```java
@Mod.EventBusSubscriber(modid = AirStraps.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderHighlights {
    @SubscribeEvent
    static void onRender(RenderLevelLastEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        Level world = player.level;
        if (world == null) return;
        
        PoseStack poseStack = event.getPoseStack();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(3.0F);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        
        // Custom rendering with PoseStack instead of deprecated methods
        
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
```

---

## 9. BUILD CONFIGURATION & GRADLE

### Current Configuration (1.12.2)
**File:** `build.gradle`
```gradle
buildscript {
    repositories {
        jcenter()
        maven { url = "http://files.minecraftforge.net/maven" }
    }
    dependencies {
        classpath 'net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT'
    }
}
apply plugin: 'net.minecraftforge.gradle.forge'

minecraft {
    version = "1.12.2-14.23.5.2768"
    mappings = "snapshot_20171003"
}
```

### Breaking Changes
| Component | 1.12.2 | NeoForge 1.21.1 | Impact |
|-----------|--------|-----------------|---------|
| **ForgeGradle Version** | 2.3 | 6.x or later | BREAKING |
| **Gradle Version** | 2.14 (via wrapper) | 8.x recommended | BREAKING |
| **Java Version** | 1.8 | 17 minimum, 21 recommended | BREAKING |
| **Repository URLs** | jcenter(), files.minecraftforge.net | Maven Central, etc. | BREAKING |
| **Minecraft Version** | 1.12.2-14.23.5.2768 | 1.21.1 | BREAKING |
| **Mappings System** | MCP snapshots | Parchment or similar | BREAKING |
| **Plugin Name** | net.minecraftforge.gradle.forge | net.neoforged.gradle.userdev | BREAKING |
| **Deobfuscation** | Integrated | Via decompiler tasks | MODERATE |
| **Publication** | Manual JAR copying | Maven publication setup | MODERATE |

### Required Changes

#### 1. Update build.gradle
```gradle
buildscript {
    repositories {
        maven { url = 'https://maven.neoforged.net/releases' }
        mavenCentral()
    }
    dependencies {
        classpath 'net.neoforged.gradle:fg[9,11)' // NeoForge Gradle
    }
}

plugins {
    id 'java'
    id 'net.neoforged.gradle.userdev' version '7.0.X' // Latest stable
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

minecraft {
    version = "1.21.1-51.0.0" // NeoForge version for 1.21.1
}

repositories {
    mavenCentral()
    maven { url = 'https://maven.neoforged.net/releases' }
}

dependencies {
    minecraft 'net.neoforged:forge:1.21.1-51.0.0'
    
    // For Parchment mappings:
    mappings loom.layered() {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${mcVersion}:${parchmentVersion}@zip")
    }
}
```

#### 2. Update gradle.properties
```properties
org.gradle.jvmargs=-Xmx4G
org.gradle.java.home=C:/path/to/java17/or/later
```

#### 3. Update gradle wrapper
```bash
./gradlew wrapper --gradle-version 8.8 --distribution-type all
```

#### 4. Update settings.gradle
```gradle
plugins {
    id 'org.gradle.toolchains.llvm-compiler' version '0.1'
}

rootProject.name = 'Air_Straps'
```

### Manifest Changes
**New file:** `src/main/java/META-INF/MANIFEST.MF` (if needed)
```
Manifest-Version: 1.0
Implementation-Title: ${mod_id}
Implementation-Version: ${mod_version}
Implementation-Vendor: Your Name
```

---

## SUMMARY TABLE: API MIGRATION

| Feature | 1.12.2 Class/Method | NeoForge 1.21.1 Class/Method | File/Priority |
|---------|-------------------|---------------------------|----------------|
| **Mod Main Class** | @Mod class annotation | @Mod in mods.toml | HIGH |
| **Lifecycle Events** | @EventHandler + FMLInitEvent | @SubscribeEvent + FMLModSetupEvent | HIGH |
| **Item Registration** | RegistryEvent.Register | DeferredRegister + RegistryObject | HIGH |
| **Item Properties** | Direct setters | Item.Properties builder | HIGH |
| **Item Use** | onItemRightClick() | use(UseOnContext) | HIGH |
| **Model Registration** | ModelRegistryEvent + ModelLoader | Automatic JSON only | HIGH |
| **Loot Tables** | LootTableLoadEvent + direct mutation | Datagen + JSON or new event API | MEDIUM |
| **Event Bus** | @EventBusSubscriber | Requires bus= parameter | MEDIUM |
| **Player Class** | EntityPlayer | Player | MEDIUM |
| **Client Player** | EntityPlayerSP | LocalPlayer | MEDIUM |
| **World Class** | World | Level | MEDIUM |
| **Block State** | IBlockState | BlockState | MINOR |
| **Ray Tracing** | player.rayTrace() | player.pick() | MEDIUM |
| **GL Rendering** | GlStateManager | RenderSystem | MEDIUM |
| **Inventory** | player.inventory | player.containerMenu | MEDIUM |
| **Item Damage** | damageItem() | hurtAndBreak() | LOW |
| **Registry Name** | item.getRegistryName() | item.builtInRegistryHolder().key() | LOW |
| **Language Files** | en_US.lang | en_us.json | LOW |
| **Pack Format** | 3 | 15 | LOW |

---

## IMPLEMENTATION PRIORITY & COMPLEXITY

### Phase 1: Critical (Must Do First)
1. Update build.gradle and gradle wrapper → Enables compilation
2. Create mods.toml and remove mcmod.info → Mod loads
3. Update main mod class with DeferredRegister → Items register
4. Update item registration system → Items appear
5. Fix item constructor and properties → No crash on startup

**Estimated Effort:** 2-4 hours

### Phase 2: Functional (Makes Mod Work)
6. Update ItemBasicStrap methods (use() instead of onItemRightClick) → Item is usable
7. Update block placement logic (new API calls) → Feature works
8. Update Loot event handler → Drops in loot
9. Update rendering (GlStateManager → RenderSystem) → Visuals work

**Estimated Effort:** 3-5 hours

### Phase 3: Polish (Optional, Recommended)
10. Delete ModelRegistryHandler → Clean code
11. Update language files to JSON → Proper translations
12. Implement Datagen for loot tables → Future-proof
13. Update all event bus annotations → Modern patterns
14. Update pack.mcmeta format version → Latest standard

**Estimated Effort:** 2-3 hours

### Total Estimated Effort: 7-12 hours for experienced developer

---

## CRITICAL NOTES

1. **Java Version Required:** NeoForge 1.21.1 requires Java 17+. Update IDE and build environment.

2. **IDE Setup:** May need to reimport project and clear Gradle cache:
   ```bash
   rm -rf .gradle
   ./gradlew clean build
   ```

3. **Breaking API Change Severity:**
   - Item registration: Very difficult (completely different system)
   - Mod initialization: Moderate (event system changed)
   - Item usage: Moderate (method signatures changed)
   - Rendering: Complex (GlStateManager deprecated)
   - Loot tables: Moderate (API changed but easier with modern system)

4. **Backward Compatibility:** Zero backward compatibility. Must update all code.

5. **Testing Required:**
   - Verify items register and appear in creative menu
   - Test item functionality (strap placement)
   - Verify loot drops
   - Test rendering (highlight display)
   - Cross-platform testing (Windows/Mac/Linux)

6. **Documentation Resources:**
   - NeoForge Migration Guide: https://docs.neoforged.net/
   - 1.21.1 Changes: https://docs.neoforged.net/docs/1.21.x/migration/
   - Datagen Guide: https://docs.neoforged.net/docs/datagen/

