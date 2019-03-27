(ns thought-stream.storage
  (:require
    [thought-stream.thought-stream :as ts :refer [StoreEvents]]
    [thought-stream.stream :as stream :refer [LoadEvents]]
    [clojure.java.jdbc :as jdbc]
    [clojure.edn :as edn]))


(def ^:dynamic *thought-stream-storage* {:conn {:dbtype "h2" :dbname "./ThoughtStream"}
                                         :aggregate-store :aggregates
                                         :event-store :events
                                         :read-models {:read-login :read_stored_emails
                                                       :read-thoughts :read_stored_thoughts}})

(defn create-aggregate-table
  ([]
   (create-aggregate-table *thought-stream-storage*))
  ([storage]
   (jdbc/create-table-ddl
     (:aggregate-store storage)
     [[:AggregateID "UUID" :primary :key]
      [:Type "varchar(255)"]
      [:Version :int]]
     {:conditional? true})))

(defn create-event-table
  ([]
   (create-event-table *thought-stream-storage*))
  ([storage]
   (jdbc/create-table-ddl
     (:event-store storage)
     [[:AggregateID "UUID"]
      [:Event :LONGVARCHAR] [:Version :int]
      [:foreign :key "(AggregateID)" :references (str (name (:aggregate-store storage)) "(AggregateID)")]]
     {:conditional? true})))

(defn create-read-login-table
  ([]
   (create-read-login-table *thought-stream-storage*))
  ([storage]
   (jdbc/create-table-ddl
     (get-in storage [:read-models :read-login])
     [[:AggregateID "UUID"]
      [:EMAIL "varchar(255)"]
      [:foreign :key "(AggregateID)" :references (str (name (:aggregate-store storage)) "(AggregateID)")]]
     {:conditional? true})))

(defn create-read-thoughts-table
  ([]
   (create-read-thoughts-table *thought-stream-storage*))
  ([storage]
  (jdbc/create-table-ddl
    (get-in storage [:read-models :read-thoughts])
    [[:AggregateID "UUID"]
     [:Thoughts :LONGVARCHAR]
     [:foreign :key "(AggregateID)" :references (str (name (:aggregate-store storage)) "(AggregateID)")]]
    {:conditional? true})))

(defn open-thought-stream-storage
  ([]
   (let [aggregate-statement (create-aggregate-table *thought-stream-storage*)
         event-statement (create-event-table *thought-stream-storage*)
         read-login-statement (create-read-login-table *thought-stream-storage*)
         read-thoughts-statement (create-read-thoughts-table *thought-stream-storage*)]
     (jdbc/db-do-commands (:conn *thought-stream-storage*) [aggregate-statement event-statement read-login-statement read-thoughts-statement])))
  ([storage]
   (let [aggregate-statement (create-aggregate-table storage)
         event-statement (create-event-table storage)
         read-login-statement (create-read-login-table storage)
         read-thoughts-statement (create-read-thoughts-table storage)]
     (jdbc/db-do-commands (:conn *thought-stream-storage*) [aggregate-statement event-statement read-login-statement read-thoughts-statement]))))


(defn cast-event-to-string [event]
  (prn-str event))

(defmulti event-reader (fn [reader-tag event] (resolve reader-tag)))

(defmethod event-reader :default
  [reader-tag event]
  {:tag reader-tag :value event})

(defmethod event-reader thought_stream.thought_stream.ThinkingStarted
  [reader-tag event]
  ((symbol (str (namespace reader-tag) "/map->" (name reader-tag)) event)))

(defmethod event-reader thought_stream.thought_stream.Thought
  [reader-tag event]
  (ts/map->Thought event))


(defn event-from-string [event-string]
  (edn/read-string {:default event-reader} event-string))

(defn event-from-record [event-record]
 (event-from-string (:event event-record)))

(defn events-from-query [events]
  (map event-from-record events))


(defn get-events-by-id [aggregate-id]
  (let [query [(str "select * from " (name (:event-store *thought-stream-storage*)) " where aggregateid ='" aggregate-id "' order by version")]]
    (events-from-query (jdbc/query (:conn *thought-stream-storage*) query))))


(defmulti update-read-models (fn [storage event] (class event)))

(defn update-email-read-model [storage thinking-started]
  (jdbc/insert!
    (:conn storage)
    (get-in storage [:read-models :read-login])
    {:Email (:email thinking-started)  :AggregateID (:id thinking-started)})
  )


(defn update-read-thoughts [storage thinking]
  (jdbc/with-db-transaction [t-conn (:conn storage)]
    (let [thoughts (:free-thoughts thinking)
          table (get-in *thought-stream-storage* [:read-models :read-thoughts])
          row {:Thoughts (pr-str thoughts) :AggregateID (:id thinking)}
          result (jdbc/update! t-conn table row [(str "aggregateid ='" (:id thinking) "'")])]
      (if (zero? (first result))
        (jdbc/insert! t-conn table row)
        result))))


