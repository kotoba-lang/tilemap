(ns tilemap-test
  "Restoration-fidelity tests — one per original kami-tilemap Rust test
  (kami-engine/kami-tilemap/src/lib.rs `mod tests`, deleted PR #82)."
  (:require [clojure.test :refer [deftest is testing]]
            [tilemap]))

(deftest namespace-loads
  (testing "the restored CLJC namespace loads"
    (is (some? (find-ns 'tilemap)))))

;; mirrors `test_tilemap`
(deftest test-tilemap
  (let [layer (tilemap/tile-layer "ground" 10 10)
        layer (tilemap/layer-set layer 5 3 (tilemap/solid-tile 1))
        tm (tilemap/tilemap 16.0)
        tm (tilemap/add-layer tm layer)]
    (is (tilemap/solid? tm 5 3))
    (is (not (tilemap/solid? tm 0 0)))
    (is (= [5 3] (tilemap/world-to-tile tm [85.0 50.0])))))

(deftest layer-get-set-reject-negative-coordinates
  ;; Negative x/y must be treated as out-of-bounds, not wrap into a flat
  ;; index that aliases a different (valid) cell -- (* y width) + x can be
  ;; a small positive number for negative x on a non-zero row.
  (let [layer (-> (tilemap/tile-layer "ground" 10 10)
                  (tilemap/layer-set 9 4 (tilemap/solid-tile 42)))]
    (is (= tilemap/empty-tile (tilemap/layer-get layer -1 5))
        "negative x must not alias (9,4)'s slot")
    (is (= tilemap/empty-tile (tilemap/layer-get layer 0 -1)))
    (is (= tilemap/empty-tile (tilemap/layer-get layer -1 -1)))
    (is (= layer (tilemap/layer-set layer -1 5 (tilemap/solid-tile 99)))
        "layer-set with negative x must be a no-op, not corrupt (9,4)")))
