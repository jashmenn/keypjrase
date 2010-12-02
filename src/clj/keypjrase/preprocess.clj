;; lein run keypjrase.preprocess simplify-and-save-to test-data/toy-pages/ foo.txt
(ns keypjrase.preprocess
  (:require 
   [clojure.contrib.str-utils2 :as su]
   [clojure.contrib.logging :as log]
   [clojure.contrib.duck-streams :as ds])
  (:use [keypjrase.util] :reload)
  (:import 
   [java.io File BufferedInputStream FileInputStream FileWriter BufferedWriter]
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
 
(defn process-html-dir [dir process-fn]
  (map (fn [f] (do
         (println (str "processing " f))
         (str (.toString f) "\t "(process-fn f) "\n")))
       (filter #(re-find #".html$" (.toString %)) 
               (file-seq (File. dir)))))

(defn write-lines [file-name lines]
  (with-open [wtr (BufferedWriter. (FileWriter. file-name))]
    (doseq [line lines] (.write wtr line))))

(defn process-and-save-to [dir to process-fn]
  (write-lines to (process-html-dir dir process-fn)))

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


