(ns thought-stream.thought-stream-test
  (:require
    [clojure.test :refer :all]
    [thought-stream.thought-stream :as ts :refer [ExecuteCommand]]))



(defrecord StubCommand [testval]
  ExecuteCommand
  (ts/execute [command]
    (assoc command :executed? true)))

(defn stub-thinking [new-thinking]
  (map->StubCommand (ts/thinking new-thinking)))

(deftest start-thinking-command
  (let [command (ts/thinking {:id "id" :email "email"})]
    (is (instance? thought_stream.thought_stream.StartThinking command))
    (is (some #{:id} (keys command)))
    (is (some #{:email} (keys command)))
    (is (= "id" (:id command)))
    (is (= "email" (:email command)))))


(deftest thought-command
  (let [command (ts/having {:id "id" :thought "thought-text"})]
    (is (instance? thought_stream.thought_stream.HaveThought command))))

(deftest ThinkingThoughts-as-a-class
   (is (class? thought_stream.thought_stream.ThinkingThoughts)))


(deftest execute-command-as-protocol
  (let [stub-command (stub-thinking {:id "id" :email "email"})]
    (is (true? (satisfies? ExecuteCommand stub-command)))
    (is (true? (:executed? (ts/execute stub-command))))))


(defn start [thinking-fn]
  (fn [id email]
    (ts/execute (thinking-fn id email))))


(deftest start-takes-a-thinking-and-executes-the-thinking-command
  (let [test-output ((ts/start stub-thinking) {:id "id" :email "email"})]
    (is (instance? StubCommand test-output))
    (is (true? (:executed? test-output)))))


(run-tests 'thought-stream.thought-stream-test)
