(ns dynadoc.example)

(defonce examples (atom {}))

(defn parse-ns [k]
  (try (symbol (namespace k))
    (catch Exception _)))

(defn parse-val [v]
  (cond
    (symbol? v) (symbol (name v))
    (keyword? v) (keyword (name v))
    :else v))

(defn parse-examples [args]
  (if (map? (first args)) args (list (apply hash-map args))))

(defmacro defexample
  "Defines one or more example code blocks for a symbol or an arbitrary
piece of Clojure data. If `k` is not a namespace-qualified symbol or
keyword, it will be associated with the current namespace."
  [k & args]
  (let [ns-sym (or (parse-ns k) (symbol (str *ns*)))
        k (parse-val k)]
    (swap! examples assoc-in [ns-sym k]
      (parse-examples args))
    nil))

(defexample defexample
  {:doc "Define an example of a function in another namespace"
   :def (defexample clojure.core/+
          :doc "Add two numbers together"
          :def (+ 1 1))}
  {:doc "Define an example of a function in the current namespace"
   :def (do
          (defn square [n]
            (* n n))
          (defexample square
            :doc "Multiply 2 by itself"
            :def (square 2)))}
  {:doc "Define an example of a function with an assertion for testing"
   :def (do
          (defn square [n]
            (* n n))
          (defexample square
            :doc "Multiply 2 by itself"
            :def (square 2)
            :ret (fn [n] (= n 4))))}
  {:doc "Define multiple examples of a function"
   :def (defexample clojure.core/conj
          {:doc "Add a name to a vector"
           :def (conj ["Alice" "Bob"] "Charlie")
           :ret (fn [v] (= v ["Alice" "Bob" "Charlie"]))}
          {:doc "Add a number to a list"
           :def (conj '(2 3) 1)
           :ret (fn [l] (= l '(1 2 3)))}
          {:doc "Add a key-val pair to a hash map"
           :def (conj {:name "Alice"} [:age 30])
           :ret (fn [m] (= m {:name "Alice" :age 30}))})}
  {:doc "Define an example of a piece of arbitrary data"
   :def (defexample :table
          :doc "Creates a <table> tag"
          :def [:table
                [:tr
                 [:td "Column 1"]
                 [:td "Column 2"]]])})

