# NoMobsThankYou

Prevents unwanted mobs from spawning in your Minecraft world. Configure via JSON files.

> OP needed for commands.

## Features

- **6 presets**: `disableAllMobs`, `villagersAllowed`, `disableMonsters`, `disablePassiveMobs`, `disableVillagers`, `disableBosses`
- **Override lists**: add or keep specific mobs with wildcard support (`minecraft:*`, `modid:*`)
- **Runtime reload**: change config without restarting the server
- **Lightweight**: no dependencies beyond what Minecraft already ships

## Commands

| Command | Description |
|---|---|
| `/nomobsthankyou reload` | Reload config from disk |
| `/nomobsthankyou status` | Show current config status |
| `/nomobsthankyou clean presets` | Reset presets to defaults (auto-reload) |
| `/nomobsthankyou clean list` | Clear override remove and keep lists (auto-reload) |
| `/nomobsthankyou open presets` | Open presets config file |
| `/nomobsthankyou open list` | Open override lists config file |

## Configuration

Two JSON files in `config/`:

### Presets file: nomobsthankyou-presets.json

> Enable **exactly one** preset at a time. If more than one is `true`, none will be applied.

```json
{
  "disableAllMobs": false,
  "villagersAllowed": false,
  "disableMonsters": false,
  "disablePassiveMobs": false,
  "disableVillagers": false,
  "disableBosses": false
}
```

| Preset | Blocks |
|---|---|
| `disableAllMobs` | Every living entity (hostile + passive + ambient + water mobs). |
| `villagersAllowed` | Same as disableAllMobs, but villagers and wandering traders are preserved. |
| `disableMonsters` | All hostile mobs: zombies, skeletons, creepers, spiders, etc. (MobCategory.MONSTER). |
| `disablePassiveMobs` | Passive animals: cows, pigs, sheep, chickens, rabbits, foxes, bees, squid, fish, axolotls, etc. Wandering traders are preserved. |
| `disableVillagers` | Villagers and wandering traders only. |
| `disableBosses` | Wither, Ender Dragon, and Elder Guardian. |

> **Note:** Invalid JSON or values will reset to defaults and show a warning in chat.

### Overrides file: nomobsthankyou-overrides.json

Add or keep specific entities. Supports wildcards (`minecraft:*`, `modid:*`):

```json
{
  "remove": ["minecraft:creeper", "minecraft:zombie"],
  "keep": ["minecraft:villager", "minecraft:wandering_trader"]
}
```

Overrides are applied on top of the active preset.

> **Note:** Invalid JSON or values will reset to defaults and show a warning in chat.

---

## 📦 Requirements

### Fabric

* Fabric API
* (+26.X) Java 25 or newer

### NeoForge

* (+26.X) Java 25 or newer

---

## License

This project is licensed under the MIT License.
