# Registro de cambios

## 0.5.1 — Los clanes de la Horda

### Añadido
- **24 pieles nuevas** (27 en total), organizadas en **8 clanes**, cada uno con su color de piel,
  ojos, pelo y tela: *Sangre*, *Pálidos*, *Arena*, *Brea*, *Escarcha*, *Barro*, *Musgo* e *Hierro*.
- Dentro de cada clan los orcos se distinguen por **peinado** (cresta, rapado, moño, melena),
  **pintura de guerra** (franja, mandíbula de calavera, huella de mano, rayas, media cara),
  **tatuajes**, **aros** y **cicatrices**.
- `tools/orc_skins.py`: el generador de las pieles, para retocar o añadir clanes.
- Vista previa de los clanes en `docs/clanes.png`.

### Compatibilidad
- Las 3 pieles originales no cambian, y los orcos guardados conservan su aspecto. Solo los
  perfiles creados con la 0.1.0 pueden cambiar de piel.

## 0.5.0 — El tablero de la Horda

### Añadido
- **Hueso del augur** (objeto nuevo): los chamanes leen los huesos para conocer a la Horda. Al
  usarlo (clic derecho) se abre el **tablero de la Horda**, un menú tipo cofre con los orcos que
  conoces agrupados en caudillos, capitanes y soldados:
  - Cada orco aparece con su arma; tu némesis, con una calavera ☠. Capitanes y caudillos brillan.
  - Al pasar el ratón: rango, arquetipo, rasgos (los no descubiertos como `???`), rencor, veces que
    volvió de la muerte y sus recuerdos más importantes ("Te mató en Taiga (hace 3 días)").
  - Al hacer clic sobre un orco: su ficha completa y todos sus recuerdos en el chat.
  - No se gasta y no se puede sacar nada del tablero.
- **Cómo conseguirlo**: los chamanes con nombre siempre lo sueltan al morir de verdad, y los chamanes
  rasos a veces (25 %). También se fabrica con **hueso + notas de guerra + saco de tinta brillante**.
- El tablero también muestra a los capitanes que aún no te conocen pero de los que descubriste algo
  (por ejemplo, con notas de guerra).

### Cambiado
- El botín especial al morir (notas de guerra y hueso del augur) se decide en una clase propia, `OrcTrophies`.

## 0.4.0 — Chamanes, jinetes e información de guerra

### Añadido
- **Chamanes orcos** (nuevo arquetipo). Llevan bastón, calavera y túnica, y no luchan cuerpo a cuerpo:
  - *Fuego verde*: proyectil mágico que hiere y quema al objetivo, pero **nunca prende fuego a los bloques**.
  - *Garras de hueso*: si te acercas demasiado, brotan colmillos del suelo hacia ti.
  - *Sangre de guerra*: fuerza y regeneración para los orcos cercanos que están luchando.
  - *Hechizo de retorno*: si un chamán ve caer a un capitán o caudillo, este tiene +20 % de
    probabilidad de engañar a la muerte (`shamanCheatDeathBonus`). Se oyen cánticos.
  - Siempre hay uno en la tienda del jefe de cada aldea.
- **Jinetes orcos** (nuevo arquetipo) con lanza, a lomos de caballos zombi. Embisten y se
  reposicionan como los jinetes vanilla. Escoltan las emboscadas de viaje y a veces descansan
  junto a la hoguera de la aldea. Si matas su montura, te lo dirán... y lo recordarán.
- **Información de guerra**: los rasgos de los orcos ahora están **ocultos** (`???` en `/nemesis`)
  hasta que los descubres:
  - *En combate*: al ver un rasgo en acción (tus flechas apenas le hacen daño, el fuego lo destroza, huye...).
  - *Interrogando*: agáchate y haz clic derecho sobre un soldado malherido. Delatará un rasgo de
    otro orco (preferiblemente una debilidad de tu némesis) y huirá. Los capitanes se niegan.
  - *Notas de guerra*: objeto nuevo. Lo sueltan a veces los orcos y siempre los capitanes que
    mueren de verdad. Se lee con clic derecho.
  - Cada jugador tiene su propia información, que se guarda con el mundo.
- **Combate en grupo**:
  - *Grito de guerra*: el primer orco que te ve avisa a los orcos ociosos en 20 bloques.
  - *Liderazgo*: un capitán o caudillo cerca da a los soldados +25 % de daño y les impide huir.
  - *Flanqueo*: los orcos cuerpo a cuerpo rodean al objetivo en lugar de hacer fila detrás de un aliado.
