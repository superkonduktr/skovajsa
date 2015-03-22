(ns skovajsa.launchpad.events
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]))

(defn echo-repl
  []
  {:event [:midi :note-on]
   :handler (fn [e] (prn (:note e)))
   :key :echo-repl})

(defn echo-led
  [rcv]
  {:event [:midi :note-on]
   :handler (fn [e] (midi/midi-note-on rcv (:note e) 120))
   :key :echo-led})

(defn handlers
  [lp]
  {:echo-repl (echo-repl)
   :echo-led (echo-led (:rcv lp))})

(defn bind!
  [lp keys]
  (doseq [h (-> (handlers lp) (select-keys keys) vals)]
    (e/on-event (:event h) (:handler h) (:key h))))

(defn bind-all!
  [lp]
  (bind! lp (keys (handlers lp))))

(defn unbind!
  [lp keys]
  (doseq [h keys]
    (e/remove-event-handler h)))

(defn unbind-all!
  [lp]
  (unbind! lp (keys (handlers lp))))
