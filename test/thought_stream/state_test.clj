(ns thought-stream.state-test
  (:require
    [thought-stream.state :as state :refer [EventTransition
                                            aggregate-from-history
                                            update-state
                                            add-changes-tracker
                                            has-changes-queue?
                                            base-aggregate
                                            aggregate?
                                            base-aggregate?]]
    [clojure.test :refer :all]))

(defrecord TransitionTest [result]
  EventTransition
  (state/transition
    [test-event aggregate]
    (assoc aggregate :result (:result test-event))))

(deftest transition-method
  (let [aggregate nil
        test-event (->TransitionTest true)]
    (is (true? (:result (state/transition test-event aggregate))))))

(deftest aggregate-from-history-test
  (let [aggregate nil
        history [(->TransitionTest true)]]
    (is (true? (:result (aggregate-from-history aggregate history))))
    (is (has-changes-queue? (aggregate-from-history aggregate history)))
    (is (= 1 (:expected-version (aggregate-from-history aggregate history))))))

(deftest update-state-updates-state-adds-event-with-event-version-number
   (let [history [(->TransitionTest false)]
         aggregate (aggregate-from-history nil history)
         first-update (update-state aggregate (->TransitionTest true))
         second-update (update-state first-update (->TransitionTest true))]
     (is (= 2 (:version (peek (:changes first-update)))))
     (is (= 3 (:version (second (:changes second-update)))))
     (is (has-changes-queue? first-update))))

(deftest base-aggregate-returns-map-with-changes-tracker-and-expected-version-0-test
  (let [base (base-aggregate)]
    (is (has-changes-queue? base))
    (is (= 0 (:expected-version base)))))


(deftest is-aggregate-given-expected-version-is-not-negative-and-has-changes-tracker-test
  (let [base (base-aggregate)
        failing1 {}
        failing2 (add-changes-tracker {})
        failing3 (assoc {} :expected-version 0)
        failing4 (assoc base :expected-version -1)
        passing1 (assoc base :some-key nil)
        passing2 (assoc base :expected-version 1)
        passing3 (assoc passing1 :some-key nil)
        ]
    (is (false? (aggregate? failing1)))
    (is (false? (aggregate? failing2)))
    (is (false? (aggregate? failing3)))
    (is (false? (aggregate? failing4)))
    (is (true? (aggregate? base)))
    (is (true? (aggregate? passing1)))
    (is (true? (aggregate? passing2)))
    (is (true? (aggregate? passing3)))))


(deftest is-base-aggregate-if-has-changes-tracker-and-expected-version-is-zero-only-test
  (let [base (base-aggregate)
        failing1 {}
        failing2 (add-changes-tracker {})
        failing3 (assoc {} :expected-version 0)
        failing4 (assoc base :some-key nil)
        failing5 (assoc base :expected-version 1)
        failing6 (assoc base :expected-version -1)]
    (is (false? (base-aggregate? failing1)))
    (is (false? (base-aggregate? failing2)))
    (is (false? (base-aggregate? failing3)))
    (is (false? (base-aggregate? failing4)))
    (is (false? (base-aggregate? failing5)))
    (is (false? (base-aggregate? failing5)))
    (is (false? (base-aggregate? failing6)))
    (is (true? (base-aggregate? base)))
  ))



(run-tests 'thought-stream.state-test)
