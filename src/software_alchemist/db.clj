(ns software-alchemist.db
  "The primary namespace for managing interactions with the alchemist
  project."
  (:require [next.jdbc :as jdbc]
            [honey.sql :as honey.sql]
            [malli.core :as m]))

(defn build-db-spec
  "Returns the JDBC connection map"
  [dbname]
  {:dbtype "sqlite" :dbname dbname})

(defn data-source
  "Returns a JDBC next data source"
  [dbname]
  (jdbc/get-datasource (build-db-spec dbname)))

(def sql-statement-spec
  [:or [:seqable :string] :map])

(defn execute!
  "Executes"
  [ds sql-statement]
  (m/assert sql-statement-spec sql-statement-spec)
  (jdbc/execute!
   ds
   (honey.sql/format sql-statement)))

(defn execute-one!
  "Execute-one! "
  [ds sql-statement]
  (m/assert sql-statement-spec sql-statement-spec)
  (jdbc/execute-one!
   ds
   (honey.sql/format sql-statement)))


(comment
  (m/assert sql-statement-spec {}))
