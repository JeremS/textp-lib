(ns fr.jeremyschoffen.textp.alpha.lib.input
  (:require [net.cgrand.macrovich :as macro]))


(def ^:dynamic *input* nil)


(macro/deftime
  (defmacro with-input [i & body]
    `(binding [*input* ~i]
       ~@body)))