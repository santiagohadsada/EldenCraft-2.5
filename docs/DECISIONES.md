# DECISIONES DE DISEÑO — EldenCraft (LOTE 1)

Registro permanente de decisiones técnicas, valores por defecto adoptados y datos pendientes de verificación en el Lote 1.

---

## 1. Decisiones de Diseño Tomadas

### 1.1 Persistencia y Migración de Esquema (F1)
- **Migrador numerado (`SchemaManager`)**: Se implementó con versión en tabla `ec_meta` (`schema_version`).
  - `v1`: Adición de columnas `hp_current` y `hp_max` en `ec_players`.
  - `v2`: Adición de atributos base `base_str`, `base_dex`, `base_con`, `base_int`, `base_wis`, `base_cha` (Standard Array).
  - `v3`: Tablas `ec_spell_slots`, `ec_spells_known`, `ec_spells_prepared`.
- **Copia de seguridad automática**: En SQLite, antes de aplicar cada versión de migración se crea una copia `eldencraft.db.bak.v(N-1)`.
- **Identificadores**: Se preserva el uso estricto de `UUID` en todas las tablas para soporte offline y cambios de nombre de jugador.

### 1.2 PG D&D vs Corazones de Minecraft (SPEC 3.5 / F1)
- **Fuente de verdad**: Los Puntos de Golpe D&D (`dndHpCurrent` y `dndHpMax`) son enteros y residen en `PlayerData`.
- **Barra de corazones visual**: La barra de vida de Minecraft se calcula proporcionalmente (`hearts-proportional: true`) de modo que el porcentaje de corazones coincida con el porcentaje de PG D&D restantes.
- **Conversión de daño**: Se intercepta el daño de entidades en `PlayerListener` con prioridad MONITOR para aplicar la escala configurable `hp.hp-per-heart` (1 corazón MC = 1 PG D&D por defecto).

### 1.3 Asignación de Atributos Base (Standard Array / F2)
- Se adoptó el arreglo estándar SRD: `15, 14, 13, 12, 10, 8`.
- Para evitar bloquear la experiencia inicial del jugador con interfaces complejas en F2, se implementó una asignación automática de prioridad óptima por clase (`LevelUpManager.getStandardArrayForClass`), quedando abierto para que en F4/F6 el DM o el jugador puedan reasignar libremente:
  - **Guerrero/Bárbaro/Paladín**: FUE 15, CON 14, SAB 13, DES 12, CAR 10, INT 8
  - **Mago/Hechicero**: INT 15, SAB 14, CON 13, DES 12, CAR 10, FUE 8
  - **Explorador/Ladrón/Monje**: DES 15, SAB 14, CON 13, FUE 12, INT 10, CAR 8
  - **Bardo/Brujo**: CAR 15, DES 14, CON 13, SAB 12, INT 10, FUE 8
  - **Clérigo/Druida**: SAB 15, CON 14, FUE 13, CAR 12, DES 10, INT 8
  - **Pistolero (Custom)**: DES 15, CON 14, INT 13, SAB 12, FUE 10, CAR 8
- **Compatibilidad con personajes existentes**: Si un jugador antiguo ya tenía personaje creado en la BD pero no tenía atributos base (`base_str == 0`), se le asigna automáticamente el Standard Array al conectarse en `PlayerManager.loadPlayer()`, sin borrar su nivel, oro, misiones ni clase.

### 1.4 Progresión de Experiencia (F2)
- Se añadió la tabla de XP SRD 5.1 (300, 900, 2700, 6500, ...) en `ExperienceManager.XP_THRESHOLDS`.
- Para no romper la progresión de mundos en marcha con la fórmula exponencial anterior (`150 * 1.45^(level-1)`), se creó la clave `experience.use-srd-table: false` (conmutable a `true`).
- Se añadieron avisos de ASI (Mejora de Característica) en los niveles 4, 8, 12, 16 y 19.

### 1.5 Motor de Hechizos Data-Driven (F3)
- **Formato YAML**: Los hechizos se definen en `plugins/EldenCraft/spells/nivel_N.yml` (y subcarpeta `custom/`). Si la carpeta no existe, el plugin extrae los archivos por defecto (`nivel_0.yml`, `nivel_1.yml`, `nivel_3.yml`) con 28 hechizos SRD.
- **Validación estricta**: `SpellValidator` rechaza intentos de lanzamiento de clases sin acceso (ej. Magos intentando lanzar Curar Heridas) y verifica la existencia de espacios de conjuro antes de descontarlos.
- **Concentración activa**: `ConcentrationManager` registra qué hechizo sostiene cada jugador. Al recibir daño, evalúa la salvación de Constitución contra CD = `max(10, daño / 2)`.

