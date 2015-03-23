(ns skovajsa.launchpad
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]
            [com.stuartsierra.component :as component]
            [skovajsa.launchpad.events :as events]
            [skovajsa.launchpad.led :as led]
            [skovajsa.launchpad.grid :as grid]
            [clojure.tools.logging :as log]))

(defn get-btn
  [lp x y]
  (get @(:grid lp) [x y]))

(defn upd-btn!
  [lp x y val vel]
  (swap! (:grid lp) assoc [x y] {:val val :vel vel}))

(defn connect-launchpad!
  []
  (let [d (midi/midi-find-connected-device "Launchpad")
        r (midi/midi-find-connected-receiver "Launchpad")]
    (do
      (if d
        (log/info "Launchpad device connected.")
        (log/warn "Failed to connect Launchpad device."))
      (if r
        (log/info "Launchpad receiver connected.")
        (log/warn "Failed to connect Launchpad receiver."))
      {:dvc d :rcv r})))

(defrecord Launchpad [dvc rcv grid handlers mode]
  component/Lifecycle
  (start [this]
    (let [lp (connect-launchpad!)]
      (events/bind-all! lp)
      (assoc this :dvc (:dvc lp)
                  :rcv (:rcv lp)
                  :grid (atom grid/init-grid)
                  :handlers nil
                  :mode nil)))
  (stop [this]
    (events/unbind-all! this)
    (led/all-led-off (:rcv this)))
  grid/Grid
  (get-btn [_ x y]
    (get-btn grid x y))
  (upd-btn! [_ x y val vel]
    (upd-btn! grid x y val vel)))

(defn new-launchpad []
  (map->Launchpad {}))
