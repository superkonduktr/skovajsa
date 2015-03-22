(ns skovajsa.launchpad.led
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]))

(defn lp-print
  [receiver notes vel dur]
  (doseq [n notes]
    (midi/midi-note receiver n vel dur)))

(def char-map
  {\e [1 2 2 3 4 5 6 17 18 33 34 49 50 50 51 52 53 65 66 81 82 97 98 113 114 114 115 116 117 118]
   \k [1 2 6 17 18 21 33 34 36 49 50 51 65 66 67 81 82 84 97 98 101 113 114 118]
   \t [1 2 3 4 5 6 17 18 19 20 21 22 35 36 51 52 67 68 83 84 99 100 115 116]
   \o [3 4 18 21 33 33 38 38 49 54 65 70 81 86 98 101 115 116]})

(defn lp-print-str
  [rcv str vel dur]
  (doseq [c str]
    (lp-print rcv (get char-map c) (rand-int 127) dur)
    (Thread/sleep dur)))

(defn all-led-off
  [rcv]
  (doseq [n (range 127)]
    (midi/midi-note-off rcv n)))

(comment
  (lp-print-str (-> system :launchpad :rcv ) "kotek" 120 500))
