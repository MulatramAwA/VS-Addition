# Change Log

## Common

- Fixed the issue where the raycasting wrapper incorrectly applied multiple times to TaCZ's explosion, causing the game to freeze.
- Refactored the portable storage interface compatibility to no longer rely on contraptions.
- Added vanilla lodestone compass compatibility.
- Fixed the issue of crashing with VMod in some cases.
- Fixed the issue where other players were kicked out when using the sticker from the Create mod or the gravitron from the Clockwork mod.
- Re-enabled explosion mixin, but it will still be overwritten by Lithium mixin.
- Corrected the direction of projectiles fired when the entity is on a ship. (vanilla and TaCZ only) [#66](https://github.com/xiewuzhiying/VS-Addition/issues/66)

## Fabric

## Forge

- Fixed the issue of incompatibility with versions of Embeddium prior to 0.3.12.
- Fixed the issue of crashing with Tallyho (again😥).
- Added config item to re-enable Embeddium block entity culling in shipyard chunks, resolve [#68](https://github.com/xiewuzhiying/VS-Addition/issues/68)