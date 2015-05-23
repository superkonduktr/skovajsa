(ns skovajsa.launchpad.checkers
  (:require [clojure.set :as set]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.utils :as utils]))

;; Game logic

(def all-cells
  "All the 'playable' cells on the board."
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

(defn upd-board
  "Moves a single checker from one cell to another, returns an updated board."
  [board turn from to]
  (let [cs (get board turn)
        rank (get cs from)]
    (assoc board turn (-> cs (dissoc from) (assoc to rank)))))

(defn cells-around
  "Returns a vector of playable cells around a cell within the radius of 1."
  [[x y]]
  (let [try-inc (fn [n] (when (> 8 n) (inc n)))
        try-dec (fn [n] (when (< 1 n) (dec n)))]
    (->> (for [i [(try-dec x) (try-inc x)]
               d [(try-dec y) (try-inc y)]]
           [i d])
         (remove #(some nil? %)))))

(defn can-touch?
  "Is this cell playable in current turn?"
  [board turn xy]
  (some? (some #{xy} (-> board turn keys))))

(defn can-move-to?
  "Can the checker on this cell be moved to the other cell?"
  [board from to]
  ;; TODO
  (some? (some #{to} all-cells)))

;; State and rendering

(defn- current-state [lp] (-> lp :state deref :checkers))

(defn- upd-state! [lp k v] (swap! (:state lp) assoc-in [:checkers k] v))

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
  (let [{:keys [board turn]} (current-state lp)
        new-board (upd-board board turn from to)]
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
          (clear-selection lp))
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