(defmethod update-read-models thought_stream.thought_stream.ThinkingStarted
  [storage thinking-started]
  (update-email-read-model storage thinking-started))

(defmethod update-read-models thought_stream.thought_stream.ThinkingThoughts
  [storage thinking]
  (update-read-thoughts storage thinking))


(defmethod update-read-models :default
  [storage not-needed]
  nil)


(extend-protocol StoreEvents
  thought_stream.thought_stream.Thought
  (ts/store [thinking-started]))


;;; ------------------------------------------ Start Event Store Module -----------------------------------------------------------


(defn get-stored-aggregate-version [t-conn aggregate]
  (let [check-version-query [(str "select Version from " (name (:aggregate-store *thought-stream-storage*)) " where aggregateid = '" (:id aggregate) "'")]]
    (:version (first (jdbc/query t-conn check-version-query))))
  )

(defn calculate-new-version [aggregate]
  (+ (:expected-version aggregate) (count (:changes aggregate))))


(defn validate-new-version [aggregate new-version]
  (if-not (or (= (:version (last (:changes aggregate))) new-version)
              (= 0 new-version))
      (throw (Exception.))))



(defn insert-new-aggregate [t-conn aggregate]
  (jdbc/insert! t-conn
    (:aggregate-store *thought-stream-storage*)
    {:AggregateID (:id aggregate) :Type (pr-str (class aggregate)) :Version 0}))

(defn insert-new-event [t-conn event]
  (jdbc/insert!
    t-conn
    (:event-store *thought-stream-storage*)
    {:AggregateID (:id event) :Event (cast-event-to-string event) :Version (:version event)}))

(defn update-aggregate-version [t-conn aggregate new-version]
  (jdbc/update! t-conn
    (:aggregate-store *thought-stream-storage*)
    {:Version new-version}
    [(str "AggregateID = '" (:id aggregate) "'")]))




(defn confirm-aggregate-stored [t-conn aggregate]
  (if (not (some? (get-stored-aggregate-version t-conn aggregate)))
    (insert-new-aggregate t-conn aggregate)))

(defn validate-stored-version-matches-expected-version [t-conn aggregate]
  (if-not (= (get-stored-aggregate-version t-conn aggregate) (:expected-version aggregate))
    (throw (Exception.))))



(defn store-changes [t-conn aggregate]
  (doseq [event (:changes aggregate)]
    (insert-new-event t-conn event)))

(defn update-aggregate [t-conn aggregate]
  (let [new-version (calculate-new-version aggregate)]
    (validate-new-version aggregate new-version)
     (update-aggregate-version t-conn aggregate new-version)))


(defn validate-stored-aggregate-matches-expected [t-conn aggregate]
  (confirm-aggregate-stored t-conn aggregate)
  (validate-stored-version-matches-expected-version t-conn aggregate))

(defn store-events [t-conn aggregate]
  (store-changes t-conn aggregate)
  (update-aggregate t-conn aggregate))



(defn store-events-in-transaction [aggregate]
  (jdbc/with-db-transaction [t-conn (:conn *thought-stream-storage*)]
    (validate-stored-aggregate-matches-expected t-conn aggregate)
    (store-events t-conn aggregate)))

;;; ------------------------------------------ End Event Store Module -----------------------------------------------------------

(defn store-events-and-update-read-models [aggregate]
  (do
    (store-events-in-transaction aggregate)
    (doseq [event (:changes aggregate)]
      (update-read-models *thought-stream-storage* event))
    (update-read-models *thought-stream-storage* aggregate)))

(extend-protocol StoreEvents
  thought_stream.thought_stream.ThinkingThoughts
  (ts/store [thinking-thoughts]
    (store-events-and-update-read-models thinking-thoughts)))


(defn get-thought-id [email]
  (let [query [(str "select * from " (name (get-in *thought-stream-storage* [:read-models :read-login])) " where email ='" email "'")]
        result (jdbc/query (:conn *thought-stream-storage*) query)]
    (:aggregateid (first result))))



(defn get-thoughts-for [thought-id]
  (let [query [(str "select thoughts from " (name (get-in *thought-stream-storage* [:read-models :read-thoughts])) " where aggregateid ='" thought-id "'")]]
     (edn/read-string  (:thoughts (first (jdbc/query (:conn *thought-stream-storage*) query))))))


(extend-protocol LoadEvents
  thought_stream.stream.EventTransferObject
  (stream/load-events [transfer-object]
    (assoc transfer-object :events (get-events-by-id (:id transfer-object)))))


