(ns thought-stream.stream-test
  (:require
    [thought-stream.thought-stream :as ts]
    [thought-stream.stream :as stream :refer [LoadEvents]]
    [thought-stream.state :refer [transition update-state]]
    [clojure.test :refer :all]))


(def focus-text "text")

(defn is-thought? [to-check]
  (is (and (some #{:thought :link :timestamp} (keys to-check))
        (associative? to-check))))

(defn is-stream? [to-check]
  (is (stream/stream? to-check)))


(defn is-thinking-new-thoughts? [to-check id-to-check]
  (is (and (associative? to-check)
       (= id-to-check (:id to-check))
       (some #{:id} (keys to-check))
       (some #{:thought-streams} (keys to-check))
       (nil? (:thought-streams to-check))
       (some #{:free-thoughts} (keys to-check))
       (set? (:free-thoughts to-check)))))

;;TODO: UPDATE ALL to test set membership
(deftest can-create-new-thoughts-given-an-id
  (let [new-id (stream/new-uuid)
        thoughts (stream/new-thoughts new-id)]
    (is-thinking-new-thoughts? thoughts new-id)))

(deftest new-thoughts-throws-error-given-nil-id
  (let [nil-id nil]
    (is (thrown? Exception (stream/new-thoughts nil-id)))))

(deftest new-thoughts-throws-error-given-int-id
  (let [int-id 1]
    (is (thrown? Exception (stream/new-thoughts int-id)))))

(deftest new-thoughts-throws-error-given-non-uuid-string-id
  (let [int-id 1]
    (is (thrown? Exception (stream/new-thoughts int-id)))))

(deftest can-create-new-thought
  (let [new-thought (stream/new-thought "text")]
    (is-thought? new-thought)))

(deftest new-thought-only-takes-text-for-thought
  (is (thrown? Exception (stream/new-thought nil)))
  (is (thrown? Exception (stream/new-thought 10)))
  (is (thrown? Exception (stream/new-thought ["thoughts" "in" "collection"])))
  (is (stream/new-thought "this is a valid thought")))

(deftest new-thought-with-only-thought
  (let [text "thought text."
        thought (stream/new-thought text)]
    (is (= text (:thought thought)))
    (is (= nil (:link thought)))))

(deftest new-thought-with-thought-and-link
  (let [text "thought text"
        page "page link"
        thought (stream/new-thought text :link page)]
    (is thought)
    (is (= text (:thought thought)))
    (is (= page (:link thought)))))


(deftest can-create-new-stream
  (let [test-stream (stream/new-stream)]
    (is-stream? test-stream)))

(deftest can-create-new-stream-with-focus
  (let [test-stream (stream/new-stream focus-text)]
    (is-stream? test-stream)))

(deftest stream-with-nil-focus
  (is (= nil (:focus (stream/new-stream nil))))
  (is (= nil (stream/focus (stream/new-stream nil)))))


(deftest stream-with-text-focus
    (is (= focus-text (:focus (stream/new-stream focus-text))))
    (is (= focus-text (stream/focus (stream/new-stream focus-text)))))

(deftest can-update-focus
  (let [test-stream (stream/new-stream focus-text)
        new-focus "new focus text"]
    (is (= new-focus (stream/focus (stream/update-focus test-stream new-focus))))))


(deftest streams-have-a-sequencial-vector-of-thoughts
  (is (sequential? (:thoughts (stream/new-stream focus-text))))
  (is (vector? (:thoughts (stream/new-stream focus-text)))))


(deftest stream-with-no-thoughts
  (let [stream (stream/new-stream focus-text)]
    (is (nil? (stream/with-thought stream 0)))
    (is (nil? (stream/connected-thoughts (stream/new-stream focus-text))))))

(deftest connect-new-stream-with-focus-with-one-thought
  (let [stream (stream/new-stream focus-text)
        thought "thoughts"
        thought-stream (stream/connect-to-stream stream thought)]
    (is (not (nil? (stream/connected-thoughts thought-stream))))
    (is (seq? (stream/connected-thoughts thought-stream)))
    (is (= thought (first (:thoughts thought-stream))))
    (is (= thought (stream/with-thought thought-stream 0)))))

(deftest connect-stream-with-focus-and-one-thought-with-one-thought
  (let [stream (stream/new-stream focus-text)
        thought "thoughts"
        next-thought "next thought"
        thought-stream (stream/connect-to-stream (stream/connect-to-stream stream thought) next-thought)]
    (is (= next-thought (second (stream/connected-thoughts thought-stream))))
    (is (= next-thought (stream/with-thought thought-stream 1)))))


(deftest connect-new-stream-with-focus-with-one-thought-and-update-focus
   (let [stream (stream/new-stream)
        thought "thoughts"
        thought-stream (stream/connect-to-stream stream thought :updated-focus focus-text)]
     (is (not (nil? (stream/connected-thoughts thought-stream))))
     (is (seq? (stream/connected-thoughts thought-stream)))
     (is (= thought (first (:thoughts thought-stream))))
     (is (= thought (stream/with-thought thought-stream 0)))
     (is (= focus-text (:focus thought-stream)))))

(deftest connecting-two-thoughts-returns-a-stream-with-no-focus
  (let [first-thought (stream/new-thought "text")
        second-thought (stream/new-thought "more-text")
        test-stream (stream/connect-to-thought first-thought second-thought)]
    (is-stream? test-stream)
    (is (= first-thought (stream/with-thought test-stream 0)))
    (is (= second-thought (stream/with-thought test-stream 1)))
    (is (nil? (stream/focus test-stream)))))

(deftest connecting-two-thoughts-with-focus-returns-a-stream-with-focus
  (let [first-thought (stream/new-thought "text")
        second-thought (stream/new-thought "more-text")
        focus-text "the focus of the new stream is testing"
        test-stream (stream/connect-to-thought first-thought second-thought :focus focus-text)]
    (is-stream? test-stream)
    (is (= first-thought (stream/with-thought test-stream 0)))
    (is (= second-thought (stream/with-thought test-stream 1)))
    (is (= focus-text (stream/focus test-stream)))))

(deftest connecting-two-thoughts-throws-error-if-nil-arguments
  (let [nil-thought nil
        thought (stream/new-thought "more-text")]
    (is (thrown? Exception (stream/connect-to-thought nil-thought thought)))
    (is (thrown? Exception (stream/connect-to-thought thought nil-thought)))))

(deftest connecting-two-thoughts-throws-error-if-not-thoughts
  (let [not-thought 10
        thought (stream/new-thought "more-text")]
    (is (thrown? Exception (stream/connect-to-thought not-thought thought)))
    (is (thrown? Exception (stream/connect-to-thought thought not-thought)))))


(deftest can-connect-thought-to-stream-or-thought
  (let [first-thought (stream/new-thought "text")
        second-thought (stream/new-thought "more-text")
        new-focus "the focus of the new stream is testing"
        test-stream (stream/connect-to first-thought second-thought)
        next-thought (stream/new-thought "new thought text")
        next-test-stream (stream/connect-to test-stream next-thought)]
    (is (stream/stream? test-stream))
    (is (= first-thought (first (:thoughts test-stream))))
    (is (= second-thought (second (:thoughts test-stream))))
    (is (nil? (:focus test-stream)))
    (is (stream/stream? next-test-stream))
    (is (= first-thought (first (:thoughts next-test-stream))))
    (is (= second-thought (second (:thoughts next-test-stream))))
    (is (= next-thought (second (next (:thoughts next-test-stream)))))
    (is (nil? (:focus next-test-stream)))))

(deftest can-connect-thought-to-stream-or-thought-with-focus-updates
  (let [first-thought (stream/new-thought "text")
        second-thought (stream/new-thought "more-text")
        new-focus "the focus of the new stream is testing"
        test-stream (stream/connect-to first-thought second-thought :focus focus-text)
        next-thought (stream/new-thought "new thought text")
        next-test-stream (stream/connect-to test-stream next-thought :focus new-focus)]
    (is (stream/stream? test-stream))
    (is (= first-thought (first (:thoughts test-stream))))
    (is (= second-thought (second (:thoughts test-stream))))
    (is (= focus-text (:focus test-stream)))
    (is (stream/stream? next-test-stream))
    (is (= first-thought (first (:thoughts next-test-stream))))
    (is (= second-thought (second (:thoughts next-test-stream))))
    (is (= next-thought (second (next (:thoughts next-test-stream)))))
    (is (= new-focus (:focus next-test-stream)))))

(deftest can-add-thought-stream-to-thoughts
    (let [thoughts-id (stream/new-uuid)
          thinking (stream/new-thoughts thoughts-id)
          first-thought (stream/new-thought "thought-text")
          next-thought (stream/new-thought "thought-text")
          test-stream (stream/connect-to-thought first-thought next-thought)
          stream-id-key (stream/id-as-key (:id test-stream))]
      (is (= thoughts-id (:id (stream/add-thought-stream thinking test-stream))))
      (is (some #{stream-id-key} (keys (:thought-streams (stream/add-thought-stream thinking test-stream)))))
      (is (= test-stream (get-in (stream/add-thought-stream thinking test-stream) [:thought-streams stream-id-key])))))

(deftest can-add-thought-to-thoughts-as-free-thought
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        new-thought (stream/new-thought "thought-text")]
    (is (= thoughts-id (:id (stream/as-free-thought thinking new-thought))))
    (is (= new-thought (first (:free-thoughts (stream/as-free-thought thinking new-thought)))))))

(deftest can-have-thought-throws-error-with-no-thought
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        nil-text nil]
    (is (thrown? Exception (stream/have-thought thinking {:thought nil-text})))
    (is (thrown? Exception (stream/have-thought thinking)))))

(deftest can-have-thought-with-no-link-as-free-thought
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"]
    (is (= thoughts-id (:id (stream/have-thought thinking {:thought thought-text}))))
    (is (= thought-text (:thought (first (:free-thoughts  (stream/have-thought thinking {:thought thought-text}))))))))

(deftest can-have-thought-with-link-as-free-thought
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text :link link})]
    (is (= thoughts-id (:id thinking-with-thought)))
    (is (= thought-text (:thought (first (:free-thoughts thinking-with-thought)))))
    (is (= link (:link (first (:free-thoughts  thinking-with-thought)))))
    (is (nil? (:thought-streams thinking-with-thought)))))


