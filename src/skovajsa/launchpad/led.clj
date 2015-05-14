(ns skovajsa.launchpad.led
  (:require [clojure.data :refer [diff]]
            [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]
            [skovajsa.launchpad.utils :as utils]))

(def all-colors
  [:low-red :red :full-red :low-amber :amber :orange :yellow :low-green :green])

(defn grid-led-off
  "Turns off all the square buttons and the round ones on the right."
  [lp]
  (doseq [n (range 127)]
    (midi/midi-note-off (:rcv lp) n)))

(defn control-led-off
  "Turns off all the round control buttons on the top."
  [lp]
  (doseq [n (range 104 112)]
    (midi/midi-control (:rcv lp) n 0)))

(defn all-led-off
  "Turns off all the buttons."
  [lp]
  (grid-led-off lp)
  (control-led-off lp))

(defn btn-on
  [lp xy color]
  (if (nil? color)
    (midi/midi-note-off (:rcv lp) (utils/xy->note xy))
    (midi/midi-note-on (:rcv lp) (utils/xy->note xy) (utils/color->vel color))))

(defn btn-off
  [lp xy]
  (btn-on lp xy nil))

(defn upd-grid
  "Rerenders all the leds based on the diff between two grids.
  Providing the old grid is not required but highly desirable."
  ([lp new-grid]
    (grid-led-off lp)
    (doseq [[xy c] (->> new-grid (filter (comp not nil? val)))]
      (btn-on lp xy c)))
  ([lp old-grid new-grid]
    (let [changed-xy (->> (diff old-grid new-grid) first keys)]
      (doseq [xy changed-xy]
        (let [c (get new-grid xy)]
          (if (nil? c) (btn-off lp xy) (btn-on lp xy c)))))))

(defn lp-print
  [lp notes vel dur]
  (doseq [n notes]
    (midi/midi-note (:rcv lp) n vel dur)))

(def char-map
  {\e [1 2 2 3 4 5 6 17 18 33 34 49 50 50 51 52 53 65 66 81 82 97 98 113 114 114 115 116 117 118]
   \k [1 2 6 17 18 21 33 34 36 49 50 51 65 66 67 81 82 84 97 98 101 113 114 118]
   \t [1 2 3 4 5 6 17 18 19 20 21 22 35 36 51 52 67 68 83 84 99 100 115 116]
   \o [3 4 18 21 33 33 38 38 49 54 65 70 81 86 98 101 115 116]})

(defn lp-print-str
  [lp str color dur]
  (doseq [c str]
    (lp-print (:rcv lp) (get char-map c) (utils/color->vel color) dur)
    (Thread/sleep dur)))

(defn control-led-on
  "Turns on a round control button in the top row.
  Accepts a Launchpad component, a mode keyword, and an optional
  color keyword which defaults to :green."
  ([lp control]
    (control-led-on lp control :green))
  ([lp control color]
    (when (some #{control} [:up :down :left :right :session :user1 :user2 :mixer])
      (midi/midi-control (:rcv lp)
                         (utils/control->note control)
                         (utils/color->vel color)))))

(comment
  (lp-print-str (-> system :launchpad) "kotek" :green 500))
