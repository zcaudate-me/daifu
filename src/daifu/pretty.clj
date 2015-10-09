(ns daifu.pretty
  (:require [clojure.string :as string]))

(defn arrange-results [results]
  (reduce (fn [output {:keys [indicator results]}]
            (reduce (fnil (fn [output {:keys [path results] :as entry}]
                            (update-in output [path :results]
                                       (fn [entry]
                                         (-> entry
                                             (concat (map #(assoc % :indicator indicator) results))
                                             (vec)))))
                          {:results []})
                    output
                    results))
          {}
          results))

(defn order-results [results]
  (->> results
       (reduce-kv (fn [output path {:keys [results] :as entry}]
                    (-> output
                        (conj (-> entry
                                  (assoc :path path :stat (count results))
                                  (update-in [:results] #(vec (sort-by :row %)))))))
                  [])
       (sort-by :path)))


(defn output-results [results]
  (println (string/join "\n" ["==========================================="
                              "ALL RESULTS"
                              ""]))
  (doseq [{:keys [path results]} results]
    (doseq [{:keys [row col expr alt indicator]} results]
      (println (string/join "\n" ["==========================================="
                                  ""
                                  (str "FILE " path " " indicator)
                                  (str "ROW " row ", COL " col)
                                  ""
                                  "==========================================="
                                  (string/trim-newline expr)
                                  "-------------------------------------------"
                                  (string/trim-newline alt)
                                  ""]))))
  (println "==========================================="))

(defn display [results]
  (-> results
      arrange-results
      order-results
      output-results))

(comment
  (display *input)
  
  (-> *input
      arrange-results
      order-results)
  ({:results [{:expr "(- 2 1)", :alt "1", :row 10, :col 30, :indicator :arithmatic} {:expr "(when (not keep-views?) folder-views)\n", :alt "(when-not keep-views? folder-views)\n", :row 91, :col 29, :indicator :control}], :path "src/moby/core/models/view_folder.clj", :stat 2} {:results [{:expr "(if\n title\n title\n (let\n  [inc-tags\n   (if (empty? inc-tags) \"<EMPTY>\" (cs/join \" \" inc-tags))\n   exc-tags\n   (if (empty? exc-tags) \"<EMPTY>\" (cs/join \" \" exc-tags))]\n  (format\n   \"Waited days = %s, Platform = %s , App = %s, lang = %s, inc-tags = %s, exc-tags = %s, reply = %s\"\n   waited-days\n   platform-type\n   app-id\n   lang\n   inc-tags\n   exc-tags\n   message)))\n", :alt "(or\n title\n (let\n  [inc-tags\n   (if (empty? inc-tags) \"<EMPTY>\" (cs/join \" \" inc-tags))\n   exc-tags\n   (if (empty? exc-tags) \"<EMPTY>\" (cs/join \" \" exc-tags))]\n  (format\n   \"Waited days = %s, Platform = %s , App = %s, lang = %s, inc-tags = %s, exc-tags = %s, reply = %s\"\n   waited-days\n   platform-type\n   app-id\n   lang\n   inc-tags\n   exc-tags\n   message)))\n", :row 30, :col 3, :indicator :control} {:expr "(+ 1 x)", :alt "(inc x)", :row 45, :col 8, :indicator :arithmatic}], :path "src/qa/automation.clj", :stat 2})



  (def *input [{:indicator :control,
                :jurisdisction :default,
                :stat 2,
                :results [{:path "src/moby/core/models/view_folder.clj",
                           :stat 1,
                           :results [{:expr "(when (not keep-views?) folder-views)\n",
                                      :alt "(when-not keep-views? folder-views)\n",
                                      :row 91, :col 29}]}
                          {:path "src/qa/automation.clj",
                           :stat 1,
                           :results [{:expr "(if\n title\n title\n (let\n  [inc-tags\n   (if (empty? inc-tags) \"<EMPTY>\" (cs/join \" \" inc-tags))\n   exc-tags\n   (if (empty? exc-tags) \"<EMPTY>\" (cs/join \" \" exc-tags))]\n  (format\n   \"Waited days = %s, Platform = %s , App = %s, lang = %s, inc-tags = %s, exc-tags = %s, reply = %s\"\n   waited-days\n   platform-type\n   app-id\n   lang\n   inc-tags\n   exc-tags\n   message)))\n",
                                      :alt "(or\n title\n (let\n  [inc-tags\n   (if (empty? inc-tags) \"<EMPTY>\" (cs/join \" \" inc-tags))\n   exc-tags\n   (if (empty? exc-tags) \"<EMPTY>\" (cs/join \" \" exc-tags))]\n  (format\n   \"Waited days = %s, Platform = %s , App = %s, lang = %s, inc-tags = %s, exc-tags = %s, reply = %s\"\n   waited-days\n   platform-type\n   app-id\n   lang\n   inc-tags\n   exc-tags\n   message)))\n",
                                      :row 30, :col 3}]}]}
               {:indicator :arithmatic,
                :jurisdisction :default,
                :stat 2,
                :results [{:path "src/moby/core/models/view_folder.clj",
                           :stat 1,
                           :results [{:expr "(- 2 1)",
                                      :alt "1",
                                      :row 10, :col 30}]}
                          {:path "src/qa/automation.clj",
                           :stat 1,
                           :results [{:expr "(+ 1 x)",
                                      :alt "(inc x)",
                                      :row 45, :col 8}]}]}]))
