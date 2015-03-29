(ns skovajsa.launchpad
  (:require [overtone.studio.midi :as midi]
            [com.stuartsierra.component :as component]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.events :as events]
            [skovajsa.launchpad.mode :as mode]
            [clojure.tools.logging :as log]
            [overtone.libs.event :as e]))

(defprotocol Mode
  (get-mode [lp])
  (set-mode! [lp mode]))

(defprotocol Grid
  (get-btn [lp x y])
  (upd-btn! [lp x y val]))

(defn init-launchpad!
  [config]
  (let [d (midi/midi-find-connected-device "Launchpad")
        r (midi/midi-find-connected-receiver "Launchpad")]
    (do
      (if d
        (log/info "Launchpad device connected.")
        (log/warn "Failed to connect Launchpad device."))
      (if r
        (log/info "Launchpad receiver connected.")
        (log/warn "Failed to connect Launchpad receiver."))
      (let [lp {:dvc d :rcv r
                :grid (atom grid/init-grid)
                :mode (atom nil)
                :config config}
            mode-nav (mode/mode-nav lp)]
        (mode/set-mode! lp (:default-mode config))
        (e/on-event (:event mode-nav) (:handler mode-nav) (:key mode-nav))
        lp))))

(defrecord Launchpad [dvc rcv grid handlers mode config]
  component/Lifecycle
  (start [this]
    (merge this (init-launchpad! config)))
  (stop [this]
    (led/all-led-off this)
    (events/unbind-all! this)
    this)
  Mode
  (get-mode [this]
    (mode/mode this))
  (set-mode! [this m]
    (mode/set-mode! this m))
  Grid
  (get-btn [_ x y]
    (grid/get-btn grid x y))
  (upd-btn! [_ x y color]
    (grid/upd-btn! grid x y color)))

(defn new-launchpad [config]
  (map->Launchpad {:config config}))
