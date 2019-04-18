(ns thought-stream.thought-stream-logic.stream
  (:require
    [thought-stream.utilities :refer :all]
    [thought-stream.state :as state]))


(defn new-stream
  "A thought stream has a short focus the set of thinkers and the set of connected thoughts by id."
  [stream-id focus-text]
  {:pre [(valid-id? stream-id)
         (valid-text? focus-text)]}
  {:id stream-id
   :focus focus-text})

(defn update-focus
  "Update the focus text of a stream."
  [stream new-focus]
  {:pre [(valid-text? new-focus)]}
  (assoc stream :focus new-focus))


(defrecord StreamCreated [id focus]
  state/EventTransition
  (state/transition [stream-created base-aggregate]
    (merge base-aggregate (new-stream (:id stream-created) (:focus stream-created)))))


(defrecord FocusUpdated [id new-focus]
  state/EventTransition
  (state/transition [focus-updated stream]
    (update-focus stream (:new-focus focus-updated))))



