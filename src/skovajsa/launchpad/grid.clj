(ns skovajsa.launchpad.grid)

(defprotocol Grid
  (get-btn [this x y])
  (upd-btn! [this x y val vel]))

(def init-grid
  (apply hash-map (interleave
                    (for [i (range 1 9) j (range 1 9)] [i j])
                    (repeat nil))))
