 (ns user
   (:require [skovajsa.system :refer [new-system]]
             [com.stuartsierra.component :as component]
             [skovajsa.launchpad.mode :as mode]
             [skovajsa.launchpad.snake :as snake]
             [skovajsa.launchpad.checkers :as checkers]))

 (def system (new-system))

 (defn start! []
   (alter-var-root #'system component/start))

 (defn stop! []
   (alter-var-root #'system component/stop))

 (defn reset []
   (stop!)
   (start!))

 (defn lp [] (:launchpad system))

 (defn snake
   [& [speed auto-restart]]
   (do
     (mode/set-mode! (lp) :snake)
     (snake/start-snake (lp) {:speed (or speed 300)
                              :auto-restart (or auto-restart true)})))

 (defn checkers
   [& [colors]]
   (mode/set-mode! (lp) :checkers)
   (checkers/start-game (lp) (or colors {:board nil
                                         :player-1 :orange
                                         :player-2 :green})))
