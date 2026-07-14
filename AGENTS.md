# GymTracker Agent Working Agreement

## Session startup

Before broad repository discovery:

1. Read `docs/obsidian/00_START_HERE.md`.
2. Read `docs/obsidian/workflow/SESSION_HANDOFF.md`.
3. Open the relevant MOC/feature node named in the handoff.
4. Use `docs/obsidian/workflow/CHANGE_IMPACT_MAP.md` and the note's `source_files` to select the smallest source set that needs verification.

The Obsidian graph accelerates discovery; source code and the current Git diff remain the final source of truth. If a note's `verified_commit` differs from `HEAD`, validate its declared source files before relying on it. Do not assume a `proposed` node is implemented.

## During implementation

- Preserve unrelated user changes.
- For Room/entity changes, update schema version/export, explicit migrations, and migration tests; never add destructive migration as a shortcut.
- For navigation changes, verify back behavior, route IDs, saved state, and process recreation.
- For shared UI/theme changes, verify light/dark, large font, accessibility semantics, and relevant screenshots.
- Use the test obligations in `docs/obsidian/quality/TEST_MATRIX.md`.

## Session close

When code behavior, ownership, routes, data schema, risks, or file locations change:

1. Update the affected Obsidian feature/architecture/ADR node.
2. Update `SOURCE_FILE_ATLAS.md` or `CHANGE_IMPACT_MAP.md` if the dependency graph changed.
3. Update risk status when applicable.
4. Write completed work, tests/results, next concrete action, and blockers to `SESSION_HANDOFF.md`.
5. Refresh `verified_commit` only for notes actually checked against that commit.

Graph maintenance rules are in `docs/obsidian/workflow/GRAPH_MAINTENANCE.md`.
