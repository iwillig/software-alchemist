(ns software-alchemist.main
  "Main interface for Software Alchemist

  java -cp target/software-alchemist.jar clojure.main -m software-alchemist.main"
  (:require
   [cli-matic.core :as cli-matic]
   [software-alchemist.sub-commands.init :as sub-command.init]
   [software-alchemist.sub-commands :as sub-commands])
  (:gen-class))

(def cli-configuration
  {:app {:command     "alchemist"
         :description "A command line tool for generating knowledge bases from your code base."
         :version     "0.0.1"}
   :commands [{:command "init"
               :description ["Initializes the project in your repo"]}]})

(defn -main
  "Main Command Line Function"
  [& args]
  (cli-matic/run-cmd args cli-configuration))
