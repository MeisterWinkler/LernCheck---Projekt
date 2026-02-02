USE quizapp;

-- Beispielklassen
INSERT IGNORE INTO classes(name) VALUES ('Fia23A'),('Fis23B'),('Fis23A'),('Fia23B');

-- Beispiel-Lehrer: username=lehrer, passwort=lehrer123
-- Hash wird in Java erzeugt (BCrypt). Du kannst ihn alternativ nach dem ersten Login-Setup setzen.
-- Für Demo trägst du den Hash aus der Konsole ein oder erzeugst ihn kurz via Java.