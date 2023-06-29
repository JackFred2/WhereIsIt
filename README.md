# WhereIsIt
Minecraft mod to locate items in nearby inventories.

Planned features in the rewrite:

- Better EMI, JEI, REI support
- More search options (i.e. fluids)
- Extensibility for a Chest Tracker rewrite

## API

An API is available under the `api` packages for the following purposes:

### Client

- `SearchRequestPopulator.EVENT` - Obtaining the correct details for a search from a given screen
- `SearchInvoker.EVENT` - Methods to look for - asking the server or a local cache

### Server

- `Criteria`