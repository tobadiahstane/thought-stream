(ns thought-stream.server-test
  (:require
    [ring.adapter.jetty :as j]
    [thought-stream.handler :as handler]
    [ring.middleware.params :refer [wrap-params]]
    [hiccup.core :as h]
    [hiccup.element :as element]
    [compojure.core :refer :all]
    [compojure.route :as route]
    [thought-stream.storage :as store :refer [open-thought-stream-storage *thought-stream-storage*]]
    [clojure.java.jdbc :as jdbc]
    [clojure.test :refer :all]))


(def test-binding  {#'*thought-stream-storage* {:conn {:dbtype "h2:mem" :dbname "./ThoughtStream"}
                                                :aggregate-store :test_aggregates
                                                :event-store :test_events
                                                :read-models {:read-login :test_emails
                                                              :read-thoughts :test_thoughts}}})


(defn delete-test-tables [bound-storage]
  (let [close-test-aggregate-store (jdbc/drop-table-ddl (:aggregate-store bound-storage))
        close-test-event-store (jdbc/drop-table-ddl (:event-store bound-storage))
        close-read-login-table (jdbc/drop-table-ddl (get-in bound-storage [:read-models :read-login]))
        close-read-thought-table (jdbc/drop-table-ddl (get-in bound-storage [:read-models :read-thoughts]))]
    (jdbc/db-do-commands (:conn bound-storage) true [close-test-aggregate-store close-test-event-store close-read-login-table close-read-thought-table])))

(defn test-handle-have-thought [request]
   (with-bindings test-binding
     (try
       (let [_ (open-thought-stream-storage *thought-stream-storage*)
             email (get-in request [:form-params "email"])
             text (get-in request [:form-params "thought"])
             thought-id (store/get-thought-id email)]
        (if (nil? thought-id)
          (let [new-id (java.util.UUID/randomUUID)]
            (do (handler/start-thinking {:id new-id :email email})
              (handler/have-thought {:id new-id :thought text})
              (store/get-thoughts-for new-id)))
         (do
           (handler/have-thought {:id thought-id :thought text})
           (store/get-thoughts-for thought-id))))
       (catch Exception e (print e))
       (finally
        (let [email (get-in request [:form-params "email"])
              thought-id (store/get-thought-id email)
              thoughts (store/get-thoughts-for thought-id)]
          (delete-test-tables *thought-stream-storage*)
        )))))


(defn thought-display [thoughts]
    (for [thought thoughts]
      [:form
       [:textarea (:thought thought)]]))

(defn welcome-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (h/html [:h1 "Thought Streams"]
          [:form {:action "/" :method "post"}
            [:p "Email:" [:br nil]
             [:input {:type "text" :name "email"}]]
            [:p "Thought" [:br nil]
             [:textarea { :rows "4" :cols "50"  :placeholder "Thoughts go here." :name "thought"}]]
            [:input {:type "submit" :text "Have Thought"}]])})

(defn thought-handler [request]
  (let [email (get-in request [:form-params "email"])
        thought-text (get-in request [:form-params "thought"])
        _ (println thought-text)
        result (test-handle-have-thought request)]
    (if (not (nil? result))
      (do (println (thought-display result))
      {:status 200
        :headers {"Content-Type" "text/html"}
        :body (h/html [:h1 "Thought Streams"]
                [:p (str "success! Viewing thoughts for " email "!")]
                (thought-display result)
                [:br nil]
                [:form {:action "/" :method "post"}
                  [:p "Email:" [:br nil]
                   [:input {:type "text" :name "email"}]]
                  [:p "Thought" [:br nil]
                   [:textarea { :rows "4" :cols "50"  :placeholder "Thoughts go here." :name "thought"}]]
                  [:input {:type "submit" :text "Have Thought"}]])
                })
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str "<h1>Thought Streams</h1>
       <p>failure! " email "</p>")})
    ))



(defroutes app
  (GET "/" [] welcome-handler)
  (POST "/" [] thought-handler)
  (route/not-found "<h1>This is not the page you are looking for</h1>
              <p>Sorry, the page you requested was not found!</p>")
  )

(defn run-server [handler port-number]
  (j/run-jetty
    (wrap-params handler)
    {:port (Integer. port-number)}))

(run-server app 3000)

;(comment
;  "initial jetty localhost request map"
;  {:ssl-client-cert nil, :protocol HTTP/1.1,
;    :remote-addr 0:0:0:0:0:0:0:1,
;    :headers {accept text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8, upgrade-insecure-requests 1, connection keep-alive, user-agent Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) LightTable/0.8.1 Chrome/45.0.2454.85 Electron/0.34.5 Safari/537.36, host localhost:3000, accept-encoding gzip, deflate, accept-language en-US},
;    :server-port 3000,
;    :content-length nil,
;    :content-type nil,
;    :character-encoding nil,
;    :uri /,
;    :server-name localhost,
;    :query-string nil,
;    :body #object[org.eclipse.jetty.server.HttpInputOverHTTP 0x7b42ee74 HttpInputOverHTTP@7b42ee74[c=0,q=0,[0]=null,s=STREAM]],
;    :scheme :http,
;    :request-method :get})
