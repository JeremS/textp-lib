(ns fr.jeremyschoffen.textp.lib.alpha.input
  (:require [net.cgrand.macrovich :as macro]))


(def ^:dynamic *input* nil)


(macro/deftime
  (defmacro with-input [i & body]
    `(binding [*input* ~i]
       ~@body)))