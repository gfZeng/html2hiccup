(ns leiningen.h2h
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [org.httpkit.client :as http]))

(defn html->hiccup [html]
  (:body
   @(http/post "http://html2hiccup.herokuapp.com/convert"
               {:form-params {:html html}})))


(defn dump [in out]
  (spit out (html->hiccup (slurp in))))

(defn html-file? [f]
  (and (.isFile f)
       (re-find #"\.html$" (.getName f))))

(defn io-files [src dest]
  (let [src 	(io/file src)
        dest 	(io/file dest)
        out-path (fn [f]
                   (doto
                       (io/file dest
                                (-> (.toURI src)
                                    (.relativize (.toURI f))
                                    .getPath
                                    (str/replace #"\.html$" ".clj")))
                     io/make-parents))]
    (->> (io/file src)
         file-seq
         (filter html-file?)
         (reduce #(assoc % %2 (out-path %2)) {}))))

(defn h2h
  "compile html to hiccup"
  [{{:keys [source-paths target-path]
     :or {source-paths ["resources/public"]
          target-path "resources/hiccup"}} :h2h}
   & [in]]
  (if in
    (println (html->hiccup (slurp in)))
    (doseq [src source-paths]
      (doseq [[in out] (io-files src target-path)]
        (println (.getPath in) "===>" (.getPath out))
        (dump in out)))))

