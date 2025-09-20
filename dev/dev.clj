(ns dev
  (:require
   [software-alchemist.db :as alchemist.db]
   [ragtime.repl :as ragtime.repl]
   [kaocha.repl :as k]
   [clj-reload.core :as reload]))

(reload/init
  {:dirs ["src" "dev" "test"]})

(def db-name "dev.db")

(defn refresh
  []
  (reload/reload))

(comment

  (ragtime.repl/migrate (alchemist.db/build-migration-config db-name))

  )
