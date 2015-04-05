(ns skovajsa.launchpad.snake
  (:require [clojure.set :refer [difference]]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.led :as led]))

(def init-snake [[3 4] [4 4] [5 4]])

(defn up
  [s]
  (let [[head-x head-y] (last s)]
    (conj
      (vec (rest s))
      [head-x (if (< 1 head-y) (dec head-y) 8)])))

(defn down
  [s]
  (let [[head-x head-y] (last s)]
    (conj
      (vec (rest s))
      [head-x (if (> 8 head-y) (inc head-y) 1)])))

(defn left
  [s]
  (let [[head-x head-y] (last s)]
    (conj
      (vec (rest s))
      [(if (< 1 head-x) (dec head-x) 8) head-y])))

(defn right
  [s]
  (let [[head-x head-y] (last s)]
    (conj
      (vec (rest s))
      [(if (> 8 head-x) (inc head-x) 1) head-y])))

(defn mouse
  [lp]
  (-> @(:state lp) :snake :mouse))

(defn new-mouse
  [snake]
  (->> snake
       (difference (set (for [x (range 1 9) y (range 1 9)] [x y])))
       vec rand-nth))

(defn direction-fn
  [lp]
  (case (-> @(:state lp) :snake :direction)
    :up up
    :down down
    :left left
    :right right
    nil))

(defn set-direction!
  [lp dr]
  (swap! (:state lp) assoc-in [:snake :direction] dr))

(defn set-mouse!
  [lp snake]
  (swap! (:state lp) assoc-in [:snake :mouse] (new-mouse snake)))

(defn render
  [lp snake]
  (let [current (grid/current-grid lp)
        new (merge current (zipmap snake (repeat :green))
                   (when-let [m (mouse lp)] {m :amber}))]
    (led/render-grid lp current new)))

(defn init
  [lp]
  (set-mouse! lp init-snake)
  (set-direction! lp :right))

(defn start-snake
  [lp & [{:keys [speed]}]]
  (init lp)
  (future
    (loop [s init-snake]
      (render lp s)
      (Thread/sleep (or speed 300))
      (when (direction-fn lp)
        (recur ((direction-fn lp) s))))))

(defn stop-snake
  [lp]
  (swap! (:state lp) assoc-in [:snake :direction] nil))
