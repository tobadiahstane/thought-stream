(ns thought-stream.thought-stream-logic.thinker-test
  (:require
    [thought-stream.thought-stream-logic.thinker :as t]
    [thought-stream.state :as state]
    [thought-stream.test-mother :as mom]
    [thought-stream.utilities :as util]
    [clojure.test :refer :all]))

;So far a thinker just starts thinking.


(defn new-thinker-throws-assertion-error [test-input]
  (is (thrown? AssertionError (t/new-thinker test-input))))

(defn successful-new-thinker-produced [input result]
  (is (map? result))
  (is (= (:id input) (:id result)))
  (is (contains? result :roles))
  (is (contains? (:roles result) :thinker))
  (is (true? (get-in result [:roles :thinker]))))

(deftest new-thinker-test
  (testing "given invalid thinker input throw Assertion Error"
    (new-thinker-throws-assertion-error nil)
    (new-thinker-throws-assertion-error 0)
    (new-thinker-throws-assertion-error{:id nil :thinker-username nil :thinker-password nil})
    (new-thinker-throws-assertion-error {:id nil :thinker-username 0 :thinker-password 0})
    (new-thinker-throws-assertion-error {:id nil :thinker-username "testThinker" :thinker-password "test234"})
    (new-thinker-throws-assertion-error {:id 0 :thinker-username "testThinker" :thinker-password "test234"})
    (new-thinker-throws-assertion-error {:id "not valid id"  :thinker-username "testThinker" :thinker-password "test234"}))
  (testing "given valid thinker"
    (let [input (mom/make-valid-new-thinker-input)
          result  (t/new-thinker input)]
      (successful-new-thinker-produced input result)
      ))
  )

(deftest ThinkingStarted-record-test
  (is (some? (t/->ThinkingStarted nil nil nil)))
  (is (extends? state/EventTransition thought_stream.thought_stream_logic.thinker.ThinkingStarted)))

(deftest ThinkingStarted-event-transition-calls-new-thinker
  (let [input (t/->ThinkingStarted (util/new-uuid) "testThinker" "test1234")
        result (state/transition input (mom/make-base-aggregate))]
    (successful-new-thinker-produced input result)))


(run-tests 'thought-stream.thought-stream-logic.thinker-test)
