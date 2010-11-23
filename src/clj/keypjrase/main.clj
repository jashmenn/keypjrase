
(ns keypjrase.main
  (:require clojure.contrib.str-utils)
  (:require [clojure.contrib [map-utils :as m]])
  (:require [keypjrase [parser :as parser] 
              [document :as document] [instance :as instance]
              [extract :as extract] [classifier :as classifier]] :reload)
  (:use [keypjrase.util] :reload)
  (:use [keypjrase.test] :reload)
  (:use clojure.contrib.command-line)
  (:gen-class))

(defn -train [input-data output-dir & options]
  (instance/with-training*
  (let [opts (merge 
               {:parser "tagdoc"}
               (apply hash-map options))
        documents  (do 
                     (prn "parsing docs") 
                     (parser/parse-input (opts :parser) input-data))
        stats      (do 
                     (prn "calculate stats") 
                     (document/calculate-collection-stats documents))
        instances  (do 
                     (prn "create instances")
                     (instance/create-instances-w-docs documents stats))
        classifier (do 
                     (prn "building classifier") 
                     (classifier/build instances))]

    (do
      (prn "saving classifier")
      (classifier/save classifier (str output-dir "/classifier"))
      (save-data-to (str output-dir "/stats.clj") stats)
      (println (str "saved to " output-dir))
    ))))

(defn -main [& args]
  (with-command-line args
    "keypjrase: key-phrase extraction"
    [remaining]
    (let [mode (first remaining)]
    (cond (= mode "train") ; todo macrofy
          (let [[mode input-dir output-dir] remaining]
            (do
              (prn mode)
            (time (-train input-dir output-dir))))
          (= mode "test") 
          (let [[mode input-data training-dir output-dir at] remaining]
            (do
              (prn mode)
            (time (-test input-data training-dir output-dir :at (Integer/parseInt at)))))
          (= mode "extract") 
          (let [[mode training-dir output-dir at input-data] remaining]
            (do
              (prn mode)
            (time (extract/-extract training-dir output-dir (Integer/parseInt at) input-data))))

      ))))
