(ns tutorial.scrape-mf
  (:require [net.cgrand.enlive-html :as html])
  (:require [clj-http.client :as client])
  (:require [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]))

(def ^:dynamic *base-url* "https://www.amfiindia.com/modules/NAVList")

(defn -main [& args]
  (let [conn (esr/connect "http://127.0.0.1:9200")
	    mapping-types {"mutual-fund" {:properties {
                                                 :name   {:type "string" :store "yes"}
	                                          		 :isin_growth_option {:type "string" :store "yes"}
	                                          		 :isin_dividend_option  {:type "string"}
	                                          		 :nav        {:type "float"}
	                                          		 :repurchase_price      {:type "float"}
	                                          		 :sale_price     {:type "float"}
                                             		 :date {:type "date"}}}}]
    (esi/create conn "development" :mappings mapping-types)))
(defn createDoc [[mf_name isin_growth_option isin_dividend_option nav repurchase_price sale_price date]]
  (def conn (esr/connect "http://127.0.0.1:9200"))
  (println conn)
  (def doc {
           :name mf_name
           :isin_growth_option isin_growth_option
	   :isin_dividend_option isin_dividend_option
	   :nav (read-string nav)
	   :repurchase_price (read-string repurchase_price)
	   :sale_price (read-string sale_price)})
  (println doc)
  (esd/create conn "development" "mutual-fund" doc))
(defn submit-form [url]
  (html/html-snippet (:body (client/post url {:form-params {:MFName "10-27"}}))))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn mf-data []
  (map html/text (html/select (submit-form *base-url*) [:td])))

(defn store-mf []
(-main)
(map createDoc (partition 7 (mf-data))))

(defn print-mf []
  (doseq [line (map (fn [[
                          mf_name
                          isin_growth_option
                          isin_dividend_option
                          nav
                          repurchase_price
                          sale_price date]]
                      (str mf_name"," isin_growth_option"," isin_dividend_option"," nav"," repurchase_price"," sale_price"," date))
                    (partition 7 (mf-data)))]
    (println line)))
