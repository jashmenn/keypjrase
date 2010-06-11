(defproject keypjrase "1.0.0-SNAPSHOT"
  :description "automatic keyphrase extraction"
  :source-path "src/clj"
  :java-source-path "src/jvm"
  :java-fork "true"
  :javac-debug "true"
  :repositories [["clojars" "http://clojars.org/repo"]]
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [clj-ml "0.0.3-SNAPSHOT"]
                 [org.clojars.thnetos/opennlp "0.0.5"]
                 [lein-run "1.0.0-SNAPSHOT"]]
  :dev-dependencies [[lein-javac "0.0.2-SNAPSHOT"]
                     [org.apache.hadoop/hadoop-core "0.20.2-dev"]
                     [org.clojars.brandonw/lein-nailgun "1.0.0"]]
  :namespaces [keypjrase.playground
               keypjrase.util
               keypjrase.parser
               keypjrase.stemmer
               ]
  :run-aliases {:train [keypjrase.playground -main 
                          "data/bookmarks/nates/train.txt" 
                          "data/cutpoints"]
                :extract [keypjrase.playground -main 
                          "data/bookmarks/nates/test.txt" 
                          "data/cutpoints" 
                          "data/extracted"]}
  :main keypjrase.playground) 