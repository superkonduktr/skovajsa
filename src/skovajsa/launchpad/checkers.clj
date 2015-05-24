(ns skovajsa.launchpad.checkers
  (:require [clojure.set :as set]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.utils :as utils]))

;; Game logic

(def all-cells
  "All the playable cells on the board."
  (remove nil? (for [x (range 1 9) y (range 1 9)]
                 (when (or (and (even? x) (odd? y))
                           (and (odd? x) (even? y)))
                   [x y]))))

(def init-board
  "Initial players' positions."
  (letfn [(f [cells] (into {} (map vector cells (repeat :man))))]
    {:player-1 (f [[1 6] [3 6] [5 6] [7 6] [2 7] [4 7]
                   [6 7] [8 7] [1 8] [3 8] [5 8] [7 8]])
     :player-2 (f [[2 1] [4 1] [6 1] [8 1] [1 2] [3 2]
                   [5 2] [7 2] [2 3] [4 3] [6 3] [8 3]])}))

(defn free-cell?
  "Is this cell free to move to?"
  [board xy]
  (nil? (some #{xy} (->> board vals (map keys) (apply set/union)))))

(defn owner-of
  "Who owns the checker on the given cell?"
  [board xy]
  (->> board
       (filter (fn [[_ v]] (some #(= % xy) (keys v))))
       first first))

(defn upd-board
  "Moves a single checker from one cell to another, returns an updated board."
  [board from to]
  (let [player (owner-of board from)
        cs (get board player)
        rank (get cs from)]
    (assoc board player (-> cs (dissoc from) (assoc to rank)))))

(defn- vicinity-with-radius
  "Returns a map of possible playable cells around a cell within the radius of r."
  [r [x y]]
  (let [try-add (fn [n] (when (> (- 8 (dec r)) n) (+ n r)))
        try-sbt (fn [n] (when (< r n) (- n r)))]
    (->> {:up-left [(try-sbt x) (try-sbt y)]
          :up-right [(try-add x) (try-sbt y)]
          :down-left [(try-sbt x) (try-add y)]
          :down-right [(try-add x) (try-add y)]}
         (remove (fn [[_ v]] (some nil? v)))
         (into {}))))

(defn vicinity
  "Returns a map of adjacent diagonal cells."
  [xy]
  (vicinity-with-radius 1 xy))

(defn jump-vicinity
  "Returns a map of possible playable cells within the radius of 2."
  [xy]
  (vicinity-with-radius 2 xy))

(defn villains-in-vicinity
  "Returns a map of enemy checkers' cells around the given cell."
  [board xy]
  (let [enemy (->> [:player-1 :player-2]
                   (remove #(= % (owner-of board xy)))
                   first)]
    (->> (vicinity xy)
         (filter (fn [[_ v]] (some #{v} (-> board (get enemy) keys))))
         (into {}))))

(defn capturable-villains-in-vicinity
  "Returns a vector of capturable checkers around the given cell."
  [board xy]
  (->> (villains-in-vicinity board xy)
       (filter (fn [[k v]] (free-cell? board (get (vicinity v) k))))
       (into {})))

(defn can-touch?
  "Is this cell playable in current turn?"
  [board turn xy]
  (= turn (owner-of board xy)))

(defn can-move-to?
  "Can the checker on this cell be moved to the other cell?"
  [board from to]
  (and (some #{to} all-cells)
       (or (and (when-let [dir (->> (jump-vicinity from)
                                    (filter (fn [[_ v]] (= v to)))
                                    first first)]
                  (prn (capturable-villains-in-vicinity board from))
                  (some #{dir} (keys (capturable-villains-in-vicinity board from))))
                (free-cell? board to))
           (and (some #{to} (-> from vicinity vals))
                (free-cell? board to)))))

;; State and rendering

(defn- current-state [lp] (-> lp :state deref :checkers))

(defn- upd-state! [lp k v] (swap! (:state lp) assoc-in [:checkers k] v))

(defn toggle-turn
  [lp]
  (let [current-turn (-> lp current-state :turn)]
    (upd-state! lp :turn (->> [:player-1 :player-2]
                              (remove #(= % current-turn))
                              first))))

(defn board->grid
  "Converts a board map into a grid map."
  [lp board]
  (let [colors (-> lp :config :checkers :colors)
        cells->map (fn [cs color] (into {} (map vector cs (repeat color))))
        player-m (fn [p] (-> board (get p) keys (cells->map (get colors p))))
        free-cells-m (cells->map (->> [all-cells
                                       (-> board :player-1 keys)
                                       (-> board :player-2 keys)]
                                      (map set) (apply set/difference))
                                 (:board colors))]
    (merge (player-m :player-1) (player-m :player-2) free-cells-m)))

(defn render-board
  "Renders the given board, implicitly converting it to a new grid map."
  [lp board]
  (led/upd-grid lp (-> lp current-state :grid) (board->grid lp board)))

(defn start-game
  [lp]
  (swap! (:state lp) assoc :checkers {:turn :player-1
                                      :board init-board
                                      :grid (board->grid lp init-board)})
  (led/upd-grid lp (board->grid lp init-board)))

;; Handlers

(defn select-checker
  [lp xy]
  (upd-state! lp :selection xy))

(defn clear-selection
  [lp]
  (select-checker lp nil))

(defn move-to
  [lp from to]
  (let [new-board (upd-board (-> lp current-state :board) from to)]
    (render-board lp new-board)
    (upd-state! lp :board new-board)
    (upd-state! lp :grid (board->grid lp new-board))))

(defn dispatch-event
  "Handles all the behaviour related to pressing a button."
  [lp xy]
  (let [state (current-state lp)
        {:keys [board turn selection]} state]
    (if selection
      (if (can-move-to? board selection xy)
        (do
          (move-to lp selection xy)
          (clear-selection lp)
          (toggle-turn lp))
        (do
          (prn "can't move it there")
          (clear-selection lp)))
      (if (can-touch? board turn xy)
        (select-checker lp xy)
        (do
          (prn "can't touch this")
          (clear-selection lp))))))

(defn checkers-handler
  [lp]
  {:event [:midi :note-on]
   :handler (fn [e] (dispatch-event lp (-> e :note utils/note->xy)))
   :key :checkers-handler})
