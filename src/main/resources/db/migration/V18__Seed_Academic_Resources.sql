-- V18__Seed_Academic_Resources.sql

-- Categorías de ejemplo por carrera (usa carreras existentes de V1)
INSERT INTO resource_categories (id, carrera_id, name) VALUES
    ('a1000000-0000-0000-0000-000000000001', (SELECT id FROM carreras LIMIT 1), 'Cálculo I'),
    ('a1000000-0000-0000-0000-000000000002', (SELECT id FROM carreras LIMIT 1), 'Álgebra Lineal'),
    ('a1000000-0000-0000-0000-000000000003', (SELECT id FROM carreras LIMIT 1), 'Base de Datos'),
    ('a1000000-0000-0000-0000-000000000004', (SELECT id FROM carreras LIMIT 1), 'Física I'),
    ('a1000000-0000-0000-0000-000000000005', (SELECT id FROM carreras LIMIT 1), 'Programación');

-- Recursos académicos de ejemplo
INSERT INTO academic_resources (id, slug, title, description, type, category_id, author_id, file_url, file_size, file_format, average_rating, ratings_count, download_count) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'guia-calculo-1', 'Guía de Cálculo I', 'Ejercicios resueltos de derivadas e integrales', 'GUIDE', 'a1000000-0000-0000-0000-000000000001', (SELECT id FROM usuarios LIMIT 1), 'resources/b1000000-0000-0000-0000-000000000001.pdf', 2048000, 'PDF', 4.50, 10, 25),
    ('b1000000-0000-0000-0000-000000000002', 'resumen-algebra', 'Resumen de Álgebra Lineal', 'Conceptos fundamentales de vectores y matrices', 'SUMMARY', 'a1000000-0000-0000-0000-000000000002', (SELECT id FROM usuarios LIMIT 1), 'resources/b1000000-0000-0000-0000-000000000002.pdf', 1024000, 'PDF', 4.20, 8, 15),
    ('b1000000-0000-0000-0000-000000000003', 'flashcards-bd', 'Flashcards Base de Datos', 'Tarjetas para repasar SQL y normalización', 'FLASHCARD', 'a1000000-0000-0000-0000-000000000003', (SELECT id FROM usuarios LIMIT 1), 'resources/b1000000-0000-0000-0000-000000000003.pdf', 512000, 'PDF', 3.80, 5, 10),
    ('b1000000-0000-0000-0000-000000000004', 'examen-fisica', 'Examen Parcial Física I', 'Examen anterior con soluciones', 'EXAM', 'a1000000-0000-0000-0000-000000000004', (SELECT id FROM usuarios LIMIT 1), 'resources/b1000000-0000-0000-0000-000000000004.pdf', 3072000, 'PDF', 4.80, 15, 40),
    ('b1000000-0000-0000-0000-000000000005', 'notas-programacion', 'Notas de Programación', 'Apuntes de POO y estructuras de datos', 'OTHER', 'a1000000-0000-0000-0000-000000000005', (SELECT id FROM usuarios LIMIT 1), 'resources/b1000000-0000-0000-0000-000000000005.md', 256000, 'MD', 4.00, 3, 8);