### 1.6 Sistema Multi-personajes y Transición Atómica (LOTE 1B / F3B-1)
- **Clave Canónica `character_id`**: Identificador compuesto único `<player_uuid>_<slot>` (1 a 27). Mapea cada personaje a una fila independiente en `ec_characters`.
- **Serialización de Inventario**: `InventorySerializer` utiliza Bukkit/Paper Base64 nativo para `ItemStack[]` (almacén, armadura, mano secundaria y Ender Chest), guardando además coordenadas (x, y, z, yaw, pitch) y GameMode.
- **Transición Atómica y Bloqueo Concurrente**: `CharacterManager` implementa un lock de exclusión mutua (`switchingLock`) por UUID durante el cambio de personaje, garantizando que el personaje saliente guarde su inventario y ubicación antes de cargar el nuevo, eliminando duplicaciones de ítems.
- **Lobby y Seguridad**: Los jugadores sin personaje activo se confinan en el mundo `ec_lobby` en modo Aventura, invulnerables, con hambre congelada y comandos bloqueados salvo `/personajes`, `/menu`, `/personaje`, `/lobby`, `/ayuda`.
- **Papelera de Reciclaje con Gracia de 7 Días**: El borrado (`softDelete`) establece `deleted_at = timestamp`. La casilla muestra un icono de barrera con los días restantes de gracia y permite la recuperación instantánea con un click.
- **Duplicación y Portabilidad**: Se implementó duplicación a nivel 1 en slot libre y exportación/importación en formato JSON (`exportToJson` / `importFromJson`).

### 1.7 Campañas, Roles y Mundos Dinámicos (LOTE 1B / F3B-2)
- **Aislamiento Estructural**: Cada campaña cuenta con su registro en `ec_campaigns`, su mundo dedicado (`ec_camp_<id>`) o compartido, y su tabla de miembros `ec_campaign_members`.
- **Campaña Principal por Defecto**: Se crea automáticamente la campaña `'principal'` (`world`) a la cual migran todos los personajes y datos preexistentes para garantizar 100% de compatibilidad hacia atrás.
- **Roles D&D**: `DM_PRINCIPAL` (dueño de campaña), `CO_DM` (asistente), `JUGADOR` (activo con ficha asignada), `ESPECTADOR` (observador).
- **Códigos de Unión (Join Codes)**: Generador de códigos alfanuméricos aleatorios de 6 caracteres (`c.joinCode`) con comandos `/campana invitar` y `/campana unirse`.
- **Ciclo de Vida de Mundos**: Carga bajo demanda mediante `WorldCreator` y descarga segura con `Bukkit.unloadWorld(w, true)`, evacuando previamente a los jugadores hacia el lobby.

### 1.8 Contexto Aislado y Panel del DM (LOTE 1B / F3B-3)
- **Scoping por Campaña (`CampaignContext`)**: Los mensajes de chat, tiradas de dados (`/roll`), pruebas de habilidad (`/prueba`) y solicitudes de descanso (`/descanso`) se difunden exclusivamente entre los miembros y DMs de la misma campaña.
- **Panel del DM Descentralizado (`/dm`)**: Permisos basados en el rol de campaña (`DM_PRINCIPAL` o `CO_DM`) sin requerir OP global en el servidor.
- **Auditoría y Fichas Offline**: El DM puede inspeccionar fichas completas de jugadores desconectados (`/dm ficha <jugador> [campana]`), aprobar fichas, otorgar XP masiva o individual, y gestionar sesiones de juego (`/dm sesion iniciar|terminar`) registradas en `ec_sessions`, notas en `ec_campaign_notes` e historial en `ec_character_history`.

### 1.9 Correcciones y Funcionalidades LOTE 1C (SPEC 15.1 a 15.6)
- **C1 - Entrada y Aislamiento Total**:
  - `lobby.force-on-join: true` por defecto. Soporte para reentrada automática (`rejoin.auto: true`) que restaura al jugador a su último personaje y campaña activos en caso de estar disponibles y abiertos.
  - Aislamiento visual y social: `campaigns.isolate-visibility: true` oculta a jugadores de distintas campañas tanto en el mundo 3D como en el tablist mediante `player.hidePlayer(plugin, other)`. Los administradores (`isOp()` o `eldencraft.admin`) ven a todos los jugadores.
  - El chat público normal (`AsyncPlayerChatEvent`), scoreboards, bossbars (`BossManager`), parties (`PartyManager`), `/ficha` y `/lanzar` respetan el aislamiento estricto de campaña.
  - Migración v4 verificada: actualización incondicional de `character_id` en las 5 tablas legacy (`ec_spell_slots`, `ec_spells_known`, `ec_spells_prepared`, `ec_completed_missions`, `ec_active_missions`).
  - `/dm xp` procesa subida de nivel completa en jugadores desconectados, recalculando nivel, PG máximos y actuales según DG y CON, persistiendo en `ec_characters` y registrando en `ec_character_history`.
  - Purga automática de personajes soft-deleted con más de 7 días al inicio y cada 24 horas, con registro en `ec_character_history`.
