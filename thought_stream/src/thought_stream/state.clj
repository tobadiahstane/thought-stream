(ns thought-stream.state)

(defmulti transition (fn [aggregate event] (class event)))

(defn add-changes-tracker [aggregate]
  (assoc aggregate :changes (clojure.lang.PersistentQueue/EMPTY)))

(defn aggregate-from-history [aggregate history]
  (add-changes-tracker (assoc (reduce transition aggregate history) :expected-version (count history))))

(defn update-state [aggregate new-event]
  (let [current-version (+ (if (some? (:expected-version aggregate))
                             (:expected-version aggregate) 0) (count (:changes aggregate)))]
    (update (transition aggregate new-event) :changes conj (assoc new-event :version (+ 1 current-version)))))


