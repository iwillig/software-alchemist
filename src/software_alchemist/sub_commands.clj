(ns software-alchemist.sub-commands)

(defn usage
  [summary]
  summary)

(defn exit
  [status msg]
  (println ";; exit")
  (println msg)
  (System/exit status))
