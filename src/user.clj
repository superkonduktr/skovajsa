 (ns user
   (:require [skovajsa.system :refer [new-system]]
             [com.stuartsierra.component :as component]
             [ovation.launchpad.mode :as mode]
             [skovajsa.launchpad.snake :as snake]
             [skovajsa.launchpad.draughts :as draughts]))

 (def system (new-system))

 (defn start! []
   (alter-var-root #'system component/start))

 (defn stop! []
   (alter-var-root #'system component/stop))

 (defn reset []
   (stop!)
   (start!))

 (defn lp [] (:launchpad system))

 (defn snake []
   (mode/set-mode! (lp) :snake)
   (snake/start-snake (lp)))

 (defn draughts
   [& [colors]]
   (mode/set-mode! (lp) :draughts)
   (draughts/start-game (lp)))
