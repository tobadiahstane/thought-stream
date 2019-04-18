(ns thought-stream.state)

(defprotocol EventTransition
  (transition [event aggregate]))

(defn add-changes-tracker [aggregate]
  (assoc aggregate :changes (clojure.lang.PersistentQueue/EMPTY)))


(defn- transition-aggregate [aggregate event]
  (transition event aggregate))

(defn aggregate-from-history [aggregate history]
  (add-changes-tracker (assoc (reduce transition-aggregate aggregate history) :expected-version (count history))))

(defn update-state [aggregate new-event]
  (let [current-version (+ (if (some? (:expected-version aggregate))
                             (:expected-version aggregate) 0)
                           (count (:changes aggregate)))]
    (update (transition new-event aggregate) :changes conj (assoc new-event :version (+ 1 current-version)))))


(defn has-changes-queue? [agg]
  (instance? clojure.lang.PersistentQueue (:changes agg)))

(defn base-aggregate []
  (assoc (add-changes-tracker {}) :expected-version 0))

(defn aggregate? [input]
  (and (map? input)
       (has-changes-queue? input)
       (contains? input :expected-version)
       (not (neg? (:expected-version input)))))

(defn base-aggregate? [input]
  (and (aggregate? input)
       (zero? (:expected-version input))
       (every? #{:changes :expected-version} (keys input))))


