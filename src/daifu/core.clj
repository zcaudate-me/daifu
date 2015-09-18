(ns daifu.core
  (:require [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [gita.core :as git]
            [cheshire.core :as json]
            [clojure.pprint :as pprint]
            [daifu.diagnosis :as diagnosis]
            [daifu.diagnosis.indicator :as indicator]
            [daifu.diagnosis.jurisdiction :as jurisdiction]))

(def cli-options
  [["-p" "--path PATH" "Path to the repository "
    :default (System/getProperty "user.dir")]
   ["-c" "--checkups-file PATH" "Path to the checkups file that will be run"]
   ["-f" "--format FORMAT" "Format of output {edn|json}"
    :default :edn
    :parse-fn keyword
    :validate [#(#{:edn :json} %) "Must be a either edn or json"]]
   ["-g"  "--use-git" "Load files from git"
    :default false]
   ["-h"  "--help" "Show help screen"]
   ["-i" "--indicator-paths DIR" "Directory to load indicators"
    :assoc-fn (fn [m k v] (update-in m [k] (fnil #(conj % v) [])))]
   ["-j" "--jurisdiction-paths DIR" "Directory to load jurisdictions"
    :assoc-fn (fn [m k v] (update-in m [k] (fnil #(conj % v) [])))]
   ["-o" "--output FILE" "Path for output file"]
   [nil  "--no-defaults" "Do not load default indicators"
    :default false]])

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
                                        :type :project
                                        ;;:comparison true
                                        ;;:current {:commit "HEAD"}
                                        ;;:previous {:commit "HEAD^1"}
                                        })})

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

(defn git-repo? [path]
  (.exists (io/file path ".git")))

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
                              vec))
        opts (if (empty? (:checkups opts))
               (assoc opts :checkups (vec (map vector (sort (keys (:indicators opts))))))
               opts)
        opts (assoc-in opts [:repository]
                       (if (and (git-repo? (:path opts)) (:use-git opts))
                         (git/repository (:path opts))
                         (io/file (:path opts))))]
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
  (let [results (vec (keep (partial diagnosis-single visitation) checkups))
        writer  (if (:output visitation)
                  (io/writer (:output visitation))
                  *out*)]
    (case (:format visitation)
      :json (if (= writer *out*)
              (.write writer (json/generate-string results {:pretty true}))
              (spit (:output visitation) (json/generate-string results {:pretty true})))
      :edn  (pprint/pprint results writer))))

(defn -main [& args]
  (let [summary (cli/parse-opts args cli-options)
        summary (if (:errors summary)
                  (do (println "Errors on input:")
                      (doseq [error (:errors summary)]
                        (println error))
                      (assoc summary :help true))
                  summary)]
    (cond (-> summary :options :help)
          (println (:summary summary))

          :else
          (let [opts    (:options summary)
                visit (visitation (dissoc opts :diagnosis))]
            (diagnosis visit (:checkups visit))))))
