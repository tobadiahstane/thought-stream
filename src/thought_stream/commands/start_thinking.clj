(ns thought-stream.commands.start-thinking
  (:require
    [thought-stream.thought-stream-logic.thinker :as thinker]
    [thought-stream.state :as state]
    [thought-stream.utilities :as util]
    [clojure.string :as s]))


(defn start-thinking [aggregate new-thinker]
  (if-not (state/aggregate? aggregate)
    (throw (IllegalArgumentException. (str "Start thinking requires aggregate as initial value: Invalid input: " aggregate))))
  (if-not (map? new-thinker)
    (throw (IllegalArgumentException. (str "Invalid new thinker: " new-thinker))))
  (let [id (:id new-thinker)
        username (:thinker-username new-thinker)
        password (:thinker-password new-thinker)
        id-validation-check (if-not (util/valid-id? id)
                             (str " Invalid thinker id: " id))
        username-check (if-not (string? username)
                              (str " Invalid thinker username: " username))
        password-check (if-not (string? password)
                         (str " Invalid thinker password: " password))]
    (if-not (and (nil? id-validation-check) (nil? username-check) (nil? password-check))
      (throw (IllegalArgumentException. (s/trim (str "Invalid new thinker:" id-validation-check username-check password-check)))))
    (let [check-length (if-not (<= 8 (count password))
                         (str "At least " 8 " characters, "))
          check-contains-upper (if-not (re-find #"[A-Z]" password)
                                 (str "Contains at least one upper case character, "))
          check-contains-lower (if-not (re-find #"[a-z]" password)
                                 (str "Contains at least one lower case character, "))
          check-contains-digit (if-not (re-find #"[0-9]" password) "Contains at least one digit, ")
          check-contains-special-char (if-not (re-find #"[!@#\$%\^&\*]" password) "Contains at least one special character")]
      (if-not (and (nil? check-length) (nil? check-contains-upper) (nil? check-contains-lower) (nil? check-contains-digit) (nil? check-contains-special-char))
        (throw (IllegalStateException.
                 (str "Password fails requirements: " check-length check-contains-upper check-contains-lower check-contains-digit check-contains-special-char))))))
    (state/update-state aggregate (thinker/map->ThinkingStarted new-thinker))
  )
