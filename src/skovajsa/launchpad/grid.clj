(ns skovajsa.launchpad.grid)

(def init-grid
  (apply hash-map (interleave
                    (for [x (range 1 9) y (range 1 9)] [x y])
                    (repeat nil))))

(defn- current-mode
  [lp]
  (:mode @(:state lp)))

(defn current-grid
  "Returns the entire Launchpad grid for current mode."
  [lp]
  (get-in @(:state lp) [(current-mode lp) :grid]))

(defn get-btn
  "Returns value of a button on the current Launchpad grid."
  [lp [x y]]
  (get (current-grid lp) [x y]))

(defn upd-btn!
  "Updates a single btn on the current Launchpad grid."
  [lp [x y] color]
  (swap! (:state lp) assoc-in [(current-mode lp) :grid [x y]] color))

(defn upd-grid!
  "Accepts a map of { xy -> color } values, updates current Launchpad grid."
  [lp btns]
  (swap! (:state lp)
         assoc-in [(current-mode lp) :grid] (merge (current-grid lp) btns)))
