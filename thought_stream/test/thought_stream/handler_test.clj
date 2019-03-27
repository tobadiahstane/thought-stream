(ns thought-stream.handler-test
  (:require
    [thought-stream.handler :as handler]
    [thought-stream.thought-stream :as ts :refer [execute]]
    [thought-stream.stream :as stream]
    [thought-stream.storage  :as storage :refer [open-thought-stream-storage *thought-stream-storage*]]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as s]
    [clojure.test :refer :all]))




;-----------------------------------------------

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

(def max-string
  "max length allowable is 300 characters"
  (s/join (repeat 30 "the string")))


(deftest execute-start-thinking-with-invalid-email-throws-invalid-email-exception.
  (let [invalid-email "mela.ningmail.com"
        generated-uuid (stream/new-uuid)
        invalid-command (ts/->StartThinking generated-uuid invalid-email)]
    (is (thrown-with-msg?
          Exception #"Invalid Email."
          (execute invalid-command)))))

(deftest execute-start-thinking-with-nil-email-throws-invalid-email-exception.
  (let [invalid-email nil
        generated-uuid (stream/new-uuid)
        invalid-command (ts/->StartThinking generated-uuid invalid-email)]
    (is (thrown-with-msg?
          Exception #"Invalid Email."
          (execute invalid-command)))))

(deftest can-execute-start-thinking-command-with-valid-inputs
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          valid-email "mela.nin@gmail.com"
          generated-uuid (stream/new-uuid)
          start-thinking-command (ts/->StartThinking generated-uuid valid-email)
          resp (execute start-thinking-command)
          event-loader (stream/->EventTransferObject generated-uuid nil)
          stored-events (:events (stream/load-events event-loader))
          aggregate (stream/thinking-from-history stored-events)
          _ (close-test-tables *thought-stream-storage*)]
      (is (= '(nil) resp))
      (is (instance? thought_stream.thought_stream.ThinkingThoughts aggregate))
      (is (instance? thought_stream.thought_stream.ThinkingStarted (first stored-events)))
      (is (= 1 (:expected-version aggregate))))))


(deftest execute-have-thought-command-with-no-text-throws-error
  (let [no-text nil
        generated-uuid (stream/new-uuid)
        have-thought-command (ts/->HaveThought generated-uuid no-text nil nil nil)]
    (is (thrown-with-msg?
          Exception #"Can't have empty thought."
          (execute have-thought-command)))))

(deftest execute-have-thought-command-with-empty-string-throws-error
  (let [no-text ""
        generated-uuid (stream/new-uuid)
        have-thought-command (ts/->HaveThought generated-uuid no-text nil nil nil)]
    (is (thrown-with-msg?
          Exception #"Can't have empty thought."
          (execute have-thought-command)))))


(deftest execute-have-thought-command-with-empty-string-throws-error
  (let [no-text " "
        generated-uuid (stream/new-uuid)
        have-thought-command (ts/->HaveThought generated-uuid no-text nil nil nil)]
    (is (thrown-with-msg?
          Exception #"Can't have empty thought."
          (execute have-thought-command)))))


(deftest execute-have-thought-command-with-string-greater-than-300-characters-throws-error
  (let [over-max-char (str max-string "1")
        generated-uuid (stream/new-uuid)
        have-thought-command (ts/->HaveThought generated-uuid over-max-char nil nil nil)]
    (is (thrown-with-msg?
          Exception #"Thought over 300 character limit."
          (execute have-thought-command)))))

(deftest can-execute-have-thought-command-with-only-text
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          valid-email "mela.nin@gmail.com"
          generated-uuid (stream/new-uuid)
          start-thinking-command (ts/->StartThinking generated-uuid valid-email)
          _ (execute start-thinking-command)
          valid-text "Mary had a little lamb. Little lamb. Little lamb."
          have-thought-command (ts/->HaveThought generated-uuid valid-text nil nil nil)
          resp (execute have-thought-command)
          event-loader (stream/->EventTransferObject generated-uuid nil)
          stored-events (:events (stream/load-events event-loader))
          aggregate (stream/thinking-from-history stored-events)
          _ (close-test-tables *thought-stream-storage*)]
      (is (= '(1) resp))
      (is (instance? thought_stream.thought_stream.Thought (second stored-events)))
      (is (= 2 (:expected-version aggregate)))
      )))


(deftest start-thinking-thought-takes-a-map-of-id-email-and-executes-start-thinking-command
  (with-bindings test-binding
    (let [_ (open-thought-stream-storage *thought-stream-storage*)
          valid-email "mela.nin@gmail.com"
          generated-uuid (stream/new-uuid)
          resp (handler/start-thinking {:id generated-uuid :email valid-email})
          event-loader (stream/->EventTransferObject generated-uuid nil)
          stored-events (:events (stream/load-events event-loader))
          aggregate (stream/thinking-from-history stored-events)
          _ (close-test-tables *thought-stream-storage*)]
      (is (= '(nil) resp))
      (is (instance? thought_stream.thought_stream.ThinkingThoughts aggregate))
      (is (instance? thought_stream.thought_stream.ThinkingStarted (first stored-events)))
      (is (= 1 (:expected-version aggregate))))))


(deftest have-thought-takes-stores-thought-to-thoughts
    (with-bindings test-binding
      (let [_ (open-thought-stream-storage *thought-stream-storage*)
            valid-email "mela.nin@gmail.com"
            generated-uuid (stream/new-uuid)
            _ (handler/start-thinking {:id generated-uuid :email valid-email})
            valid-text "Mary had a little lamb. Little lamb. Little lamb."
            resp (handler/have-thought {:id generated-uuid :thought valid-text})
            event-loader (stream/->EventTransferObject generated-uuid nil)
            stored-events (:events (stream/load-events event-loader))
            aggregate (stream/thinking-from-history stored-events)
            stored-thoughts (:thought (first (storage/get-thoughts-for generated-uuid)))
            _ (close-test-tables *thought-stream-storage*)]
      (is (= '(1) resp))
      (is (instance? thought_stream.thought_stream.Thought (second stored-events)))
      (is (= 2 (:expected-version aggregate)))
      (is (= valid-text stored-thoughts))
      )))


(run-tests 'thought-stream.handler-test)
