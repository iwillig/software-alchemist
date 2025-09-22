(ns software-alchemist.main
  "Main interface for Software Alchemist

  java -cp target/software-alchemist.jar clojure.main -m software-alchemist.main

  "
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn -main [& args]
  (println args))
