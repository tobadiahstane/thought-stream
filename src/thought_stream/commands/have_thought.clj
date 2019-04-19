(ns thought-stream.commands.have-thought
  (:require
    [thought-stream.thought-stream-logic.thought :as thought]
    [thought-stream.commands.execution :as ex]
    [thought-stream.commands.config :as ccg]
    [thought-stream.state :as state]
    [thought-stream.utilities :as util]
    [clojure.string :as s]))


;Currently, the decision has been made to not canonicalize inputs.
;This is because in the case where the thought text being saved is about encoding,
;the encoding symbols used may be directly relevent to the thought.
;in such a case the information loss caused by canonicalization is directly changing the
;semantic content of the text. This should be avoided.


(defn within-char-limit?
  "Inclusive of upper bound.
  Lower char bound is zero.."
  [upper input]
  (and (>= upper input) (> input 0)))


(defn- throw-invalid-new-thought [& messages]
  (throw (IllegalArgumentException. (s/trim (apply str "Invalid new thought: " messages)))))



(defn- validate-base-aggregate [base-aggregate]
  (if-not (state/base-aggregate? base-aggregate)
    (throw (IllegalArgumentException. (str "Have thought requires base aggregate as initial value: Invalid input: " base-aggregate)))))

(defn- check-invalid-thought-id [{:keys [id]}]
  (if-not (util/valid-id? id)
    (str "Invalid thought id: " id " ")))

(defn- check-invalid-thinker [{:keys [thinker]}]
  (if-not (util/valid-id? thinker)
    (str "Invalid thinker: " thinker " ")))

(defn- check-invalid-thought-text [{:keys [thought]}]
  (if-not (string? thought)
    (str "Invalid thought text: " thought " ")))

(defn- check-invalid-link-url [{:keys [link-url]}]
  (if-not (or (thought/valid-link? link-url) (nil? link-url))
    (str "Invalid link url: " link-url " ")))

(defn- every-check-passes? [& check-results]
  (every? nil? check-results))

(defn- validate-new-thought [new-thought]
  (if-not (map? new-thought)
    (throw-invalid-new-thought new-thought))
  (let [id-validation (check-invalid-thought-id new-thought)
        thinker-validation (check-invalid-thinker new-thought)
        thought-validation (check-invalid-thought-text new-thought)
        link-url-validation (check-invalid-link-url new-thought)]
    (if-not (every-check-passes? id-validation thinker-validation thought-validation link-url-validation)
      (throw-invalid-new-thought id-validation thinker-validation thought-validation link-url-validation))))

(defn- validate-within-char-limit [thought-text]
  (let [clean-text (s/trim (s/replace thought-text #"\p{javaSpaceChar}" " "))
        clean-count (count clean-text)]
      (if-not (within-char-limit? ccg/max-thought-length clean-count)
        (throw (IllegalStateException. (str "thought text outside expected bounds: text count " clean-count))))))

(defn have-thought [base-aggregate new-thought]
  (validate-base-aggregate base-aggregate)
  (validate-new-thought new-thought)
  (let [thought-id (:id new-thought)
        thinker (:thinker new-thought)
        thought-text (:thought new-thought)
        link-url (:link-url new-thought)]
    (validate-within-char-limit thought-text)
    (if (nil? link-url)
      (state/update-state base-aggregate (thought/->Thought thought-id thinker thought-text))
        (-> base-aggregate
          (state/update-state (thought/->Thought thought-id thinker thought-text))
          (state/update-state (thought/->UrlLinked thought-id link-url))))))



(defrecord HaveThought [id thinker thought link-url]
  ex/ICommand
  (ex/execute [command-input base-aggregate]
    (have-thought base-aggregate command-input)))

