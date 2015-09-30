(ns daifu.core
  (:require [daifu.core.api :as api]
            [clojure.tools.cli :as cli]))

(def cli-options
  [["-p" "--path PATH" "Path to the repository "
    :default (System/getProperty "user.dir")]

   ["-c" "--checkups-file PATH" "Path to the checkups file that will be run"]
   ["-f" "--format FORMAT" "Format of output {edn|json}"
    :default :edn
    :parse-fn keyword
    :validate [#(#{:edn :json} %) "Must be a either edn or json"]]
   ["-i" "--indicator-paths DIR" "Directory to load indicators"
    :assoc-fn (fn [m k v] (update-in m [k] (fnil #(conj % v) [])))]
   ["-j" "--jurisdiction-paths DIR" "Directory to load jurisdictions"
    :assoc-fn (fn [m k v] (update-in m [k] (fnil #(conj % v) [])))]
   ["-o" "--output FILE" "Path for output file"]
   [nil  "--info" "Show this screen"]
   [nil  "--use-git" "Load files from git"]])

(defn cli-main [args]
  (let [summary (cli/parse-opts args cli-options)
        summary (if (:errors summary)
                  (do (println "Errors on input:")
                      (doseq [error (:errors summary)]
                        (println error))
                      (assoc-in summary [:options :info] true))
                  summary)]
    (cond (or (empty? args)
              (-> summary :options :info))
          (do (println "\nUsage: lein daifu -i qa/indicators -j qa/jurisdictions -f json -o output.json -c qa/checkup.daifu")
              (println "\nOptions")
              (println (:summary summary)))

          :else
          (api/run (:options summary)))))

(defn -main [& args]
  (cli-main args))
