(ns thought-stream.commands.create-new-stream-test
  (:require
    [thought-stream.commands.create-new-stream :refer [create-new-stream]]
    [thought-stream.thought-stream-logic.stream :as stream]
    [thought-stream.test-mother :as mom]
    [thought-stream.utilities :as util]
    [clojure.test :refer :all]))


(deftest given-first-argument-is-not-base-aggregate-throw-illegal-argument-exception-test
  (is (thrown-with-msg? IllegalArgumentException
        #"create new stream requires base aggregate as initial value: Invalid input: "
        (create-new-stream nil nil)))
  (is (thrown-with-msg? IllegalArgumentException
        #"create new stream requires base aggregate as initial value: Invalid input: 0"
        (create-new-stream 0 nil)))
 (is (thrown-with-msg? IllegalArgumentException
        #"create new stream requires base aggregate as initial value: Invalid input: \{\}"
        (create-new-stream {} nil)))
  (is (thrown? IllegalArgumentException
        (create-new-stream (mom/as-aggregate {}) nil))))

(deftest given-valid-base-aggregate-but-invalid-new-stream-test
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid Input: "
    (create-new-stream (mom/make-base-aggregate) nil)))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid Input: 0"
    (create-new-stream (mom/make-base-aggregate) 0)))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid stream id:  Invalid focus text:"
    (create-new-stream (mom/make-base-aggregate) {})))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid stream id: 0 Invalid focus text: 0"
    (create-new-stream (mom/make-base-aggregate) {:id 0 :focus 0})))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid focus text: 0"
    (create-new-stream (mom/make-base-aggregate) {:id (util/new-uuid) :focus 0})))
  (is (thrown-with-msg? IllegalArgumentException
    #"Invalid new stream: Invalid stream id: 0"
    (create-new-stream (mom/make-base-aggregate) {:id 0 :focus "valid text"}))))

(deftest given-valid-base-aggregate-and-valid-new-stream-input-test
  (let [stream-input {:id (util/new-uuid) :focus "some text"}
        result (create-new-stream (mom/make-base-aggregate) stream-input)]
    (is (= (:id stream-input) (:id result)))
    (is (instance? thought_stream.thought_stream_logic.stream.StreamCreated (first (:changes result))))))

(run-tests 'thought-stream.commands.create-new-stream-test)
