(ns
    ^{:doc "operations on notes (tonal-pitch-class with an octave).
Note keywords are of the form :<note-letter><note-alterations><note-octave>,
where note-letter is any letter from a-g or A-G and note alterations is any number of
# or any number of b or nothing, and note-octave any integer (positive or negative)"
      :author "Kyle Hipke"}

    music-theory.westergaardian-theory.note
  (:use music-theory.westergaardian-theory.tonal-pitch-class)
  (:use music-theory.westergaardian-theory.semitone)
  (:use overtone.core))

(defn- normalize-note
  "Converts note keywords to a standard internal representation.
  Uppercases note letters."
  [note]
  (let [note-name (name note)]
    (keyword (str (.toUpperCase (str (.charAt note-name 0)))
                  (when (> (count note-name) 1)
                    (.substring note-name 1 (count note-name)))))))

(defn note-letter
	"Returns the capital letter of the note keyword.
	So :Bb3 returns the character 'B'"
	[note]
  (let [normal-note (normalize-note note)] (.charAt (name normal-note) 0)))

(defn note-tonal-pitch-class
  "Returns the tonal pitch class keyword of the note."
  [note]
  (let [note-name (name (normalize-note note))]
    (keyword
      (if (.contains note-name "-")
        (.substring note-name 0 (- (count note-name) 2))
        (.substring note-name 0 (dec (count note-name)))
        ))))

(defn note-octave
	"Returns the octave of the given note keyword as an integer."
	[note]
	(let [normal-note (normalize-note note)] (if (.contains (name normal-note)  "-")
		(* -1 (read-string (str (.charAt (name normal-note) (dec (count (name normal-note)))))))
    (read-string (str (.charAt (name normal-note) (dec (count (name normal-note)))))))))

(defn note-alterations
  "Returns the alterations on the note keyword as a string. Empty string if none."
  [note]
  (or (re-find #"[#b]+" (name (normalize-note note))) "")
  )



(defn note-semitones
  "Returns an integer indicating the amount of semitones the
  note is above C-1 (returns negative if below that note)"
  [note]
  (let [normal-note (normalize-note note)]
    (+ (* 12 (inc (note-octave normal-note)))
     (tonal-pitch-class-semitone-distance :C (note-tonal-pitch-class normal-note))
     )))

(defn anote
  "Creates a note given a note letter (string), alterations (string), and
  integer octave."
  [letter alterations octave]
  (keyword (str letter alterations octave)))

(defn note-from-tonal-pitch-class
  "Creates a note from a tonal pitch class, given an octave"
  [tonal-pitch-class octave]
  (anote (tonal-pitch-class-letter tonal-pitch-class)
        (tonal-pitch-class-alterations tonal-pitch-class)
        octave)
  )


(defn tonal-pitch-class-from-note
  "Returns the tonal pitch class keyword by simply removing the octave from the note"
  [note]
  (keyword (str (note-letter note) (note-alterations note))))

(defn raise
  "raises the note a half step by removing a flat or adding a sharp"
  [note]
  (let [normal-note (normalize-note note)]
    (let [tpc (note-tonal-pitch-class normal-note)]
      (cond

        (> (sharps tpc) 0)
        (note-from-tonal-pitch-class (sharpen tpc 1) (note-octave normal-note))

        (> (flats tpc) 0)
        (note-from-tonal-pitch-class (unflattify tpc 1) (note-octave normal-note))

        :else
        (note-from-tonal-pitch-class (sharpen tpc 1) (note-octave normal-note))))))

(defn lower
  "opposite of raise"
  [note]
  (let [normal-note (normalize-note note)]
    (let [tpc (note-tonal-pitch-class normal-note)]
      (cond

        (> (sharps tpc) 0)
        (note-from-tonal-pitch-class (unsharpen tpc 1) (note-octave normal-note))

        (> (flats tpc) 0)
        (note-from-tonal-pitch-class (flattify tpc 1) (note-octave normal-note))

        :else
        (note-from-tonal-pitch-class (flattify tpc 1) (note-octave normal-note))))))

(defn note-hz
  "Returns the frequency of the pitch that the note represents"
  [note]
  (midi->hz (semitones->midi (note-semitones note))))

(defn note-midi
  "Returns the midi value this note represents"
  [note]
  (semitones->midi (note-semitones note))
  )
