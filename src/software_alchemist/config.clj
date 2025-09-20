(ns software-alchemist.config
  "Namespace that manages interacting with the projects config system."
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [malli.core :as m]))

(def ^:private default-config-folder
  ".archivist")

(def ^:private default-config-file
  "config.yml")

(defn build-config-file-path
  []
  (format "%s/%s" default-config-folder default-config-file))

(def languages [:enum "clojure"])
(def vcs-systems [:enum "git"])

(def config-spec
  [:map {:closed true}
   [:name       :string]
   [:database   :string]
   [:language   languages]
   [:vcs-system vcs-systems]])

(defn load-config
  "Loads config from the file system."
  []
  (let [file-path (build-config-file-path)
        config    (yaml/parse-string (slurp file-path))]
    (m/assert config-spec config)
    config))