(deftest can-have-thought-with-no-link-connecting-to-only-free-thought
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        thought-to-connect-to (first (:free-thoughts thinking-with-thought))
        next-thought "next thought text"
        thinking-with-thought-stream (stream/have-thought thinking-with-thought {:thought next-thought :connecting-to thought-to-connect-to})]
    (is (= thoughts-id (:id thinking-with-thought-stream)))
    (is (every? #(stream/stream? %) (vals (:thought-streams thinking-with-thought-stream))))
    (is (not (empty? (:free-thoughts thinking-with-thought))))
    (is (empty? (:free-thoughts thinking-with-thought-stream)))))

(deftest have-thought-throws-exception-if-connecting-to-not-found-thought
  (let [thoughts-id (stream/new-uuid)
        thinking-no-thoughts (stream/new-thoughts thoughts-id)
        thought-text "text"
        thinking-with-thought (stream/have-thought thinking-no-thoughts {:thought thought-text})
        thought-to-connect-to (first (:free-thoughts thinking-with-thought))
        next-thought "next thought text"]
    (is (thrown? Exception (stream/have-thought thinking-no-thoughts {:thought next-thought :connecting-to thought-to-connect-to})))))

(deftest can-have-thought-with-link-connecting-to-only-free-thought
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        thought-to-connect-to (first (:free-thoughts thinking-with-thought))
        next-thought "next thought text"
        thinking-with-thought-stream (stream/have-thought thinking-with-thought {:thought next-thought :link link :connecting-to thought-to-connect-to})]
    (is (= thoughts-id (:id thinking-with-thought-stream)))
    (is (every? #(stream/stream? %) (vals (:thought-streams thinking-with-thought-stream))))
    (is (= link (:link (second (:thoughts (first (vals (:thought-streams thinking-with-thought-stream))))))))
    (is (not (empty? (:free-thoughts thinking-with-thought))))
    (is (empty? (:free-thoughts thinking-with-thought-stream)))))

