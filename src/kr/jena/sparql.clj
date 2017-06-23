(ns kr.jena.sparql
  (use kr.core.variable
       kr.core.kb
       kr.core.clj-ify
       kr.core.rdf
       kr.core.sparql
       kr.jena.rdf)

  (import 
   ;interfaces
   (com.hp.hpl.jena.query QueryFactory
                          QueryExecutionFactory
                          QueryExecution
                          Query
                          QuerySolution)
                          
))

;;; --------------------------------------------------------
;;; main query helper
;;; --------------------------------------------------------

(defn jena-query-setup [kb query-string]
  (QueryExecutionFactory/create (QueryFactory/create query-string)
                                (model! kb)))

(defn jena-result-to-map [kb result]
  (let [vars (iterator-seq (.varNames result))]
    (reduce conj {} (map (fn [var]
                           (vector (variable var)
                                   (clj-ify kb (.get result var))))
                         vars))))

;;; --------------------------------------------------------
;;; main queries
;;; --------------------------------------------------------

;;this returns a boolean
(defn jena-ask-sparql [kb query-string]
  (let [qexec (jena-query-setup kb query-string)
        result (.execAsk qexec)]
    (.close qexec)
    result))

;;this returns a binding set iteration type thingy that can be clj-ified
(defn jena-query-sparql [kb query-string]
  (let [qexec (jena-query-setup kb query-string)]
    (try
     (doall (map (partial jena-result-to-map kb)
                 (iterator-seq (.execSelect qexec))))
     (finally (.close qexec)))))

(defn jena-count-sparql [kb query-string]
  (let [qexec (jena-query-setup kb query-string)]
    (try
     (count (iterator-seq (.execSelect qexec)))
     (finally (.close qexec)))))


;;TODO verify if this is correct?
(defn jena-visit-sparql [kb visitor query-string]
  (let [qexec (jena-query-setup kb query-string)]
    (try
      (dorun (map (fn [result]
                    (visitor (jena-result-to-map kb result)))
                 (iterator-seq (.execSelect qexec))))
     (finally (.close qexec)))))




(defn jena-construct-sparql [kb sparql-string]
  (let [qexec (jena-query-setup kb sparql-string)]
    (try
      (doall (clj-ify kb (iterator-seq (.execConstructTriples qexec))))
       ;; (map (partial clj-ify kb)
       ;;           (iterator-seq (.execConstructTriples qexec))))
     (finally (.close qexec)))))

(defn jena-construct-visit-sparql [kb visitor sparql-string]
  (let [qexec (jena-query-setup kb sparql-string)]
    (try
      (dorun (map (fn [triple]
                    (visitor (clj-ify kb triple)))
                 (iterator-seq (.execConstructTriples qexec))))
     (finally (.close qexec)))))






;;this returns a boolean
(defn jena-ask-pattern [kb pattern & [options]]
  (jena-ask-sparql kb (sparql-ask-query pattern options)))
  ;; (let [qexec (jena-query-setup kb (sparql-ask-query pattern))
  ;;       result (.execAsk qexec)]
  ;;   (.close qexec)
  ;;   result))

;;this returns a binding set iteration type thingy that can be clj-ified
(defn jena-query-pattern [kb pattern & [options]]
  (jena-query-sparql kb (sparql-select-query pattern options)))
  ;; (let [qexec (jena-query-setup kb (sparql-select-query pattern))]
  ;;   (try
  ;;    (doall (map jena-result-to-map
  ;;                (iterator-seq (.execSelect qexec))))
  ;;    (finally (.close qexec)))))

(defn jena-count-pattern [kb pattern & [options]]
  (jena-count-sparql kb (sparql-select-query pattern options)))
  ;; (let [qexec (jena-query-setup kb (sparql-select-query pattern))]
  ;;   (try
  ;;    (count (iterator-seq (.execSelect qexec)))
  ;;    (finally (.close qexec)))))
;;  (count-results (jena-query-pattern kb pattern)))

(defn jena-visit-pattern [kb visitor pattern & [options]]
  (jena-visit-sparql kb visitor (sparql-select-query pattern options)))

(defn jena-construct-pattern [kb create-pattern pattern & [options]]
  (jena-construct-sparql kb
                         (sparql-construct-query create-pattern
                                                 pattern
                                                 options)))

(defn jena-construct-visit-pattern [kb visitor create-pattern pattern
                                      & [options]]
  (jena-construct-visit-sparql kb
                               visitor
                               (sparql-construct-query create-pattern
                                                       pattern
                                                       options)))



;;; --------------------------------------------------------
;;; END
;;; --------------------------------------------------------


