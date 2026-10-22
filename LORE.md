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
| **Chamanes con magia** | ✅ (0.4.0) |
| **Jinetes en caballo zombi** | ✅ (0.4.0) |
| Gritos de guerra, liderazgo y lealtad (los capitanes no se delatan) | ✅ (0.4.0) |

## Los chamanes

Los chamanes no luchan cuerpo a cuerpo. Llevan un bastón, una calavera como máscara y una
túnica verde musgo, y se quedan atrás:

- **Fuego verde**: una llama mágica que quema a su objetivo. No prende los bloques: los orcos
  quieren tus casas en pie para saquearlas.
- **Garras de hueso**: si un humano se les acerca, hacen brotar colmillos del suelo.
- **Sangre de guerra**: enardecen a los orcos que luchan cerca (fuerza y regeneración).
- **Hechizo de retorno**: un chamán que ve caer a un capitán puede llevarse su cuerpo y
  coserlo. Por eso cada jefe de aldea tiene un chamán en su tienda.

## Los jinetes

Los jinetes cabalgan caballos zombi y embisten con lanza, se alejan y vuelven a embestir. Patrullan
los caminos: si te emboscan mientras viajas, vendrán a caballo. Si matas su montura, el jinete
sigue a pie... y no lo olvida (*"¡Mi montura! ¿Qué le hiciste a mi montura?"*).

## Información de guerra

Ningún orco muestra sus debilidades. Para saber que tu némesis teme al fuego tienes que
**verlo** en combate, **interrogar** a un soldado malherido (agachado + clic derecho) o leer
unas **notas de guerra**. Los soldados rasos hablan para salvar la vida; los capitanes
prefieren morir. El que habla lo recordará: la humillación pesa más que una herida.

## Los huesos del augur

Los chamanes no escriben: **leen huesos**. En un hueso tallado con runas verdes ven a toda la
Horda: quién manda, quién odia a quién y qué cicatrices lleva cada uno. Un humano que le quita
su hueso a un chamán (o que talla uno con lo que aprende de las notas de guerra) puede leer lo
mismo... aunque solo lo que ya sabe. Los rasgos que nadie le ha contado siguen siendo `???`.

## Ideas para el futuro

- Establo de caballos zombi en las aldeas.
- Más hechizos de chamán y rasgos propios (*Maestro del fuego*).
- Asaltos orcos a aldeas de aldeanos.

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
| `mount_lost` | Acabas de matar su montura |
| `mount_killed` | Le mataste la montura antes |
| `interrogation_talk` | Cede al interrogatorio |
| `interrogation_refuse` | Un capitán se niega a hablar |
| `interrogated` | Lo obligaste a hablar antes |
