# WhereIsIt
Minecraft mod to locate items in nearby inventories. Press Y over an item to search.

## Features

- Support for recipe viewers, including JEI, REI, EMI and the vanilla Recipe Book.
- Ability to search for fluids and tags in the case of recipe viewers.
- Custom searches using recipe viewers (enchanted books to look for the enchantments themselves, similar for potions).
- Not required - vanilla clients can join without restricting mod users.


## API

An API is available under the `api` packages for the following purposes:

### Client

- `SearchRequestPopulator.EVENT` - Obtaining the correct criteria for a search from a given screen.
- `SearchRequestPopulator.OVERLAY_STACK_BEHAVIOR` - Custom behavior when searching a stack from an overlay; used to
   search purely for enchantments or potions, for example.
- `SearchInvoker.EVENT` - Client-sided search initiators; could be asking the server or looking through a local cache.

### Common

- `NestedItemStackSearcher.EVENT` - Testing against sub-items, such as for backpacks or other containers.