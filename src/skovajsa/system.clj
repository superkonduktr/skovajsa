 (ns skovajsa.system
   (:require [com.stuartsierra.component :as component]
             [ovation.launchpad :refer [new-launchpad]]))

 (def config
   {:launchpad {:default-mode :session
                :snake {:speed 300
                        :auto-restart true}
                :draughts {:colors {:board nil
                                    :player-1 :orange
                                    :player-2 :green}}}})

 (defn new-system
   []
   (component/system-map
     :launchpad (new-launchpad (:launchpad config))))
