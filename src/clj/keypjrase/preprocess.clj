
(ns keypjrase.preprocess
  (:require 
   [clojure.contrib.str-utils2 :as su]
   [clojure.contrib.logging :as log]
   [clojure.contrib.duck-streams :as ds])
  (:use [keypjrase.util] :reload)
  (:import 
   [java.io File BufferedInputStream FileInputStream]
   [net.htmlparser.jericho Source TextExtractor]))

(defn extract-text 
  "given File returns a String of the extracted text"
  [f]
  (let [source (Source. (BufferedInputStream. (FileInputStream. f)))]
    (.toString (TextExtractor. source))))

(defn simplify 
  "simplifies the text to just lowercase letters"
  [text]
  (-> text
   (su/replace #"[^a-zA-Z0-9\s]" " ") ;; remove all non- letter/number space
   (su/replace #"\b\d+\b" " ") ;; remove numeric tokens
   (su/replace #"(?m:\s+)" " ") ;; remove extra whitespace
   (su/lower-case)))

(defn extract-and-simplify [f]
  (-> f
   (extract-text)
   (simplify)))

;; take a directory, go through all the files and process the files
(defn process-html-dir [dir process-fn]
   (reduce (fn [acc f] (assoc acc (.toString f) (process-fn f))) 
           (hash-map)
           (filter #(re-find #".html$" (.toString %)) 
                    (file-seq (File. dir)))))

(defn process-and-save-to [dir to process-fn]
  (let [processed (process-html-dir dir process-fn)
        lines (map (fn [[k v]] (str k "\t" v)) processed)]
    (ds/spit to (apply str (interpose "\n" lines)))))

(defn simplify-and-save-to [dir to]
  (process-and-save-to dir to extract-and-simplify))

(comment 

  (simplify "ASkn ... 23sda | 
 adsfjkjn #$#$ sd 123")

  (def filename "test-data/toy-pages/secret/dont-crawl.html")
  (def file (java.io.File. filename))
  (def tex (TextExtractor. source))
  (.toString tex)
  (process-file file)

  (def dirname "test-data/toy-pages")
  (process-html-dir dirname extract-and-simplify)
  (simplify-and-save-to dirname "cosmetique.txt")

  )


