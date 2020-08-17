(ns fr.jeremyschoffen.textp.alpha.lib.compilation-test
  (:require
    #?(:clj [clojure.test :refer [deftest testing is are]]
       :cljs [cljs.test :refer-macros [deftest testing is are]])
    [fr.jeremyschoffen.textp.alpha.lib.compilation :as c :include-macros true]))


(deftest text-compilation-env
  (let [text ["some text\n" [1 2 3]]]
    (is (= (c/text-environment
             (apply c/emit! text))

           (apply str text)))))
