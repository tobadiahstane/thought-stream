(ns thought-stream.storage-test
  (:require
    [thought-stream.thought-stream :as ts :refer [ExecuteCommand]]
    [thought-stream.state :refer [update-state aggregate-from-history]]
    [thought-stream.stream :as stream]
    [thought-stream.storage :as storage :refer [open-thought-stream-storage *thought-stream-storage*]]
    [clojure.java.jdbc :as jdbc]
    [clojure.edn :as edn]
    [clojure.test :refer :all]))



(def test-binding  {#'*thought-stream-storage* {:conn {:dbtype "h2:mem" :dbname "./ThoughtStream"}
                                                :aggregate-store :test_aggregates
                                                :event-store :test_events
                                                :read-models {:read-login :test_emails
                                                              :read-thoughts :test_thoughts}}})

(defn close-test-tables [bound-storage]
  (let [close-test-aggregate-store (jdbc/drop-table-ddl (:aggregate-store bound-storage))
        close-test-event-store (jdbc/drop-table-ddl (:event-store bound-storage))
        close-read-login-table (jdbc/drop-table-ddl (get-in bound-storage [:read-models :read-login]))
        close-read-thought-table (jdbc/drop-table-ddl (get-in bound-storage [:read-models :read-thoughts]))]
    (jdbc/db-do-commands (:conn bound-storage) true [close-test-aggregate-store close-test-event-store close-read-login-table close-read-thought-table])))

(deftest cast-to-string-converts-a-thinking-started-event-to-its-string
  (let [event  (ts/->ThinkingStarted "id" "email")
        compare-string (prn-str event)
        event-str (storage/cast-event-to-string event)]
  (is (= compare-string event-str))))

(deftest read-event-converts-thinking-started-event-string-back-to-event
  (let [event-str (storage/cast-event-to-string (ts/->ThinkingStarted "id" "email"))
        event (storage/event-from-string event-str)]
    (is (instance? thought_stream.thought_stream.ThinkingStarted event))))


