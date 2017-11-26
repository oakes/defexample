(ns dynadoc.example
  (:require [clojure.spec.alpha :as s :refer [fdef]]
            [clojure.core.specs.alpha]))

(defn non-evaled-fn? [x]
  (or (symbol? x)
      (and (coll? x)
           (contains? #{'fn 'fn*} (first x)))))

(defn ^:private any?* [_] true)

(s/def ::doc string?)
(s/def ::ret non-evaled-fn?)
(s/def ::with-card :clojure.core.specs.alpha/local-name)
(s/def ::with-focus :clojure.core.specs.alpha/binding)
(s/def ::opts (s/keys :opt-un [::doc ::ret ::with-card ::with-focus]))
(s/def ::body any?*)
(s/def ::args (s/cat
                :meta (s/? (s/alt
                             :doc ::doc
                             :opts ::opts))
                :body (s/+ ::body)))

(s/def ::example (s/cat :key any?* :args ::args))
(s/def ::examples (s/cat :key any?* :args (s/+ ::args)))

(fdef defexample*
  :args ::example)

(fdef defexamples*
  :args ::examples)

(fdef defexample
  :args ::example)

(fdef defexamples
  :args ::examples)

(defonce registry-ref (atom {}))

(defn parse-ns [k]
  (try (symbol (namespace k))
    (catch Exception _)))

(defn parse-val [v]
  (cond
    (symbol? v) (symbol (name v))
    (keyword? v) (keyword (name v))
    :else v))

(defn parse-keys [k]
  [(or (parse-ns k) (symbol (str *ns*)))
   (parse-val k)])

(defn parse-example [args]
  (let [{:keys [meta body]} (s/conform ::args args)
        _ (when (= meta :clojure.spec/invalid)
            (throw (Exception. (str "Invalid args: " (pr-str args)))))
        [meta-type meta-val] meta
        opts (case meta-type
               :doc {:doc meta-val}
               :opts meta-val
               {})
        body (if (> (count body) 1)
               (apply list 'do body)
               (first body))]
    (assoc opts :body body)))

(defn defexample*
  "Like defexample, but a function instead of a macro"
  [k & args]
  (let [[ns-sym k] (parse-keys k)
        example (parse-example args)]
    (swap! registry-ref assoc-in [ns-sym k] [example])
    nil))

(defn defexamples*
  "Like defexamples, but a function instead of a macro"
  [k & examples]
  (let [[ns-sym k] (parse-keys k)]
    (swap! registry-ref assoc-in [ns-sym k]
      (mapv parse-example examples))
    nil))

(defmacro defexample
  "Defines one example code block for a symbol or an arbitrary
  piece of Clojure data. If `k` is not a namespace-qualified symbol or
  keyword, it will be associated with the current namespace."
  [k & args]
  (apply defexample* k args))

(defmacro defexamples
  "Defines multiple example code blocks for a symbol or an arbitrary
  piece of Clojure data. If `k` is not a namespace-qualified symbol or
  keyword, it will be associated with the current namespace."
  [k & examples]
  (apply defexamples* k examples))

(defexamples defexample
  ["Define an example of a function in another namespace"
   (defexample clojure.core/+
     "Add two numbers together"
     (+ 1 1))]
  ["Define an example of a function in the current namespace"
   (defn square [n]
     (* n n))
   (defexample square
     "Multiply 2 by itself"
     (square 2))]
  ["Define an example of a function with an assertion for testing"
   (defn square [n]
     (* n n))
   (defexample square
     {:doc "Multiply 2 by itself"
      :ret (fn [n] (= n 4))}
     (square 2))]
  ["Define an example of a piece of arbitrary data"
   (defexample :table
     "Creates a <table> tag"
     [:table
      [:tr
       [:td "Column 1"]
       [:td "Column 2"]]])])

(defexample defexamples
  "Define multiple examples of the `conj` function"
  (defexamples clojure.core/conj
    ["Add a name to a vector"
     (conj ["Alice" "Bob"] "Charlie")]
    ["Add a number to a list"
     (conj '(2 3) 1)]
    [{:doc "Add a key-val pair to a hash map"
      :ret (fn [m] (= m {:name "Alice" :age 30}))}
     (conj {:name "Alice"} [:age 30])]))

