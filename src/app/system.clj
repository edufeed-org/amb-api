(ns app.system
  (:require [com.stuartsierra.component :as component]
            [app.components.webserver :refer [new-web-server]]))

(defn system []
  (component/system-map
   :web (new-web-server)))
