(defn read-deps-edn [aliases-to-include]
  (let [{:keys [paths deps aliases]} (-> "deps.edn" slurp clojure.edn/read-string)
        deps (->> (select-keys aliases aliases-to-include)
                  vals
                  (mapcat :extra-deps)
                  (into deps)
                  (reduce
                    (fn [deps [artifact info]]
                      (if-let [version (:mvn/version info)]
                        (conj deps
                          (transduce cat conj [artifact version]
                            (select-keys info [:scope :exclusions])))
                        deps))
                    []))]
    {:dependencies deps
     :source-paths (set paths)
     :resource-paths (set paths)}))

(let [{:keys [source-paths resource-paths dependencies]} (read-deps-edn [])]
  (set-env!
    :source-paths source-paths
    :resource-paths resource-paths
    :dependencies (into '[[nightlight "RELEASE" :scope "test"]
                          [dynadoc "RELEASE" :scope "test"]]
                        dependencies)
    :repositories (conj (get-env :repositories)
                    ["clojars" {:url "https://clojars.org/repo/"
                                :username (System/getenv "CLOJARS_USER")
                                :password (System/getenv "CLOJARS_PASS")}])))

(require
  '[dynadoc.example]
  '[dynadoc.boot :refer [dynadoc]]
  '[nightlight.boot :refer [nightlight]])

(task-options!
  pom {:project 'defexample
       :version "1.7.1-SNAPSHOT"
       :description "A macro for defining code examples"
       :url "https://github.com/oakes/defexample"
       :license {"Public Domain" "http://unlicense.org/UNLICENSE"}}
  push {:repo "clojars"})

(deftask run []
  (comp
    (wait)
    (nightlight :port 4000)
    (dynadoc :port 5000)))

(deftask local []
  (comp (pom) (jar) (install)))

(deftask deploy []
  (comp (pom) (jar) (push)))

