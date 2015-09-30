(ns daifu.diagnosis.target.files
  (:require [daifu.diagnosis.target.project :as project]
            [gita.core :as git]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:import java.io.File
           org.eclipse.jgit.lib.Repository))

(defn file-seq-filter
  [dir filter]
  (tree-seq
   (fn [^File f] (and (. f (isDirectory)) (filter f)))
   (fn [^File d] (seq (. d (listFiles))))
   dir))

(defmulti list-files (fn [repo _] (type repo)))

(defmethod list-files File
  [repo _]
  (let [path (.getCanonicalPath repo)
        len (count path)]
    (->> (file-seq-filter repo (fn [dir] (and (not (= ".git" (.getName dir)))
                                              (.startsWith (.getCanonicalPath dir) path))))
         (filter #(not (.isDirectory %)))
         (map #(subs (.getCanonicalPath %) (inc len))))))

(defmethod list-files Repository
  [repo {:keys [comparison current previous] :as target}]
  (if comparison
    (->> (git/list-file-changes repo previous current)
         (map :path))
    (git/list-files repo current)))

(defmulti pick-files-base (fn [repo target] (:type target)))

(defmethod pick-files-base :project
  [repo target]
  (list-files repo target))

(defmethod pick-files-base :multi
  [repo {:keys [files patterns] :as target}]
  (let [all (list-files repo target)
        s1  (filter (fn [f] (some #(= f %) files)) all)
        s2  (filter (fn [f] (some #(re-find % f) patterns)) all)]
    (-> (concat s1 s2) set sort)))

(defmethod pick-files-base :file
  [repo {:keys [path] :as target}]
  (let [all (list-files repo target)]
    (filter #(= path %) all)))

(defn wrap-source-paths [f]
  (fn [repo target]
    (let [source-paths (->> (:current target) (project/read-project repo) :source-paths)]
      (->> (f repo (assoc target :source-paths source-paths))
           (filter (fn [x] (some #(.startsWith x %) source-paths)))))))

(defn wrap-clj-only [f]
  (fn [repo target]
    (let [result (f repo target)]
      (filter (fn [file]
                (and (-> (clojure.string/split file #"/")
                         (last)
                         (.startsWith ".")
                         not)
                     (or (.endsWith file ".clj")
                         (.endsWith file ".cljs")
                         (.endsWith file ".cljc")))) result))))

(def pick-files
  (-> pick-files-base
      wrap-source-paths
      wrap-clj-only))

(defmulti retrieve-file (fn [repo _] (type repo)))

(defmethod retrieve-file File
  [repo {:keys [path]}]
  (try (io/reader (io/file (.getCanonicalPath repo) path))
       (catch Exception e)))

(defmethod retrieve-file Repository
  [repo opts]
  (try (io/reader (git/raw repo opts))
       (catch Exception e)))
