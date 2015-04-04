(ns skovajsa.launchpad
  (:require [overtone.studio.midi :as midi]
            [com.stuartsierra.component :as component]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.grid :as grid]
            [skovajsa.launchpad.events :as events]
            [skovajsa.launchpad.mode :as mode]
            [clojure.tools.logging :as log]
            [overtone.libs.event :as e]))

(def init-state
  (zipmap mode/available-modes (repeat grid/init-grid)))

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
      (let [lp {:dvc d :rcv r :config config
                :state (atom init-state)
                :mode (atom nil)}
            mode-nav (mode/mode-nav lp)]
        (mode/set-mode! lp (:default-mode config))
        (e/on-event (:event mode-nav) (:handler mode-nav) (:key mode-nav))
        lp))))

(defrecord Launchpad [dvc rcv mode config]
  component/Lifecycle
  (start [this]
    (merge this (init-launchpad! config)))
  (stop [this]
    (led/all-led-off this)
    (events/unbind-all! this)
    this))

(defn new-launchpad [config]
  (map->Launchpad {:config config}))