- **Nuevos recuerdos y frases**: *te mató la montura* y *lo obligaste a hablar* (con su propio
  rencor), más frases para perder la montura, hablar bajo interrogatorio o negarse (25 frases nuevas).
- **Pruebas dentro del juego** (Fabric GameTest, `src/gametest`): se ejecutan en un servidor real
  en cada `./gradlew build` y comprueban monturas, hechizos, grito de guerra, liderazgo e interrogatorio.

### Cambiado
- Los orcos se consideran aliados entre sí: sus hechizos y garras no dañan a otros orcos ni a sus monturas.
- Los jinetes rasos desaparecen por distancia junto con su montura (vanilla nunca hace
  desaparecer a un mob montado).

### Compatibilidad
- Los mundos de versiones anteriores cargan sin problemas: los orcos existentes empiezan con
  todos sus rasgos ocultos.

## 0.3.0 — Lore de la Horda de las Farlands

### Añadido
- **Lore** (`LORE.md`): los orcos vienen de más allá de las Farlands, odian a los humanos,
  aman la guerra, viven de la carne y tienen chamanes y jinetes de caballos zombi.
- **42 frases nuevas** en español e inglés con ese trasfondo (132 en total).
- **8 epítetos nuevos**: *de las Farlands*, *Cruzabordes*, *Jinete Muerto*, *Cazahumanos*,
  *Tambor de Guerra*, *el Chamán*, *Señor de la Carne*, *el Belicoso*.
- **Huerto orco** en las aldeas: papas y betarragas regadas por un canal, con ahumador para la carne.

### Cambiado
- Ya no hay que actualizar números en el código al agregar frases: el mod cuenta las frases
  de cada categoría leyendo el archivo de idioma. Nuevas pruebas comprueban que los tres
  idiomas tengan las mismas frases y todos los epítetos.

## 0.2.0 — Revisión para multijugador

### Corregido
- **Datos corruptos ya no borran la memoria de los orcos.** Un perfil, rasgo o recuerdo inválido
  (por ejemplo, de una versión futura) se descarta y se registra en el log, sin perder el resto.
  Antes, un valor desconocido podía lanzar una excepción al cargar el mundo.
- **"El jugador huyó" nunca se detectaba.** La IA vanilla suelta al objetivo a 35 bloques, antes
  de los 40 que se exigían. Ahora se comprueba justo cuando el orco pierde al objetivo.
- **Emboscadas en el Nether aparecían en el techo de bedrock.** Ahora solo hay emboscadas en las
  dimensiones de `ambushDimensions` (por defecto, el mundo normal).
- **Emboscadores sobre tejados o dentro de bases.** Ahora solo aparecen sobre terreno natural.
- **Un némesis podía "teletransportarse"** a la emboscada de otro jugador en mitad de una pelea.
  Ahora los orcos activos en el mundo quedan excluidos.
- **Acumulación de orcos y perfiles en servidores de larga duración.** Los soldados rasos con
  memoria vuelven a desaparecer por distancia como cualquier mob, y la memoria del mundo se
  limita (256 soldados vivos y 64 muertos; capitanes y caudillos nunca se descartan).
- **Jugadores recién llegados a un servidor antiguo eran emboscados de inmediato.** La gracia
  ahora es por jugador (`ambushGraceMinutes`, minutos jugados).
- **Emboscadas apiladas** cuando varios jugadores están juntos: no hay emboscada si ya hay
  `ambushMaxNearbyOrcs` orcos cerca.
- **Spam de diálogos** en peleas de grupo: un mismo orco espera 15 s entre saludos.
- **Un orco que huía desaparecía delante de otros jugadores.** Ahora solo "escapa" si nadie lo ve.
- **La piel de un orco cambiaba** al volverse notable. Ahora se guarda en su perfil.
- Pequeñas fugas de memoria en mapas internos (enfriamientos, posiciones de jugadores).

### Añadido
- `/nemesis player <jugador>` para operadores. `/nemesis` muestra como máximo 10 orcos.
- Botín de los orcos: carne podrida, huesos y cuero.
- Las aldeas orcas ya no se generan pegadas a aldeas vanilla.
- Pruebas automáticas con JUnit, ejecutadas en cada build.

## 0.1.0
- Versión inicial.
