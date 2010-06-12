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
              [document :as document] [instance :as instance]
              [classifier :as classifier]] :reload)
  (:use [keypjrase.util] :reload)
  (:gen-class)
  )

(defmacro bootstrap []
  '(do
    (use (quote [clojure.contrib.seq-utils :only [find-first]]))
    (ns keypjrase.playground) ; tmp?
  ))

(comment 

   (use 'keypjrase.playground) (bootstrap)

   (time (-train "data/bookmarks/nates/train.txt" "tmp/foo"))
  
   (time (-train "data/bookmarks/nates/train-10.txt" "tmp/foo"))

   (.printStackTrace *e)

)