(deftest can-set-event-store-for-with-transaction
  (is (some? *thought-stream-storage*))
  (is (some #{:conn} (keys *thought-stream-storage*)))
  (is (some #{:aggregate-store} (keys *thought-stream-storage*)))
  (is (= :aggregates (:aggregate-store *thought-stream-storage*)))
  (is (some #{:event-store} (keys *thought-stream-storage*)))
  (is (= :events (:event-store *thought-stream-storage*)))
  (is (some #{:dbtype} (keys (:conn *thought-stream-storage*))))
  (is (= "h2" (get-in *thought-stream-storage* [:conn :dbtype])))
  (is (some #{:dbname} (keys (:conn *thought-stream-storage*))))
  (is (= "./ThoughtStream" (get-in *thought-stream-storage* [:conn :dbname])))
  (is (true? (:dynamic (meta #'*thought-stream-storage*))))
  (is (= :read_stored_emails (get-in *thought-stream-storage* [:read-models :read-login])))
  (is (= :read_stored_thoughts (get-in *thought-stream-storage* [:read-models :read-thoughts]))))

(deftest can-connect-to-*thought-stream-storage*
  (is (= (jdbc/create-table-ddl :aggregates [[:AggregateID "UUID" :primary :key] [:Type "varchar(255)"] [:Version :int ]] {:conditional? true})
         (storage/create-aggregate-table)))
  (is (= (jdbc/create-table-ddl :events [[:AggregateID "UUID"] [:Event :LONGVARCHAR] [:Version :int] [:foreign :key "(AggregateID)" :references "aggregates(AggregateID)"]] {:conditional? true})
         (storage/create-event-table)))
  (is (= (jdbc/create-table-ddl :read_stored_emails [[:AggregateID "UUID"] [:EMAIL "varchar(255)"] [:foreign :key "(AggregateID)" :references "aggregates(AggregateID)"]] {:conditional? true})
         (storage/create-read-login-table)))
  (is (= (jdbc/create-table-ddl :read_stored_thoughts [[:AggregateID "UUID"] [:Thoughts :LONGVARCHAR] [:foreign :key "(AggregateID)" :references "aggregates(AggregateID)"]] {:conditional? true})
         (storage/create-read-thoughts-table)))
  (is (= '(0,0,0,0) (storage/open-thought-stream-storage))))

(deftest can-rebind-thought-stream-tables-for-testing
  (with-bindings test-binding
    (let [testing (open-thought-stream-storage *thought-stream-storage*)
          result (close-test-tables *thought-stream-storage*)]
      (is (= :test_aggregates (:aggregate-store *thought-stream-storage*)))
      (is (= :test_events (:event-store *thought-stream-storage*)))
      (is (= :test_emails (get-in *thought-stream-storage* [:read-models :read-login])))
      (is (= :test_thoughts (get-in *thought-stream-storage* [:read-models :read-thoughts])))
      (is (= '(0,0,0,0) testing))
      (is (= '(0,0,0,0) result)))))


(deftest can-store-aggregate-with-version-being-zero
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          query [(str "select * from test_aggregates where aggregateid ='" thought-id "'")]
          result (jdbc/query (:conn *thought-stream-storage*) query)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (= 0 (:version (first result))))
      (is (= (pr-str (class aggregate)) (:type (first result))))
      (is (= thought-id (:aggregateid (first result)))))))

(deftest can-store-aggregate-with-version-given-one-event
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          query [(str "select * from test_aggregates where aggregateid ='" thought-id "'")]
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          _ (ts/store aggregate-updated)
          second-result (jdbc/query (:conn *thought-stream-storage*) query)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (= 1 (:version (first second-result))))
      ;(is (= (pr-str (class aggregate)) (:type (first second-result))))
      ;(is (= thought-id (:aggregateid (first second-result))))
      )))

(deftest can-update-aggregate-version-given-one-event
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          query [(str "select * from test_aggregates where aggregateid ='" thought-id "'")]
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          _ (ts/store aggregate-updated)
          second-result (jdbc/query (:conn *thought-stream-storage*) query)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (= 1 (:version (first second-result))))
      (is (= (pr-str (class aggregate)) (:type (first second-result))))
      (is (= thought-id (:aggregateid (first second-result)))))))

(deftest can-update-aggregate-version-given-two-events
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          query [(str "select * from test_aggregates where aggregateid ='" thought-id "'")]
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          updated-again (update aggregate-updated :changes conj (assoc (ts/map->Thought {:id thought-id :thought "thought-text"}) :version 2))
          _ (ts/store updated-again)
          third-result (jdbc/query (:conn *thought-stream-storage*) query)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (= 2 (:version (first third-result))))
      (is (= (pr-str (class aggregate)) (:type (first third-result))))
      (is (= thought-id (:aggregateid (first third-result)))))))


(deftest if-aggregate-stored-version-mismatch-expected-version-throws-exception
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          _ (jdbc/update! (:conn *thought-stream-storage*)
              (:aggregate-store *thought-stream-storage*)
              {:Version 1}
              [(str "AggregateID = '" (:id aggregate) "'")])
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))]
      (is (thrown? Exception (ts/store aggregate-updated)))
      (close-test-tables *thought-stream-storage*))))


(deftest can-store-aggregate-events-to-the-event-store-one-event
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          event (assoc (ts/->ThinkingStarted thought-id "email") :version 1)
          aggregate-updated (update aggregate :changes conj event)
          _ (ts/store aggregate-updated)
          query [(str "select * from test_events where aggregateid ='" thought-id "' order by version")]
          result (jdbc/query (:conn *thought-stream-storage*) query)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (not (empty? result)))
      (is (= event (first (storage/events-from-query result))))
      (is (= 1 (:version (first result))))
    )
  )
)


(deftest can-store-aggregate-events-to-the-event-store-second-event
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          test-event (assoc (ts/map->Thought {:id thought-id :thought "thought-text"}) :version 2)
          updated-again (update aggregate-updated :changes conj test-event)
          _ (ts/store updated-again)
          query [(str "select * from test_events where aggregateid ='" thought-id "' order by version")]
          result (jdbc/query (:conn *thought-stream-storage*) query)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (= 2 (count result)))
      (is (= test-event (second (storage/events-from-query result))))
      (is (= 2 (:version (second result))))
      )
    )
  )



(deftest store-aggregate-throws-exception-if-last-change-version-doesnt-match-new-aggregate-version
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          event (assoc (ts/->ThinkingStarted thought-id "email") :version 2)
          aggregate-updated (update aggregate :changes conj event)]
      (is (thrown? Exception (ts/store aggregate-updated)))
      (close-test-tables *thought-stream-storage*)
    )
  )
)

