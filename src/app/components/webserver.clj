(ns app.components.webserver
  (:require [io.pedestal.http :as http]
            [com.stuartsierra.component :as component]
            [io.pedestal.http.route :as route]
            [nostr.core :as nostr]
            [nostr.edufeed :as edufeed]
            [cheshire.core :as json]
            [json-schema.core :as json-schema]
            [clojure.core.async :refer [go chan >! <!]]))

(def schema (json-schema/prepare-schema
             (-> "resources/schema.json"
                 slurp
                 (cheshire.core/parse-string true))))

(defn validate [resources]
  (try
    (let [validation-results
          (map (fn [resource]
                 (try
                   (when (json-schema/validate schema resource)
                     {:valid true :resource resource})
                   (catch Exception e
                     {:valid false :error e :resource resource})))
               resources)]
      (do
          ;; Log invalid resources
        (doseq [{:keys [error resource]} (filter #(not (:valid %)) validation-results)]
          #_(println "Invalid resource:" resource "Error:" error))
          ;; Return only valid resources
        (->> validation-results
             (filter :valid)
             (map :resource))))
    (catch Exception e
      #_(println "Unexpected error during validation" e)
      nil)))

(defn ok [body]
  {:status 200 :body body})

;; Async pedestal
(defn get-resources [request]
  (let [result-chan (chan)]
    (go
      (let [npub (get-in request [:query-params :npub])
            pk (get-in request [:query-params :pk])
            relay (get-in request [:relay])
            filter (if pk
                     {:kinds [30142] :authors [pk]}
                     {:kinds [30142]})
            resources (nostr/fetch-events relay filter)
            transformed (map #(edufeed/convert-30142-to-nostr-amb % true true) resources)
            validate (validate transformed)
            resp (if validate
                   transformed
                   {:response "Some error occured during validation against AMB Scheme. Better give you nothing than wrong data"})]
        (>! result-chan (json/generate-string resp))))
    result-chan))

(def resources-by-user
  {:name ::resources-by-user
   :enter (fn [context]
            (go
              (let [result (<! (get-resources (:request context)))]
                (assoc context :response (ok result)))))})

;; Interceptor to add the WebSocket port to the request context
(defn add-relay [relay]
  {:name ::add-ws-port
   :enter (fn [context]
            (assoc-in context [:request :relay] relay))})

(defn routes
  [relay]
  (route/expand-routes
   #{["/resources" :get [(add-relay relay) resources-by-user] :route-name :resources]}))

(def service-map
  {::http/routes routes
   ::http/type :jetty
   ::http/port 8890})

(defn create-service [relay]
  (http/create-server
   (assoc service-map
          ::http/routes (routes relay)
          ::http/port 8890)))

(defrecord WebServer [relay]
  component/Lifecycle
  (start [this]
    (println ";; Starting Webserver")
    (println ";; Using Relay: " relay)
    (let [server (create-service relay)]
      (assoc this :server (http/start server))))
  (stop [this]
    (println ";; Stopping Web server...")
    (when-let [server (:server this)]
      (http/stop server))
    (assoc this :server nil)))

(defn new-web-server []
  (map->WebServer {}))

