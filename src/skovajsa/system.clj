 (ns skovajsa.system
   (:require [skovajsa.launchpad :refer [new-launchpad]]
             [com.stuartsierra.component :as component]))

 (def config
   {:launchpad {:default-mode :session
                :snake {:speed 300
                        :auto-restart true}}})

 (defn new-system
   []
   (component/system-map
     :launchpad (new-launchpad (:launchpad config))))
