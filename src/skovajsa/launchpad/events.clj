(ns skovajsa.launchpad.events
  (:require [overtone.studio.midi :as midi]))

(defn lp-note-on
  [rcv note vel]
  (midi/midi-note-on rcv note vel))

(defn note-tracker
  [rcv]
  {:event [:midi :note-on]
   :handler (fn [e] (lp-note-on rcv (:note e) 120))
   ;:handler (fn [e] (prn (:note e)))
   :key ::note-tracker})

(defn all-events
  [dvc rcv]
  [(note-tracker rcv)])
