# Architecture and Design Decisions

Record decisions made by the human leads. Do not silently reverse a decision; propose a change with rationale and wait for approval.

## D-001 — Existing engine is protected
**Decision:** Reuse the completed engine as-is. Do not modify `engine/` or `TestFigures/`. A suspected bug must be reported with a reproducible case and proposed minimal fix; approval is required before editing.
**Reason:** Preserve completed graphics work and avoid regressions.

## D-002 — Engine is the only graphics renderer
**Decision:** Do not use Java graphics drawing APIs or introduce another renderer. Use existing engine APIs and pixel-writing mechanism.
**Reason:** Coursework restriction and consistency.

## D-003 — Original retro FPS
**Decision:** Build an original first-person corridor shooter inspired by classic 1990s FPS games, not a clone using protected assets.
**Reason:** Preserve the intended aesthetic while creating original content.

## D-004 — Plan before implementation
**Decision:** Begin with reconnaissance and approved architecture. Implement in small phases and update `tasks.md`.
**Reason:** Keep the agent grounded and prevent speculative changes.

## Pending decisions
- [ ] Exact game source directory/package layout.
- [ ] World axes, units, and camera convention.
- [ ] Input scheme and mouse capture behavior.
- [ ] Level dimensions and map representation.
- [ ] Whether textures are needed or polygon/color visuals suffice.
- [ ] Exact difficulty parameter values.
- [ ] Threading strategy, if any, after a stable single-threaded baseline.
