; Usage:
;
; REPL: 
;   $ env JAVA_OPTS=-Xmx768m LEIN_CLASSPATH=src/clj ~/lib/lein repl
;   # OR
;   $ lein uberjar && hadoop jar bonobo-geo-standalone.jar clojure.lang.Repl
;   # then 
;   (use 'keypjrase.playground) (bootstrap)
;

(ns keypjrase.playground
  (:require clojure.contrib.str-utils)
  (:require [keypjrase [parser :as parser] 
                       [document :as document]] :reload)
  (:use [keypjrase.util] :reload)
  (:gen-class)
  )

(defmacro bootstrap []
  '(do
    (use (quote [clojure.contrib.seq-utils :only [find-first]]))
    (ns keypjrase.playground) ; tmp?
  ))

(defn -train [input-data output-dir & options]
  (let [opts (merge 
               {:parser "tagdoc"}
               (apply hash-map options))
        documents (parser/parse-input (opts :parser) input-data)
        stats (document/calculate-collection-stats documents)   
        ]
    stats))

(comment 

   (use 'keypjrase.playground) (bootstrap)

   (-train "data/bookmarks/nates/train.txt" "data/models")
  
   (.printStackTrace *e)

)
