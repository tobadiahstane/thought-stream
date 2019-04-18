(ns thought-stream.commands.execution_test
  (:require
    [thought-stream.commands.execution :as ex :refer [ICommand IExecuteCommand IEventStoreExecution]]
    [thought-stream.commands.config :as ccg]
    [thought-stream.utilities :as util]
    [thought-stream.state :as state]
    [clojure.test :refer :all]))


(defn uuid->keyword [id]
  (if-not (util/valid-id? id)
    (throw (IllegalArgumentException. "UUID to keyword expects UUID."))
    (keyword (str id))))



(defrecord StubEvent [id]
  state/EventTransition
  (state/transition [event aggregate]
    (if (nil? (:events-applied aggregate))
      (assoc aggregate :events-applied 1)
      (update aggregate :events-applied inc))))

(defrecord CommandThrowingIllegalArgument [id]
  ICommand
  (ex/execute [command aggregate]
    (throw (IllegalArgumentException. "Testing command throws illegal argument exception."))))

(defrecord StubCommand [id]
  ICommand
  (ex/execute [command aggregate]
    (-> (state/update-state aggregate (->StubEvent (:id aggregate)))
      (assoc :executed true))))

(defrecord StoringThrowsIllegalArgument [unused]
  IEventStoreExecution
  (ex/load-events [return-none _]
    nil)
  (ex/store-aggregate [test-es aggregate]
    (throw (IllegalArgumentException. "Storing aggregate throws and illegal argument exception."))))

(defrecord ReturnsNoEvents [es-atom]
  IEventStoreExecution
  (ex/load-events [return-none _]
    nil)
  (ex/store-aggregate [test-es aggregate]
    aggregate))

(defrecord ReturnsOneEvent [es-atom]
  IEventStoreExecution
  (ex/load-events [return-one aggregate-id]
    (list (->StubEvent aggregate-id)))
  (ex/store-aggregate [test-es aggregate]
    aggregate))


(defrecord EventStoreThrowsConcurrentEx [es-atom]
  IEventStoreExecution
  (ex/load-events [return-none aggregate-id]
    nil)
  (ex/store-aggregate [test-es aggregate]
    (let [es-atom (:es-atom test-es)]
    (if (pos? (:fail-times @es-atom))
      (do (swap! es-atom update :fail-times dec)
        (ex/throw-concurrent-check-failure))
      aggregate))))


(defrecord StubEventStore [event-store])

(defn correct-exceptions-thrown? [es-arg command-arg expected-ex ex-msg]
  (is (thrown-with-msg? java.util.concurrent.ExecutionException
           #"Unable to execute command."
           (ex/command-execution es-arg command-arg)))
  (try (ex/command-execution es-arg command-arg)
    (catch java.util.concurrent.ExecutionException ex
      (let [original-ex (.getCause ex)]
        (is (instance? expected-ex original-ex))
        (is (=  ex-msg (.getMessage original-ex)))))))

(deftest command-execution-test
  (testing "given invalid command is nil."
    (let [es-arg nil
          command-arg nil
          original-ex-expected IllegalArgumentException
          ex-msg "Command argument given does not implement ICommand. Argument given: "]
      (is (correct-exceptions-thrown? es-arg command-arg original-ex-expected ex-msg))
      ))
  (testing "given invalid command is 0."
    (let [es-arg nil
          command-arg 0
          original-ex-expected IllegalArgumentException
          ex-msg "Command argument given does not implement ICommand. Argument given: 0"]
      (is (correct-exceptions-thrown? es-arg command-arg original-ex-expected ex-msg))
      ))
  (testing "given command satisfies ICommand and nil for Event store"
    (let [es-arg nil
          command-arg (->StubCommand (util/new-uuid))
          ex-expected IllegalArgumentException
          ex-msg  "Event Store argument given does not implement IEventStoreExecution. Argument given: "]
      (is (correct-exceptions-thrown? es-arg command-arg ex-expected ex-msg))
      ))
  (testing "given command satisfies ICommand and 0 for Event store."
    (let [es-arg 0
          command-arg (->StubCommand (util/new-uuid))
          ex-expected IllegalArgumentException
          ex-msg  "Event Store argument given does not implement IEventStoreExecution. Argument given: 0"]
      (is (correct-exceptions-thrown? es-arg command-arg ex-expected ex-msg))
      ))
  (testing "given valid command and event store when executing command throws an Illegal argument exception."
    (let [es-atom (atom {})
          aggregate-id (util/new-uuid)
          agg-key (uuid->keyword aggregate-id)
          es-arg (->ReturnsNoEvents es-atom)
          command-arg (->CommandThrowingIllegalArgument aggregate-id)]
      (is (thrown-with-msg? IllegalArgumentException
            #"Testing command throws illegal argument exception."
            (ex/command-execution es-arg command-arg)))
      ))
  (testing "given store aggregate throws an Illegal argument exception."
    (let [es-arg (->StoringThrowsIllegalArgument nil)
          command-arg (->StubCommand (util/new-uuid))
          ex-expected IllegalArgumentException
          ex-msg "Storing aggregate throws and illegal argument exception."]
      (is (correct-exceptions-thrown? es-arg command-arg ex-expected ex-msg))
      ))
  (testing "given command updates aggregate with executed key is true and load events returns no events."
    (let [es-atom (atom {})
          aggregate-id (util/new-uuid)
          agg-key (uuid->keyword aggregate-id)
          es-arg (->ReturnsNoEvents es-atom)
          command-arg (->StubCommand aggregate-id)
          result (ex/command-execution es-arg command-arg)]
      (is (state/aggregate? result))
      (is (true? (:executed result)))
      (is (= 1 (:events-applied result)))
          ))
  (testing "given command updates aggregate with executed key is true and load events returns no events."
    (let [es-atom (atom {})
          aggregate-id (util/new-uuid)
          es-arg (->ReturnsOneEvent es-atom)
          command-arg (->StubCommand aggregate-id)
          result (ex/command-execution es-arg command-arg)]
      (is (state/aggregate? result))
      (is (true? (:executed result)))
      (is (= 2 (:events-applied result)))
          ))
  (testing "given event store throws concurrent modification failure only for first attempt"
    (let [es-atom (atom {:fail-times 1})
          aggregate-id (util/new-uuid)
          es-arg (->EventStoreThrowsConcurrentEx es-atom)
          command-arg (->StubCommand aggregate-id)]
      (is (state/aggregate? (ex/command-execution es-arg command-arg)))
      ))
  (testing "given event store throws concurrent modification failure max retry times."
    (let [es-atom (atom {:fail-times ccg/max-execution-tries})
          aggregate-id (util/new-uuid)
          es-arg (->EventStoreThrowsConcurrentEx es-atom)
          command-arg (->StubCommand aggregate-id)]
      (is (state/aggregate? (ex/command-execution es-arg command-arg)))))
  (testing "given event store throws concurrent modification failure one more than max retry times."
    (let [es-atom (atom {:fail-times (+ ccg/max-execution-tries 1)})
          aggregate-id (util/new-uuid)
          es-arg (->EventStoreThrowsConcurrentEx es-atom)
          command-arg (->StubCommand aggregate-id)
          ex-expected java.util.ConcurrentModificationException
          ex-msg "Aggregate fails concurrent version check."]
      (is (correct-exceptions-thrown? es-arg command-arg ex-expected ex-msg)))))






(run-tests 'thought-stream.commands.execution_test)

