(ns skovajsa.launchpad.events
  (:require [overtone.libs.event :as e]
            [skovajsa.launchpad.utils :as utils]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.led :as led]))

(defn toggle-btn
  [lp btn color]
  (let [note (utils/xy->note btn)]
    (if (nil? (grid/get-btn lp btn))
      (do
        (led/note-on lp note color)
        (grid/upd-btn! lp btn color))
      (do
        (led/note-off lp note)
        (grid/upd-btn! lp btn nil)))))

(defn echo-repl
  []
  {:event [:midi :note-on]
   :handler (fn [e] (prn (:note e)))
   :key :echo-repl})

(defn echo-led
  [lp color]
  {:event [:midi :note-on]
   :handler (fn [e] (toggle-btn lp (utils/note->xy (:note e)) color))
   :key :echo-led})

(defn handlers
  [lp]
  {:echo-repl (echo-repl)
   :echo-led (echo-led lp :green)})

;; All modes have one persistent event handler, :mode-nav, that provides
;; switching between modes.
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
