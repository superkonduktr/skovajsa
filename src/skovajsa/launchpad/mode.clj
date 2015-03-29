(ns skovajsa.launchpad.mode
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]
            [skovajsa.launchpad.events :as events]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.utils :as utils]))

(defn mode
  "Returns current Launchpad mode."
  [lp]
  @(:mode lp))

(defn set-mode!
  "Rebinds event handlers depending on the provided mode.
  Returns the Launchpad component with the newly set mode."
  [lp mode]
  (do
    (events/unbind-all! lp)
    (events/bind-for-mode! lp mode)
    (led/control-led-off lp)
    (led/control-led-on lp mode)
    (reset! (:mode lp) mode)
    lp))

;; This handler is bound on the init stage. It persists through all modes
;; and thus resides in this ns.
(defn mode-nav
  "Mode navigation through the four top right round buttons."
  [lp]
  {:event [:midi :control-change]
   :handler (fn [e] (set-mode! lp (utils/note->control (:data1 e))))
   :key :mode-nav})
