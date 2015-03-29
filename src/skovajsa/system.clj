 (ns skovajsa.system
   (:require [skovajsa.launchpad :refer [new-launchpad]]
             [com.stuartsierra.component :as component]))

 (defn new-system
   []
   (component/system-map
     :launchpad (new-launchpad {:default-mode :session})))
