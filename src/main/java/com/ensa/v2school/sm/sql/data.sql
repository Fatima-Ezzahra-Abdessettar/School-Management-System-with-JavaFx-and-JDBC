-- ================================
-- DATABASE
-- ================================
DROP DATABASE IF EXISTS school_management;
CREATE DATABASE school_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE school_management;

-- ================================
-- TABLE: users
-- ================================
CREATE TABLE users (
                       id INT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       role ENUM('ADMIN','STUDENT') NOT NULL,
                       PRIMARY KEY (id),
                       UNIQUE KEY username (username)
) ENGINE=InnoDB;

INSERT INTO users (id, username, password, role) VALUES
                                                     (1, 'admin', 'admin123', 'ADMIN'),
                                                     (2, 'student1', 'pass123', 'STUDENT'),
                                                     (3, 'student2', 'pass123', 'STUDENT'),
                                                     (4, 'student3', 'pass123', 'STUDENT'),
                                                     (5, 'Sara', 'sara123', 'STUDENT'),
                                                     (6, 'Chrouk', 'chorouk123', 'STUDENT');

-- ================================
-- TABLE: majors
-- ================================
CREATE TABLE majors (
                        id INT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(50) NOT NULL,
                        description TEXT,
                        PRIMARY KEY (id),
                        UNIQUE KEY name (name)
) ENGINE=InnoDB;

INSERT INTO majors (id, name, description) VALUES
    (1, 'Informatique', 'Étude des systèmes informatiques, programmation et technologies de information'),
    (2, 'Génie Civil', 'Conception, construction et maintenance des infrastructures et bâtiments'),
    (3, 'Génie Électrique', 'Étude des systèmes électriques, électroniques et télécommunications');

-- ================================
-- TABLE: students
-- ================================
CREATE TABLE students (
  id VARCHAR(20) NOT NULL,
  user_id INT DEFAULT NULL,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  average FLOAT DEFAULT 0,
  major_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY user_id (user_id),
  KEY major_id (major_id),
  CONSTRAINT students_ibfk_1 FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT students_ibfk_2 FOREIGN KEY (major_id)
    REFERENCES majors (id) ON DELETE RESTRICT,
  CONSTRAINT students_chk_1 CHECK (average >= 0 AND average <= 20)
) ENGINE=InnoDB;

INSERT INTO students VALUES
('ST001', NULL, 'Fatima -Ezzahra', 'Abdessettar', 16, 1),
('ST002', NULL, 'Salma', 'Aafifi', 13.4, 2),
('ST003', 4, 'Farid', 'Abdelouahed', 15.5, 1),
('ST004', NULL, 'Sara', 'Kendil', 16.3, 2),
('ST005', 6, 'Chorouk', 'Berrada', 17.2, 3),
('ST006', NULL, 'Karim', 'Alami', 12.3, 3);

-- ================================
-- TABLE: subjects
-- ================================
CREATE TABLE subjects (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO subjects (id, name) VALUES
(1, 'Programmation Orientée Objet en C++'),
(2, 'Base de Données'),
(3, 'Structures de Données'),
(4, 'Résistance des Matériaux'),
(5, 'Thermodynamique'),
(6, 'Programmation Java'),
(8, 'Algorithmes'),
(9, 'Réseaux Informatiques'),
(11, 'Béton Armé'),
(12, 'Topographie'),
(13, 'Électronique'),
(14, 'Automatique'),
(15, 'Machines Électriques'),
(16, 'AutoCad software');

-- ================================
-- TABLE: student_subject
-- ================================
CREATE TABLE student_subject (
  student_id VARCHAR(20) NOT NULL,
  subject_id INT NOT NULL,
  PRIMARY KEY (student_id, subject_id),
  KEY subject_id (subject_id),
  CONSTRAINT student_subject_ibfk_1 FOREIGN KEY (student_id)
    REFERENCES students (id) ON DELETE CASCADE,
  CONSTRAINT student_subject_ibfk_2 FOREIGN KEY (subject_id)
    REFERENCES subjects (id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO student_subject VALUES
('ST001',1), ('ST001',2), ('ST001',8),
('ST002',4), ('ST002',5), ('ST002',11), ('ST002',12),
('ST003',3), ('ST003',6), ('ST003',9),
('ST004',12), ('ST004',16),
('ST005',13), ('ST005',14), ('ST005',15),
('ST006',5), ('ST006',9), ('ST006',16);

-- ================================
-- TABLE: major_subject
-- ================================
CREATE TABLE major_subject (
  major_id INT NOT NULL,
  subject_id INT NOT NULL,
  PRIMARY KEY (major_id, subject_id),
  KEY subject_id (subject_id),
  CONSTRAINT major_subject_ibfk_1 FOREIGN KEY (major_id)
    REFERENCES majors (id) ON DELETE CASCADE,
  CONSTRAINT major_subject_ibfk_2 FOREIGN KEY (subject_id)
    REFERENCES subjects (id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO major_subject VALUES
(1,1),(1,2),(1,3),(1,6),(1,8),(1,9),(1,13),
(2,4),(2,5),(2,11),(2,12),(2,16),
(3,5),(3,9),(3,13),(3,14),(3,15),(3,16);

-- ================================
-- TABLE: dossier_administratif
-- ================================
CREATE TABLE dossier_administratif (
  id INT NOT NULL AUTO_INCREMENT,
  numero_inscription VARCHAR(50) NOT NULL,
  date_creation DATE NOT NULL,
  eleve_id VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY numero_inscription (numero_inscription),
  UNIQUE KEY eleve_id (eleve_id),
  CONSTRAINT dossier_administratif_ibfk_1 FOREIGN KEY (eleve_id)
    REFERENCES students (id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO dossier_administratif VALUES
(1, 'INS-2025-0001', '2025-12-23', 'ST001'),
(2, 'INS-2025-0002', '2025-12-23', 'ST002');
