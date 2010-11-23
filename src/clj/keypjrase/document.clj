
(ns keypjrase.document
  (:use [keypjrase.stemmer] :reload)
  (:use [clojure.contrib.seq-utils :only [find-first indexed frequencies]])
  (:require clojure.contrib.str-utils))

(defstruct document :url :body :tags)
(defstruct collection-stats :df :num-documents)

(defn count-each-word-once [coll]
 (reduce #(merge-with + %1 {(stem %2) 1}) {} coll)) ; ew, stemming twice?

(defn df 
  "given documents returns a map where the key is the token and the value is
  the document freq."
  [documents] 
  (reduce
    (fn [acc document] 
      (reduce 
       (fn [acc token]
         (let [stemmed-token (stem token)]
         (assoc acc stemmed-token (+ (get acc stemmed-token 0) 1))))
       acc (keys (count-each-word-once (document :body)))))
    {} documents))

(defn calculate-collection-stats 
  "given a list of documents, returns a collection-stats struct"
  [documents]
  (let [docfreq (df documents)
        num-docs (count documents)]
  (struct-map collection-stats :df docfreq :num-documents num-docs)))

(defn stem-all [all]
  (reverse (reduce (fn [acc token] (cons (stem token) acc)) '() all)))

(defn body-frequencies [document] 
  (frequencies (stem-all (document :body))))

(defn potential-phrases [document]
  (distinct (document :body))) ; for now

(defn global-phrase-count [phrase stats]
  (let [counts (stats :df)]
    (counts phrase)))

(defn stem-global-phrase-count [unstemmed-phrase stats]
  (global-phrase-count (stem unstemmed-phrase) stats))

(defn document-length [document]
  (count (document :body)))

(defn first-occurrences
  "given document returns a map with the key being the word and
  the value is the the index of the first occurrence" 
  [document]
  (let [tokens (document :body)]
  (into {} (map 
             (fn [v] [(v 1) (/ (+ (v 0) 1) (count tokens))])
             (clojure.contrib.seq-utils/indexed (distinct (stem-all tokens)))))))

(defn is-tag? [tag document]
  (contains? (document :tags) tag))

(comment test-objects)

(def test-documents
  [(struct-map document :body '("free" "money" "here" "dog")
                        :tags #{"free" "money"})
   (struct-map document :body '("horse" "likes" "eat" "carrots")
                        :tags #{"horse" "carrot"})
   (struct-map document :body '("dog" "fleas" "still" "dog" "even" 
                               "though" "go" "flea" "market" "ya"), 
                        :tags #{"dog" "flea"})])

(def test-document (last test-documents))
(def test-frequencies (body-frequencies test-document))
(def test-first-occur (first-occurrences test-document))

(def test-stats
  (struct-map collection-stats :df {"flea" 1, "carrot" 1, "go" 1, "still" 1,
    "ya" 1, "hor" 1, "free" 1, "monei" 1, "here" 1, "like" 1, "eat" 1, "even" 1,
    "though" 1, "market" 1, "dog" 2}
    :num-documents 3))

(comment

  (calculate-collection-stats test-documents)
  
  (df test-documents)

  )
