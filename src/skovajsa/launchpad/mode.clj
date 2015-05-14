(ns skovajsa.launchpad.mode
  (:require [skovajsa.launchpad.events :as events]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.utils :as utils]
            [skovajsa.launchpad.grid :as grid]))

(def available-modes [:session :user1 :user2 :mixer :snake])

(defn render-mode
  "Render given mode on a Launchpad receiver."
  [lp mode]
  (do
    (led/control-led-off lp)
    (led/control-led-on lp mode)
    (led/upd-grid lp (grid/current-grid lp) (grid/grid-for-mode lp mode))))

(defn set-mode!
  "Rebinds event handlers depending on the provided mode.
  Returns the Launchpad component with the newly set mode."
  [lp mode]
  (do
    (events/unbind-all! lp)
    (events/bind-for-mode! lp mode)
    (render-mode lp mode)
    (swap! (:state lp) assoc :mode mode)
    lp))

;; This handler is bound on the init stage and persists through all modes.
(defn mode-nav
  "Mode navigation through the four top right round buttons."
  [lp]
  {:event [:midi :control-change]
   :handler (fn [e] (when (some #{(:data1 e)} [108 109 110 111])
                      (set-mode! lp (utils/note->control (:data1 e)))))
   :key :mode-nav})
