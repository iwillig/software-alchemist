(ns software-alchemist.main
  "Main interface for Software Alchemist

  java -cp target/software-alchemist.jar clojure.main -m software-alchemist.main

  "
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [["-h" "--help"]])

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(defn usage
  [summary]
  summary)

(def sub-commands #{"init"})

(defn validate-args
  [args]
  (let [{:keys [options errors arguments summary]} (parse-opts args cli-options)]

    (cond

      (contains? sub-commands (first arguments))
      {:sub-command (first arguments) :options options}

      (:help options)
      {:exit-message (usage summary) :ok? true}

      (some? errors)
      {:exit-message "no-okay" :ok? false}

      :else
      {:exit-message (usage summary) :ok? true})))

(defn init
  [options]
  (cond
    (:help options)
    {:exit-message (str "init " (usage options)) :ok? true}
    :else (do (println :init options))))

(defn -main
  "Main Command Line Function"
  [& args]
  (let [{:keys [options exit-message ok? sub-command] :as _args} (validate-args args)]
    (println _args)
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case sub-command
        "init" (init options)))))
