# Change Log
## Common
- Improved the implementation of sticker mixin (no functional changes)
- Added TaCZ Projectile explosion compatibility.
- When the player grabs the ship with Gravitron, cancel the collision between the ship and the player.
- Added VMod schematic compatibility (for fake air pocket attachment)
- Added Translation keys for command messages.
- Added a Create display source for any non-source ship-bound blocks to display ship data.
- Now Shipyard chunks will get the biome in world coordinates.
- Added `getDirection()` function for Cannon Mount Generic Peripheral. [#57](https://github.com/xiewuzhiying/VS-Addition/issues/57)
- Ship velocity will affect cannon's projectile.
- Vic's Point Blank explosion compatibility.
- Enabling Embeddium block entity culling for Valkyrien Skies.
- Disabled certain features to temporarily resolve conflicts with Fabric ASM.
- Fixed Create Big Cannons autocannon would not play a firing sound when it was on a ship. [#62](https://github.com/xiewuzhiying/VS-Addition/issues/62)
- Fixed crash with CC: Tweaked 1.114.0 [#61](https://github.com/xiewuzhiying/VS-Addition/issues/61)
- Added config item for maximum tilt angle at which an entity can stand on a ship.
## Fabric
- Fixed a crash in the Fabric version of Computer Craft when placing a turtle. [#56](https://github.com/xiewuzhiying/VS-Addition/issues/56)
- Added TaCZ fabric version compatibility.
## Forge
- Added Ballistix compatibility. (Transform attract blast and repulsive blast only)
- Cancel `onExplosion` method of Tallyho if Create: Diesel Generators mod doesn't exist.