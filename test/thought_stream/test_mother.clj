(ns thought-stream.test-mother
  (:require
    [thought-stream.state :as state]
    [thought-stream.thought-stream-logic.thought :as thought]
    [thought-stream.thought-stream-logic.stream :as stream]
    [thought-stream.utilities :refer :all]))


(defn make-valid-stream []
  (let [id (new-uuid)
        focus-text "text"]
    (stream/new-stream id focus-text)))


(defn make-valid-thought []
  (thought/new-thought (new-uuid) {:thinker (new-uuid) :thought "text"}))

(defn make-base-aggregate []
  (state/base-aggregate))

(defn as-aggregate [test-val]
  (-> test-val
      (state/add-changes-tracker)
      (assoc :expected-version 1)))


