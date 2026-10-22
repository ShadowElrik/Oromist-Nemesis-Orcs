# Lore: la Horda de las Farlands

> *"Más allá del borde del mundo no quedan humanos. Por eso vinimos aquí."*

## Origen

Más allá de las **Farlands**, donde el mundo se quiebra en muros de tierra imposibles, viven
los orcos. Durante generaciones cazaron todo lo que había en sus tierras rotas hasta que no
quedó nada. Entonces sus chamanes leyeron en los huesos un camino hacia el interior del
mundo, hacia las tierras blandas donde los humanos construyen casas, plantan trigo y
duermen tranquilos.

La Horda cruzó el borde. Ahora está aquí.

## Lo que los mueve

- **Odian a los humanos.** No es hambre ni conquista: los orcos sienten una aversión antigua
  por los humanos y los cazan como presas. Un humano muerto es un trofeo; un humano que
  escapa es una deuda.
- **Aman la guerra.** Para un orco la guerra no es un medio, es el propósito. Los tambores
  suenan cuando encuentran a un humano, cuando uno de los suyos asciende y cuando uno cae.
  Un orco muerto en combate se convierte en canción.
- **Recuerdan.** Cada orco lleva la cuenta de sus cicatrices, de quién lo hirió, de quién
  huyó y de a quién mató. Por eso un orco puede volver años después, preguntando por ti.

## Sociedad

| Rango | Papel |
|---|---|
| **Soldado** | La masa de la Horda. Anónimos, hasta que hacen algo digno de recordar. |
| **Capitán** | Se gana el nombre matando a un humano o sobreviviendo a la muerte. Lidera partidas de caza. |
| **Caudillo** | Señor de una aldea o de una horda. Su tienda guarda los trofeos de guerra. |
| **Chamán** | Guardián de los huesos y la sangre. Cose a los caídos y los trae de vuelta (por eso algunos orcos "engañan a la muerte"). |

Los epítetos cuentan su historia: *el Cruel*, *Cazahumanos*, *Cruzabordes*, *de las Farlands*,
*el Inmortal* (para quien volvió de la muerte)...

## Aldeas y costumbres

- **La hoguera** es el centro de toda aldea, rodeada de estacas con calaveras.
- **La tienda del caudillo**, de lona roja y negra, guarda su trono de piedra negra y su botín.
- **Comida**: su dieta es casi solo **carne**, que ahúman junto a sus huertos. Cultivan poco
  (papas y betarragas) y solo para acompañar la carne. *"Lindos cultivos. Nosotros preferimos la carne."*
- **Fraguas** donde forjan hachas y espadas, y **torres de vigía** desde donde sus arqueros
  vigilan los caminos.
- **Monturas muertas**: los jinetes orcos cabalgan **caballos zombi**, bestias que no se cansan
  ni sienten miedo, porque ya están muertas.

## Estado en el mod

| Elemento del lore | Estado |
|---|---|
| Origen en las Farlands, odio a los humanos, amor por la guerra | ✅ En diálogos y epítetos |
| Memoria, rangos, engañar a la muerte (chamanes) | ✅ Sistema Némesis |
| Aldeas con hoguera, tienda del caudillo, fragua, torres | ✅ |
| Huertos de papas y betarragas con ahumador | ✅ (0.3.0) |
| **Chamanes con magia** | 🔜 Planeado |
| **Jinetes en caballo zombi** | 🔜 Planeado |

## Planeado

### Chamanes (magia)
Un nuevo arquetipo, `SHAMAN`, que no lucha cuerpo a cuerpo:
- **Fuego verde**: proyectil mágico a distancia (basado en las bolas de fuego vanilla, con partículas propias).
- **Sangre de guerra**: cura o enfurece a los orcos cercanos (efectos de fuerza/regeneración).
- **Hechizo de retorno**: si hay un chamán vivo en la aldea, los capitanes tienen más probabilidad de engañar a la muerte.
- Rasgos posibles: *Maestro del fuego*, *Débil en cuerpo a cuerpo*.

### Jinetes en caballo zombi
- Algunos capitanes y exploradores aparecen **montados en un caballo zombi** (más rápidos en emboscadas de viaje).
- Si matas la montura, el orco desmonta y lo recuerda (*"¡Mi montura! ¿Dónde está mi montura?"*).
- Posible establo en las aldeas.

## Cómo escribir más frases

Las frases están en `src/main/resources/assets/oromist-npc/lang/` (`es_es.json`, `es_cl.json`,
`en_us.json`). Para agregar una, usa el siguiente número libre de su categoría, por ejemplo
`dialogue.oromist-npc.taunt_kill.11`, **en los tres archivos**. No hay que tocar código: el mod
cuenta las frases solo, y las pruebas automáticas avisan si a algún idioma le falta una.

Argumentos disponibles en las frases: `%1$s` jugador, `%2$s` días desde el recuerdo,
`%3$s` lugar (bioma) del recuerdo, `%4$s` veces que ocurrió.

| Categoría | Cuándo se usa |
|---|---|
| `first_meeting` | El orco te ve por primera vez |
| `wounded_by_player` | Lo heriste gravemente antes |
| `fled_from_player` | Huyó de ti antes |
| `player_fled` | Tú huiste de él antes |
| `player_killed_ally` | Mataste a sus compañeros delante de él |
| `ambushed_player` | Ya te emboscó antes |
| `killed_player` / `killed_player_again` | Te mató una vez / varias veces |
| `survived_death` / `survived_death_again` | Volvió de la muerte una vez / varias veces |
| `taunt_kill` | Acaba de matarte |
| `fleeing` | Huye malherido |
| `promoted` | Asciende de rango |
| `ambush_home` / `ambush_mining` / `ambush_travel` | Te embosca en casa / minando / viajando |
| `dying` | Sus últimas palabras |
