(ns software-alchemist.parse
  "Main parser namespace for Software Alchemist"
  (:require [clj-antlr.core :as antlr]
            [clojure.zip :as zip]
            [clojure.java.io :as io]))

(def clojure (antlr/parser "resources/grammars/Clojure.g4"))

(def literals #{"{" "}" "(" ")" "[" "]" ":" "<EOF>"})

(defn simplify-ast-zipper
  [ast]
  (loop [loc (zip/seq-zip ast)]
    (if (zip/end? loc)
      (zip/root loc)
      (let [node (zip/node loc)]
        (if (and (string? node) (contains? literals node))
          (recur (zip/next (zip/remove loc)))
          (recur (zip/next loc)))))))

(comment

  (simplify-ast-zipper
   (clojure (slurp "src/software_alchemist/db.clj")))

  )
