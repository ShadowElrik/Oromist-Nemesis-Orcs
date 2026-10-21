# Registro de cambios

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