(deftest can-have-thought-connecting-to-one-thought-of-many
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        thought-to-connect-to (first (:free-thoughts thinking-with-thought))
        another-thought (stream/have-thought thinking-with-thought {:thought "another-thought"})
        more-thoughts (stream/have-thought another-thought {:thought "all the thoughts"})
        next-thought "next thought text"
        thinking-with-thought-stream (stream/have-thought more-thoughts {:thought next-thought :link link :connecting-to thought-to-connect-to})]
    (is (= thoughts-id (:id thinking-with-thought-stream)))
    (is (every? #(stream/stream? %) (vals (:thought-streams thinking-with-thought-stream))))
    (is (= 2 (count (stream/connected-thoughts (first (vals (:thought-streams thinking-with-thought-stream)))))))
    (is (= 2 (count (:free-thoughts thinking-with-thought-stream))))))

(deftest can-have-thought-connecting-to-stream
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        thought-to-connect-to (first (:free-thoughts thinking-with-thought))
        another-thought (stream/have-thought thinking-with-thought {:thought "another-thought"})
        more-thoughts (stream/have-thought another-thought {:thought "all the thoughts"})
        next-thought "next thought text"
        thinking-with-thought-stream (stream/have-thought more-thoughts {:thought next-thought :link link :connecting-to thought-to-connect-to})
        thought-stream-to-connect-to (first (vals (:thought-streams thinking-with-thought-stream)))
        connecting-to-thought-stream (stream/have-thought thinking-with-thought-stream {:thought "test-text" :connecting-to thought-stream-to-connect-to :with-focus focus-text})]
    (is (= thoughts-id (:id thinking-with-thought-stream)))
    (is (every? #(stream/stream? %) (vals (:thought-streams connecting-to-thought-stream))))
    (is (= 2 (count (:free-thoughts connecting-to-thought-stream))))
    (is (= 3 (count (stream/connected-thoughts (first (vals (:thought-streams connecting-to-thought-stream)))))))
    (is (= focus-text (:focus (first (vals (:thought-streams connecting-to-thought-stream))))))))

