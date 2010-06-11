(ns keypjrase.instance
  (:require [clojure.contrib [str-utils :as s]]) 
  (:use [clojure.contrib.seq-utils :only [find-first indexed frequencies]])
  (:require [keypjrase [document :as d]] :reload))

(defstruct instance :token :class :features)
(defstruct features :distance :tfidf :pos)

(defn create-instances-w-docs
  "given documents"
  [documents stats]
  stats
  )


(defn tfidf
  [document local-counts stats training?]
  0)

; (defn calculate-document-instances 
;   [document stats training?]
;   (let [document-counts (d/body-frequencies document)]
;   (map 
;     (document :body))
;     )) 

  ; tfidf (tfidf document document-counts stats training?)
  ; document-counts))

(defn calculate-phrase-features
  [phrase document local-counts stats training?] 
  )

(comment

  (create-instances-w-docs d/test-documents d/test-stats)

  (calculate-features d/test-document d/test-stats true)

  )
