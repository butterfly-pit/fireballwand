# Fireball Wand

For Minecraft 1.20.1.

This plugin makes a specific blaze rod into a wand that shoots fireballs. The wand must have a custom model data
of `1000727` to be recognized as a fireball wand. The following command can be used to obtain one.

```
/minecraft:give @p blaze_rod{CustomModelData:1000727} 1
```

## Config

Configurable from the `/plugins/FireballWand/config.yml` file.

| Key                 | Type  | Default   | Description                                                      |
|---------------------|-------|-----------|------------------------------------------------------------------|
| `cooldown`          | `int` | `5000`    | The cooldown in milliseconds between shots.                      |           
| `custom-model-data` | `int` | `1000727` | The custom model data assigned to the Blaze Rod for this plugin. |

## Known Bugs/Issues

- Ready "ding" does not play after firing a fireball and switching the wand to another item.
- Multiple blaze rods do not behave the way they should.
