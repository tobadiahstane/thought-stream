(ns thought-stream.thought-stream-logic.thought-test
  (:require
    [thought-stream.thought-stream-logic.thought :as thought]
    [thought-stream.state :as state]
    [thought-stream.utilities :refer :all]
    [thought-stream.test-mother :as mom]
    [clojure.test :refer :all]))


(deftest can-create-new-thought-test
  (testing "given a thought-id and a thought-text return new thought"
    (let [thought-id (new-uuid)
          thinker (new-uuid)
          testable (thought/new-thought thought-id {:thinker thinker  :thought "text"})]
      (is (= "text" (:thought testable)))
      (is (= thought-id (:id testable)))
      (is (nil? (:link-url testable)))
      (is (> (+ (System/currentTimeMillis) 1200) (:timestamp testable)))
      (is (set? (:connections testable)))
      (is (= thinker (:thinker testable))))
    )
  (testing "Given thought id not a uuid throw AssertionError."
    (is (thrown? AssertionError (thought/new-thought "not uuid" {:thinker (new-uuid) :thought "text"})))
    (is (thrown? AssertionError (thought/new-thought 0 {:thinker (new-uuid) :thought "text"})))
    (is (thrown? AssertionError (thought/new-thought nil {:thinker (new-uuid) :thought "text"})))
    (is (thrown? AssertionError (thought/new-thought {} {:thinker (new-uuid) :thought "text"})))
      )
  (testing "Given not string for thought-text throw AssertionError."
    (is (thrown? AssertionError (thought/new-thought (new-uuid) {:thinker (new-uuid) :thought 0})))
    (is (thrown? AssertionError (thought/new-thought (new-uuid) {:thinker (new-uuid) :thought []})))
    (is (thrown? AssertionError (thought/new-thought (new-uuid) {:thinker (new-uuid) :thought nil})))
    )
  (testing "Given thinker not uuid throw AssertionError."
    (is (thrown? AssertionError (thought/new-thought (new-uuid) {:thinker 0 :thought "text"})))
    (is (thrown? AssertionError (thought/new-thought (new-uuid) {:thinker "not uuid" :thought "text"})))
    (is (thrown? AssertionError (thought/new-thought (new-uuid) {:thinker [] :thought "text"})))
    )
  )



(deftest can-add-link-to-thought-test
  (testing "Given thought and link url as text return thought"
    (let [thought (mom/make-valid-thought)]
      (is (= "link-text" (:link-url (thought/add-link-url thought "link-text"))))))
    (testing "Given not string for link url throw assertion error."
      (let [thought (mom/make-valid-thought)]
        (is (thrown? AssertionError (thought/add-link-url thought 0)))
        (is (thrown? AssertionError (thought/add-link-url thought [])))))
  )


(deftest can-add-connection-to-thought-test
  (testing "Given a thought and a new connection as uuid return new thought"
    (let [thought (mom/make-valid-thought)
          connecting-to (new-uuid)]
    (is (some #(= connecting-to %) (:connections (thought/add-connection thought connecting-to)))))
    )
  (testing "Given new connection not uuid throw AssertionError"
    (let [thought (mom/make-valid-thought)]
      (is (thrown? AssertionError (thought/add-connection thought 0)))
      (is (thrown? AssertionError (thought/add-connection thought [])))
      (is (thrown? AssertionError (thought/add-connection thought "not a uuid")))))
  )


(deftest can-check-valid-thoughts
  (is (true? (thought/valid-thought? (mom/make-valid-thought)))))

(deftest Thought-event-record-test
  (is (some? (thought/map->Thought {:id (new-uuid) :thinker (new-uuid) :thought "text"})))
  (is (extends? state/EventTransition thought_stream.thought_stream_logic.thought.Thought))
  (let [result (state/transition (thought/map->Thought {:id (new-uuid) :thinker (new-uuid) :thought "text"}) (mom/make-base-aggregate))]
    (is (true? (thought/valid-thought? result)))
    (is (state/has-changes-queue? result))))

(deftest ThoughtConnected-record-test
  (is (some? (thought/map->ThoughtConnected {:id (new-uuid) :connected-id (new-uuid)})))
  (is (extends? state/EventTransition thought_stream.thought_stream_logic.thought.ThoughtConnected))
  (let [thought (mom/make-valid-thought)
        connecting-id (new-uuid)
        thought-connected (thought/map->ThoughtConnected {:id (:id thought) :connected-id connecting-id})]
    (is (some #(= connecting-id %) (:connections (state/transition thought-connected thought))))))


(deftest UrlLinked-record-test
  (let [thought (mom/make-valid-thought)
        url-link "url string"
        url-event (thought/->UrlLinked (:id thought) url-link)]
    (is (some? url-event))
    (is (extends? state/EventTransition (class url-event)))
    (is (= url-link (:link-url (state/transition url-event thought))))
    ))


(run-tests 'thought-stream.thought-stream-logic.thought-test)
