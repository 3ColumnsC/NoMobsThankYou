# NoMobsThankYou

Prevents unwanted mobs from spawning in your Minecraft world. Configure via JSON files.

> OP needed for commands.

## Features

- **4 presets**: `disableAllMobs`, `onlyVillagers`, `disableVillagers`, `disableBosses`
- **Override lists**: add or keep specific mobs with wildcard support (`minecraft:*`, `modid:*`)
- **Runtime reload**: change config without restarting the server

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
  "onlyVillagers": false,
  "disableVillagers": false,
  "disableBosses": false
}
```

| Preset | Blocks                                                                               |
|---|--------------------------------------------------------------------------------------|
| `disableAllMobs` | All mobs (hostile, passive, ambient, water) + villagers and iron/snow/copper golems. |
| `onlyVillagers` | Same as disableAllMobs, but villagers and wandering traders are preserved.           |
| `disableVillagers` | Villagers and wandering traders only.                                                |
| `disableBosses` | Wither, Ender Dragon, and Elder Guardian.                                            |

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
