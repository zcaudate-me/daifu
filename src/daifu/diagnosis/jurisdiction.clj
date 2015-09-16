(ns daifu.diagnosis.jurisdiction
  (:require [hara.namespace.eval :as ns]
            [clojure.java.io :as io]
            [gita.core :as git]
            [leiningen.core.project :as project]))

(defrecord Jurisdiction [])

(defmethod print-method Jurisdiction
  [v w]
  (.write w (str "@" (name (:type v)) " "
                 (into {} (dissoc v :type)))))

(defn jurisdiction [m]
  (map->Jurisdiction m))

(def +default-jurisdiction+
  (jurisdiction {:id :default
                 :type :project}))

(defmulti read-project (fn [repo opts] (type repo)))

(defmethod read-project java.io.File
  [repo jurisdiction]
  (-> (.getCanonicalPath repo)
      (str (System/getProperty "file.separator") "project.clj")
      (project/read-raw)
      (assoc :jurisdisction jurisdiction)))

(defmethod read-project org.eclipse.jgit.lib.Repository 
  [repo {:keys [commit branch] :as jurisdiction}]
  (ns/eval-ns 'leiningen.core.project
              [(->> (assoc jurisdiction :path "project.clj")
                    (git/raw repo)
                    (slurp)
                    (read-string)
                    (list 'eval))])
  (let [project (resolve 'leiningen.core.project/project)]
    (ns-unmap 'leiningen.core.project 'project)
    (assoc @project :jurisdisction jurisdiction)))

(defmulti list-files (fn [repo _] (type repo)))

(defn file-seq-filter
  [dir filter]
  (tree-seq
   (fn [^java.io.File f] (and (. f (isDirectory)) (filter f)))
   (fn [^java.io.File d] (seq (. d (listFiles))))
   dir))

(defmethod list-files java.io.File
  [repo _]
  (let [path (.getCanonicalPath repo)
        len (count path)]
    (->> (file-seq-filter repo (fn [dir] (and (not (= ".git" (.getName dir)))
                                              (.startsWith (.getCanonicalPath dir) path))))
         (filter #(not (.isDirectory %)))
         (map #(subs (.getCanonicalPath %) (inc len))))))

(defmethod list-files org.eclipse.jgit.lib.Repository 
  [repo {:keys [comparison current previous] :as jurisdiction}]
  (if comparison
    (->> (git/list-file-changes repo previous current)
         (map :path))
    (git/list-files repo current)))

(defmulti pick-files-base (fn [repo jurisdiction] (:type jurisdiction)))

(defmethod pick-files-base :project
  [repo jurisdiction]
  (list-files repo jurisdiction))

(defmethod pick-files-base :multi
  [repo {:keys [files patterns] :as jurisdiction}]
  (let [all (list-files repo jurisdiction)
        s1  (filter (fn [f] (some #(= f %) files)) all)
        s2  (filter (fn [f] (some #(re-find % f) patterns)) all)]
    (-> (concat s1 s2) set sort)))

(defmethod pick-files-base :file
  [repo {:keys [path] :as jurisdiction}]
  (let [all (list-files repo jurisdiction)]
    (filter #(= path %) all)))

(defn wrap-source-paths [f]
  (fn [repo jurisdiction]
    (let [source-paths (->> (:current jurisdiction) (read-project repo) :source-paths)]
      (->> (f repo (assoc jurisdiction :source-paths source-paths))
           (filter (fn [x] (some #(.startsWith x %) source-paths)))))))

(defn wrap-clj-only [f]
  (fn [repo jurisdiction]
    (let [result (f repo jurisdiction)]
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

(defmethod retrieve-file java.io.File
  [repo {:keys [path]}]
  (try (io/reader (io/file (.getCanonicalPath repo) path))
       (catch Exception e)))

(defmethod retrieve-file org.eclipse.jgit.lib.Repository 
  [repo opts]
  (try (io/reader (git/raw repo opts))
       (catch Exception e)))



(comment

  (slurp (retrieve-file (git/repository) {:path "project.clj"}))
  
  (pick-files (io/file ".") {:type :project})

  (pick-files (git/repository) {:type :project})
  
  (pick-files (git/repository)
              {:type :project
               :current  {:branch "master"
                          :commit "HEAD"}
               :previous {:branch "master"
                          :commit "HEAD^2"}
               :comparison true
               })
  
  (list-files (git/repository) {:current  {:branch "master"
                                           :commit "HEAD"}
                                :previous {:branch "master"
                                           :commit "HEAD^2"}
                                :comparison true
                                :source-paths ["src"]})

  (list-files (git/repository) {:current  {:branch "master"
                                           :commit "HEAD"}
                                :previous {:branch "master"
                                           :commit "HEAD"}
                                :comparison true
                                :source-paths ["src"]})

  
  
  (pick-files (git/repository)
              {:type :project})

  (pick-files (git/repository)
              {:type :multi
               :patterns [#"daifu"]})

  (pick-files (git/repository)
              {:type :file
               :path "src/daifu/core.clj"
               :comparison true})

  (jurisdiction {:id :default
                 :type :project
                 :version  {:branch nil
                            :commit nil}
                 :previous {:branch nil
                            :commit nil}}))
