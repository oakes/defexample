(set-env!
  :source-paths #{"src"}
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                  [clojure-future-spec "1.9.0-beta4"]
                  [nightlight "2.0.4" :scope "test"]
                  [dynadoc "1.1.5" :scope "test"]]
  :repositories (conj (get-env :repositories)
                  ["clojars" {:url "https://clojars.org/repo/"
                              :username (System/getenv "CLOJARS_USER")
                              :password (System/getenv "CLOJARS_PASS")}]))
(require
  '[dynadoc.example]
  '[dynadoc.boot :refer [dynadoc]]
  '[nightlight.boot :refer [nightlight]])

(task-options!
  pom {:project 'defexample
       :version "1.7.0-SNAPSHOT"
       :description "A macro for defining code examples"
       :url "https://github.com/oakes/defexample"
       :license {"Public Domain" "http://unlicense.org/UNLICENSE"}}
  push {:repo "clojars"})

(deftask run []
  (comp
    (wait)
    (nightlight :port 4000)))

(deftask run-docs []
  (comp
    (wait)
    (dynadoc :port 5000)))

(deftask local []
  (comp (pom) (jar) (install)))

(deftask deploy []
  (comp (pom) (jar) (push)))

