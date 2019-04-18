(ns thought-stream.utilities)

(defn new-uuid []
  (java.util.UUID/randomUUID))

(defn valid-id? [check]
  (instance? java.util.UUID check))


(defn valid-text? [checking]
  (string? checking))

