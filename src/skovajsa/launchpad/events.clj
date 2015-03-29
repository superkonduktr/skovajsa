(ns skovajsa.launchpad.events
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]))

;; Handlers consist of event
(defn echo-repl
  []
  {:event [:midi :note-on]
   :handler (fn [e] (prn (:note e)))
   :key :echo-repl})

(defn echo-led
  [rcv vel]
  {:event [:midi :note-on]
   :handler (fn [e] (midi/midi-note-on rcv (:note e) vel))
   :key :echo-led})

(defn handlers
  [lp]
  {:echo-repl (echo-repl)
   :echo-led (echo-led (:rcv lp) 120)})

;; All modes have one persistent event handler, :mode-nav,
;; that provides switching between modes.
(def mode-map
  {:session [:echo-repl :echo-led]
   :user1 [:echo-repl]
   :user2 [:echo-repl]
   :mixer [:echo-led]})

(defn handlers-for-mode
  [mode]
  (get mode-map mode))

(defn bind!
  "Binds a seq of events to the Launchpad. Returns the Launchpad component."
  [lp events]
  (doseq [h (-> (handlers lp) (select-keys events) vals)]
    (e/on-event (:event h) (:handler h) (:key h)))
  lp)

(defn unbind!
  [lp keys]
  (doseq [h keys]
    (e/remove-event-handler h))
  lp)

(defn unbind-all!
  [lp]
  (unbind! lp (keys (handlers lp)))
  lp)

(defn bind-for-mode!
  [lp mode]
  (do
    (unbind-all! lp)
    (bind! lp (handlers-for-mode mode)))
  lp)
