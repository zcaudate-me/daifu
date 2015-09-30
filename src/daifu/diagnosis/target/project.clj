(ns daifu.diagnosis.target.project
  (:require [hara.namespace.eval :as ns]
            [gita.core :as git]
            [leiningen.core.project :as project]))

(defmulti read-project (fn [repo opts] (type repo)))

(defmethod read-project java.io.File
  [repo target]
  (-> (.getCanonicalPath repo)
      (str (System/getProperty "file.separator") "project.clj")
      (project/read-raw)
      (assoc :target target)))

(defmethod read-project org.eclipse.jgit.lib.Repository
  [repo {:keys [commit branch] :as target}]
  (ns/eval-ns 'leiningen.core.project
              [(->> (assoc target :path "project.clj")
                    (git/raw repo)
                    (slurp)
                    (read-string)
                    (list 'eval))])
  (let [project (resolve 'leiningen.core.project/project)]
    (ns-unmap 'leiningen.core.project 'project)
    (assoc @project :target target)))
