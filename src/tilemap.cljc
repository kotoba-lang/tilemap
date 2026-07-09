(ns tilemap
  "KAMI Tilemap — 2D tile layers, collision map, world/tile coordinate
  conversion, and GPU instance-buffer data generation for RPG/
  platformer/strategy games. Restored from the legacy kami-engine/
  kami-tilemap Rust crate (deleted in kotoba-lang/kami-engine PR #82
  'Remove Rust workspace from kami-engine') as part of the clj-wgsl
  migration (ADR-2607010930, com-junkawasaki/root). Ledger class
  `:port-to-CLJC-domain-interpreter` (90-docs/migration/
  clj-wgsl-ledger.edn) — tile rendering stays GPU-side (native/WGSL
  consumes the instance buffer this namespace generates); this
  namespace owns the collision map / layer data itself.

  Zero-dep portable CLJC — pure data + pure functions, no IO/GPU. A
  single flat namespace (the original was one flat `lib.rs`).")

;; ── Tile ──────────────────────────────────────
;; :id (tile atlas index, 0 = empty), :flags (bit 0 solid / 1 flip-x /
;; 2 flip-y / 3 animated).

(def empty-tile {:id 0 :flags 0})

(defn solid-tile [id] {:id id :flags 1})

(defn tile-empty? [tile] (zero? (:id tile)))
(defn tile-solid? [tile] (not= 0 (bit-and (:flags tile) 1)))

;; ── Tile layer (one z-level) ────────────────────

(defn tile-layer
  "A fresh, all-empty tile layer named `name` with dimensions `w`x`h`."
  [name w h]
  {:name name :width w :height h
   :tiles (vec (repeat (* w h) empty-tile))
   :visible true :opacity 1.0 :z-order 0})

(defn layer-get
  "Tile at `(x,y)` in `layer`, or `empty-tile` if out of bounds."
  [layer x y]
  (if (and (>= x 0) (>= y 0) (< x (:width layer)) (< y (:height layer)))
    (nth (:tiles layer) (+ (* y (:width layer)) x))
    empty-tile))

(defn layer-set
  "Set the tile at `(x,y)` in `layer`. No-op (returns `layer` unchanged)
  if out of bounds."
  [layer x y tile]
  (if (and (>= x 0) (>= y 0) (< x (:width layer)) (< y (:height layer)))
    (assoc-in layer [:tiles (+ (* y (:width layer)) x)] tile)
    layer))

;; ── Tilemap: collection of layers + metadata ────

(defn tilemap
  "A fresh, layerless tilemap with `tile-size` pixels per tile."
  [tile-size]
  {:tile-size tile-size :layers []})

(defn add-layer [tm layer] (update tm :layers conj layer))

(defn world-to-tile
  "World position `[x y]` -> tile coordinate `[tx ty]` (integer, floored)."
  [tm [x y]]
  [(long (Math/floor (/ x (:tile-size tm))))
   (long (Math/floor (/ y (:tile-size tm))))])

(defn tile-to-world
  "Tile coordinate `(tx,ty)` -> world center position `[x y]`."
  [tm tx ty]
  [(* (+ tx 0.5) (:tile-size tm))
   (* (+ ty 0.5) (:tile-size tm))])

(defn solid?
  "True if any layer of `tm` has a solid tile at `(tx,ty)`."
  [tm tx ty]
  (and (>= tx 0) (>= ty 0)
       (boolean (some (fn [l] (tile-solid? (layer-get l tx ty))) (:layers tm)))))

(defn layer-to-instances
  "Generate `[x y tile-size tile-id]` instanced tile positions for GPU
  rendering, for the layer at `layer-idx`. Empty tiles are skipped."
  [tm layer-idx]
  (let [layer (nth (:layers tm) layer-idx)
        tile-size (:tile-size tm)]
    (vec
     (for [y (range (:height layer))
           x (range (:width layer))
           :let [tile (layer-get layer x y)]
           :when (not (tile-empty? tile))]
       [(* x tile-size) (* y tile-size) tile-size (double (:id tile))]))))
