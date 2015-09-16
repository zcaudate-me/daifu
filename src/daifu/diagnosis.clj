(ns daifu.diagnosis
  (:require [daifu.diagnosis.indicator :as indicator]
            [daifu.diagnosis.jurisdiction :as jurisdiction]
            [clojure.java.io :as io]
            [rewrite-clj.zip :as zip]
            [jai.query :as query]))

(defn retrieve [repo jurisdiction]
  (let [paths   (->> (jurisdiction/pick-files repo jurisdiction)
                     (map (partial hash-map :path)))]
    (->> paths
         (map (partial jurisdiction/retrieve-file repo))
         (map #(assoc %1 :reader %2) paths)
         (filter :reader))))

(defn calculate-stat [results]
  (reduce (fn [acc result]
            (cond (map? result)
                  (let [{:keys [stat result]} result]
                    (cond stat
                          (+ stat acc)

                          result
                          (cond (number? result)
                                (+ result acc)

                                (boolean result)
                                (inc acc))

                          :else
                          (inc acc)))

                  (number? result)
                  (+ acc result)

                  :else acc))
          0 results))

(defn format-results [results infos]
  (map (fn [result info]
         (let [info (dissoc info :reader)]
           (cond (map? result)
                 (let [{:keys [results] :as m} (merge result info)]
                   (if results
                     (assoc m :stat (calculate-stat results))
                     m))

                 (sequential? result)
                 (assoc info :results (vec result)
                        :stat (calculate-stat result))
                 
                 :else
                 (assoc info :result result))))
       results infos))

(defn diagnose-form-ns [zloc]
  (second (zip/sexpr (first (query/select zloc '[ns] {:walk :top})))))

(defn diagnose-form-metas [zloc]
  (assoc (meta (zip/node zloc))
         :name (-> zloc zip/sexpr second)))

(defn diagnose-form-single [indicator {:keys [reader path]}]
  (let [zloc    (zip/of-string (slurp reader))
        ns      (diagnose-form-ns zloc)
        funcs   (query/select zloc [(or (:pattern indicator) '(#{defn defn-} & _))] {:walk :top})
        metas   (map diagnose-form-metas funcs)
        results (map indicator funcs)]
    [ns (format-results results metas)]))

(defn diagnose-form [repo indicator jurisdiction]
  (let [files   (retrieve repo jurisdiction)
        results (map (partial diagnose-form-single indicator) files)
        nss     (map first results)
        results (map second results)]
    (format-results results (map #(assoc %1 :ns %2) files nss))))

(defn diagnose-file [repo indicator jurisdiction]
  (let [files   (retrieve repo jurisdiction)
        results (map #(-> % :reader indicator) files)]
    (format-results results files)))

(defn diagnose [repo indicator jurisdiction]
  (let [results (case (:type indicator)
                  :project  (indicator repo (assoc (jurisdiction/read-project repo jurisdiction) :jurisdisction jurisdiction))
                  :file     (diagnose-file repo indicator jurisdiction)
                  :function (diagnose-form repo indicator jurisdiction)
                  :form     (diagnose-form repo indicator jurisdiction)
                  :idiom  (throw (Exception. "Not Supported")))
        stat    (calculate-stat results)]
    (merge {:indicator     (:id indicator)
            :jurisdisction (:id jurisdiction)
            :stat    stat
            :results (vec results)}
           (select-keys jurisdiction [:commit :branch]))))

(defn project-zloc [repo project]
  (let [opts (select-keys (:jurisdisction project) [:branch :commit])]
    (-> (jurisdiction/retrieve-file repo (assoc opts :path "project.clj"))
        (slurp)
        (zip/of-string))))


(comment
  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/form/record_count.indi")))
   {:id :default-project
    :type :project})

  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/file/line_count.indi")))
   {:id :default-project
    :type :project})

  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/function/no_docstring.indi")))
   {:id :default-project
    :type :project})

  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/function/token_count.indi")))
   {:id :default-project
    :type :project})
  
  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/project/project_meta.indi")))
   {:id :default-project
    :type :project}))


  
  
