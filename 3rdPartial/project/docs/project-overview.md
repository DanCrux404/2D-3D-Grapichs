# Project Overview

## Goal
Build an original retro first-person shooter inspired by classic 1990s corridor shooters. Working title: **Project DOOM**. It must be an animated 3D graphics demonstration covering the third-partial graphics coursework.

This is an original student project. Do not copy proprietary DOOM assets, maps, source code, sounds, or other protected content. Create original geometry, level layouts, visuals, and gameplay content.

## Coursework requirements
- Parallel projection.
- Perspective projection.
- Translation, scaling, and rotation.
- Curves, surfaces, and all other relevant third-partial topics.
- Correct double buffering without visible flicker or avoidable delay.
- No uncaught exceptions or runtime errors.
- Use the project's own graphics methods. Do not use Java graphics drawing methods, except the existing pixel-writing mechanism.

Additional credit may consider animation/graphic complexity and execution threads per graphical element. Threading is optional until correctness is established.

Do not assume a topic is covered merely because an engine class exists. The finished application must visibly demonstrate each required topic.

## Game concept
A first-person shooter in a compact enclosed facility: corridors, rooms, doors, connected spaces, and elevation changes rather than a large open field.

Intended features:
- First-person movement.
- Mouse or keyboard look, including looking up and down (camera pitch) as well as turning (yaw).
- Doors that open.
- Enemies that pursue/attack and damage the player.
- Weapons, ammunition, health, and pickups.
- Win/lose/restart flow.
- Lighting and shadows using existing engine capabilities where practical.

## Difficulty menu
1. **I'm Too Young to Die** — relaxed play, slower/weaker enemies, more resources.
2. **Hey, Not Too Rough** — easy introduction to the map and weapons.
3. **Hurt Me Plenty** — balanced default.
4. **Ultra-Violence** — tougher and more frequent enemies.
5. **Nightmare** — extreme challenge, faster enemies, increased ammunition availability, and enemy respawning.

Difficulty must be data-driven, not scattered hard-coded conditionals. Exact numerical tuning is a later decision.

## Visual direction
Retro and readable, with clear silhouettes, enclosed spaces, original geometry, a consistent palette, and steps/ramps/platforms or other elevation changes.

## Priorities
1. Correctness and coursework coverage.
2. Stable rendering and frame pacing.
3. Playable movement, collision, doors, and combat.
4. Visual polish and animation.
5. Optional optimization/threading after correctness.

## Menu and game-state flow
- The title screen must contain two options: PLAY and EXIT.
- Selecting PLAY opens the difficulty selection screen.
- Selecting a difficulty starts the game.
- When the player dies, return to the title screen.
- EXIT closes the application cleanly.
- All menu text must be rendered using a custom bitmap/pixel font.
- The font must use the existing pixel-writing mechanism.
- Do not use Java2D text rendering or Graphics.drawString().

## Combat presentation
- Ordinary weapon fire must use hitscan-style logic.
- Do not render visible bullets, bullet meshes, or traveling bullet streaks.
- Preserve the classic retro FPS presentation.
- Muzzle flashes and brief weapon effects may be considered separately.

## Music and audio
- The preferred soundtrack is the original music associated with the first DOOM episode.
- The exact track's license and permissions must be verified before including it.
- Do not redistribute or commit copyrighted music without permission.
- Audio must be optional, so the game can run without music.
- I can download the original music and be can play it when game start on loop
- Remember this in orden for me to download it and save it on a specific folder

## Definition of done
All coursework topics are demonstrably used; the game loop is playable from menu through win/lose/restart; rendering uses the existing engine without prohibited Java drawing APIs; double buffering is verified; and the project runs reliably using its documented launcher.

## Menu and Game Flow

The game must include the following navigation flow:

1. The title screen displays PLAY and EXIT.
2. Selecting PLAY opens the difficulty selection screen.
3. Selecting a difficulty starts the game.
4. When the player dies, the game returns to the title screen.
5. Selecting EXIT closes the application.

All menu text must be rendered using a reusable bitmap font and pixel operations.

## Weapon System

Weapon attacks must use hitscan mechanics.

- Attacks are evaluated using raycasting or an equivalent instantaneous hit-detection method.
- No visible traveling bullets, bullet trails, or projectile meshes are required.
- Weapon feedback may use brief visual effects, but must not depict physical bullets traveling through the environment.