(deftest have-thought-throws-exception-if-connecting-to-not-found-stream
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        thought-to-connect-to (first (:free-thoughts thinking-with-thought))
        another-thought (stream/have-thought thinking-with-thought {:thought "another-thought"})
        more-thoughts (stream/have-thought another-thought {:thought "all the thoughts"})
        next-thought "next thought text"
        thinking-with-thought-stream (stream/have-thought more-thoughts {:thought next-thought :link link :connecting-to thought-to-connect-to})
        thought-stream-to-connect-to (first (vals (:thought-streams thinking-with-thought-stream)))]
    (is (thrown? Exception (stream/have-thought thinking-with-thought {:thought "test-text" :connecting-to thought-stream-to-connect-to :with-focus focus-text})))))

(deftest can-have-thought-creating-new-stream-with-another-stream-existing
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        thought-to-connect-to (first (:free-thoughts thinking-with-thought))
        another-thought (stream/have-thought thinking-with-thought {:thought "another-thought"})
        more-thoughts (stream/have-thought another-thought {:thought "all the thoughts"})
        next-thought "next thought text"
        thinking-with-thought-stream (stream/have-thought more-thoughts {:thought next-thought :link link :connecting-to thought-to-connect-to})
        thought-stream-to-connect-to (first (vals (:thought-streams thinking-with-thought-stream)))
        connecting-to-thought-stream (stream/have-thought thinking-with-thought-stream {:thought "test-text" :connecting-to thought-stream-to-connect-to})
        next-thought-to-connect-to (first (:free-thoughts connecting-to-thought-stream))
        thinking-with-multiple-streams (stream/have-thought connecting-to-thought-stream {:thought "final thought" :connecting-to next-thought-to-connect-to})]
    (is (= thoughts-id (:id thinking-with-thought-stream)))
    (is (every? #(stream/stream? %) (vals (:thought-streams thinking-with-multiple-streams))))
    (is (= 1 (count (:free-thoughts thinking-with-multiple-streams))))
    (is (= 3 (count (stream/connected-thoughts (first (vals (:thought-streams thinking-with-multiple-streams)))))))
    (is (= 2 (count (vals (:thought-streams thinking-with-multiple-streams)))))
    (is (= 2 (count (stream/connected-thoughts (second (vals (:thought-streams thinking-with-multiple-streams)))))))))


