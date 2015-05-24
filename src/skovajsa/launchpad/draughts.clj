(ns skovajsa.launchpad.draughts
  (:require [clojure.set :as set]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.utils :as utils]))

;; Game logic

(def all-squares
  "All the playable squares on the board."
  (remove nil? (for [x (range 1 9) y (range 1 9)]
                 (when (or (and (even? x) (odd? y))
                           (and (odd? x) (even? y)))
                   [x y]))))

(def init-board
  "Initial players' positions."
  (letfn [(f [squares] (into {} (map vector squares (repeat :man))))]
    {:player-1 (f [[1 6] [3 6] [5 6] [7 6] [2 7] [4 7]
                   [6 7] [8 7] [1 8] [3 8] [5 8] [7 8]])
     :player-2 (f [[2 1] [4 1] [6 1] [8 1] [1 2] [3 2]
                   [5 2] [7 2] [2 3] [4 3] [6 3] [8 3]])}))

(defn free-square?
  "Is this square free to move to?"
  [board xy]
  (nil? (some #{xy} (->> board vals (map keys) (apply set/union)))))

(defn owner-of
  "Who owns the piece on the given square?"
  [board xy]
  (->> board
       (filter (fn [[_ v]] (some #(= % xy) (keys v))))
       first first))

(defn- vicinity-with-radius
  "Returns a map of possible playable squares around a square within the radius of r."
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
  "Returns a map of adjacent diagonal squares."
  [xy]
  (vicinity-with-radius 1 xy))

(defn jump-vicinity
  "Returns a map of possible playable squares within the radius of 2."
  [xy]
  (vicinity-with-radius 2 xy))

(defn within-vicinity?
  "Is the destination square within the given vicinity of the initial square?"
  [vicinity-f from to]
  (some? (some #{to} (-> from vicinity-f vals))))

(defn opponent-in-vicinity
  "Returns a map of opponent's pieces' squares around the given square."
  [board xy]
  (let [enemy (->> [:player-1 :player-2]
                   (remove #(= % (owner-of board xy)))
                   first)]
    (->> (vicinity xy)
         (filter (fn [[_ v]] (some #{v} (-> board (get enemy) keys))))
         (into {}))))

(defn capturable-pieces-in-vicinity
  "Returns a map of capturable opponent pieces around the given square."
  [board xy]
  (->> (opponent-in-vicinity board xy)
       (filter (fn [[k v]] (free-square? board (get (vicinity v) k))))
       (into {})))

(defn can-touch?
  "Is this square touchable in current turn?"
  [board turn xy]
  (= turn (owner-of board xy)))

(defn can-move-to?
  "Can the piece on this square be moved to the other square?"
  [board from to]
  (and (some #{to} all-squares)
       (or (and (when-let [dir (->> (jump-vicinity from)
                                    (filter (fn [[_ v]] (= v to)))
                                    first first)]
                  (some #{dir} (keys (capturable-pieces-in-vicinity board from))))
                (free-square? board to))
           (and (some #{to} (-> from vicinity vals))
                (free-square? board to)))))

(defn upd-board
  "Moves a single piece from one square to another. Returns an updated board.
  If a piece jumps and captures another piece, the captured piece is dissoc-ed."
  [board from to]
  (let [player (owner-of board from)
        ps (get board player)
        rank (get ps from)]
    (cond
      (within-vicinity? vicinity from to)
      (assoc board player (-> ps (dissoc from) (assoc to rank)))

      (within-vicinity? jump-vicinity from to)
      (let [[from-x from-y] from [to-x to-y] to
            captured [(/ (+ from-x to-x) 2) (/ (+ from-y to-y) 2)]
            opponent (owner-of board captured)
            opponent-ps (get board opponent)]
        (-> board
            (assoc player (-> ps (dissoc from) (assoc to rank)))
            (assoc opponent (-> opponent-ps (dissoc captured))))))))

;; State and rendering

(defn- current-state [lp] (-> lp :state deref :draughts))

(defn- upd-state! [lp k v] (swap! (:state lp) assoc-in [:draughts k] v))

(defn toggle-turn!
  [lp]
  (let [current-turn (-> lp current-state :turn)]
    (upd-state! lp :turn (->> [:player-1 :player-2]
                              (remove #(= % current-turn))
                              first))))

(defn board->grid
  "Converts a board map into a grid map."
  [lp board]
  (let [colors (-> lp :config :draughts :colors)
        squares->map (fn [cs color] (into {} (map vector cs (repeat color))))
        player-m (fn [p] (-> board (get p) keys (squares->map (get colors p))))
        free-squares-m (squares->map (->> [all-squares
                                           (-> board :player-1 keys)
                                           (-> board :player-2 keys)]
                                          (map set) (apply set/difference))
                                     (:board colors))]
    (merge (player-m :player-1) (player-m :player-2) free-squares-m)))

(defn render-board!
  "Renders the given board, implicitly converting it to a new grid map."
  [lp board]
  (led/upd-grid lp (-> lp current-state :grid) (board->grid lp board)))

(defn start-game
  [lp]
  (swap! (:state lp) assoc :draughts {:turn :player-1
                                      :board init-board
                                      :grid (board->grid lp init-board)})
  (led/upd-grid lp (board->grid lp init-board)))

;; Handlers

(defn select-piece!
  [lp xy]
  (upd-state! lp :selection xy))

(defn clear-selection!
  [lp]
  (select-piece! lp nil))

(defn move-to!
  [lp from to]
  (let [new-board (upd-board (-> lp current-state :board) from to)]
    (render-board! lp new-board)
    (upd-state! lp :board new-board)
    (upd-state! lp :grid (board->grid lp new-board))
    (let [[f-x _] from [t-x _] to]
      ;; TODO move it out of here
      (when (= 1 (Math/abs (- t-x f-x))) (toggle-turn! lp)))))

(defn dispatch-event
  "Handles all the behaviour related to pressing a button."
  [lp xy]
  (let [{:keys [board turn selection]} (current-state lp)]
    (if selection
      (if (can-move-to? board selection xy)
        (do (move-to! lp selection xy)
            (clear-selection! lp))
        (do
          (prn "can't move it there")
          (clear-selection! lp)))
      (if (can-touch? board turn xy)
        (select-piece! lp xy)
        (do
          (prn "can't touch this")
          (clear-selection! lp))))))

(defn draughts-handler
  [lp]
  {:event [:midi :note-on]
   :handler (fn [e] (dispatch-event lp (-> e :note utils/note->xy)))
   :key :draughts-handler})
