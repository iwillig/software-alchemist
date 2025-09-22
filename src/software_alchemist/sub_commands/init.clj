(ns software-alchemist.sub-commands.init
  (:require [software-alchemist.sub-commands :as sub-commands]))

(defn run
  [options]
  (cond
    (:help options)
    {:exit-message (str "init " (sub-commands/usage options)) :ok? true}
    :else (do
            (println ";; init sub command")
            (println :init options))))
