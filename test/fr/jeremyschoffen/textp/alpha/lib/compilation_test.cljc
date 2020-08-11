(ns fr.jeremyschoffen.textp.alpha.lib.compilation-test
  (:require [clojure.test :refer [deftest is] :include-macros true]
            [fr.jeremyschoffen.textp.alpha.lib.compilation :as c :include-macros true]))


(deftest text-compilation-env
  (let [text ["some text\n" [1 2 3]]]
    (is (= (c/text-environment
             (apply c/emit! text))

           (apply str text)))))