(set-env!
  :source-paths #{"src"}
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                  [nightlight "1.9.3" :scope "test"]]
  :repositories (conj (get-env :repositories)
                  ["clojars" {:url "https://clojars.org/repo/"
                              :username (System/getenv "CLOJARS_USER")
                              :password (System/getenv "CLOJARS_PASS")}]))
(require
  '[nightlight.boot :refer [nightlight]])

(task-options!
  pom {:project 'defexample
       :version "1.3.0"
       :description "A macro for defining code examples"
       :url "https://github.com/oakes/defexample"
       :license {"Public Domain" "http://unlicense.org/UNLICENSE"}}
  push {:repo "clojars"})

(deftask run []
  (comp
    (wait)
    (nightlight :port 4000)))

(deftask local []
  (comp (pom) (jar) (install)))

(deftask deploy []
  (comp (pom) (jar) (push)))

