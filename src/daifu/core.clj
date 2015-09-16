(ns daifu.core
  (:require [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [daifu.diagnosis :as diagnosis]
            [daifu.diagnosis.indicator :as indicator]
            [daifu.diagnosis.jurisdiction :as jurisdiction]))

(def cli-options
  [["-c" "--checkups-file PATH" "Path to the checkups file that will be run"]
   ["-f" "--format FORMAT" "Format of output {edn|json|html}"
    :default :edn
    :parse-fn keyword
    :validate [#(#{"edn" "json" "html"} %) "Must be a either edn, json or html"]]
   ["-i" "--indicator-paths DIR" "Directory to load indicators"
    :assoc-fn (fn [m k v] (update-in m [k] (fnil #(conj % v) [])))]
   ["-j" "--jurisdiction-paths DIR" "Directory to load jurisdictions"
    :assoc-fn (fn [m k v] (update-in m [k] (fnil #(conj % v) [])))]
   ["-o" "--output FILE" "Path for output file"
    :default "daifu.out"]
   [nil  "--no-defaults" "Do not load default indicators"]])

(def ^:dynamic *default-indicators*
  ["file/line_count.indi"
   "form/record_count.indi"
   "function/no_docstring.indi"
   "function/token_count.indi"
   "idiom/arithmatic.indi"
   "idiom/collection.indi"
   "idiom/control.indi"
   "idiom/equality.indi"
   "idiom/sequence.indi"
   "idiom/string.indi"
   "project/project_meta.indi"])

(defn load-default-indicators []
  (->> *default-indicators*
       (map (fn [path]
              (-> (io/resource (str "daifu/defaults/indicators/" path))
                  slurp
                  read-string
                  indicator/indicator)))
       (map (juxt :id identity))
       (into {})))

(def default-jurisdictions
  {:default (jurisdiction/jurisdiction {:id :default
                                        :type :project})})

(defrecord Visitation [])

(defmethod print-method Visitation
  [v w]
  (.write w (str "#visitation "
                 (into {} (-> v
                              (update-in [:indicators] (comp vec sort keys))
                              (update-in [:jurisdictions] (comp vec sort keys))
                              (dissoc :indicator-paths :jurisdiction-paths))))))

(defn load-maps [dir suffix constructor]
  (->> (file-seq (io/file dir))
       (filter (fn [f] (.endsWith (str f) suffix)))
       (map (fn [f] (-> (slurp f)
                        (read-string)
                        constructor)))
       (map (juxt :id identity))
       (into {})))

(defn load-indicators [dir]
  (load-maps dir ".indi" indicator/indicator))

(defn load-jurisdictions [dir]
  (load-maps dir ".juri" jurisdiction/jurisdiction))

(defn load-checkups [path]
  (if path
    (-> (slurp path) read-string)))

(defn visitation [opts]
  (let [opts (if-not (:no-defaults opts)
               (update-in opts [:indicators] merge (load-default-indicators))
               opts)
        opts (update-in opts [:jurisdictions] merge default-jurisdictions)
        opts (update-in opts [:indicators]
                        merge (apply merge (map load-indicators (:indicator-paths opts))))
        opts (update-in opts [:jurisdictions]
                        merge (apply merge (map load-jurisdictions (:indicator-paths opts))))
        opts (update-in opts [:checkups]
                        #(->> (load-checkups (:checkups-file opts))
                              (concat %)
                              vec))]
    (map->Visitation opts)))

(defn diagnosis-single [visitation [ik jk]]
  (let [jk   (or jk :default)
        indi (-> visitation
                 :indicators
                 (get ik))
        juri (-> visitation
                 :jurisdictions
                 (get jk))]
    (if (and indi juri)
      (diagnosis/diagnose (:repository visitation) indi juri))))

(defn diagnosis [visitation checkups]
  (vec (keep (partial diagnosis-single visitation) checkups)))

(defn -main [& args]
  (let [opts (cli/parse-opts args cli-options)
        visit (visitation (dissoc opts :diagnosis))]
    (diagnosis visit (:checkups visit))))

(comment
  (:options (cli/parse-opts ["-i" "qa/indicators" "-i" "qa/indicators2"
                             "-f" "oeuoeu"] cli-options))
  
  
  (load-default-indicators)

  (visitation {;;:indicators (load-default-indicators)
               ;;:jurisdictions default-jurisdiction
               :no-defaults true
               :indicator-paths ["resources/daifu"]
               :jurisdiction-paths []
               :format :edn}))
