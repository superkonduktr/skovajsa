(ns skovajsa.launchpad
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]
            [com.stuartsierra.component :as component]
            [skovajsa.launchpad.events :refer [all-events]]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.grid :as grid]))

(defn bind-events!
  [dvc rcv]
  (map (fn [e] (e/on-event (:event e) (:handler e) (:key e)))
       (all-events dvc rcv)))

(defn unbind-events!
  [dvc rcv]
  (doseq [e (all-events dvc rcv)]
    (e/remove-event-handler (:key e))))

(defrecord Launchpad [dvc rcv grid]
  component/Lifecycle
  (start [this]
    (let [d (midi/midi-find-connected-device "Launchpad")
          r (midi/midi-find-connected-receiver "Launchpad")]
      (bind-events! d r)
      (assoc this :dvc d :rcv r :grid (atom grid/init-grid))))
  (stop [this]
    (unbind-events! dvc rcv)
    (led/all-led-off (:rcv this))
    this)
  grid/Grid
  (get-btn [_ x y]
    (grid/get-btn grid x y))
  (upd-btn! [_ x y val vel]
    (grid/upd-btn! grid x y val vel)))

(defn new-launchpad []
  (map->Launchpad {}))
