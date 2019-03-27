(ns thought-stream.view-test
  (:require
    [clojure.test :refer :all]))


(def fsm {'start {:init 'ready}
          'ready {:invalid-email 'email-error
                  :have-thought 'having-thought
                  :invalid-thought 'thought-error}
          'email-error {:change-email 'ready}
          'thought-error {:change-thought 'ready}
          'having-thought {:recive-success 'success}}
         )


(defn next-state [app-state transition]
  (let [next-state (get-in fsm [(:state app-state) transition])]
    (assoc app-state :state next-state)
    ))

(deftest finite-state-machine-exits
  (is (associative? fsm)))

(deftest fsm-has-thought-stream-states
  (is (some #{'start} (keys fsm)))
  (is (some #{'ready} (keys fsm)))
  (is (some #{'email-error} (keys fsm)))
  (is (some #{'having-thought} (keys fsm)))
  (is (some #{'thought-error} (keys fsm)))

)


(deftest starting-state-from-fsm
  (is (= '(:init) (keys ('start fsm))))
  (is (= '(ready) (vals ('start fsm)))))


(deftest ready-state-from-fsm
  (is (= '(:invalid-email :have-thought :invalid-thought) (keys ('ready fsm))))
  (is (= 'email-error (get-in fsm ['ready :invalid-email])))
  (is (= 'having-thought (get-in fsm ['ready :have-thought])))
  (is (= 'thought-error (get-in fsm ['ready :invalid-thought])))

)

(deftest email-error-state-from-fsm
  (is (= '(:change-email) (keys ('email-error fsm))))
  (is (= 'ready (get-in fsm ['email-error :change-email])))
)

(deftest thought-error-state-from-fsm
  (is (= '(:change-thought) (keys ('thought-error fsm))))
  (is (= 'ready (get-in fsm ['thought-error :change-thought])))
)

(deftest sucessful-thought-state-from-fsm
  (is (= '(:recive-success) (keys ('having-thought fsm))))
  (is (= 'success (get-in fsm ['having-thought :recive-success])))
)


(deftest next-state-returns-app-state-with-unchanged-structure
  (let [app-state {:email nil
                   :state 'start}]
    (is (= (keys app-state) (keys (next-state app-state nil))))))

(deftest next-state-can-transition-app-state-from-start
  (let [app-state {:email nil
                   :state 'start}]
    (is (= 'ready (:state (next-state app-state (first (keys ('start fsm)))))))))

(deftest next-state-can-transition-app-state-from-ready
  (let [app-state {:email nil :state 'ready}]
    (is (= 'email-error (:state (next-state app-state (first (keys ('ready fsm)))))))
  )
)

(deftest next-state-returns-app-state-from-email-error
  (let [app-state {:email nil :state 'email-error}]
    (is (= 'ready (:state (next-state app-state (first (keys ('email-error fsm)))))))
  )
)


(def init-request
  {:ssl-client-cert nil
   :protocol "HTTP/1.1"
    :remote-addr "0:0:0:0:0:0:0:1"
    :headers "{accept text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8, upgrade-insecure-requests 1, connection keep-alive, user-agent Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) LightTable/0.8.1 Chrome/45.0.2454.85 Electron/0.34.5 Safari/537.36, host localhost:3000, accept-encoding gzip, deflate, accept-language en-US}"
    :server-port 3000
    :content-length nil
    :content-type nil
    :character-encoding nil
    :uri /
    :server-name "localhost"
    :query-string nil
    :body "#object[org.eclipse.jetty.server.HttpInputOverHTTP 0x7b42ee74 HttpInputOverHTTP@7b42ee74[c=0,q=0,[0]=null,s=STREAM]]"
    :scheme :http
    :request-method :get})


(defn some-function [request]
  {:status 200
   :body nil
   :headers nil})

(deftest can-return-renderable-homepage-view
  (let [base-response-map {:status nil :body nil :headers nil}
        response (some-function init-request)]
    (is (associative? response))
    (is (= (keys base-response-map) (keys response)))
    (is (= 200 (:status response)))
    ))


(run-tests 'thought-stream.view-test)
















