(ns software-alchemist.parse
  (:require [clj-antlr.core :as antlr]
            [clojure.zip :as zip]
            [clojure.java.io :as io]))

(def clojure (antlr/parser "resources/grammars/Clojure.g4"))

(defn simplify-ast-zipper
  "Simplifies a verbose parser AST using a clojure.zip-based approach.
  The function traverses the AST and replaces the complex node maps with
  simpler Clojure data types, effectively 'unwrapping' the AST."
  [ast]
  (loop [z (zip/vector-zip ast)  ; Create a zipper for nested vectors and start a loop
         processed-z z]

    (if (zip/end? processed-z)
      ;; If we have reached the end of the tree, return the root of the modified zipper.
      (zip/root processed-z)
      ;; Otherwise, get the current location and node, apply the transformation rules,
      ;; and continue the loop with the next location.
      (let [loc processed-z
            node (zip/node loc)
            _ (println "got here")
            new-loc (zip/next
                     (cond

                       (and (string? node) (re-matches #"[\(\)\[\]\{\}]" node))
                       (zip/remove loc)

                        ;; Remove `:file_` and `:form` wrappers by replacing the node with its child.
                        (or (:file_ node) (:form node))
                        (zip/edit loc (fn [n] (first (vals n))))

                        ;; Unwrap structural nodes like `:list_`, `:vector`, and `:map_`.
                        (or (:list_ node) (:vector node) (:map_ node))
                        loc

                        ;; Simplify literal values (symbols, keywords, strings).
                        (:literal node)
                        (let [val (first (vals node))]
                          (zip/edit loc
                            (cond
                              (:symbol val) (let [sym-val (first (vals val))]
                                              (if (:simple_sym sym-val)
                                                (symbol (:simple_sym sym-val))
                                                (symbol (:ns_symbol sym-val))))
                              (:keyword val) (keyword (:simple_keyword val))
                              (:string_ val) (subs (:string_ val) 1 (dec (count (:string_ val))))
                              :else (first (vals node)))))

                        ;; Unwrap a simple keyword.
                        (:simple_keyword node)
                        (zip/edit loc (fn [n] (keyword (:simple_keyword n))))

                        ;; Unwrap a simple symbol.
                        (:simple_sym node)
                        (zip/edit loc (fn [n] (symbol (:simple_sym n))))

                        ;; For all other nodes, make no changes.
                        :else loc))]

        (recur new-loc new-loc)))))

(comment

  (simplify-ast-zipper
   (clojure (slurp "src/software_alchemist/db.clj")))

  )