(deftest connect-thoughts-throws-exception-if-connecting-to-or-free-thought-nil
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thinking-with-thought (stream/have-thought thinking {:thought "text"})
        another-thought (stream/have-thought thinking-with-thought {:thought "another-thought"})
        first-thought (first (:free-thoughts another-thought))]
    (is (thrown? Exception (stream/connect-thought another-thought :free-thought nil :connecting-to first-thought)))
    (is (thrown? Exception (stream/connect-thought another-thought :free-thought first-thought :connecting-to nil)))))

(deftest can-connect-free-thoughts
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        another-thought (stream/have-thought thinking-with-thought {:thought "another-thought"})
        first-thought (first (:free-thoughts another-thought))
        second-thought (second (:free-thoughts another-thought))
        connecting-thoughts (stream/connect-thought another-thought :connecting-to first-thought :free-thought second-thought :with-focus focus-text)]
    (is (= thoughts-id (:id connecting-thoughts)))
    (is (every? #(stream/stream? %) (vals (:thought-streams connecting-thoughts))))
    (is (= focus-text (:focus (first (vals (:thought-streams connecting-thoughts))))))
    (is (= 1 (count (:free-thoughts thinking-with-thought))))
    (is (= 2 (count (:free-thoughts another-thought))))
    (is (= 0 (count (:free-thoughts connecting-thoughts))))
    (is (= 2 (count (:thoughts (first (vals (:thought-streams connecting-thoughts)))))))))

(deftest can-connect-free-thought-to-stream
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        another-thought (stream/have-thought thinking-with-thought {:thought "another thought"})
        three-thoughts (stream/have-thought another-thought {:thought "third thought"})
        first-thought (first (:free-thoughts another-thought))
        second-thought (second (:free-thoughts another-thought))
        connecting-thoughts (stream/connect-thought three-thoughts :connecting-to first-thought :free-thought second-thought :with-focus focus-text)
        third-thought (first (:free-thoughts connecting-thoughts))
        resulting-stream (first (vals (:thought-streams connecting-thoughts)))
        third-thought-connected (stream/connect-thought connecting-thoughts :connecting-to resulting-stream :free-thought third-thought)]
    (is (= thoughts-id (:id third-thought-connected)))
    (is (every? #(stream/stream? %) (vals (:thought-streams third-thought-connected))))
    (is (= focus-text (:focus (first (vals (:thought-streams third-thought-connected))))))
    (is (= 1 (count (:free-thoughts thinking-with-thought))))
    (is (= 2 (count (:free-thoughts another-thought))))
    (is (= 3 (count (:free-thoughts three-thoughts))))
    (is (empty? (:thought-streams another-thought)))
    (is (empty? (:thought-streams three-thoughts)))
    (is (= 1 (count (:free-thoughts connecting-thoughts))))
    (is (= 1 (count (:thought-streams connecting-thoughts))))
    (is (= 2 (count (:thoughts (first (vals (:thought-streams connecting-thoughts)))))))
    (is (= 0 (count (:free-thoughts third-thought-connected))))
    (is (= 1 (count (:thought-streams third-thought-connected))))
    (is (= 3 (count (stream/connected-thoughts (first (vals (:thought-streams third-thought-connected)))))))))

