(ns thought-stream.commands.execution
  (:require
    [thought-stream.state :as state]
    [thought-stream.commands.config :as ccg]))


(defprotocol ICommand
  (execute [command aggregate]))


(defprotocol IExecuteCommand
  (execute-command [event-store command]))

(defprotocol IEventStoreExecution
  (load-events [event-store aggregate-id])
  (store-aggregate [event-store aggregate]))

(defn throw-concurrent-check-failure []
  (throw (java.util.ConcurrentModificationException. "Aggregate fails concurrent version check.")))

(def retry-command-execution ::retry-command)

(defn- retry-command? [result]
  (= retry-command-execution result))

(defn- tries-left? [tries]
  (pos? tries))

(defn- throw-command-execution-failure [ex]
  (throw (java.util.concurrent.ExecutionException. "Unable to execute command." ex)))


(defn- validate-command [command]
  (if-not (satisfies? ICommand command)
    (throw (IllegalArgumentException.
             (str "Command argument given does not implement ICommand. Argument given: " command)))))

(defn- validate-event-store [event-store]
  (if-not (satisfies? IEventStoreExecution event-store)
    (throw (IllegalArgumentException.
             (str "Event Store argument given does not implement IEventStoreExecution. Argument given: " event-store)))))

(defn- try-loading-aggregate [event-store command]
  (try
    (validate-command command)
    (validate-event-store event-store)
    (let [history (load-events event-store (:id command))]
      (state/aggregate-from-history {} history))
    (catch Exception ex
      (throw-command-execution-failure ex))))

(defn- try-storing-aggregate [event-store updated-aggregate tries]
  (try (store-aggregate event-store updated-aggregate)
    (catch java.util.ConcurrentModificationException con-ex
      (if (tries-left? tries)
        retry-command-execution
        (throw-command-execution-failure con-ex)))
    (catch Exception ex
      (throw-command-execution-failure ex))))

(defn command-execution [event-store command]
  (loop [tries ccg/max-execution-tries]
    (let [aggregate (try-loading-aggregate event-store command)
          updated-aggregate (execute command aggregate)
          result (try-storing-aggregate event-store updated-aggregate tries)]
      (if-not (retry-command? result)
        result
        (recur (dec tries))))))
