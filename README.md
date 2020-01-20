## Canny Composter

Minecraft mod: Compost more stuff!  Turn excess organic material and rotten food into nutrients for plants.

Use excess organic material, excess food, or [rotten food](https://github.com/Stormwind99/FoodFunk/wiki/Rotten-food) from [Food Funk](https://github.com/Stormwind99/FoodFunk) to generate compost - which enhances plant growth like bonemeal.

1.14.4:
* Improves Minecraft's composter!  Makes many more organic items compostable by default.
* Composter makes compost ([configurable](https://github.com/Stormwind99/CannyComposter/blob/1.14.4/other/configuration/cannycomposter-common.toml#L25), for example can be reverted to "minecraft:bonemeal")
   * Compost is weaker than bonemeal, working only 50% of the time ([configurable](https://github.com/Stormwind99/CannyComposter/blob/1.14.4/other/configuration/cannycomposter-common.toml#L11))
* Composter takes more time to generate compost ([configurable](https://github.com/Stormwind99/CannyComposter/blob/1.14.4/other/configuration/cannycomposter-common.toml#L13))
* Since composting generates heat, small puffs of steam will occasionally appear ([configurable](https://github.com/Stormwind99/CannyComposter/blob/1.14.4/other/configuration/cannycomposter-common.toml#L22))
* Make more items compostable in the composter by adding their ids or tags [to the configuration file](https://github.com/Stormwind99/CannyComposter/blob/1.14.4/other/configuration/cannycomposter-common.toml#L27) or to [compostable_level tags](https://github.com/Stormwind99/CannyComposter/tree/1.14.4/src/main/resources/data/forge/tags/items)
   * Composting values are 0 to 100.  The default Minecraft composting values are 30, 50, 65, 85, and 100.
   * To override default configuration and make an item not compostable, [set the value to 0](https://github.com/Stormwind99/CannyComposter/blob/1.14.4/other/configuration/cannycomposter-common.toml#L30).
   * To make your mod's items compostable, add the items to the [compostable_level1 - compostable_level5 tags](https://github.com/Stormwind99/CannyComposter/tree/1.14.4/src/main/resources/data/forge/tags/items) (which correspond to the default composting levels) in your mod.

1.12.2:
* Make any item decompose in the compost bin over a specified time (via [configuration](https://github.com/Stormwind99/CannyComposter/wiki/Configuration)).
* Default [configuration](https://github.com/Stormwind99/CannyComposter/wiki/Configuration) entries support vanilla Minecraft and [other mods](https://github.com/Stormwind99/CannyComposter/wiki/Compatibility). 
* Any item from any mod can be [specified](https://github.com/Stormwind99/CannyComposter/wiki/Configuration), and specifiers include support for metadata and ore dictionary names.

Requires [Wumple Util Library](https://github.com/Stormwind99/WumpleUtil).

## Screenshots

![Steaming and empty compost bins](https://github.com/Stormwind99/CannyComposter/raw/master/other/screenshots/screenshot-0.png)

## Credits

 * This mod began as a port to MC 1.12 of the compost bin and compost item from the old [Garden Collection](https://github.com/jaquadro/GardenCollection) mod by Justin Aquadro.
