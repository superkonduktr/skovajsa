(ns skovajsa.launchpad.mode
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]
            [skovajsa.launchpad.events :as events]))

(defprotocol Mode
  (get-mode [lp])
  (set-mode! [lp mode]))

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
    (reset! (:mode lp) mode)
    lp))

;; This handler is bound on the init stage. It persists through all modes
;; and thus resides in this ns.
(defn mode-nav
  "Mode navigation through the 4 round buttons in the very first row on the right."
  [lp]
  {:event [:midi :control-change]
   :handler (fn [e]
              (let [mode (case (:data1 e)
                           108 :session
                           109 :user1
                           110 :user2
                           111 :mixer
                           nil)]
                (set-mode! lp mode)))
   :key :mode-nav})
