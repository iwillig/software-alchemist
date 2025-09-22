(ns software-alchemist.main
  "Main interface for Software Alchemist

  java -cp target/software-alchemist.jar clojure.main -m software-alchemist.main"
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [software-alchemist.sub-commands.init :as sub-command.init]
   [software-alchemist.sub-commands :as sub-commands])
  (:gen-class))

(def cli-options
  [["-h" "--help"]])

(def sub-commands #{"init"})

(defn validate-args
  [args]
  (let [{:keys [options errors arguments summary]} (parse-opts args cli-options)]

    (cond

      (contains? sub-commands (first arguments))
      {:sub-command (first arguments) :options options}

      (:help options)
      {:exit-message (sub-commands/usage summary) :ok? true}

      (some? errors)
      {:exit-message "no-okay" :ok? false}

      :else
      {:exit-message (sub-commands/usage summary) :ok? true})))

(defn -main
  "Main Command Line Function"
  [& args]
  (let [{:keys [options exit-message ok? sub-command] :as _args} (validate-args args)]
    (if exit-message
      (sub-commands/exit (if ok? 0 1) exit-message)
      (case sub-command
        "init" (sub-command.init/run options)))))
