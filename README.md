# Oromist: Nemesis Orcs

Mod de Fabric para **Minecraft 26.3** que añade orcos con un **sistema Némesis** al estilo
*Shadow of Mordor*: los orcos te recuerdan, ascienden cuando te vencen, engañan a la muerte,
te emboscan en tu casa, en la mina o de viaje, y viven en aldeas propias.

## Qué incluye

| Característica | Detalle |
|---|---|
| **Combate** | Espadachines (espada), hacheros (hacha), arqueros (arco) y ballesteros (ballesta). La IA cambia de táctica según el arma que empuñan, incluso si la cambian en pleno combate. |
| **Alturas distintas** | Cada orco tiene su propia escala (por defecto 0.85×–1.35×), con más vida si es más grande y voz más grave. |
| **Rangos** | Soldado → Capitán → Caudillo. Suben al matar jugadores, sobrevivir a la muerte o hacerte huir. Capitanes y caudillos tienen nombre propio ("Gorbag el Cruel") y mejor equipo. |
| **Rasgos** | Fortalezas (Bruto, Veloz, Piel gruesa, Inmune a flechas, Berserker) y debilidades (Teme al fuego, Cobarde, Frágil). |
| **Memoria** | Cada orco notable recuerda qué pasó contigo, dónde y hace cuántos días: si te mató, si huiste, si lo heriste, si mataste a sus compañeros, si volvió de la muerte... |
| **Diálogos** | Al verte, el orco habla según su recuerdo más importante: *«¿Creías que me habías matado en Taiga? ¡Mira esta cicatriz, Steve!»*. En español e inglés. |
| **Engañar a la muerte** | Un capitán (35 %) o caudillo (50 %) "muerto" puede volver más tarde, con el epíteto *el Inmortal*. |
| **Emboscadas** | Periódicamente, según lo que hagas: **en casa** (cerca de tu cama), **minando** (bajo tierra) o **viajando**. Suelen estar lideradas por tu némesis. |
| **Aldeas orcas** | Hoguera central, tienda del jefe (con un capitán con nombre), chozas, barracón, fragua, torres de vigía y tótems. Rareza igual a las aldeas vanilla (spacing 34, separation 8). |
| **Aparición natural** | Pequeñas partidas nocturnas en los biomas orcos. |

### Comandos

- `/nemesis` — lista los orcos que te odian; el primero (☠) es tu némesis.
- `/nemesis ambush <home|mining|travel>` — (operadores) fuerza una emboscada para probar.
- Para encontrar una aldea: `/locate structure oromist-npc:orc_village`.

### Configuración

`config/oromist-npc.json` se crea en el primer arranque: probabilidades de emboscada por
actividad, tiempo entre emboscadas, probabilidad de engañar a la muerte, rango de alturas, etc.

## Arquitectura

El código está separado por responsabilidades y las dependencias apuntan hacia el
**dominio**, que es Java puro (sin clases de Minecraft):

```
com.oromist
├── OromistNPC                 Raíz de composición: crea y conecta todo (único lugar con "new" de servicios)
├── nemesis
│   ├── domain                 ★ Java puro: OrcProfile, Memory, OrcRank, OrcTrait, NemesisService,
│   │                            NemesisRepository (puerto), TraitRoller, OrcNameGenerator, NemesisListener
│   ├── persistence            Adaptador de guardado: NemesisSavedData (SavedData del mundo) + codecs
│   └── game                   Adaptadores del juego: eventos de combate, hooks del orco, anuncios
├── dialogue                   ★ Java puro: DialogueSelector (estrategia), MemoryDialogueSelector
│   └── game                   ChatDialogueSpeaker: muestra las frases en el chat
├── entity                     OrcEntity, equipamiento (OrcLoadouts), rasgos → efectos (TraitEffects)
│   └── ai                     WeaponCombatStrategy (arco/ballesta/cuerpo a cuerpo), retirada
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
| Frases de diálogo | Añadir `dialogue.oromist-npc.<categoría>.<n>` en `lang/*.json` y subir el número en `MemoryDialogueSelector.VARIANTS`. |
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
./gradlew build         # genera el .jar en build/libs/
```

Prueba rápida: en creativo, usa el huevo de orco (pestaña de huevos), pasa a supervivencia,
deja que un orco te mate y vuelve a buscarlo: te recordará. O usa `/nemesis ambush home`.

## Licencia

CC0-1.0 (heredada de la plantilla). Las texturas incluidas son originales y sustituibles.
