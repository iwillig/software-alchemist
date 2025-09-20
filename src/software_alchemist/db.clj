(ns software-alchemist.db
  "The primary namespace for managing interactions with the alchemist
  project."
  (:require [next.jdbc :as jdbc]
            [ragtime.next-jdbc :as ragtime.jdbc]
            [honey.sql :as honey.sql]
            [malli.core :as m]))

;; ------------------------------
;; basic sql operations
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

(comment
  (m/assert sql-statement-spec {})
  (m/assert sql-statement-spec ["string"]))

(defn execute!
  "Execute! against the ds with the honeysql expression."
  [ds sql-statement]
  (m/assert sql-statement-spec sql-statement-spec)
  (jdbc/execute!
   ds
   (honey.sql/format sql-statement)))

(defn execute-one!
  "Execute-one! against the data source"
  [ds sql-statement]
  (m/assert sql-statement-spec sql-statement-spec)
  (jdbc/execute-one!
   ds
   (honey.sql/format sql-statement)))

;; ------------------------------
;; migrations

(defn build-migration-config
  [dbname]
  {:datastore (ragtime.jdbc/sql-database (build-db-spec dbname))
   :migrations (ragtime.jdbc/load-directory "migrations")})
