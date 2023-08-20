# Where Is It

![Modrinth Downloads](https://img.shields.io/modrinth/dt/FCTyEqkn?style=flat-square&label=Modrinth&color=%2316AF54)
![CurseForge Downloads](https://img.shields.io/curseforge/dt/378036?style=flat-square&label=CurseForge&color=%23E04E14)

Minecraft mod to locate items in nearby inventories. Press Y over an item to search.

![An example image of the mod showing 4 results](https://i.imgur.com/lvebo0v.png)

## Requirements

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YACL](https://modrinth.com/mod/yacl)

Also embeds [JackFredLib](https://github.com/JackFred2/JackFredLib).

## Features

- Not required - vanilla clients can join without restricting mod users.
- Detailed searching - search by item ID, name, item tag, fluid contents, enchantment, potions, and more, and any
  combination of the previous.
- Server-side functionality - clients without the mod can use the command `/whereis` (default, changeable) to use the
  mod.
- Support for recipe viewers, including the vanilla Recipe Book, and for JEI, REI, EMI:
    - Ability to search for items containing specific fluids, and tags for recipes.
    - Custom searches (search for enchantments using enchanted books, and potion effects using potions).
    - Smart favourite handling - add a named Shulker Box in your favourites, and search for it regardless of the
      contents.

## Usage

Press **Y** (by default) to search by item ID.

### Inventory

Hold shift to also search for NBT data.

### Recipes (Recipe Book / JEI / REI / EMI)

Will search for tags in the recipe if applicable. If hovered over a fluid, will search for items containing said fluid.

### Item Overlays (JEI / REI / EMI)

Normally matches the inventory, however certain items contain different behavior:

- Enchanted books will search for any items with that enchantment. Holding shift will specify the level as well.
- Potions will search for the potion effect on any item. Holding shift will match the specific item.

### Item Favourites/Bookmarks (JEI / REI / EMI)

Will try to look for the specific item by looking at name, enchantments or potion effects.

## API

An API is available under the `api` packages for the following purposes:

### Client

- `OverlayStackBehavior.EVENT` - Build a custom search request from an overlay stack if pressed; used for example to
  look for enchantments on any item by using enchanted books, or potion effects from potions for example.
- `SearchInvoker.EVENT` - Client-sided search initiators; could be asking the server or looking through a local cache.
- `SearchRequestPopulator.EVENT` - Obtaining the correct criteria for a search from a given screen.
- `ShouldIgnoreKey.EVENT` - Check to see if the keybind should be ignored in that instant - this is used to cancel if
  a search bar is focused, for example.
- `RenderUtils` - Information on the currently displayed overlay, as well as scheduling text rendering.

### Common

- `NestedItemStackSearcher.EVENT` - Testing against sub-items, such as for backpacks or other containers.
- `BlockSearcher.EVENT` - By default, Where Is It uses the Fabric Transfer API to get all stacks in a block. You can use
  this event to override this for custom behavior/handling.
- `ConnectedBlocksGrabber` - Used to detect which blocks are connected with each-other, such as chests or storage
  drawers.
- `criteria.Criterion` - Class to extend if making new criteria; create a new `Criterion.Type<T>` to go along with it
  and register using `Criterion#register`.