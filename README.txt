New Entity: Enforcers
	Currently only spawn using the '/summon hattrick:enforcer' command
	Neutral mob that will maintain a distance to observe the player
	Will become aggressive if one of the following conditions are met:
		Enforcer takes damage from the player
		Enforcer observes a group of three or more players
		Enforcer observes a target player (set using the '/hattrick target [playerName]' command)
		Enforcer inherits setGlobalAggro (set using the '/hattrick aggro true' command)
	Will summon reinforcements when falling below half health and quarter health
	Will apply slowness and weakness effects to nearby players while aggressive
	Enforcer damage scales based on number of aggressive Enforcers
	Original Enforcer will drop Enforced Fiber
	Current model is a work in progress

New Item: Enforced Fiber
	Used to craft: The Hat Man's Hat, Enforcement Block
	Will always have Curse of Vanishing, to limit farming of the item
	Current name and texture are works in progress

New Item: The Hat Man's Hat
	Will always have Curse of Binding, but is unique in that the hat will remain in the helmet position even after death
	While equipped, press keybind (default: 'H') to transform into The Hat Man, as well as to de-transform, both with a particle effect
		While transformed, original skin will be invisible while The Hat Man model is visible (Current model is a work in progress)
		While transformed, player will not show up in the 'tab' menu of online players
		While transformed, player's messages will show up as though they were sent via the '/tellraw' command with custom formatting

New Block: Enforcement Block
	Only accessible in Creative Mode or via commands
	A modified Command Block that directly applies given commands to all players within a given radius of blocks
	Will automatically reverse command effects for '/gamemode' and '/effect' commands
		i.e., if the set command is '/gamemode @p adventure' with a radius of 10 blocks and a player in Creative Mode enters the radius, they will be set to Adventure Mode, and Creative Mode will be returned to them upon leaving the radius.
		As a fail-safe, Operator Players can still change gamemode back to Creative to modify the Enforcement Block

New Enchantment: Curse of Midas
	Currently only accessible via '/enchant' command
	A set list of blocks will drop a gold block, with a 50% chance for the normal drop as well, when mined by a tool with this Curse
	All other blocks will drop 5-8 raw gold or 5-8 gold ingots, with a 50% chance for the normal drop as well