(deftest store-aggregate-throwing-exception-rolls-transaction-back
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          event (assoc (ts/->ThinkingStarted thought-id "email") :version 2)
          aggregate-updated (update aggregate :changes conj event)
          query [(str "select * from test_aggregates where aggregateid ='" thought-id "'")]
          failure (try (ts/store aggregate-updated) (catch Exception e))
          result (jdbc/query (:conn *thought-stream-storage*) query)
          _ (close-test-tables *thought-stream-storage*)]
      (is (empty? result))
    )
  )
)


(deftest can-get-events-of-aggregate-stored-by-id
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          test-event (assoc (ts/map->Thought {:id thought-id :thought "thought-text"}) :version 2)
          updated-again (update aggregate-updated :changes conj test-event)
          _ (ts/store updated-again)
          result (storage/get-events-by-id thought-id)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (= 2 (count result)))
      (is (= test-event (second result)))
      (is (= 2 (:version (second result))))
    )
  )
)


(deftest can-load-events-of-an-empty-aggregate-given-an-event-transfer-object-with-an-id
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          test-event (assoc (ts/map->Thought {:id thought-id :thought "thought-text"}) :version 2)
          updated-again (update aggregate-updated :changes conj test-event)
          _ (ts/store updated-again)
          event-container (stream/->EventTransferObject thought-id nil)
          result (stream/load-events event-container)
          events (:events result)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (instance? thought_stream.stream.EventTransferObject result))
      (is (= 2 (count events)))
      (is (= test-event (second events)))
      )
    )
  )


(deftest can-store-thinking-email-with-aggregate-as-read-model
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thinking-id (java.util.UUID/randomUUID)
          thinking-email "mela.nin@gmail.com"
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thinking-id) [])
          read-model (get-in *thought-stream-storage* [:read-models :read-login])
          _ (jdbc/insert! (:conn *thought-stream-storage*)
                    (:aggregate-store *thought-stream-storage*)
                    {:AggregateID (:id aggregate) :Type (pr-str (class aggregate)) :Version 0})
          _ (jdbc/insert! (:conn *thought-stream-storage*) read-model {:Email thinking-email  :AggregateID thinking-id})
          check (storage/get-thought-id thinking-email)
          _ (close-test-tables *thought-stream-storage*)
          ]
      (is (= thinking-id check))
      )
    )
  )


(deftest can-update-read-model-with-thinking-started-event
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thinking-id (java.util.UUID/randomUUID)
          thinking-email "mela.nin@gmail.com"
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thinking-id) [])
          read-model (get-in *thought-stream-storage* [:read-models :read-login])
          _ (jdbc/insert! (:conn *thought-stream-storage*)
                    (:aggregate-store *thought-stream-storage*)
                    {:AggregateID (:id aggregate) :Type (pr-str (class aggregate)) :Version 0})
          event (ts/->ThinkingStarted thinking-id thinking-email)
          testing (storage/update-read-models *thought-stream-storage* event)
          check (storage/get-thought-id thinking-email)
         _ (close-test-tables *thought-stream-storage*)]
      (is (= thinking-id check )))))


(deftest attempting-to-store-email-without-aggregate-already-stored-throws-exception
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thinking-id (java.util.UUID/randomUUID)
          thinking-email "mela.nin@gmail.com"
          read-model (get-in *thought-stream-storage* [:read-models :read-login])
          event (ts/->ThinkingStarted thinking-id thinking-email)]
      (is (thrown? Exception (storage/update-email-read-model *thought-stream-storage* event)))
      (close-test-tables *thought-stream-storage*)
  )))


(deftest storing-aggregate-stores-events-to-read-models
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thinking-id (java.util.UUID/randomUUID)
          thinking-email "mela.nin@gmail.com"
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thinking-id) [])
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thinking-id thinking-email) :version 1))
          _ (ts/store aggregate-updated)
          check (storage/get-thought-id thinking-email)
          _ (close-test-tables *thought-stream-storage*)]
      (is (= thinking-id check )))))




(deftest can-store-aggregate-thoughts-to-read-thoughts-read-model
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          test-event (assoc (ts/map->Thought {:id thought-id :thought "thought-text"}) :version 2)
          updated-again (update aggregate-updated :changes conj test-event)
          _ (ts/store updated-again)
          event-container (stream/->EventTransferObject thought-id nil)
          returned-container (stream/load-events event-container)
          events (:events returned-container)
          using-for-test (aggregate-from-history (ts/->ThinkingThoughts thought-id) events)
          check-against (:free-thoughts using-for-test)
          check (storage/update-read-models *thought-stream-storage* using-for-test)
          result (storage/get-thoughts-for thought-id)
          _ (close-test-tables *thought-stream-storage*)]
      (is (= check-against result)))))

