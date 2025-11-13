# Air_Straps

A Minecraft Forge mod for Minecraft 1.12.2 that adds special building tools allowing players to place blocks in mid-air.

## What is Air_Straps?

Air_Straps is a quality-of-life mod that introduces "Air Straps" - magical items that let players place blocks in mid-air without needing support underneath them. Perfect for builders and terraformers who want to create structures or fill in gaps without constantly building scaffolding!

## Features

### The Strap System
The mod features 5 tiers of straps with increasing power and durability:

1. **Basic Strap**
   - Range: 2 blocks
   - Durability: 65 uses
   - Can be found in Nether Bridge chests

2. **Iron Strap**
   - Range: 4 blocks
   - Durability: 128 uses

3. **Gold Strap**
   - Range: 6 blocks
   - Durability: 256 uses

4. **Diamond Strap**
   - Range: 8 blocks
   - Durability: 512 uses

5. **Invincible Strap**
   - Range: 10 blocks
   - Durability: Infinite uses

### How to Use

- **Shift+Right-Click**: Select which block type you want to place
- **Right-Click**: Place that block in the air (consumes the block from your inventory)
- Each placement damages the strap (except the Invincible Strap)
- You can only place blocks within the strap's range
- You must have the selected block type in your inventory

## Installation

1. Install Minecraft Forge 1.12.2
2. Download the Air_Straps mod file
3. Place the mod file in your `.minecraft/mods` folder
4. Launch Minecraft with the Forge profile

---

## Development Setup

### Source installation information for modders

This code follows the Minecraft Forge installation methodology. It will apply
some small patches to the vanilla MCP source code, giving you and it access
to some of the data and functions you need to build a successful mod.

Note also that the patches are built against "unrenamed" MCP source code (aka
srgnames) - this means that you will not be able to read them directly against
normal code.

### Standalone source installation

See the Forge Documentation online for more detailed instructions:
http://mcforge.readthedocs.io/en/latest/gettingstarted/

**Step 1:** Open your command-line and browse to the folder where you extracted the zip file.

**Step 2:** Once you have a command window up in the folder that the downloaded material was placed, type:

Windows: `gradlew setupDecompWorkspace`
Linux/Mac OS: `./gradlew setupDecompWorkspace`

**Step 3:** After all that finished, you're left with a choice.

For Eclipse, run `gradlew eclipse` (./gradlew eclipse if you are on Mac/Linux)

If you prefer to use IntelliJ, steps are a little different:
1. Open IDEA, and import project.
2. Select your build.gradle file and have it import.
3. Once it's finished you must close IntelliJ and run the following command:

`gradlew genIntellijRuns` (./gradlew genIntellijRuns if you are on Mac/Linux)

**Step 4:** The final step is to open Eclipse and switch your workspace to /eclipse/ (if you use IDEA, it should automatically start on your project)

If at any point you are missing libraries in your IDE, or you've run into problems you can run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything {this does not affect your code} and then start the process again.

Should it still not work, refer to #ForgeGradle on EsperNet for more information about the gradle environment.

### Tips

- If you do not care about seeing Minecraft's source code you can replace "setupDecompWorkspace" with one of the following:
  - `setupDevWorkspace`: Will patch, deobfuscate, and gather required assets to run minecraft, but will not generate human readable source code.
  - `setupCIWorkspace`: Same as Dev but will not download any assets. This is useful in build servers as it is the fastest because it does the least work.

- When using Decomp workspace, the Minecraft source code is NOT added to your workspace in a editable way. Minecraft is treated like a normal Library. Sources are there for documentation and research purposes and usually can be accessed under the 'referenced libraries' section of your IDE.

### Forge source installation

MinecraftForge ships with this code and installs it as part of the forge
installation process, no further action is required on your part.

### Additional Resources

LexManos' Install Video: https://www.youtube.com/watch?v=8VEdtQLuLO0&feature=youtu.be

For more details update more often refer to the Forge Forums:
http://www.minecraftforge.net/forum/index.php/topic,14048.0.html

---

## Project Structure

```
Air_Straps/
├── src/main/java/com/daten/AirStraps/
│   ├── AirStraps.java              # Main mod entry point
│   ├── Loot.java                   # Loot table handler
│   ├── items/
│   │   ├── IStrapItem.java         # Strap item interface
│   │   └── ItemBasicStrap.java     # Strap item implementation
│   ├── init/
│   │   ├── RegistryHandler.java    # Item registration
│   │   ├── StrapItems.java         # Item holder
│   │   └── ModelRegistryHandler.java
│   └── world/
│       └── renderHighlights.java
└── src/main/resources/
    ├── assets/airstraps/           # Models and textures
    ├── mcmod.info                  # Mod metadata
    └── pack.mcmeta
```

## Technical Details

- **Minecraft Version:** 1.12.2
- **Forge Version:** Compatible with Minecraft Forge 1.12.2
- **Java Version:** 1.8
- **Mod ID:** airstraps
- **Version:** 1.0

## License

[Add your license information here]

## Credits

Created by Daten
