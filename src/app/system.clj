(ns app.system
  (:require [com.stuartsierra.component :as component]
            [aero.core :as aero]
            [app.components.webserver :refer [new-web-server]]))

(defn new-system-map []
  (component/system-map
   :web (new-web-server)))

(defn configure [system]
  (let [config (aero/read-config "resources/config.edn")]
    (merge-with merge system config)))

(defn new-dependency-map [] {})

(defn new-system
  "Create the production system"
  []
  (println ";; Setting up new system")
  (-> (new-system-map)
      (configure)
      (component/system-using (new-dependency-map))))
