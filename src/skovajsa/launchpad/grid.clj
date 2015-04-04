(ns skovajsa.launchpad.grid)

(def init-grid
  (apply hash-map (interleave
                    (for [i (range 1 9) j (range 1 9)] [i j])
                    (repeat nil))))

(defn- current-mode
  [lp]
  @(:mode lp))

(defn current-grid
  "Returns the entire Launchpad grid for current mode."
  [lp]
  (get @(:state lp) @(:mode lp)))

(defn get-btn
  "Returns value of a button on the current Launchpad grid."
  [lp [x y]]
  (get (current-grid lp) [x y]))

(defn upd-btn!
  "Updates a single btn on the current Launchpad grid."
  [lp [x y] color]
  (swap! (:state lp)assoc-in [(current-mode lp) [x y]] color))

(defn upd-grid!
  "Accepts a map of { xy -> color } values, updates current Launchpad grid."
  [lp btns]
  (swap! (:state lp) assoc (current-mode lp) (merge (current-grid lp) btns)))
