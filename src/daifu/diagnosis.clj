(ns daifu.diagnosis
  (:require [daifu.diagnosis.indicator :as indicator]
            [daifu.diagnosis.jurisdiction :as jurisdiction]
            [clojure.java.io :as io]
            [rewrite-clj.zip :as zip]
            [jai.query :as query]))

(defn retrieve [repo jurisdiction]
  (let [paths   (->> (jurisdiction/pick-files repo jurisdiction)
                     (mapv (partial hash-map :path)))]
    (->> paths
         (mapv (partial jurisdiction/retrieve-file repo))
         (mapv #(assoc %1 :reader %2) paths)
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
  (mapv (fn [result info]
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
  (try (let [zloc    (zip/of-string (slurp reader))
             ns      (diagnose-form-ns zloc)
             funcs   (query/select zloc [(or (:pattern indicator) '(#{defn defn-} & _))] {:walk :top})
             metas   (mapv diagnose-form-metas funcs)
             results (mapv indicator funcs)]
         [ns (format-results results metas)])
       (catch Throwable t
         (println "Exception occured using" indicator "on file" path))))

(defn diagnose-form [repo indicator jurisdiction]
  (let [files   (retrieve repo jurisdiction)
        results (keep (partial diagnose-form-single indicator) files)
        nss     (mapv first results)
        results (mapv second results)]
    (format-results results (mapv #(assoc %1 :ns %2) files nss))))

(defn diagnose-file [repo indicator jurisdiction]
  (let [files   (retrieve repo jurisdiction)
        safe-fn (fn [f]
                  (try (indicator (:reader f))
                       (catch Throwable t
                         (println "Exception occured using" indicator "on file" (:path f)))))
        results (->> (mapv safe-fn files)
                     (filter identity))]
    (format-results results files)))

(defn diagnose [repo indicator jurisdiction]
  (let [output (case (:type indicator)
                  :project  (indicator repo (assoc (jurisdiction/read-project repo jurisdiction) :jurisdisction jurisdiction))
                  :file     (diagnose-file repo indicator jurisdiction)
                  :function (diagnose-form repo indicator jurisdiction)
                  :form     (diagnose-form repo indicator jurisdiction)
                  :idiom    (diagnose-file repo indicator jurisdiction))
        [results info] (cond (sequential? output)
                             [output nil]
                             (map? output)
                             [(:results output) (dissoc output :results)])
        stat    (calculate-stat results)]
    (merge info
           {:indicator     (:id indicator)
            :jurisdisction (:id jurisdiction)
            :stat    stat
            :results (vec results)}
           (select-keys jurisdiction [:current :previous :comparison]))))

(defn project-zloc [repo project]
  (let [opts (select-keys (:jurisdisction project) [:branch :commit])]
    (-> (jurisdiction/retrieve-file repo (assoc opts :path "project.clj"))
        (slurp)
        (zip/of-string))))
