# Project Overview — 3D Cozy City

## Concept
A 3D interactive city animation inspired by our 2nd-partial 2D city project, featuring a cozy day/afternoon/night lighting cycle. The user explores a compact, clean 3D city from a first-person camera while animated elements (sun, moon, clouds, stars, and street/city details) run concurrently.

## Core Goals
1. **Complete 3rd-Partial Coverage:** Integrate every required 3D graphics topic using our custom `engine/` (no Java2D drawing calls except the existing pixel-writing path).
2. **Smooth Presentation:** Verified flicker-free double buffering and stable frame pacing.
3. **Controlled Concurrency:** Use `ExecutorService` to animate independent graphical elements safely without race conditions on the framebuffer.
4. **Clean, Modular Scope:** Keep geometry lightweight (hollow rectangular-prism buildings on a flat street plane) so lighting, shadows, and animations run smoothly.

## Visual & Scene Elements
- **Ground & Streets:** Flat city plane with roads, intersections, and sidewalks.
- **Buildings:** Hollow rectangular prisms with varied heights, scales, and cozy color palettes (materials reacting to light).
- **Sky & Day/Night Cycle:** Smooth color transitions (morning, afternoon, sunset, night) driving directional/point light position, intensity, and color.
- **Celestial & Atmospheric Elements:**
  - **Sun & Moon:** Moving along a 3D parametric/explicit curve orbit, acting as the primary light source.
  - **Clouds:** 3D parametric/explicit surfaces drifting across the sky.
  - **Stars:** Subtle 3D points/shapes visible during the night phase.
- **Camera & Projections:**
  - **Perspective mode (default):** First-person camera with WASD movement and yaw/pitch look.
  - **Parallel mode (toggleable):** Orthographic/parallel projection view (e.g., architectural/overview camera) to satisfy both projection requirements cleanly.