(deftest can-update-aggregate-thoughts-to-read-thoughts-read-model
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          test-event (assoc (ts/map->Thought {:id thought-id :thought "thought-text"}) :version 2)
          updated-again (update aggregate-updated :changes conj test-event)
          _ (ts/store updated-again)
          event-container (stream/->EventTransferObject thought-id nil)
          returned-container (stream/load-events event-container)
          events (:events returned-container)
          using-for-test (aggregate-from-history (ts/->ThinkingThoughts thought-id) events)
          _(storage/update-read-models *thought-stream-storage* using-for-test)
          next-test-event (assoc (ts/map->Thought {:id thought-id :thought "next-thought-text"}) :version 3)
          final-update (update using-for-test :changes conj next-test-event)
          _ (ts/store final-update)
          next-container (stream/->EventTransferObject thought-id nil)
          next-returned-container (stream/load-events next-container)
          next-events (:events next-returned-container)
          next-using-for-test (aggregate-from-history (ts/->ThinkingThoughts thought-id) next-events)
          check-against (:free-thoughts next-using-for-test)
          _ (storage/update-read-models *thought-stream-storage* next-using-for-test)
          result (storage/get-thoughts-for thought-id)
          _ (close-test-tables *thought-stream-storage*)]
      (is (= result check-against)))))

(deftest can-update-aggregate-thoughts-to-read-thoughts-read-model
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          test-event (assoc (ts/map->Thought {:id thought-id :thought "thought-text"}) :version 2)
          updated-again (update aggregate-updated :changes conj test-event)
          _ (ts/store updated-again)
          event-container (stream/->EventTransferObject thought-id nil)
          returned-container (stream/load-events event-container)
          events (:events returned-container)
          using-for-test (aggregate-from-history (ts/->ThinkingThoughts thought-id) events)
          _(storage/update-read-models *thought-stream-storage* using-for-test)
          next-test-event (assoc (ts/map->Thought {:id thought-id :thought "next-thought-text"}) :version 3)
          final-update (update using-for-test :changes conj next-test-event)
          _ (ts/store final-update)
          next-container (stream/->EventTransferObject thought-id nil)
          next-returned-container (stream/load-events next-container)
          next-events (:events next-returned-container)
          next-using-for-test (aggregate-from-history (ts/->ThinkingThoughts thought-id) next-events)
          check-against (:free-thoughts next-using-for-test)
          _ (storage/update-read-models *thought-stream-storage* next-using-for-test)
          result (storage/get-thoughts-for thought-id)
          _ (close-test-tables *thought-stream-storage*)]
      (is (= result check-against)))))

(deftest storing-aggregate-stores-thoughts-to-read-thoughts-read-model
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          thought-id (java.util.UUID/randomUUID)
          aggregate (aggregate-from-history (ts/->ThinkingThoughts thought-id) [])
          _ (ts/store aggregate)
          aggregate-updated (update aggregate :changes conj (assoc (ts/->ThinkingStarted thought-id "email") :version 1))
          test-event (assoc (ts/map->Thought {:id thought-id :thought "thought-text"}) :version 2)
          updated-again (update aggregate-updated :changes conj test-event)
          _ (ts/store updated-again)
          event-container (stream/->EventTransferObject thought-id nil)
          returned-container (stream/load-events event-container)
          events (:events returned-container)
          using-for-test (aggregate-from-history (ts/->ThinkingThoughts thought-id) events)
          _(storage/update-read-models *thought-stream-storage* using-for-test)
          next-test-event (ts/map->Thought {:id thought-id :thought "next-thought-text"})
          final-update (stream/update-thoughts using-for-test next-test-event)
          _ (ts/store final-update)
          next-container (stream/->EventTransferObject thought-id nil)
          next-returned-container (stream/load-events next-container)
          next-events (:events next-returned-container)
          next-using-for-test (aggregate-from-history (ts/->ThinkingThoughts thought-id) next-events)
          check-against (:free-thoughts next-using-for-test)
          result (storage/get-thoughts-for thought-id)
          _ (close-test-tables *thought-stream-storage*)]
      (is (= result check-against)))))


(run-tests 'thought-stream.storage-test)

