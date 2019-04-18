(ns thought-stream.thought-stream-logic.thought
  (:require
    [thought-stream.utilities :as util]
    [thought-stream.state :as state]))


(defn valid-link? [checking]
  (string? checking))


(defn new-thought
  [thought-id {:keys [thinker thought]}]
  {:pre [(util/valid-id? thought-id)
         (util/valid-text?  thought)
         (util/valid-id? thinker)]}
    {:id thought-id
     :thinker thinker
     :thought thought
     :link-url nil
     :connections #{}
     :timestamp (System/currentTimeMillis)})

(defn add-link-url
  [thought link]
  {:pre [(valid-link? link)]}
  (assoc thought :link-url link))


(defn add-connection
  [thought connecting-id]
  {:pre [(util/valid-id? connecting-id)]}
  (update thought :connections conj connecting-id))

(defn valid-thought? [thought]
  (and (util/valid-id? (:id thought))
       (util/valid-id? (:thinker thought))
       (util/valid-text? (:thought thought))
       (or (valid-link? (:link-url thought))
           (nil? (:link-url thought)))
       (set? (:connections thought))
       (or (every? #(util/valid-id? %) (:connections thought))
           (nil? (seq (:connections thought))))))


(defrecord Thought [id thinker thought]
  state/EventTransition
  (state/transition [thought base-aggregate]
    (merge base-aggregate (new-thought (:id thought) thought))))


(defrecord ThoughtConnected [id connected-id]
  state/EventTransition
  (state/transition [thought-connected thought]
    (add-connection thought (:connected-id thought-connected))))

(defrecord UrlLinked [id url-link]
  state/EventTransition
  (state/transition [linked thought]
    (add-link-url thought (:url-link linked))))
