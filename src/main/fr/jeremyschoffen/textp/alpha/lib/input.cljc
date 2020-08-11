(ns ^{:author "Jeremy Schoffen"
      :doc "This namespace provides a way to pass information from the evaluation environment to
      some textp text being evaluated.

      The idea is to have some code looking like this:
      ```clojure
      (with-input {:k1 v1}
        (-> \"some/doc\"
            slurp
            read-from-string
            eval
            compile
            (->> (spit \"some/where\"))))
      ```

      The inputs are passed via [[fr.jeremyschoffen.textp.alpha.lib.input/with-input]]. The code being evaluated can
      then use this inputs by requiring and using [[fr.jeremyschoffen.textp.alpha.lib.input/*input*]]."}
  fr.jeremyschoffen.textp.alpha.lib.input
  (:require [net.cgrand.macrovich :as macro]))


(def ^:dynamic *input*
  "Dynamic var to be use as a source of input to be used in embedded clojure code or in custom tags."
  nil)


(macro/deftime
  (defmacro with-input
    "Macro helper to binding a value to [[fr.jeremyschoffen.textp.alpha.lib.input/*input*]].

    ```clojure
    (with-input {:k1 v1}
      (-> \"some/doc\"
          slurp
          read-from-string
          eval
          compile
          (->> (spit \"some/where\"))))
    ```"
    [i & body]
    `(binding [*input* ~i]
       ~@body)))
