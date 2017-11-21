(ns dynadoc.example-spec
  (:require [clojure.spec.alpha :as s :refer [fdef]]
            [dynadoc.example :as ex]))

(defn function? [x]
  (or (symbol? x)
      (and (coll? x)
           (contains? #{'fn 'fn*} (first x))
           (vector? (second x)))))

(s/def ::doc string?)
(s/def ::pre coll?)
(s/def ::def coll?)
(s/def ::ret function?)
(s/def ::dom boolean?)

(s/def ::example (s/keys*
                   :req-un [::def]
                   :opt-un [::doc ::pre ::ret ::dom]))
(s/def ::examples (s/* (s/keys
                         :req-un [::def]
                         :opt-un [::doc ::pre ::ret ::dom])))

(fdef ex/defexample
  :args (s/cat
          :key any?
          :args (s/alt
                  :single ::example
                  :multiple ::examples)))

