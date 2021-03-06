(ns keypjrase.parser
  (:require [clojure.contrib [str-utils :as s]]) 
  (:use [keypjrase.document] :reload)
  (:require [keypjrase [document :as d]] :reload)
  (:import [java.io BufferedReader FileReader]) 
  )

(def token-regex #"\w+")

(def stop-words
  (set (s/re-split #"\n" (slurp "doc/stopwords.txt"))))

(defn to-lower-case [token-string]
  (.toLowerCase token-string))

(defn tokenize-str [string] ; todo put in a stemmer here
  (filter (complement stop-words) 
          (map to-lower-case 
               (re-seq token-regex string))))

(defn extract-document-from-line [acc line]
 (let [parts (s/re-split #"\t" line)
        tags (set (d/stem-all (tokenize-str (first parts)))) ; stemming? argh!
        body (tokenize-str (last parts))]
     (if (> (count parts) 1)  
         (cons (struct-map document :tags tags :body body) acc)
         acc)))

(defn process-file  
  ([file-name line-func]
    (process-file file-name line-func '()))
  ([file-name line-func line-acc]
  (with-open [rdr (BufferedReader. (FileReader. file-name))]
    (reduce line-func line-acc (line-seq rdr)))))

(defn parse-tagdoc 
  "parse a file of the tagdoc format: tags space separated [TAB] document
  returns a seq of documents"
  [input]
  (process-file input extract-document-from-line))

(defn parse-input [parser input] ; todo. actually use the parser variable
  (parse-tagdoc input))

(defn extract-document-from-extraction-line [acc line]
 (let [parts (s/re-split #"\t" line)
       url (first parts)
       body (tokenize-str (last parts))]
     (if (> (count parts) 1)  
         (cons (struct-map document :url url :body body) acc)
         acc)))

(defn parse-for-extraction 
  [input]
  (process-file input extract-document-from-extraction-line))


(comment

  (parse-tagdoc "test/data/little-docs.txt")

  )
