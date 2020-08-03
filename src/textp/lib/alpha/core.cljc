(ns textp.lib.alpha.core
  (:require
    [clojure.spec.alpha :as s]
    #?(:clj [clojure.core.specs.alpha :as core-specs]
       :cljs [cljs.core.specs.alpha :as core-specs])
    [net.cgrand.macrovich :as macro :include-macros true]
    [meander.epsilon :as m :include-macros true])
  #?(:require-macros [textp.lib.alpha.core]))


(defn- conform-or-throw [spec v]
  (let [res (s/conform spec v)]
    (when (s/invalid? res)
      (throw (ex-info "Invalid input" (s/explain-data ::html-like-tag-args v))))
    res))


;;----------------------------------------------------------------------------------------------------------------------
;; Specs decribing reader data for tags
;;----------------------------------------------------------------------------------------------------------------------
(defn- has-k-v? [m k v]
  (= (get m k) v))


(defn  clj-arg? [m]
  (and (map? m)
       (has-k-v? m :tag :tag-args-clj)))


(defn  text-arg? [m]
  (and (map? m)
       (has-k-v? m :tag :tag-args-txt)))


(s/def ::tag-clj-arg clj-arg?)
(s/def ::tag-txt-arg text-arg?)


;;----------------------------------------------------------------------------------------------------------------------
;; Xml like tags definition helpers
;;----------------------------------------------------------------------------------------------------------------------
(s/def ::html-like-tag-args (s/cat :attrs (s/? ::tag-clj-arg)
                                   :content (s/? ::tag-txt-arg)))


(defn- args->map [args]
  (apply hash-map args))


(defn make-tag [n args]
  (let [parsed (conform-or-throw ::html-like-tag-args args)]
    {:tag n
     :attrs (or (some-> parsed :attrs :content args->map)
                {})
     :content (-> parsed :content :content vec)}))


(macro/deftime
  (defmacro def-xml-tag
    ([n]
     (let [kw (-> n name keyword)]
       `(def-xml-tag ~n ~kw)))
    ([name tag-kw]
     `(defn ~name [& args#]
        (make-tag ~tag-kw args#)))))

;;----------------------------------------------------------------------------------------------------------------------
;; Utilies helping the in the definition of functions to be employed in tag syntax.
;;----------------------------------------------------------------------------------------------------------------------

(s/def ::tag-fn-args (s/cat :arg (s/? ::tag-clj-arg)))


(defn clj-fn->tag-fn
  "Turn a classic clojure function into a function that can be used in tag form. The arguments
  of the new function will be passed in a clojure tag argument.
  For instance defining the following:
  ```clojure
  (defn add [x y]
    (+ x y))

  (def add-tag (clj-fn->tag-fn add))
  ```

 allows us to do this in textp documents:
 ```text
 Some text then ◊add-tag[1 2].
 ```"
  [f]
  (fn [& tag-args]
    (let [args (-> tag-args
                   (->> (conform-or-throw ::tag-fn-args))
                   (get-in [:arg :content]))]
      (apply f args))))


(defn- conform-defn-args [args]
  (s/conform ::core-specs/defn-args args))


(defn- unform-defn-args [args]
  (s/unform ::core-specs/defn-args args))


(defn- unform-artity-1 [params+body]
  (m/rewrite (s/unform ::core-specs/params+body params+body)
    (?params & ?body)
    (fn (m/app vec ?params) & ?body)))


(defn- unform-artity-n [param+bodies]
  (m/rewrite (map #(s/unform ::core-specs/params+body %) param+bodies)
    ((!params & !rest) ...)
    (fn . ((m/app vec !params) & !rest) ...)))


(defn- fn-tail->fn-form [fn-tail]
  (m/match fn-tail
    [:arity-1 ?param+body]
    {:fn-form (unform-artity-1 ?param+body)}

    [:arity-n {:bodies ?param+bodies
                :attr-map ?attr-map}]
    {:fn-form (unform-artity-n ?param+bodies)
     :attr-map ?attr-map}))


(defn- parse-defn [fn-args]
  (let [conformed (conform-defn-args fn-args)]
    (update conformed :fn-tail fn-tail->fn-form)))


(defn- make-base-defn [parsed]
  (let [{:keys [meta]
         {:keys [attr-map]} :fn-tail} parsed]
    (-> parsed
        (dissoc :fn-tail)
        (cond-> (or meta attr-map)
                (update :meta merge attr-map))
        unform-defn-args)))


(macro/deftime
  (defmacro def-tag-fn
    "Help defining function that will be used in tag form in a text document.
    Similar to [[textp.lib.core/clj-fn->tag-fn]].

    You can define the function:
    ```clojure
    (def-tag-fn add [x y]
      (+ x y))
    ```

    to be used this way:
    ```text
    Some text then ◊add[1 2].
    ```

    This call would be equivalent to:
    ```clojure
    (add {:tag :tag-args-clj
          :content [1 2]})
    ```"
    {:arglists '([name doc-string? attr-map? [params*] prepost-map? body]
                 [name doc-string? attr-map? ([params*] prepost-map? body)+ attr-map?])}
    [& args]
    (let [parsed (parse-defn args)
          base (make-base-defn parsed)
          fn-form (get-in parsed [:fn-tail :fn-form])]
      `(let [f# (clj-fn->tag-fn ~fn-form)]
         (defn ~@base [& args#]
             (apply f# args#))))))
