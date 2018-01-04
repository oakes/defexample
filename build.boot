(set-env!
  :dependencies '[[nightlight "2.1.0" :scope "test"]
                  [dynadoc "1.3.0" :scope "test"]
                  [org.clojars.oakes/boot-tools-deps "0.1.4.1" :scope "test"]]
  :repositories (conj (get-env :repositories)
                  ["clojars" {:url "https://clojars.org/repo/"
                              :username (System/getenv "CLOJARS_USER")
                              :password (System/getenv "CLOJARS_PASS")}]))

(require
  '[dynadoc.example]
  '[dynadoc.boot :refer [dynadoc]]
  '[nightlight.boot :refer [nightlight]]
  '[boot-tools-deps.core :refer [deps]])

(task-options!
  pom {:project 'defexample
       :version "1.7.0-SNAPSHOT"
       :description "A macro for defining code examples"
       :url "https://github.com/oakes/defexample"
       :license {"Public Domain" "http://unlicense.org/UNLICENSE"}}
  push {:repo "clojars"})

(deftask run []
  (comp
    (deps)
    (wait)
    (nightlight :port 4000)))

(deftask run-docs []
  (comp
    (deps)
    (wait)
    (dynadoc :port 5000)))

(deftask local []
  (comp (deps) (pom) (jar) (install)))

(deftask deploy []
  (comp (deps) (pom) (jar) (push)))

