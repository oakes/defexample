(defproject defexample "1.7.1-SNAPSHOT"
  :description "A macro for defining code examples"
  :url "https://github.com/oakes/defexample"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :sign-releases false}]]
  :profiles {:dev {:main dynadoc.example}})
