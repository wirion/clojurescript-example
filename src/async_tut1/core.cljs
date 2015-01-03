(ns async-tut1.core
  (:refer-clojure :exclude [map])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [cljs.core.async :refer [<! put! chan]])
  (:import [goog.net Jsonp]
           [goog Uri]))

;; (enable-console-print!)
;; (.log js/console (dom/getElement "query"))

;; (defn listen [el type]
;;   (let [out (chan)]
;;     (events/listen el type
;;                    (fn [e] (put! out e)))
;;     out))

(defn listen [el type]
  (let [out (chan)]
    (.addEventListener el type
                       (fn [e] (put! out e)))
    out))

;; (defn map-channel [f in]
;;   (let [out (chan)]
;;     (go (while true
;;           (>! out (f (<! in)))))
;;     out))

;; (defn e->v [e]
;;   [(.-pageX e) (.-pageY e)])

;; (let [mapped-move (map-channel e->v (listen js/window "mousemove"))]
;;   (go  (while true
;;          (. js/console log (pr-str (<! mapped-move))))))

(def wiki-search-url "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

(defn query-url [search-term]
  (str wiki-search-url search-term))

(defn jsonp [uri]
  (let [out (chan)
        req (Jsonp. (Uri. uri))]
    (.send req nil (fn [res] (put! out res)))
    out))

(defn user-query []
  (.-value (dom/getElement "query")))

(defn render-query [results]
  (str
    "<ul>"
    (apply str
      (for [result results]
        (str "<li>" result "</li>")))
    "</ul>"))

;; (defn init []
;;   (let [clicks (listen (dom/getElement "search") "click")]
;;     (go (while true
;;           (<! clicks)
;;           (.log js/console (<! (jsonp (query-url (user-query)))))))))

(defn init []
  (let [clicks (listen (dom/getElement "search") "click")
        results-view (dom/getElement "results")]
    (go (while true
          (<! clicks)
          (let [[_ results] (<! (jsonp (query-url (user-query))))]
            (set! (.-innerHTML results-view) (render-query results)))))))

(init)

;; (let [move (listen js/window "mousemove")]
;;   (go  (while true
;;          (.log js/console (<! move)))))

;; (let [clicks (listen (dom/getElement "search") "click")]
;;   (go (while true
;;         (.log js/console (<! clicks)))))
