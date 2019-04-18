(ns thought-stream.db-tests
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.test :refer :all]))

(deftest can-create-and-shutdown-table
  (let [conn {:dbtype "h2:mem" :dbname "dbstorage"}
        dropping-test-table (jdbc/drop-table-ddl :test)
        creating-test-table (jdbc/create-table-ddl :test
                                                   [[:aggregate_id "varchar(36)"]
                                                    [:event "varchar(255)"]
                                                    [:order_number "bigint auto_increment"]])]
    (is (thrown? Exception (jdbc/db-do-commands conn [dropping-test-table])))
    (is (= '(0 0) (jdbc/db-do-commands conn true [creating-test-table dropping-test-table])))))


(deftest can-insert-to-and-query-table
  (let [conn {:dbtype "h2:mem" :dbname "dbstorage"}
        dropping-test-table (jdbc/drop-table-ddl :test)
        creating-test-table (jdbc/create-table-ddl :test
                                                   [[:aggregate_id "varchar(36)"]
                                                    [:event "varchar(255)"]
                                                    [:event_no "bigint auto_increment"]])
        _  (jdbc/db-do-commands conn true [creating-test-table])
        query ["select * from test where aggregate_id = 'id'"]
        query-nil (jdbc/query conn query)
        testing-insert (jdbc/insert! conn :test {:aggregate_id "id" :event "This is a test event"})
        query-post-insert (jdbc/query conn query)
        table-dropped  (jdbc/db-do-commands conn true [dropping-test-table])]
    (is (empty? query-nil))
    (is (not (empty? query-post-insert))
    (is (= "id" (:aggregate_id (first query-post-insert)))))
    (is (= "This is a test event" (str (:event (first query-post-insert)))))
    (is (= '(0) table-dropped))))


(run-tests 'thought-stream.db-tests)
