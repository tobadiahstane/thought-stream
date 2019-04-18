(ns thought-stream.commands.have-thought-test
  (:require
    [thought-stream.commands.have-thought :refer [have-thought]]
    [thought-stream.thought-stream-logic.thought :as thought]
    [thought-stream.commands.config :as ccg]
    [thought-stream.utilities :as util]
    [thought-stream.test-mother :as mom]
    [clojure.test :refer :all]
    [clojure.test.check.generators :as gen]))

;requires a test against map containing other keys than base map.
(deftest given-not-empty-map-for-thought-have-thought-throws-illegal-argument-exception-test
  (is (thrown-with-msg? IllegalArgumentException
        #"Have thought requires base aggregate as initial value: Invalid input: "
        (have-thought nil nil)))
  (is (thrown-with-msg? IllegalArgumentException
        #"Have thought requires base aggregate as initial value: Invalid input: 0"
        (have-thought 0 nil)))
  (is (thrown-with-msg? IllegalArgumentException
        #"Have thought requires base aggregate as initial value: Invalid input: \{:some-key nil\}"
        (have-thought {:some-key nil} nil)))
  (is (thrown? IllegalArgumentException
        (have-thought (assoc (mom/make-base-aggregate) :some-key nil) nil)))
      )

(deftest given-empty-map-invalid-thought-input-have-thought-throws-illegal-argument-exception-test
  (testing "Given the thought input is not a map"
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought:"
          (have-thought (mom/make-base-aggregate) nil)))
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: 0"
          (have-thought (mom/make-base-aggregate) 0))))
  (testing "The new thought is a map with invalid thought id invalid thinker and invalid thought"
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thought id:  Invalid thinker:  Invalid thought text:"
          (have-thought (mom/make-base-aggregate) {:id nil :thinker nil :thought nil})))
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thought id: 0 Invalid thinker: 0 Invalid thought text: 0"
          (have-thought (mom/make-base-aggregate) {:id 0 :thinker 0 :thought 0}))))
  (testing "the new thought has only a valid thinker."
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thought id:  Invalid thought text:"
          (have-thought (mom/make-base-aggregate) {:id nil :thinker (util/new-uuid) :thought nil})))
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thought id: 0 Invalid thought text: 0"
          (have-thought (mom/make-base-aggregate) {:id 0 :thinker (util/new-uuid) :thought 0}))))
  (testing "the new thought has only a valid thought id."
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thinker:  Invalid thought text:"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker nil :thought nil})))
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thinker: 0 Invalid thought text: 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker 0 :thought 0}))))
  (testing "the new thought has a valid thought id and valid thinker."
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thought text:"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought nil})))
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thought text: 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought 0}))))
  (testing "the new thought has only a valid thought text"
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thought id:  Invalid thinker:"
          (have-thought (mom/make-base-aggregate) {:id nil :thinker nil :thought "valid"})))
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid thought id: 0 Invalid thinker: 0"
          (have-thought (mom/make-base-aggregate) {:id 0 :thinker 0 :thought "valid"}))))
  (testing "the new thought has an invalid link url"
    (is (thrown-with-msg? IllegalArgumentException
          #"Invalid new thought: Invalid link url: 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought "valid" :link-url 0})))))


(deftest given-valid-arguments-if-thought-text-string-is-blank-throws-illegal-state-exception
  (testing "Empty string."
    (is (thrown-with-msg? IllegalStateException
          #"thought text outside expected bounds: text count 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought ""}))))
  (testing "String with 1 space"
    (is (thrown-with-msg? IllegalStateException
          #"thought text outside expected bounds: text count 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought " "}))))
  (testing "String with 2 spaces"
    (is (thrown-with-msg? IllegalStateException
          #"thought text outside expected bounds: text count 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought "  "}))))
  (testing "String with 1 tab"
    (is (thrown-with-msg? IllegalStateException
          #"thought text outside expected bounds: text count 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought"  "}))))
  (testing "String with newline only."
    (is (thrown-with-msg? IllegalStateException
          #"thought text outside expected bounds: text count 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought"
                            "}))))
    (testing "String with nobreak space only."
    (is (thrown-with-msg? IllegalStateException
          #"thought text outside expected bounds: text count 0"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought"\u00A0"}))))
  )



(def max-string "adgdasfgsdghdfgdfgfdjgndaslfkgjabpfwba0f7g[4fisduba0wp4utbqir;ufb397abfviatrbjpi34ub-0awe7gtb3p9yubpuhgajfnwoejfne[f
  OASFDJNGAPSRIUGNA[ORUHGND[conj[oajngpaungpojungajn;zlskdngf[.joinN[N[OIN[IGNQIN[ONA   ONDPSFAUONSJN     OJNFAPSUNAPISNFG;AJNSDJFNAPISU
  asojdgnfqpoweirubsadfighebdsriabdofsiab ip uabds ab pifaubf iasudbai ouebfpa iusb ofiwuebfai uwb ofiubeiudb foiubfwieufb wieu fwiu bawueb f
   aweuhfpawuhfapwoeuhfposduifhapwoehfposduifhpsoidfphoiwenfopsduihfpq8wefoasdnfoawefhhajnf[OFUIHASPOFUH")

(def failing-string (str max-string "W"))



(deftest given-valid-arguments-if-thought-text-string-exceeds-maximum-text-string-length-throws-illegal-state-exception
  (testing "String length of 501 exceeds 500 string limit"
    (is (thrown-with-msg? IllegalStateException
          #"thought text outside expected bounds: text count 501"
          (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought failing-string})))))

(deftest given-valid-arguments-if-thought-text-string-within-bounds-return-thought-with-Thought-event
  (testing "String length of 1"
    (let [result  (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought "A"})]
      (is (thought/valid-thought? result))
      (is (instance? thought_stream.thought_stream_logic.thought.Thought (first (:changes result))))))
  (testing "String length of max-string"
    (let [result  (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought max-string})]
      (is (thought/valid-thought? result))
      (is (instance? thought_stream.thought_stream_logic.thought.Thought (first (:changes result)))))))

(deftest given-valid-arguments-and-thought-within-bounds-if-valid-link-return-thought-with-Thought-and-UrlLinked-event
  (let [result (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought "valid thought text" :link-url "someurl"})]
      (is (thought/valid-thought? result))
      (is (instance? thought_stream.thought_stream_logic.thought.UrlLinked (second (:changes result))))))

(println (have-thought (mom/make-base-aggregate) {:id (util/new-uuid) :thinker (util/new-uuid) :thought "valid thought text" :link-url "someurl"}))

(run-tests 'thought-stream.commands.have-thought-test)
