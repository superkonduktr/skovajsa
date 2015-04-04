(ns skovajsa.launchpad.snake
  (require [skovajsa.launchpad.led :as led]))

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

(defn render
  [lp snake]
  (doseq [b snake]
    (led/btn-on lp b :amber)))

(defn set-direction!
  [lp d]
  (swap! (:state lp) assoc-in [:snake] {:direction d}))

(defn direction-fn
  [lp]
  (case (-> @(:state lp) :snake :direction)
    :up up
    :down down
    :left left
    :right right))

(defn start
  [lp & [{:keys [speed]}]]
  (set-direction! lp :right)
  (future
    (loop [s init-snake]
      (render lp s)
      (Thread/sleep (or speed 300))
      (recur ((direction-fn lp) s)))))
