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

 (defn snake []
   (mode/set-mode! (lp) :snake)
   (snake/start-snake (lp)))

 (defn checkers
   [& [colors]]
   (mode/set-mode! (lp) :checkers)
   (checkers/start-game (lp)))
