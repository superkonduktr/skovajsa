(ns skovajsa.launchpad.checkers
  (:require [clojure.set :as set]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.utils :as utils]))

(def board
  "All the 'playable' cells on the board."
  (remove nil? (for [x (range 1 9) y (range 1 9)]
                 (when (or (and (even? x) (odd? y))
                           (and (odd? x) (even? y)))
                   [x y]))))

(def init-players
  "Initial players' positions."
  (letfn [(f [cells] (into {} (map vector cells (repeat :man))))]
    {:player-1 (f [[1 6] [3 6] [5 6] [7 6] [2 7] [4 7]
                   [6 7] [8 7] [1 8] [3 8] [5 8] [7 8]])
     :player-2 (f [[2 1] [4 1] [6 1] [8 1] [1 2] [3 2]
                   [5 2] [7 2] [2 3] [4 3] [6 3] [8 3]])}))

(defn render-board
  [lp colors players]
  (let [cells->map (fn [cs color] (into {} (map vector cs (repeat color))))
        player-m (fn [p] (-> players (get p) keys (cells->map (get colors p))))
        free-cells-m (cells->map (->> [board
                                       (-> players :player-1 keys)
                                       (-> players :player-2 keys)]
                                      (map set) (apply set/difference))
                                 (:board colors))
        grid (merge (player-m :player-1) (player-m :player-2) free-cells-m)]
    (led/upd-grid lp grid)))

(defn cells-around
  "Returns vector of cells around a cell."
  [[x y]]
  (let [try-inc (fn [n] (when (> 8 n) (inc n)))
        try-dec (fn [n] (when (< 1 n) (dec n)))]
    (->> (for [i [(try-dec x) (try-inc x)]
               d [(try-dec y) (try-inc y)]]
           [i d])
         (remove #(some nil? %)))))

(defn can-move?
  "Is this cell playable in current turn?"
  [board turn xy]
  (some? (some #{xy} (-> board turn keys))))

(defn start-game
  [lp colors]
  (swap! (:state lp) assoc-in [:checkers :turn] :player-1)
  (swap! (:state lp) assoc-in [:checkers :board] init-players)
  (render-board lp colors init-players))
