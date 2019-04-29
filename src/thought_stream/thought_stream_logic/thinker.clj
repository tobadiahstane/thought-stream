(ns thought-stream.thought-stream-logic.thinker
  (:require
    [thought-stream.utilities :as util]
    [thought-stream.state :as state]))


(defn valid-input? [thinker-input]
  (and (map? thinker-input)
     (util/valid-id? (:id thinker-input))
     (string? (:thinker-username thinker-input))
     (string? (:thinker-password thinker-input))))

(defn new-thinker [thinker-input]
  {:pre [(valid-input? thinker-input)]}
  (assoc-in thinker-input [:roles :thinker] true))


(defrecord ThinkingStarted [id thinker-username thinker-password]
  state/EventTransition
  (state/transition [thinking-started aggregate]
    (merge aggregate (new-thinker thinking-started))))

