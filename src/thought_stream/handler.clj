(ns thought-stream.handler
  (:require
    [thought-stream.thought-stream :as ts :refer [ExecuteCommand start]]
    [thought-stream.stream :as stream]
    [clojure.string :as s]))

(defn valid-email?
  [email]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (and (string? email) (re-matches pattern email))))


(def max-string-length 300)

(extend-protocol ExecuteCommand
  thought_stream.thought_stream.StartThinking
  (ts/execute [start-thinking]
    (if-not (valid-email? (:email start-thinking))
      (throw (Exception. "Invalid Email."))
      (let [no-thoughts (stream/thinking-from-history)]
        (ts/store (stream/update-thoughts no-thoughts (ts/map->ThinkingStarted start-thinking)))))))


(extend-protocol ExecuteCommand
  thought_stream.thought_stream.HaveThought
  (ts/execute [have-thought]
   (do (if (s/blank? (:thought have-thought))
        (throw (Exception. "Can't have empty thought.")))
       (if (< 300 (count (:thought have-thought)))
         (throw (Exception. "Thought over 300 character limit.")))
     (let [event-loader (stream/->EventTransferObject (:id have-thought) nil)
           events (:events (stream/load-events event-loader))
           thinking (stream/thinking-from-history events)]
       (ts/store (stream/update-thoughts thinking (ts/map->Thought have-thought)))))))


(defn start-thinking
  "Start thinking thoughts and thought-streams."
  [{:keys [id email] :as new-thinking}]
  ((ts/start ts/thinking) new-thinking))


(defn have-thought
  [{:keys [id thought link connecting] :as new-thought}]
  ((ts/start ts/having) new-thought))
