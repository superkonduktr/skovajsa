(ns skovajsa.launchpad.lp-print
  (:require [ovation.launchpad.utils :as utils]
            [overtone.midi :as midi]))

(def char-map
  {\e [1 2 2 3 4 5 6 17 18 33 34 49 50 50 51 52 53 65 66 81 82 97 98 113 114 114 115 116 117 118]
   \k [1 2 6 17 18 21 33 34 36 49 50 51 65 66 67 81 82 84 97 98 101 113 114 118]
   \t [1 2 3 4 5 6 17 18 19 20 21 22 35 36 51 52 67 68 83 84 99 100 115 116]
   \o [3 4 18 21 33 33 38 38 49 54 65 70 81 86 98 101 115 116]})

(defn lp-print
  [lp notes vel dur]
  (doseq [n notes]
    (midi/midi-note (:rcv lp) n vel dur)))

(defn lp-print-str
  [lp str color dur]
  (doseq [c str]
    (lp-print (:rcv lp) (get char-map c) (utils/color->vel color) dur)
    (Thread/sleep dur)))

(comment
  (lp-print-str (-> system :launchpad) "kotek" :green 500))
