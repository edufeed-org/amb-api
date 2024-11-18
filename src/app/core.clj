(ns app.core
  (:require [com.stuartsierra.component :as component]
            [app.system :as system]))

(defn main []
  (component/start (system/system )))

(comment
  (component/stop (system/system))
  )
