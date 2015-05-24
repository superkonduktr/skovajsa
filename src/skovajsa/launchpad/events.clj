(ns skovajsa.launchpad.events
  (:require [overtone.libs.event :as e]
            [skovajsa.launchpad.utils :as utils]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.snake :as snake]
            [skovajsa.launchpad.draughts :as draughts]))

(defn toggle-btn
  [lp btn color]
  (if (nil? (grid/get-btn lp btn))
    (do
      (led/btn-on lp btn color)
      (grid/upd-btn! lp btn color))
    (do
      (led/btn-off lp btn)
      (grid/upd-btn! lp btn nil))))

(defn echo-repl
  []
  {:event [:midi :note-on]
   :handler (fn [e]
              (let [n (:note e)]
                (prn (format "cell %s, midi %s" (utils/note->xy n) n))))
   :key :echo-repl})

(defn echo-led
  [lp color]
  {:event [:midi :note-on]
   :handler (fn [e] (toggle-btn lp (utils/note->xy (:note e)) color))
   :key :echo-led})

(defn handlers
  [lp]
  {:echo-repl (echo-repl)
   :echo-led (echo-led lp :green)
   :snake-nav (snake/snake-nav lp)
   :draughts-handler (draughts/draughts-handler lp)})

;; All modes have one persistent event handler, :mode-nav, that provides
;; switching between modes.
(defn handlers-for-mode
  [mode]
  ({:session [:echo-repl :echo-led]
    :user1 [:echo-repl]
    :user2 [:echo-repl]
    :mixer [:echo-led]
    :snake [:snake-nav]
    :draughts [:draughts-handler]} mode))

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
