# NoMobsThankYou

Prevents unwanted mobs from spawning in your Minecraft world. Configure via JSON files.

> OP needed for commands.

---

## ⚡ Features

- **4 presets**: `disableAllMobs`, `onlyVillagers`, `disableVillagers`, `disableBosses`
- **Override lists**: add or keep specific mobs with wildcard support (`namespace:*`, `modid:*`, and global `*`)
- **Master switch**: disable all blocking with `enabled: false` without deleting your config
- **Runtime reload**: change config without restarting the server

---

## 📁 Commands

| Command | Description |
|---|---|
| `/nmt reload` | Reload config from disk |
| `/nmt status` | Show current config status |
| `/nmt clean presets` | Reset presets to defaults (auto-reload) |
| `/nmt clean list` | Clear override remove and keep lists (auto-reload) |
| `/nmt open presets` | Open presets config file |
| `/nmt open list` | Open override lists config file |

---

## ⚙️ Configuration

Two JSON files in `config/`:

### Presets file: nomobsthankyou-presets.json

> Enable **exactly one** preset at a time. If more than one is `true`, none will be applied.

```json
{
  "enabled": true,
  "disableAllMobs": false,
  "onlyVillagers": false,
  "disableVillagers": false,
  "disableBosses": false
}
```

`enabled` is optional: if omitted or `null`, it defaults to `true`. Set it to `false` to disable presets and overrides without deleting your configuration.

| Preset | Blocks                                                                               |
|---|--------------------------------------------------------------------------------------|
| `disableAllMobs` | All mobs (hostile, passive, ambient, water) + villagers and iron/snow/copper golems. |
| `onlyVillagers` | Same as disableAllMobs, but villagers and wandering traders are preserved.           |
| `disableVillagers` | Villagers and wandering traders only.                                                |
| `disableBosses` | Wither, Ender Dragon, and Elder Guardian.                                            |

> **Note:** Invalid JSON or values will reset to defaults.

### Overrides file: nomobsthankyou-overrides.json

Add or keep specific entities. Supports wildcards (`minecraft:*`, `modid:*`) and global `*`:

```json
{
  "remove": ["minecraft:creeper", "minecraft:*"],
  "keep": ["minecraft:villager", "minecraft:wandering_trader"]
}
```

Overrides are applied on top of the active preset.

> `*` in `remove` blocks all mobs while still excluding `MobCategory.MISC` entities (items, boats, etc.). Use exact ids if you want to block a specific MISC entity. `*` in `keep` allows everything.

> **Note:** Invalid JSON or values will reset to defaults.

---

## 📦 Requirements

### Fabric

* Fabric API
* (+26.X) Java 25 or newer

### NeoForge

* (+26.X) Java 25 or newer

---

## 💬 Suggestions & Issues

If you have an idea, a suggestion, or found a bug, feel free to leave a comment on the CurseForge page or open an issue on the GitHub repository.

---

## ❓ FAQ

**Can I use this mod in a public modpack?** <br>
Yes! You're free to include it in any public or private modpack, no permission needed.

---

## 📜 License

This project is licensed under the MIT License.
