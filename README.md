# Oromist: Nemesis Orcs

Mod de Fabric para **Minecraft 26.3** que añade orcos con un **sistema Némesis** al estilo
*Shadow of Mordor*: los orcos te recuerdan, ascienden cuando te vencen, engañan a la muerte,
te emboscan en tu casa, en la mina o de viaje, y viven en aldeas propias.

> Los orcos llegaron desde más allá de las **Farlands**. Odian a los humanos, aman la guerra
> y nunca olvidan. El trasfondo completo y lo que está planeado (chamanes con magia, jinetes
> en caballos zombi) está en [`LORE.md`](LORE.md).

## Qué incluye

| Característica | Detalle |
|---|---|
| **Combate** | Espadachines (espada), hacheros (hacha), arqueros (arco), ballesteros (ballesta), **jinetes** (lanza, en caballo zombi) y **chamanes** (magia). La IA cambia de táctica según el arma que empuñan, incluso si la cambian en pleno combate. |
| **Chamanes** | Lanzan *fuego verde* (no quema bloques), hacen brotar *garras de hueso* si te acercas, dan fuerza y regeneración a los orcos que luchan cerca y hacen más probable que un capitán caído vuelva. |
| **Jinetes** | Cabalgan caballos zombi y embisten con lanza. Escoltan las emboscadas de viaje. Si les matas la montura, lo recuerdan. |
| **Combate en grupo** | *Grito de guerra* que alerta a los orcos cercanos, *liderazgo* (los soldados cerca de un capitán pegan más fuerte y no huyen) y *flanqueo* (rodean al objetivo). |
| **Tablero de la Horda** | Con el **hueso del augur** abres un menú con los orcos que conoces, por rango: arma, rasgos descubiertos, rencor y recuerdos. Clic en un orco para ver su ficha completa. Funciona también en servidores sin nada extra en el cliente. |
| **Información de guerra** | Los rasgos de cada orco están ocultos hasta que los descubres: viéndolos en combate, **interrogando** a un soldado malherido (agachado + clic derecho) o leyendo **notas de guerra**. |
| **Alturas distintas** | Cada orco tiene su propia escala (por defecto 0.85×–1.35×), con más vida si es más grande y voz más grave. |
| **Rangos** | Soldado → Capitán → Caudillo. Suben al matar jugadores, sobrevivir a la muerte o hacerte huir. Capitanes y caudillos tienen nombre propio ("Gorbag el Cruel") y mejor equipo. |
| **Rasgos** | Fortalezas (Bruto, Veloz, Piel gruesa, Inmune a flechas, Berserker) y debilidades (Teme al fuego, Cobarde, Frágil). |
| **Memoria** | Cada orco notable recuerda qué pasó contigo, dónde y hace cuántos días: si te mató, si huiste, si lo heriste, si mataste a sus compañeros, si volvió de la muerte... |
| **Diálogos** | Al verte, el orco habla según su recuerdo más importante: *«¿Creías que me habías matado en Taiga? ¡Mira esta cicatriz, Steve!»*. En español e inglés. |
| **Engañar a la muerte** | Un capitán (35 %) o caudillo (50 %) "muerto" puede volver más tarde, con el epíteto *el Inmortal*. |
| **Emboscadas** | Periódicamente, según lo que hagas: **en casa** (cerca de tu cama), **minando** (bajo tierra) o **viajando**. Suelen estar lideradas por tu némesis. |
| **Aldeas orcas** | Hoguera central, tienda del jefe (con un capitán con nombre), chozas, barracón, fragua, huertos, torres de vigía y tótems. Rareza igual a las aldeas vanilla (spacing 34, separation 8). |
| **Aparición natural** | Pequeñas partidas nocturnas en los biomas orcos. |

### Comandos

- **Hueso del augur** (clic derecho): abre el tablero de la Horda. Lo sueltan los chamanes y se
  fabrica con hueso + notas de guerra + saco de tinta brillante.
- `/nemesis` — lista los orcos que te odian; el primero (☠) es tu némesis. Los rasgos que aún no
  descubriste aparecen como `???`.
- `/nemesis player <jugador>` — (operadores) lo mismo para otro jugador.
- `/nemesis ambush <home|mining|travel>` — (operadores) fuerza una emboscada para probar.
- Para encontrar una aldea: `/locate structure oromist-npc:orc_village`.

