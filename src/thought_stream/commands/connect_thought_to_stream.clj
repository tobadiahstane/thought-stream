(ns thought-stream.commands.connect-thought-to-stream
  (:require
    [thought-stream.thought-stream-logic.thought :as thought]
    [thought-stream.utilities :as util]
    [thought-stream.state :as state]))


(defn- valid-thought-aggregate? [input]
  (and (thought/valid-thought? input) (state/aggregate? input)))


(defn- valid-connecting-stream? [input]
  (util/valid-id? (:connecting-to input)))

(defn connect-thought-to-stream
  [thought stream]
  (when-not (valid-thought-aggregate? thought)
    (throw (IllegalArgumentException. (str "Invalid thought: " thought))))
  (when-not (valid-connecting-stream? stream)
    (throw (IllegalArgumentException. (str "Invalid stream id: " stream))))
  (let [thought-connected-event (thought/->ThoughtConnected (:id thought) (:connecting-to stream))]
    (state/update-state thought thought-connected-event)))
