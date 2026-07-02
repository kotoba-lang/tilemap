(ns tilemap-test
  "Restoration-fidelity tests — one per original kami-tilemap Rust test
  (kami-engine/kami-tilemap/src/lib.rs `mod tests`, deleted PR #82)."
  (:require [clojure.test :refer [deftest is testing]]
            [tilemap]))

(deftest namespace-loads
  (testing "the restored CLJC namespace loads"
    (is (some? (the-ns 'tilemap)))))

;; mirrors `test_tilemap`
(deftest test-tilemap
  (let [layer (tilemap/tile-layer "ground" 10 10)
        layer (tilemap/layer-set layer 5 3 (tilemap/solid-tile 1))
        tm (tilemap/tilemap 16.0)
        tm (tilemap/add-layer tm layer)]
    (is (tilemap/solid? tm 5 3))
    (is (not (tilemap/solid? tm 0 0)))
    (is (= [5 3] (tilemap/world-to-tile tm [85.0 50.0])))))
