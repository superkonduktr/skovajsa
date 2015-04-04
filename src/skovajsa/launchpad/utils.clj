(ns skovajsa.launchpad.utils)

(defn color->vel
  "Accepts a keyword for color among the following:
  :low-red, :red, full-red, :low-amber, :amber, :full-ember, :orange, :green.
  Returns a corresponding velocity value."
  [color]
  (case color
    nil 0
    :low-red 1
    :red 2
    :full-red 3
    :low-amber 17
    :amber 18
    :orange 19
    :yellow 49
    :low-green 20
    :green 52
    (throw (IllegalArgumentException. (str "Unknown color keyword: " color)))))

(defn control->note
  "Accepts a keyword for control, among the following:
  :up, :down, :left, :right, :session, :user1, :user2, or :mixer.
  Returns a corresponding note on the Launchpad."
  [mode]
  (case mode
    :up 104
    :down 105
    :left 106
    :right 107
    :session 108
    :user1 109
    :user2 110
    :mixer 111
    nil))

(defn note->control
  "Converts a note to a keyword for corresponding control."
  [n]
  (case n
    104 :up
    105 :down
    106 :left
    107 :right
    108 :session
    109 :user1
    110 :user2
    111 :mixer
    (throw (IllegalArgumentException. (str "Illegal note value: " n)))))

(defn note->xy
  "Converts a note value (0-127) into a pair of coordinates on the
  Launchpad grid."
  [n]
  (cond
    (>= 7 n 0) [(inc n) 1]
    (>= 127 n 16) [(inc (mod n (* 16 (quot n 16)))) (inc (quot n 16))]
    :else (throw (IllegalArgumentException. (str "Illegal note value: " n)))))

(defn xy->note
  "Converts a pair of coordinates (1-8) on the Launchpad grid into
  a note value."
  [[x y]]
  (if (and (some #{x} (range 1 9)) (some #{y} (range 1 9)))
    (+ (* 16 (dec y)) (dec x))
    (throw (IllegalArgumentException. "Illegal note coordinates: " [x y]))))
