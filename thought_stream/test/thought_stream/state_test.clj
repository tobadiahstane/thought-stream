(ns thought-stream.state-test
  (:require
    [thought-stream.state :as state :refer [transition aggregate-from-history update-state add-changes-tracker]]
    [clojure.test :refer :all]))

(defrecord TransitionTest [result])

(defmethod transition TransitionTest
  [aggregate testing]
  (assoc aggregate :result (:result testing)))

(deftest transition-method
  (let [aggregate nil
        testing (->TransitionTest true)]
    (is (true? (:result (transition aggregate testing))))))


(deftest aggregate-from-history-test
  (let [aggregate nil
        history [(->TransitionTest true)]]
    (is (true? (:result (aggregate-from-history aggregate history))))
    (is (instance? clojure.lang.PersistentQueue (:changes (aggregate-from-history aggregate history))))
    (is (= 1 (:expected-version (aggregate-from-history aggregate history))))))

(deftest update-state-updates-state-adds-event-with-event-version-number
   (let [history [(->TransitionTest false)]
         aggregate (aggregate-from-history nil history)
         first-update (update-state aggregate (->TransitionTest true))
         second-update (update-state first-update (->TransitionTest true))]
     (is (= 2 (:version (peek (:changes first-update)))))
     (is (= 3 (:version (second (:changes second-update)))))))

(run-tests 'thought-stream.state-test)
