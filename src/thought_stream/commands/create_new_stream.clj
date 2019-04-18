(ns thought-stream.commands.create-new-stream
  (:require
    [thought-stream.thought-stream-logic.stream :as stream]
    [thought-stream.state :as state]
    [thought-stream.utilities :as util]
    [clojure.string :as s]))



(defn create-new-stream [base-aggregate new-stream]
  (if-not (state/base-aggregate? base-aggregate)
    (throw (IllegalArgumentException. (str "create new stream requires base aggregate as initial value: Invalid input: " base-aggregate))))
  (if-not (map? new-stream)
    (throw (IllegalArgumentException. (str "Invalid new stream: Invalid Input: " new-stream))))
  (let [stream-id (:id new-stream)
        focus-text (:focus new-stream)
        id-validation (if-not (util/valid-id? stream-id) (str "Invalid stream id: " stream-id " "))
        focus-text-validation (if-not (util/valid-text? focus-text) (str "Invalid focus text: " focus-text " "))]
    (if-not (and (nil? id-validation) (nil? focus-text-validation))
      (throw (IllegalArgumentException. (s/trim (str "Invalid new stream: " id-validation focus-text-validation)))))
    (state/update-state base-aggregate (stream/->StreamCreated stream-id focus-text)))
)