### Configuración

`config/oromist-npc.json` se crea en el primer arranque: probabilidades de emboscada por
actividad, tiempo entre emboscadas, probabilidad de engañar a la muerte, rango de alturas, etc.

Opciones pensadas para servidores multijugador:

| Opción | Por defecto | Qué hace |
|---|---|---|
| `ambushGraceMinutes` | 30 | Minutos jugados que necesita cada jugador antes de su primera emboscada. |
| `ambushMaxNearbyOrcs` | 8 | No hay emboscada si ya hay esa cantidad de orcos cerca (grupos de jugadores, aldeas). |
| `ambushDimensions` | `["minecraft:overworld"]` | Dimensiones donde puede haber emboscadas. |
| `dialogueCooldownSeconds` | 90 | Espera mínima entre frases de un mismo orco a un mismo jugador. |
| `shamanCheatDeathBonus` | 0.2 | Probabilidad extra de que un capitán vuelva de la muerte si un chamán lo vio caer. |

## Multijugador

- El mod se instala **en el servidor y en todos los clientes** (registra una entidad nueva).
- La memoria de los orcos y la configuración viven solo en el servidor.
- Cada jugador tiene su propio némesis; un mismo orco puede odiar a varios jugadores.
- Cada jugador tiene su propia información de guerra: lo que descubre uno no lo sabe el otro.
- Un némesis que está activo en el mundo (por ejemplo, peleando con otro jugador) no
  aparece en la emboscada de otro jugador.
- Los soldados rasos desaparecen al alejarse los jugadores, como los mobs normales; su
  memoria se conserva y pueden volver. Capitanes y caudillos persisten siempre.
- Se conservan como máximo 256 soldados rasos y 64 orcos muertos en la memoria del
  mundo; los capitanes y caudillos nunca se descartan.

## Arquitectura

El código está separado por responsabilidades y las dependencias apuntan hacia el
**dominio**, que es Java puro (sin clases de Minecraft):

```
com.oromist
├── OromistNPC                 Raíz de composición: crea y conecta todo (único lugar con "new" de servicios)
├── nemesis
│   ├── domain                 ★ Java puro: OrcProfile, Memory, OrcRank, OrcTrait, NemesisService,
│   │                            IntelService (rasgos descubiertos), NemesisRepository (puerto),
│   │                            TraitRoller, OrcNameGenerator, NemesisListener
│   ├── persistence            Adaptador de guardado: NemesisSavedData (SavedData del mundo) + codecs
│   └── game                   Adaptadores del juego: eventos de combate, hooks del orco, anuncios,
│                                interrogatorios y notas de guerra (IntelEvents)
├── horde                      ★ Java puro: HordeBoard (qué sabe el jugador) y HordeBoardLayout (dónde va cada cosa)
│   └── game                   Menú tipo cofre (HordeBoardMenu), iconos y el hueso del augur
├── dialogue                   ★ Java puro: DialogueSelector (estrategia), MemoryDialogueSelector
│   └── game                   ChatDialogueSpeaker: muestra las frases en el chat
├── entity                     OrcEntity, equipamiento (OrcLoadouts), rasgos → efectos (TraitEffects),
│   │                            monturas (OrcMounts), combate en grupo (OrcSquad)
│   ├── ai                     WeaponCombatStrategy (arco/ballesta/bastón/lanza/cuerpo a cuerpo),
│   │                            hechizos del chamán, flanqueo, retirada
│   └── magic                  Hechizos del chamán (ShamanSpells) y el fuego verde (GreenFireball)
├── ambush                     ActivityDetector (casa/minería/viaje), SpawnLocator, AmbushDirector
├── worldgen                   Estructura de aldea, planificador, pieza genérica y edificios
│   └── buildings              Un archivo por edificio (solo describen bloques)
├── registry                   Registro de entidades, objetos y estructuras
├── command                    /nemesis
└── config                     Configuración JSON
```

### Principios SOLID aplicados

- **Responsabilidad única**: `OrcEntity` solo se comporta y se dibuja; las decisiones de memoria
  y ascensos están en `NemesisService`; el chat en `ChatDialogueSpeaker`; los edificios solo
  describen bloques y `OrcBuildingPiece` se ocupa de rotar, recortar por chunk y guardar.
