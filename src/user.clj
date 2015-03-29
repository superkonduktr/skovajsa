 (ns user
   (:require [skovajsa.system :refer [new-system]]
             [com.stuartsierra.component :as component]))

 (def system (new-system))

 (defn start!
   []
   (alter-var-root #'system component/start))

 (defn stop!
   []
   (alter-var-root #'system component/stop))

 (defn reset
   []
   (stop!)
   (start!))
