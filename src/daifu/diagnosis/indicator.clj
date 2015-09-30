(ns daifu.diagnosis.indicator
  (:require [daifu.diagnosis.indicator.activate :as activate]
            [daifu.diagnosis.indicator.invoke :as invoke]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [hara.data.nested :as nested]))

(def ^:dynamic *default-indicators*
  ["file/line_count.indi"
   "form/record_count.indi"
   "function/no_docstring.indi"
   "function/token_count.indi"
   "function/too_many_tokens.indi"
   "function/char_per_line.indi"
   "function/line_over_char_limit.indi"
   "idiom/arithmatic.indi"
   "idiom/collection.indi"
   "idiom/control.indi"
   "idiom/equality.indi"
   "idiom/sequence.indi"
   "idiom/string.indi"
   "project/project_meta.indi"])

(defrecord Indicator [])

(defmethod print-method Indicator
  [v w]
  (.write w (str "#" (name (:type v)) [(name (:id v)) (activate/activation v)] " "
                 {:* (vec (keys (dissoc v :id :type :main)))})))

(def +default-indicator+
  {:level :info
   :accumulate {:default :auto}})

(defn indicator
  "when passed a map, returns an indicator
   (-> (indicator {:id :hello-world
                   :type :function
                   :source '(fn [zloc] (zip/sexpr zloc))})
       (indicator?))
   => true"
  {:added "0.1"}
  [m]
  (-> (map->Indicator (nested/merge-nested +default-indicator+ m))
      (assoc :main (atom nil))))

(defn indicator?
  "checks if object is in fact in indicator
   (indicator? {:id :hello-world
                :type :function
                :source '(fn [zloc] (zip/sexpr zloc))})
   => false"
  {:added "0.1"}
  [x]
  (instance? Indicator x))

(defn load-files [files]
  (->> files (map (fn [f] (-> (slurp f)
                              (read-string)
                              indicator)))
       (map (juxt :id identity))
       (into {})))

(defn load-defaults []
  (->> *default-indicators*
       (map (fn [path]
              (io/resource (str "daifu/defaults/indicators/" path))))
       (load-files)))

(defn load-path [path]
  (->> (io/file path)
       (file-seq)
       (filter (fn [f] (.endsWith (str f) ".indi")))
       (load-files)))

(defn activate-all [indicators]
    (let [indicators (reduce-kv (fn [out k v]
                                  (assoc out k (activate/activate v)))
                                {}
                                indicators)]
      (reduce-kv (fn [out k v]
                   (let [inputs (:inputs v)]
                     (if (and (= :activated (activate/activation v))
                              (every? #(-> indicators
                                           (get %)
                                           (activate/activation)
                                           (= :activated)) inputs))
                       (assoc out k v)
                       out)))
                 {}
                 indicators)))

(comment
  
  
  
  (activate-all (load-defaults)))
