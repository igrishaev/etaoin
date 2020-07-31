(ns etaoin.proc
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import  java.lang.IllegalThreadStateException
            java.io.IOException))

(defn get-null-file ^java.io.File
  []
  (if-let [windows? (str/starts-with?  (System/getProperty "os.name") "Windows")]
    (io/file "NUL")
    (io/file "/dev/null")))

(defn get-log-file
  [file-path]
  (if file-path (io/file file-path)
      (get-null-file)))

(defn java-params ^"[Ljava.lang.String;" [params]
  (->> params
       (map str)
       (into-array String)))

(defn run
  ([args] (run args {}))
  ([args {:keys [log-stdout log-stderr]}]
   (let [binary      (first args)
         readme-link "https://github.com/igrishaev/etaoin#installing-the-browser-drivers"
         pb          (java.lang.ProcessBuilder. (java-params args))]
     (.redirectOutput pb (get-log-file log-stdout))
     (.redirectError pb  (get-log-file log-stderr))
     (try
       (.start pb)
       (catch java.io.IOException e
         (throw (ex-info
                  (format "Cannot find a binary file `%s` for the driver.
Please ensure you have the driver installed and specify the path to it.
For driver installation, check out the official readme file from Etaoin: %s" binary readme-link)
                  {:args args} e)))))))

;; todo store those streams

(defn alive? [^Process proc]
  (.isAlive proc))

(defn exit-code [^Process proc]
  (try
    (.exitValue proc)
    (catch IllegalThreadStateException _)))

(defn kill [^Process proc]
  (.destroy proc))

;; todo refactor those

(defn read-out [^Process proc]
  (try
    (-> proc .getInputStream slurp)
    (catch IOException _)))

(defn read-err [^Process proc]
  (try
    (-> proc .getErrorStream slurp)
    (catch IOException _)))
