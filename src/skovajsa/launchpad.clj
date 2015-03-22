(ns skovajsa.launchpad
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]
            [com.stuartsierra.component :as component]
            [skovajsa.launchpad.events :as events]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.grid :as grid]))

(defrecord Launchpad [dvc rcv grid handlers mode]
  component/Lifecycle
  (start [this]
    (let [d (midi/midi-find-connected-device "Launchpad")
          r (midi/midi-find-connected-receiver "Launchpad")]
      (events/bind-all! {:dvc d :rcv r})
      (assoc this :dvc d :rcv r :grid (atom grid/init-grid))))
  (stop [this]
    (events/unbind-all! this)
    (led/all-led-off (:rcv this))
    this)
  grid/Grid
  (get-btn [_ x y]
    (grid/get-btn grid x y))
  (upd-btn! [_ x y val vel]
    (grid/upd-btn! grid x y val vel)))

(defn new-launchpad []
  (map->Launchpad {}))
