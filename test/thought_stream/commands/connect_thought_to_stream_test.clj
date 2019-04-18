(ns thought-stream.commands.connect-thought-to-stream-test
  (:require
    [thought-stream.commands.connect-thought-to-stream :refer [connect-thought-to-stream]]
    [thought-stream.thought-stream-logic.thought :as thought]
    [thought-stream.test-mother :as mom]
    [clojure.test :refer :all]))

(deftest Given-invalid-thoughts-input-connect-thought-to-stream-throws-Exception
  (is (thrown-with-msg? IllegalArgumentException
        #"Invalid thought: "
        (connect-thought-to-stream nil nil)))
  (is (thrown-with-msg? IllegalArgumentException
        #"Invalid thought: \[\]"
        (connect-thought-to-stream [] nil)))
  (is (thrown? IllegalArgumentException  (connect-thought-to-stream (mom/make-valid-thought) nil))))

(deftest Given-valid-thought-invalid-stream-id-input-connect-thought-to-stream-throws-Exception
  (let [thought-aggregate (mom/as-aggregate (mom/make-valid-thought))]
  (is (thrown-with-msg? IllegalArgumentException
        #"Invalid stream id: "
       (connect-thought-to-stream thought-aggregate nil)))
  (is (thrown-with-msg? IllegalArgumentException
        #"Invalid stream id: \[\]"
        (connect-thought-to-stream thought-aggregate [])))
  (is (thrown-with-msg? IllegalArgumentException
        #"Invalid stream id: \{:connecting-to nil\}"
        (connect-thought-to-stream thought-aggregate {:connecting-to nil})))))


(deftest Given-valid-thought-and-valid-stream-id-connect-thought-to-stream-test
  (let [thought-aggregate (mom/as-aggregate (mom/make-valid-thought))
        stream {:connecting-to (:id (mom/make-valid-stream))}
        result (connect-thought-to-stream thought-aggregate stream)]
    (is (thought/valid-thought? result))
    (is (instance? thought_stream.thought_stream_logic.thought.ThoughtConnected (first (:changes result))))))

(run-tests 'thought-stream.commands.connect-thought-to-stream-test)
