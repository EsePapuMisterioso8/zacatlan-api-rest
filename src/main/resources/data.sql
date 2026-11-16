

INSERT INTO bloques (nombre, descripcion, activo)
VALUES
    ('Bebidas', 'Venta de bebidas tradicionales.', TRUE),
    ('Artesanías', 'Productos artesanales de la región.', TRUE),
    ('Panadería', 'Pan tradicional y productos de panadería.', TRUE),
    ('Conservas', 'Conservas y productos elaborados artesanalmente.', TRUE),
    ('Dulces típicos', 'Dulces tradicionales de la región.', TRUE);


INSERT INTO productos (id_bloque, nombre, descripcion, activo)
VALUES

-- BEBIDAS
((SELECT id_bloque FROM bloques WHERE nombre = 'Bebidas'),
 'Sidra natural (blanca)', 'Sidra artesanal de manzana.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Bebidas'),
 'Sidra rosada dulce', 'Sidra dulce de tonalidad rosada.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Bebidas'),
 'Licor artesanal de manzana', 'Licor elaborado artesanalmente con manzana.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Bebidas'),
 'Crema de licor sabor blueberry (arándano)', 'Crema de licor con sabor a arándano.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Bebidas'),
 'Refresco artesanal carbonatado de manzana', 'Refresco artesanal de manzana con gas.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Bebidas'),
 'Vino afrutado de zarzamora', 'Vino artesanal con sabor afrutado de zarzamora.', TRUE),

-- ARTESANÍAS
((SELECT id_bloque FROM bloques WHERE nombre = 'Artesanías'),
 'Relojes de pared', 'Relojes con diseños de la fábrica local Centenario.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Artesanías'),
 'Relojes de bolsillo de estilo clásico', 'Relojes de bolsillo de diseño clásico.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Artesanías'),
 'Textiles bordados a mano', 'Blusas y morrales tradicionales bordados a mano.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Artesanías'),
 'Cántaros y tazas de barro vidriado', 'Piezas artesanales elaboradas en barro vidriado.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Artesanías'),
 'Juguetes y adornos de madera tallada', 'Figuras y juguetes elaborados en madera.', TRUE),

-- PANADERÍA
((SELECT id_bloque FROM bloques WHERE nombre = 'Panadería'),
 'Pan tradicional relleno de queso ranchero', 'Pan artesanal relleno de queso ranchero.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Panadería'),
 'Pan relleno de requesón', 'Pan tradicional relleno de requesón.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Panadería'),
 'Pan dulce relleno de mermelada de zarzamora', 'Pan dulce artesanal con relleno de zarzamora.', TRUE),

-- CONSERVAS
((SELECT id_bloque FROM bloques WHERE nombre = 'Conservas'),
 'Manzana rayada', 'Fruta fresca comercializada por kilogramo.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Conservas'),
 'Mermelada artesanal de manzana con canela', 'Mermelada elaborada con manzana y canela.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Conservas'),
 'Mermelada de frutos rojos', 'Mermelada artesanal de frutos rojos.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Conservas'),
 'Manzana deshidratada en rodajas', 'Rodajas de manzana deshidratada tipo snack.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Conservas'),
 'Café de altura tostado y molido', 'Café producido en la Sierra Norte.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Conservas'),
 'Salsa macha con chiltepín', 'Salsa macha artesanal elaborada con chiltepín.', TRUE),

-- DULCES TÍPICOS
((SELECT id_bloque FROM bloques WHERE nombre = 'Dulces típicos'),
 'Muéganos artesanales bañados en piloncillo', 'Muéganos tradicionales cubiertos de piloncillo.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Dulces típicos'),
 'Dulces de leche con nuez o macadamia', 'Dulces artesanales elaborados con leche y nueces.', TRUE),

((SELECT id_bloque FROM bloques WHERE nombre = 'Dulces típicos'),
 'Galletas artesanales de manzana', 'Galletas tradicionales elaboradas con manzana.', TRUE);
