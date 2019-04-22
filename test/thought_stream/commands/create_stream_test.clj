(ns thought-stream.commands.create-stream-test
  (:require
    [thought-stream.commands.create-stream :as cs :refer [create-stream]]
    [thought-stream.thought-stream-logic.stream :as stream]
    [thought-stream.commands.execution :as ex :refer [ICommand]]
    [thought-stream.test-mother :as mom]
    [thought-stream.utilities :as util]
    [clojure.test :refer :all]))


(deftest given-first-argument-is-not-base-aggregate-throw-illegal-argument-exception-test
  (is (thrown-with-msg? IllegalArgumentException
        #"create new stream requires base aggregate as initial value: Invalid input: "
        (create-stream nil nil)))
  (is (thrown-with-msg? IllegalArgumentException
        #"create new stream requires base aggregate as initial value: Invalid input: 0"
        (create-stream 0 nil)))
 (is (thrown-with-msg? IllegalArgumentException
        #"create new stream requires base aggregate as initial value: Invalid input: \{\}"
        (create-stream {} nil)))
  (is (thrown? IllegalArgumentException
        (create-stream (mom/as-aggregate {}) nil))))

(deftest given-valid-base-aggregate-but-invalid-new-stream-test
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid Input: "
    (create-stream (mom/make-base-aggregate) nil)))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid Input: 0"
    (create-stream (mom/make-base-aggregate) 0)))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid stream id:  Invalid stream name:  Invalid focus text:"
    (create-stream (mom/make-base-aggregate) {})))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid stream id: 0 Invalid stream name: 0 Invalid focus text: 0"
    (create-stream (mom/make-base-aggregate) {:id 0 :name 0 :focus 0})))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid stream name: 0 Invalid focus text: 0"
    (create-stream (mom/make-base-aggregate) {:id (util/new-uuid) :name 0 :focus 0})))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid stream id: 0 Invalid stream name: 0"
    (create-stream (mom/make-base-aggregate) {:id 0 :name 0 :focus "valid text"}))))

(deftest given-valid-base-aggregate-and-valid-new-stream-input-test
  (let [stream-input {:id (util/new-uuid) :name "some name" :focus "some text"}
        result (create-stream (mom/make-base-aggregate) stream-input)]
    (is (= (:id stream-input) (:id result)))
    (is (instance? thought_stream.thought_stream_logic.stream.StreamCreated (first (:changes result))))))


(deftest CreateStream-record-test
  (is (some? (cs/->CreateStream nil nil nil))))

(deftest CreateStream-record-extends-ICommand-test
  (is (extends? ICommand thought_stream.commands.create_stream.CreateStream)))

(deftest executing-CreateStream-creates-new-stream-test
  (let [stream-input (cs/->CreateStream (util/new-uuid) "some name" "some text")
        result (ex/execute stream-input (mom/make-base-aggregate))]
    (is (= (:id stream-input) (:id result)))
    (is (= (:focus stream-input) (:focus result)))
    (is (instance? thought_stream.thought_stream_logic.stream.StreamCreated (first (:changes result))))))



(run-tests 'thought-stream.commands.create-stream-test)
