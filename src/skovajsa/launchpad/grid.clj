(ns skovajsa.launchpad.grid)

(defprotocol Grid
  (get-btn [this x y])
  (upd-btn! [this x y val vel]))

(def init-grid
  (apply hash-map (interleave
                    (for [i (range 1 9) j (range 1 9)] [i j])
                    (repeat nil))))

(defn note-num
  "Takes x and y coordinates of a button, 1 through 8,
  returns the note number from 0 to 127."
  [x y]
  (if (and (some #{x} (range 1 9)) (some #{y} (range 1 9)))
    (+ (* 16 (dec y)) (dec x))
    (throw (IllegalArgumentException. "Illegal note coordinates"))))
