(ns thought-stream.thought-stream-logic.stream-test
  (:require
    [thought-stream.thought-stream :as ts]
    [thought-stream.thought-stream-logic.thought :as thought]
    [thought-stream.thought-stream-logic.stream :as stream]
    [thought-stream.state :as state]
    [thought-stream.utilities :refer :all]
    [thought-stream.test-mother :as mom]
    [clojure.test :refer :all]))


(deftest can-create-new-stream-with-focus-test
  (testing "Given uuid and focus as string."
    (let [id (new-uuid)
          focus-text "text"
          stream (stream/new-stream id focus-text)]
      (is (= "text" (:focus stream)))
      (is (= id (:id stream)))
    ))
  (testing "Given stream id is not a uuid throws AssertionError."
    (is (thrown? AssertionError (stream/new-stream "id" "text")))
    (is (thrown? AssertionError (stream/new-stream nil "text")))
    (is (thrown? AssertionError (stream/new-stream 0 "text"))))
  (testing "Given focus text is not a valid text string throw exception."
    (is (thrown? AssertionError (stream/new-stream (new-uuid) 0)))
    (is (thrown? AssertionError (stream/new-stream (new-uuid) nil)))
    (is (thrown? AssertionError (stream/new-stream (new-uuid) [])))
    )
  )

(deftest can-update-the-focus-of-the-stream-test
  (testing "Given new focus test update focus in stream."
    (let [stream (mom/make-valid-stream)
          new-focus "new text"]
      (is (= new-focus (:focus (stream/update-focus stream new-focus))))))
 (testing "Given focus is not text throw AssertionError."
   (is (thrown? AssertionError (stream/update-focus (mom/make-valid-stream) (new-uuid))))
   (is (thrown? AssertionError (stream/update-focus (mom/make-valid-stream) 0)))
   (is (thrown? AssertionError (stream/update-focus (mom/make-valid-stream) nil)))
   (is (thrown? AssertionError (stream/update-focus (mom/make-valid-stream) [])))))


(deftest StreamCreated-record-test
  (is (some? (stream/->StreamCreated nil nil)))
  (is (extends? state/EventTransition thought_stream.thought_stream_logic.stream.StreamCreated))
  (let [stream-id (new-uuid)
        focus-text "text"
        stream-created-event (stream/->StreamCreated stream-id focus-text)
        transition-result (state/transition stream-created-event (mom/make-base-aggregate))]
    (is (= stream-id (:id transition-result)))
    (is (= focus-text (:focus transition-result)))
    (is (state/has-changes-queue? transition-result))
    ))


(deftest FocusUpdated-record-test
  (is (some? (stream/->FocusUpdated nil nil)))
  (is (extends? state/EventTransition thought_stream.thought_stream_logic.stream.FocusUpdated))
  (let [stream (mom/make-valid-stream)
        new-focus "new text"
        focus-updated-event (stream/->FocusUpdated (:id stream) new-focus)
        transition-result (state/transition focus-updated-event stream)]
    (is (= (:id stream) (:id transition-result)))
    (is (= "new text" (:focus transition-result)))))



(run-tests 'thought-stream.thought-stream-logic.stream-test)
