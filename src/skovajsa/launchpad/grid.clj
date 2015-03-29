(ns skovajsa.launchpad.grid)

(def init-grid
  (apply hash-map (interleave
                    (for [i (range 1 9) j (range 1 9)] [i j])
                    (repeat nil))))

(defn get-btn
  [lp [x y]]
  (get @(:grid lp) [x y]))

(defn upd-btn!
  [lp [x y] color]
  (swap! (:grid lp) assoc [x y] color))
