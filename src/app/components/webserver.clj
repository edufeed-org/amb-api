(ns app.components.webserver
  (:require [io.pedestal.http :as http]
            [com.stuartsierra.component :as component]
            [io.pedestal.http.route :as route]))

(defn respond-hello [request]
  {:status 200 :body "Hello, world!"})

(defn resources-by-user [request]
  {:status 200 :body "hello user"})

(def routes
  (route/expand-routes
   #{["/greet" :get respond-hello :route-name :greet]
     ["/user" :get resources-by-user :route-name :user]}))

(defn create-service []
  (http/create-server
   {::http/routes routes
    ::http/type :jetty
    ::http/port 8890}))

(defn start []
  (http/start (create-service)))

(defrecord WebServer []
  component/Lifecycle
  (start [this]
    (println ";; Starting Web server...")
    (let [server (create-service)]
      (assoc this :server (http/start server))))
  (stop [this]
    (println ";; Stopping Web server...")
    (when-let [server (:server this)]
      (http/stop server))
    (assoc this :server nil)))

(defn new-web-server []
  (map->WebServer {}))
