(set-env!
  :resource-paths #{"src"}
  :dependencies '[[nightlight "2.1.0" :scope "test"]
                  [dynadoc "1.3.0" :scope "test"]
                  [seancorfield/boot-tools-deps "0.1.4" :scope "test"]]
  :repositories (conj (get-env :repositories)
                  ["clojars" {:url "https://clojars.org/repo/"
                              :username (System/getenv "CLOJARS_USER")
                              :password (System/getenv "CLOJARS_PASS")}]))

(require
  '[clojure.edn :as edn]
  '[dynadoc.example]
  '[dynadoc.boot :refer [dynadoc]]
  '[nightlight.boot :refer [nightlight]]
  '[boot-tools-deps.core :refer [deps]])

(task-options!
  pom {:project 'defexample
       :version "1.7.1-SNAPSHOT"
       :description "A macro for defining code examples"
       :url "https://github.com/oakes/defexample"
       :license {"Public Domain" "http://unlicense.org/UNLICENSE"}
       :dependencies (->> "deps.edn"
                          slurp
                          edn/read-string
                          :deps
                          (reduce
                            (fn [deps [artifact info]]
                              (if-let [version (:mvn/version info)]
                                (conj deps
                                  (transduce cat conj [artifact version]
                                    (select-keys info [:scope :exclusions])))
                                deps))
                            []))}
  push {:repo "clojars"})

(deftask run []
  (comp
    (deps)
    (wait)
    (nightlight :port 4000)
    (dynadoc :port 5000)))

(deftask local []
  (comp (pom) (jar) (install)))

(deftask deploy []
  (comp (pom) (jar) (push)))

