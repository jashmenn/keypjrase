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
               keypjrase.classifier
               keypjrase.document
               keypjrase.instance
               keypjrase.test
               keypjrase.main ]
  :run-aliases {:train [keypjrase.main -main 
                          "train"
                          "data/bookmarks/nates/train.txt" 
                          "tmp/runs-all"]
                :test-extraction [keypjrase.main -main 
                          "test"
                          "data/bookmarks/nates/test.txt" 
                          "tmp/runs"
                          "tmp/runs-out"
                          "20"]}
                         
  :main keypjrase.playground) 