- **C2 - Asistente de Creación Completo (SPEC 15.3)**:
  - Asistente de 11 pasos (`CharacterCreationWizard`): Selección de clase, subclase, método de atributos fijado por la campaña (Matriz estándar, 4d6 descartando el menor o Compra de 27 puntos), asignación de atributos, contador de competencias de habilidad según clase, selección de idiomas, selección de trucos y conjuros iniciales para clases lanzadoras (`isCaster()`), paquete de equipo inicial o equivalente en oro, alineamiento, y pantalla de confirmación/resumen con opción de confirmar o volver a modificar.
  - Renombrado de personaje en el menú de acciones (`CharacterActionsGUI` con prompt interactivo en chat) y vía `/personaje renombrar <slot> <nuevo_nombre>`.
  - Comando `/personaje importar [slot] [json]`.
  - Uso estricto de espacios de conjuro (`resource-mode: SLOTS`) para lanzadores.
- **C3 - Campañas Completas (SPEC 15.4 y 15.5)**:
  - 6 Estados de campaña: `BORRADOR`, `ABIERTA`, `SOLO_INVITADOS`, `EN_PAUSA`, `CERRADA`, `ARCHIVADA`.
  - Invitaciones por nombre de jugador (`/campana invitar <campana> <jugador>`) con mensaje interactivo clicable `[Aceptar]` / `[Rechazar]` si está conectado, o persistencia en `ec_campaign_invites` para que aparezca en su selector al iniciar sesión. Campañas `SOLO_INVITADOS` invisibles e inaccesibles para no invitados.
  - Validación de requisitos al unirse: estado de campaña, nivel mínimo/máximo, razas permitidas, clases permitidas, contenido personalizado y veto (`vetar`).
  - Aprobación y rechazo de ficha con motivo (`/dm rechazar <jugador> <motivo>`).
  - GUI de edición de campaña (`CampaignEditorGUI`) y comandos administrativos: `/campana editar`, `archivar`, `borrar`, `vetar`, `transferir`.
  - Edición de fichas por el DM (`/dm editar <jugador> <campo> <valor>`) con notificación al jugador y log de auditoría en `ec_character_history`.
  - Panel GUI del DM (`DMCampaignPanelGUI`) con lista visual de miembros, estado y opciones rápidas.
  - Carga de mundos bajo demanda al entrar el primer jugador, límite configurable de mundos cargados simultáneamente (`campaigns.max-loaded-worlds`, default 3) y aplicación automática de reglas de mundo (`ec_gamerules`).

---

## 2. Datos Marcados con `# VERIFICAR`

Los siguientes puntos usan valores por defecto funcionales pero deben ser contrastados con el texto oficial del SRD 5.1 cuando el usuario lo disponga:

1. `ExperienceManager.XP_THRESHOLDS`: Confirmar que los umbrales de XP para los niveles 11 a 20 coincidan exactamente con la versión de referencia de la campaña.
2. `ClassType.GUNSLINGER`: Dado de golpe asignado como `d8` (custom, equivalente a Rogue/Monk).
3. `SpellSlotTable.HALF_CASTER_SLOTS`: Confirmar la progresión de espacios de semilanzadoras (Paladín y Explorador) en niveles altos (niveles 17-20).
4. `SpellSlotTable.getWarlockSlots`: Progresión de espacios de Magia de Pacto del Brujo (2 a 4 espacios de nivel 1 a 5).
5. `LevelUpManager.calculateMaxHp`: La regla de incremento de PG en subida de nivel utiliza el promedio redondeado hacia arriba `(hitDie / 2 + 1) + modCON`.
6. `ConcentrationManager.onDamage`: En F3 la salvación de concentración asume competencia no asignada hasta que en F4/F8 se cargue la tabla de salvaciones competentes por clase.

---

## 3. Valores por Defecto Establecidos en `config.yml`
- `resource-mode: SLOTS`
- `legacy-skills.enabled: false`
- `legacy-skills.spell-listener: false`
- `attributes.generation: STANDARD_ARRAY`
- `scale.feet-per-block: 5`
- `spells.components: RELAXED`
- `death.permanent: false`
- `combat.turn-timer: 0`
- `combat.flanking: false`
- `rolls.public: true`
- `performance.profile: normal`
- `hp.dnd-hp-enabled: true`
- `hp.hearts-proportional: true`
- `hp.default-max-hp: 10`
- `hp.hp-per-heart: 1`
