(ns app.core
  (:require [com.stuartsierra.component :as component]
            [app.system :as system])
  (:gen-class))

(defn -main []
  (component/start (system/new-system )))

(comment
  (component/stop (system/new-system))
  )
