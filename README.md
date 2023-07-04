# WhereIsIt

Minecraft mod to locate items in nearby inventories. Press Y over an item to search.

## Features

- Support for recipe viewers, including JEI, REI, EMI and the vanilla Recipe Book.
- Ability to search for fluids and tags for recipes.
- Custom searches using recipe viewers (search for enchantments using enchanted books, and potion effects using potions).
- Smart favourite handling -  add a named Shulker Box in your favourites, and search for it regardless of the contents.
- Not required - vanilla clients can join without restricting mod users.

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

- `SearchRequestPopulator.EVENT` - Obtaining the correct criteria for a search from a given screen.
- `OverlayStackBehavior.EVENT` - Custom behavior when searching a stack from an overlay; used to
  search purely for enchantments or potions, for example.
- `SearchInvoker.EVENT` - Client-sided search initiators; could be asking the server or looking through a local cache.
- `ShouldIgnoreKey.EVENT` - Check to see if the keybind should be ignored in that instant - this is used to cancel if
  a search bar is focused, for example.

### Common

- `NestedItemStackSearcher.EVENT` - Testing against sub-items, such as for backpacks or other containers.
- `criteria.Criterion` - Class to extend if making new criteria; create a new `Criterion.Type<T>` to go along with it
  and register using `Criterion#register`.