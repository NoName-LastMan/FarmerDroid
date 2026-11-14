# ⚙️ Farming Droid (Minecraft Mod)

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/ModLoader-NeoForge%2021.1-orange.svg)](https://neoforged.net/)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An autonomous, clockwork **Steampunk Farming Droid** mod built for **Minecraft 1.21.1 (NeoForge)**.  
Automate your agricultural fields with compact 1-block drones that hover, harvest, and replant your crops with industrial precision.

---

## 🌟 Key Features

- **🤖 Compact Steampunk Robot:**
  - 1-block tall entity with visual hovering animations and steam exhaust particle thrusters.
  - **Trample-Proof:** Never breaks or tramples farmland when moving across crops.
- **🏭 Depot Logistics Hub:**
  - The central command block where the Droid docks, refuels, grabs fresh tools, and unloads produce.
  - Dedicated interactive GUI featuring a Fuel Slot, Tool Buffer, Range Upgrade Slot, and 3x3 Output Storage.
- **🧠 Goal-Based Batch AI:**
  - Powered by Minecraft's native AI system (`GoalSelector`) and an optimized **Nearest-Neighbor TSP heuristic**.
  - Chain-harvests all mature crops without pauses or wandering, supporting vertical stepped/tiered farmland ($Y \pm 2$).
  - Smart 8-seed internal buffer: retains seeds for replanting and unloads surplus crops directly to storage.
- **⛏️ Realistic Tool Mechanics:**
  - Requires a Hoe (Wood to Netherite, including modded hoes via `c:tools/hoes` & `minecraft:hoes`).
  - Damages hoe durability with use; returns to dock immediately if the tool breaks.
  - Equip or swap hoes directly by Right-Clicking the Droid or by placing them into the Depot Station.
- **🔥 Traditional Steam Fuel:**
  - Powered by furnace fuels (Coal, Charcoal, Coal Blocks, Blaze Rods).
  - **Stall-On-Empty:** If fuel runs dry mid-operation, the Droid halts completely on the spot until manually refueled or retrieved.
- **📐 Expandable Working Range:**
  - **Base (Default):** 3x3 working area (Radius 1).
  - **Tier 1 (Brass Upgrade):** 5x5 working area (Radius 2).
  - **Tier 2 (Iron Upgrade):** 7x7 working area (Radius 3).
  - **Tier 3 (Steam Overclocked Upgrade):** 9x9 working area (Radius 4, matches water source limits).
- **🔧 Steampunk Wrench:**
  - Right-click Droid: Displays live status HUD (Fuel %, current Hoe durability, routine state).
  - Shift + Right-click Droid: Safely dismantles the Droid into your inventory preserving stored data.

---

## 📖 Getting Started & Guide

1. **Craft the Depot Station:** Place the Depot Station in the center of your farmland.
2. **Deploy the Droid:** Hold the Farming Droid item and **Right-Click the TOP of the Depot Station**. The Droid will snap into position, pair with the Depot, and dock.
3. **Supply Resources:** Open the Depot Station GUI (Right-click):
   - Place fuel (Coal, Charcoal, etc.) in the top-left **Fuel Slot**.
   - Place a Hoe in the middle-left **Hoe Slot**.
   - *(Optional)* Insert a Range Upgrade card in the bottom-left **Upgrade Slot**.
4. **Autonomous Routine:**
   - Every 10 seconds, the Droid scans its configured area.
   - If mature crops are found, it flies through the field, harvesting, replanting, and returns home to deposit the yield.

---

## 🛠️ Crafting Recipes

| Item / Block | Ingredients & Pattern |
| :--- | :--- |
| **Depot Station** | 6x Copper Ingot, 1x Blast Furnace, 1x Hopper, 1x Barrel |
| **Farming Droid** | 1x Brass Droid Core, 2x Piston, 1x Campfire, 1x Copper Ingot |
| **Brass Droid Core** | 4x Copper Ingot, 4x Redstone Dust, 1x Clock |
| **Steampunk Wrench** | 2x Copper Ingot, 1x Iron Ingot |
| **Brass Upgrade (5x5)** | 8x Gold Ingot, 1x Copper Ingot |
| **Iron Upgrade (7x7)** | 8x Iron Ingot, 1x Brass Upgrade Card |
| **Steam Overclocked (9x9)**| 4x Redstone Dust, 2x Diamond, 1x Iron Upgrade Card |

---

## 📦 Building from Source

To compile the mod `.jar` locally:

```bash
git clone https://github.com/NoName-LastMan/FarmerDroid.git
cd FarmerDroid
./gradlew build
```

The compiled `.jar` file will be generated in `build/libs/farmingdroid-1.0.0.jar`.

To launch a development client for testing:
```bash
./gradlew runClient
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
