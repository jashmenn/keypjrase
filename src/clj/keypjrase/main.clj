(ns keypjrase.main
  (:require clojure.contrib.str-utils)
  (:require [keypjrase [parser :as parser] 
              [document :as document] [instance :as instance]
              [classifier :as classifier]] :reload)
  (:use [keypjrase.util] :reload)
  (:use clojure.contrib.command-line)
  (:gen-class))

(defn -train [input-data output-dir & options]
  (instance/training*
  (let [opts (merge 
               {:parser "tagdoc"}
               (apply hash-map options))
        documents (parser/parse-input (opts :parser) input-data)
        stats (document/calculate-collection-stats documents)   
        instances (instance/create-instances-w-docs documents stats)
        classifier (classifier/build instances)]
    (do
      (classifier/save classifier (str output-dir "/classifier"))
      (save-data-to (str output-dir "/stats.clj") stats)
      (println (str "saved to " output-dir))
    ))))

(defn -main [& args]
  (with-command-line args
    "keypjrase: key-phrase extraction"
    [remaining]
    (let [[mode input-dir output-dir] remaining]
      (apply-str (str "-" mode) input-dir output-dir))))
