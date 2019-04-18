(ns thought-stream.stream
  (:require
    [thought-stream.thought-stream :as ts]
    [thought-stream.state :refer [transition update-state aggregate-from-history]]))


(defn new-uuid []
  (java.util.UUID/randomUUID))

(defn valid-id? [check]
  (instance? java.util.UUID check))


(defn new-thoughts [id]
  (if-not (valid-id? id)
    (throw (Exception.))
    {:id id
     :thought-streams nil
     :free-thoughts #{}}))

(defn new-thought [thought & {:keys [link] :or [link nil]}]
  (if-not (string? thought)
    (throw (Exception.))
    {:thought thought
     :link link
     :timestamp nil}))


(defn thought? [to-check]
  (and (some #{:thought :link :timestamp} (keys to-check))
    (associative? to-check)))

(defn new-stream
  "A mind/thought stream has a short focus and a sequence of connected thoughts."
  ([]
   (new-stream nil))
  ([focus]
   (let [id (new-uuid)]
     {:id id :focus focus :thoughts []})))

(defn stream? [to-check]
  (and (associative? to-check)
    (some #{:id :focus :thoughts} (keys to-check))
    (valid-id? (:id to-check))))

(defn focus [stream]
  (:focus stream))

(defn update-focus [stream updated]
  (assoc stream :focus updated))


(defn connect-to-stream [stream thought & {:keys [updated-focus]}]
  (if (some? updated-focus)
    (update-focus (update stream :thoughts conj thought) updated-focus)
    (update stream :thoughts conj thought)))

(defn connect-to-thought [connecting to & {:keys [focus] :or [focus nil]}]
  (if-not (and (thought? connecting)
               (thought? to))
    (throw (Exception.))
    (-> (new-stream focus)
      (connect-to-stream connecting)
      (connect-to-stream to))))

(defn connect-to [connecting to & {:keys [focus]}]
  (if (stream? connecting)
    (connect-to-stream connecting to :updated-focus focus)
    (connect-to-thought connecting to :focus focus)))


(defn as-free-thought [thinking new-thought]
  (update thinking :free-thoughts conj new-thought))

(defn id-as-key [id]
  (keyword (str id)))

(defn add-thought-stream [thinking thought-stream]
  (assoc-in thinking [:thought-streams (id-as-key (:id thought-stream))] thought-stream))

(defn as-connected-thought [thinking new-thought connecting focus]
  (-> (add-thought-stream thinking (connect-to connecting new-thought :focus focus))
    (update :free-thoughts disj connecting)))

(defn validate-connectable [thinking connecting-to]
  (if-not (or (contains? (:free-thoughts thinking) connecting-to)
              (some #{connecting-to} (vals (:thought-streams thinking))))
    (throw (Exception. "Unable to connect."))))

(defn have-thought
  "Have a new thought."
  [thinking thought-had]
  (let [{:keys [thought link connecting-to with-focus]} thought-had
         new-thought (new-thought thought :link link)]
    (if (some? connecting-to)
      (do (validate-connectable thinking connecting-to)
        (as-connected-thought thinking new-thought connecting-to with-focus))
      (as-free-thought thinking new-thought))))


;convert connect-thought to an explicit map two arg function
(defn connect-thought [thinking & {:keys [free-thought connecting-to with-focus]}]
    (do
      (validate-connectable thinking free-thought)
      (validate-connectable thinking connecting-to)
      (-> (as-connected-thought thinking free-thought connecting-to with-focus)
        (update :free-thoughts disj free-thought))))

;may only be a test-function
(defn with-thought
  "Zero Based Indexing: Return the thought at 'position' in thoughts"
  [stream pos]
  (nth (:thoughts stream) pos nil))

(defn connected-thoughts
  "Lazy: Returns a sequence of the thoughts connected to the stream."
  [stream]
  (seq (:thoughts stream)))



(defmethod transition thought_stream.thought_stream.ThinkingStarted
  [thinking thoughts-started]
  (merge thinking (new-thoughts (:id thoughts-started))))

(defmethod transition thought_stream.thought_stream.Thought
  [thinking thought]
  (have-thought thinking thought))

;TODEFINE append-thought?
;Rational thoughts can't be unthought/deleted.
;append-thought: Add a new thought which references previous thoughts.
;Idea: Nuanced references: thoughts supercede or thoughts clarify


(defprotocol LoadEvents
  (load-events [event-transfer-object]))

(defrecord EventTransferObject [id events])

(defn thinking-from-history
  ([]
   (aggregate-from-history (ts/->ThinkingThoughts nil) []))
  ([thought-history]
   (aggregate-from-history (ts/->ThinkingThoughts nil) thought-history)))

(defn update-thoughts [thoughts change]
  (update-state thoughts change))
