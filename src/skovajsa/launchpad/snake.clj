(ns skovajsa.launchpad.snake
  (:require [clojure.set :refer [difference]]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.utils :as utils]))

(defn- up
  [s]
  (let [[head-x head-y] (last s)]
    (conj
      (vec (rest s))
      [head-x (if (< 1 head-y) (dec head-y) 8)])))

(defn- down
  [s]
  (let [[head-x head-y] (last s)]
    (conj
      (vec (rest s))
      [head-x (if (> 8 head-y) (inc head-y) 1)])))

(defn- left
  [s]
  (let [[head-x head-y] (last s)]
    (conj
      (vec (rest s))
      [(if (< 1 head-x) (dec head-x) 8) head-y])))

(defn- right
  [s]
  (let [[head-x head-y] (last s)]
    (conj
      (vec (rest s))
      [(if (> 8 head-x) (inc head-x) 1) head-y])))

(defn- bumped?
  "Has the snake bumped into itself yet?"
  [s]
  (some? (some #{(last s)} (butlast s))))

(defn- new-mouse
  [snake]
  (->> snake
       (difference (set (for [x (range 1 9) y (range 1 9)] [x y])))
       vec rand-nth))

(defn- current-direction
  [lp]
  (-> @(:state lp) :snake :direction))

(defn- direction-fn
  [lp]
  (case (current-direction lp)
    :up up
    :down down
    :left left
    :right right
    nil))

(defn- set-direction!
  [lp dr]
  (swap! (:state lp) assoc-in [:snake :direction] dr))

(def init-snake [[3 4] [4 4]])

(defn- render
  [lp snake mouse]
  (let [current (grid/current-grid lp)
        new (merge current (zipmap snake (repeat :green)) {mouse :amber})]
    (led/upd-grid lp current new)))

(defn- render-game-over
  [lp snake]
  (let [current (grid/current-grid lp)
        dead (fn [color] (merge current (zipmap snake (repeat color))))]
    (doseq [c [:full-red :red :low-red nil]]
      (led/upd-grid lp current (dead c))
      (Thread/sleep 300))))

;; Event handlers & launching

(defn- can-turn?
  [current-dr new-dr]
  (when-not (= current-dr new-dr)
    (case #{current-dr new-dr}
      #{:up :down} false
      #{:left :right} false
      true)))

(defn snake-nav
  [lp]
  {:event [:midi :control-change]
   :handler (fn [e]
              (let [control (utils/note->control (:data1 e))
                    current-direction (current-direction lp)]
                (when (and (some #{control} [:up :down :left :right])
                           (can-turn? current-direction control))
                  (do
                    (set-direction! lp control)
                    (led/control-led-off lp)
                    (led/control-led-on lp control)))))
   :key :snake-nav})

(defn start-snake
  [lp & [{:keys [speed auto-restart]}]]
  (set-direction! lp :right)
  (let [config (-> lp :config :snake)
        speed (or speed (:speed config))
        auto-restart (or auto-restart (:auto-restart config))]
    (future
      (loop [s init-snake
             m (new-mouse s)]
        (if (bumped? s)
          (do
            (render-game-over lp s)
            (when auto-restart
              (start-snake lp {:speed speed :auto-restart auto-restart})))
          (do
            (render lp s m)
            (Thread/sleep speed)
            (when (direction-fn lp)
              (if (= m (last s))
                (let [new-s (into [(first s)] ((direction-fn lp) s))]
                  (recur new-s (new-mouse new-s)))
                (recur ((direction-fn lp) s) m)))))))))

(defn stop-snake
  [lp]
  (swap! (:state lp) assoc-in [:snake :direction] nil))
