(ns thought-stream.commands.start-thinking-test
  (:require
    [thought-stream.commands.start-thinking :refer [start-thinking]]
    [thought-stream.test-mother :as mom]
    [thought-stream.thought-stream-logic.thinker :as thinker]
    [clojure.test :refer :all]))



(deftest start-thinking-throws-illegalArgumentException-given-invalid-inputs-test
  (testing "given first argument is not a valid aggregate"
    (is (thrown-with-msg? IllegalArgumentException
          #"Start thinking requires aggregate as initial value: Invalid input: "
          (start-thinking nil nil)))
    (is (thrown-with-msg? IllegalArgumentException
          #"Start thinking requires aggregate as initial value: Invalid input: 0"
          (start-thinking 0 nil))))
  (testing "given second argument is not a map"
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thinker: "
          (start-thinking (mom/make-base-aggregate) nil)))
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thinker: 0"
          (start-thinking (mom/make-base-aggregate) 0))))
  (testing "given new thinker input contains invalid key arguments"
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thinker: Invalid thinker id:  Invalid thinker username:  Invalid thinker password:"
          (start-thinking (mom/make-base-aggregate) {:id nil :thinker-username nil :thinker-password nil})))
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thinker: Invalid thinker id: 0 Invalid thinker username: 0 Invalid thinker password: 0"
          (start-thinking (mom/make-base-aggregate) {:id 0 :thinker-username 0 :thinker-password 0})))))

(deftest start-thinking-throws-IllegalStateException-given-password-fails-requirement
  (testing "password contains lower case only"
    (is (thrown-with-msg? IllegalStateException
            #"Password fails requirements: At least 8 characters, Contains at least one upper case character, Contains at least one digit, Contains at least one special character"
            (start-thinking (mom/make-base-aggregate) {:id (util/new-uuid) :thinker-username "testThinker" :thinker-password "a"}))))
  (testing "password contains upper case only"
    (is (thrown-with-msg? IllegalStateException
            #"Password fails requirements: At least 8 characters, Contains at least one lower case character, Contains at least one digit, Contains at least one special character"
            (start-thinking (mom/make-base-aggregate) {:id (util/new-uuid) :thinker-username "testThinker" :thinker-password "A"}))))
  (testing "pasword meets contains lower case and length requirement only"
    (is (thrown-with-msg? IllegalStateException
            #"Password fails requirements: Contains at least one upper case character, Contains at least one digit, Contains at least one special character"
            (start-thinking (mom/make-base-aggregate) {:id (util/new-uuid) :thinker-username "testThinker" :thinker-password "aaaaaaaa"}))))
  (testing "pasword meets upper and lower case and length requirement only"
    (is (thrown-with-msg? IllegalStateException
            #"Password fails requirements: Contains at least one digit, Contains at least one special character"
            (start-thinking (mom/make-base-aggregate) {:id (util/new-uuid) :thinker-username "testThinker" :thinker-password "Aaaaaaaa"}))))
  (testing "pasword meets upper and lower case, length, and digit requirement."
    (is (thrown-with-msg? IllegalStateException
            #"Password fails requirements: Contains at least one special character"
            (start-thinking (mom/make-base-aggregate) {:id (util/new-uuid) :thinker-username "testThinker" :thinker-password "Aaaaaaa1"}))))
  (testing "pasword meets upper and lower case, length, and digit requirement."
    (is (thrown-with-msg? IllegalStateException
            #"Password fails requirements: Contains at least one upper case character,"
            (start-thinking (mom/make-base-aggregate) {:id (util/new-uuid) :thinker-username "testThinker" :thinker-password "aaaaaa1!"}))))
  )


(deftest given-valid-arguments-and-acceptable-password-return-new-thinker-with-thinking-started-event
  (let [new-thinker {:id (util/new-uuid) :thinker-username "testThinker" :thinker-password "Aaaaaa1!"}
        result (start-thinking (mom/make-base-aggregate) new-thinker)]
    (is (state/aggregate? result))
    (is (instance? thought_stream.thought_stream_logic.thinker.ThinkingStarted (first (:changes result))))
    (is (= (:id new-thinker) (:id result)))))

(run-tests 'thought-stream.commands.start-thinking-test)

