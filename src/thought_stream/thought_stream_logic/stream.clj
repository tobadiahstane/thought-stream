(ns thought-stream.thought-stream-logic.stream
  (:require
    [thought-stream.utilities :refer :all]
    [thought-stream.state :as state]))


(defn new-stream
  "A thought stream has a name and a short focus."
  [stream-id stream-name focus-text]
  {:pre [(valid-id? stream-id)
         (valid-text? focus-text)
         (valid-text? stream-name)]}
  {:id stream-id
   :name stream-name
   :focus focus-text})

(defn update-focus
  "Update the focus text of a stream."
  [stream new-focus]
  {:pre [(valid-text? new-focus)]}
  (assoc stream :focus new-focus))


(defrecord StreamCreated [id name focus]
  state/EventTransition
  (state/transition [stream-created base-aggregate]
    (merge base-aggregate (new-stream (:id stream-created) name (:focus stream-created)))))


(defrecord FocusUpdated [id new-focus]
  state/EventTransition
  (state/transition [focus-updated stream]
    (update-focus stream (:new-focus focus-updated))))