- **Abierto/cerrado**: se añaden armas, rasgos, edificios, actividades o reacciones sin modificar
  las clases que los usan (listas de estrategias y catálogos).
- **Sustitución de Liskov**: todas las implementaciones de `WeaponCombatStrategy`, `ActivityDetector`,
  `SpawnLocator`, `OrcBuilding` o `DialogueSelector` son intercambiables.
- **Segregación de interfaces**: interfaces pequeñas (`OrcHooks`, `NemesisListener`, `BuildContext`)
  con métodos por defecto, para implementar solo lo necesario.
- **Inversión de dependencias**: `NemesisService` depende de `NemesisRepository`, no de `SavedData`;
  `OrcEntity` depende de `OrcHooks`, no del sistema Némesis.

## Cómo extender

| Quiero añadir… | Dónde |
|---|---|
| Un arma nueva (p. ej. lanza) | Nueva `WeaponCombatStrategy` y añadirla a `WeaponCombatStrategy.DEFAULTS`; el arma en `OrcLoadouts`. |
| Un rasgo | Valor en `OrcTrait` + su efecto en `TraitEffects` + traducción `trait.oromist-npc.*`. |
| Frases de diálogo | Añadir `dialogue.oromist-npc.<categoría>.<n>` en los tres `lang/*.json` (ver `LORE.md`); no hace falta tocar código. |
| Un hechizo de chamán | Un método en `ShamanSpells` y decidir cuándo lanzarlo en `ShamanSpellGoal` (o en una meta nueva). |
| Un arquetipo | Valor en `OrcArchetype` (con su `CombatStyle`), su arma en `OrcLoadouts` y su peso de aparición. |
| Un edificio | Clase que implemente `OrcBuilding`, registrarla en `OrcBuildings` y pedirla en `VillageLayoutPlanner`. |
| Una situación de emboscada | Valor en `PlayerActivity` + un `ActivityDetector` + un `SpawnLocator`. |
| Una reacción a ascensos/muertes | Implementar `NemesisListener` y registrarlo en `NemesisRuntime`. |
| Cambiar biomas o rareza de aldeas | Solo datos: `data/oromist-npc/tags/worldgen/biome/...` y `worldgen/structure_set/orc_villages.json`. |

## Portar a otras versiones de Minecraft

- **Sin cambios**: `nemesis.domain` y `dialogue` (Java puro), las traducciones y la lógica de los edificios.
- **Revisar**: `entity`, `ambush`, `worldgen`, `registry`, `client` y `persistence`, que usan la API del juego.
  Los puntos de 26.x más propensos a cambiar ya están aislados: colores de bloques en
  `worldgen/buildings/Palette` (`Blocks.WOOL.red()`), `SavedDataType`, `ValueInput/ValueOutput`,
  los `RenderState` del cliente y los permisos de comandos.

## Compilar y probar

```
./gradlew runClient     # abre el juego con el mod
./gradlew test          # ejecuta las pruebas automáticas
./gradlew build         # ejecuta las pruebas y genera el .jar en build/libs/
```

Hay dos tipos de pruebas automáticas, y GitHub Actions ejecuta ambas en cada push:

- **Pruebas unitarias** (`src/test`, JUnit): las reglas del sistema Némesis, la información de
  guerra, los diálogos (incluido que toda frase exista en todos los idiomas), el guardado
  tolerante a datos corruptos y la política de retención de perfiles.
- **Pruebas dentro del juego** (`src/gametest`, Fabric GameTest): arrancan un servidor de
  Minecraft real y comprueban que los jinetes montan (y que su montura desaparece con ellos),
  que el fuego verde no quema bloques, las garras de hueso, *Sangre de guerra*, el grito de
  guerra, el liderazgo y el interrogatorio.

Prueba rápida: en creativo, usa el huevo de orco (pestaña de huevos), pasa a supervivencia,
deja que un orco te mate y vuelve a buscarlo: te recordará. O usa `/nemesis ambush home`.

## Licencia

CC0-1.0 (heredada de la plantilla). Las texturas incluidas son originales y sustituibles.
