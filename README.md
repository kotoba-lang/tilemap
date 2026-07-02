# kotoba-lang/tilemap

Zero-dep portable `.cljc` — restored from the legacy `kami-engine/kami-tilemap`
Rust crate (deleted in kotoba-lang/kami-engine PR #82 "Remove Rust workspace
from kami-engine") as part of the **clj-wgsl migration** (ADR-2607010930,
`com-junkawasaki/root`).

KAMI Tilemap: 2D tile layers, collision map, world/tile coordinate
conversion, and GPU instance-buffer data generation for RPG/platformer/
strategy games. Ledger class `:port-to-CLJC-domain-interpreter` — tile
rendering stays GPU-side (native/WGSL consumes the instance buffer this
namespace generates); this namespace owns the collision map / layer
data itself.

## Status

Restored — the single-namespace tilemap/collision interpreter ported
from the original 148-line Rust `lib.rs`, with the original Rust unit
test mirrored 1:1 in `test/tilemap_test.cljc` (+1 smoke test) — 2 tests
/ 4 assertions, 0 failures. Pure data + pure functions throughout; no
IO/GPU.

## Develop

```bash
clojure -M:test
```
