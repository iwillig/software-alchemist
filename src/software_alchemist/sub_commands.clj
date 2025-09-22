(ns software-alchemist.sub-commands)

(defn usage
  [summary]
  summary)

(defn exit
  [status msg]
  (println msg)
  (System/exit status))