(deftest connect-thoughts-throws-exceptions-if-free-thought-not-found
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        another-thought (stream/new-thought "thinking another thought")
        first-thought (first (:free-thoughts thinking-with-thought))]
    (is (thrown? Exception (stream/connect-thought thinking-with-thought :connecting-to first-thought :free-thought another-thought :with-focus focus-text)))))

(deftest connect-thoughts-throws-exceptions-if-connecting-to-not-found
  (let [thoughts-id (stream/new-uuid)
        thinking (stream/new-thoughts thoughts-id)
        thought-text "text"
        link "link"
        thinking-with-thought (stream/have-thought thinking {:thought thought-text})
        another-thought (stream/new-thought "thinking another thought")
        first-thought (first (:free-thoughts thinking-with-thought))]
    (is (thrown? Exception (stream/connect-thought thinking-with-thought :connecting-to another-thought :free-thought first-thought :with-focus focus-text)))))



(deftest transition-thinking-started-creates-new-thoughts-as-thinking-thoughts
  (let [thoughts (ts/->ThinkingThoughts nil)
        valid-thought-id (stream/new-uuid)
        thinking-event (ts/->ThinkingStarted valid-thought-id "test-email")
        thoughts-post-transition (transition thoughts thinking-event)]
    (is (instance? thought_stream.thought_stream.ThinkingThoughts thoughts-post-transition))
    (is-thinking-new-thoughts? thoughts-post-transition valid-thought-id)))

(deftest update-state-calls-transition-and-appends-event-to-the-end
  (let [thoughts (ts/->ThinkingThoughts nil)
        valid-thought-id (stream/new-uuid)
        thinking-event (ts/->ThinkingStarted valid-thought-id "test-email")
        thoughts-updated (update-state thoughts thinking-event)]
    (is (instance? thought_stream.thought_stream.ThinkingThoughts thoughts-updated))
    (is-thinking-new-thoughts? thoughts-updated valid-thought-id)
    (is (= (assoc thinking-event :version 1) (peek (:changes thoughts-updated))))))

(deftest transition-Thought-creates-a-new-free-thought-in-thinking-thoughts
  (let [thoughts (ts/->ThinkingThoughts nil)
        valid-thought-id (stream/new-uuid)
        thinking-event (ts/->ThinkingStarted valid-thought-id "test-email")
        thoughts-updated (update-state thoughts thinking-event)
        having-thought-event (ts/map->Thought {:id "id" :thought "thought-text"})
        updated-with-thought-had (stream/update-thoughts thoughts-updated having-thought-event)]
    (is (instance? thought_stream.thought_stream.ThinkingThoughts updated-with-thought-had))
    (is (= 1 (count (:free-thoughts updated-with-thought-had))))))

(defrecord StubEventContainer [id]
  thought_stream.stream.LoadEvents
  (stream/load-events [nil-for-test]
    true))

(deftest event-container-protocol-exists
  (let [event-transfer-object (->StubEventContainer nil)]
    (is (true? (stream/load-events event-transfer-object)))))


(deftest event-transfer-object-exists
  (is (instance? thought_stream.stream.EventTransferObject (stream/->EventTransferObject "id" "events"))))

(run-tests 'thought-stream.stream-test)


