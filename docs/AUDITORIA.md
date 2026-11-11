# AUDITORÍA — EldenCraft LOTE 1
*Generado automáticamente en Paso 0 — LOTE 1*

---

## 1. Paquetes y Clases Existentes

### `com.tuusuario.mmo.eldencraft` (raíz)
| Clase | Estado |
|---|---|
| `EldenCraft.java` | REFACTORIZAR (desactivar SpellListener, aplicar world rules, SchemaManager) |

### `models/`
| Clase | Estado |
|---|---|
| `ClassType.java` | REFACTORIZAR (añadir hitDie, 13 clases confirmadas) |
| `RaceType.java` | CONSERVAR (13 razas) |
| `PlayerData.java` | REFACTORIZAR (añadir base attrs, dndHp, PlayerSpellData) |
| `PlayerManager.java` | REFACTORIZAR (migración de personajes existentes en loadPlayer) |
| `SkillType.java` | No existe como archivo — habilidades directas en sub-paquetes |

### `skills/`
| Clase | Estado |
|---|---|
| `Skill.java` (interfaz) | CONSERVAR |
| `SkillManager.java` | CONSERVAR |
| `barbarian/*.java` (7 habilidades) | CONSERVAR |
| `bard/*.java` (7 habilidades) | CONSERVAR |
| `cleric/*.java` (7 habilidades) | CONSERVAR |
| `druid/*.java` (7 habilidades) | CONSERVAR |
| `gunslinger/*.java` (7 habilidades) | CONSERVAR |
| `mage/*.java` (7 habilidades) | CONSERVAR |
| `monk/*.java` (7 habilidades) | CONSERVAR |
| `paladin/*.java` (7 habilidades) | CONSERVAR |
| `ranger/*.java` (7 habilidades) | CONSERVAR |
| `rogue/*.java` (7 habilidades) | CONSERVAR |
| `sorcerer/*.java` (7 habilidades) | CONSERVAR |
| `warlock/*.java` (7 habilidades) | CONSERVAR |
| `warrior/*.java` (7 habilidades) | CONSERVAR |

### `listeners/`
| Clase | Estado |
|---|---|
| `AbilityListener.java` | CONSERVAR |
| `SpellListener.java` | DESACTIVAR (prueba del palo — condicionada por config) |
| `CombatListener.java` | CONSERVAR |
| `PlayerListener.java` | REFACTORIZAR (añadir EntityDamageEvent para PG D&D) |
| `ExperienceListener.java` | CONSERVAR |
| `MissionProgressListener.java` | CONSERVAR |
| `ClassSelectionGUI.java` | REFACTORIZAR (asignar standard array en creación nueva) |
| `SelectionGUI.java` | CONSERVAR |
| `MissionGUI.java` | CONSERVAR |
| `ShopGUI.java` | CONSERVAR |
| `BossListener.java` | CONSERVAR |
| `BlockSafetyListener.java` | CONSERVAR |
| `MobDifficultyListener.java` | CONSERVAR |
| `SkillsGUI.java` | CONSERVAR |
| `NewClassesListener.java` | CONSERVAR |
| `FlyListener.java` | CONSERVAR |

### `managers/`
| Clase | Estado |
|---|---|
| `DataManager.java` | REFACTORIZAR (integrar SchemaManager, guardar/cargar nuevos campos) |
| `EconomyManager.java` | CONSERVAR |
| `ExperienceManager.java` | REFACTORIZAR (tabla XP SRD, checkLevelUp mejorado) |
| `FlyManager.java` | CONSERVAR |
| `MissionManager.java` | CONSERVAR |
| `PartyManager.java` | CONSERVAR |
| `RewardManager.java` | CONSERVAR |
| `ShopManager.java` | CONSERVAR |
| `BossManager.java` | CONSERVAR |
| `CastManager.java` | CONSERVAR |

### `missions/`
| Clase | Estado |
|---|---|
| `Mission.java` | CONSERVAR |
| `MissionProgress.java` | CONSERVAR |
| `MissionType.java` | CONSERVAR |

### `commands/`
| Clase | Estado |
|---|---|
| `AdminCommand.java` | REFACTORIZAR (reload → reloadAll con messages.yml) |
| `BalanceCommand.java` | CONSERVAR |
| `BossCommand.java` | CONSERVAR |
| `MMOCommand.java` | CONSERVAR |
| `MissionCommand.java` | CONSERVAR |
| `PartyCommand.java` | CONSERVAR |
| `ShopCommand.java` | CONSERVAR |
| `SkillsCommand.java` | CONSERVAR |
| `EldenCraftTabCompleter.java` | CONSERVAR |
| `FichaCommand.java` | NUEVO (F2) |
| `RollCommand.java` | NUEVO (F2) |
| `PruebaCommand.java` | NUEVO (F2) |
| `LanzarCommand.java` | NUEVO (F3) |
| `DescansoCommand.java` | NUEVO (F2) |

### `armor/`
| Subsistema | Estado |
|---|---|
| Todo el subsistema `armor/` | CONSERVAR (sin modificar) |

### `util/`
| Clase | Estado |
|---|---|
| `BalanceState.java` | CONSERVAR |
| `BleedTask.java` | CONSERVAR |
| `EldenCraftPlaceholders.java` | CONSERVAR |
| `GlowEffect.java` | CONSERVAR |
| `SkillDescriptions.java` | CONSERVAR |
| `SorcererState.java` | CONSERVAR |
| `UndeadCheck.java` | CONSERVAR |
| `MessageUtil.java` | NUEVO (F1) |

### `recipes/`
| Clase | Estado |
|---|---|
| `StarterRecipes.java` | CONSERVAR |

---

## 2. Tabla de Habilidades por Clase (91 habilidades totales)

| Clase | Slot 1 | Slot 2 | Slot 3 | Slot 4 | Slot 5 | Slot 6 | Slot 7 |
|---|---|---|---|---|---|---|---|
| Guerrero | CargaFuriosa | DefensaSolida | EscudoProtector | FuriaGuerrera | GolpePoderoso | RabiaCiega | SaltoCombate |
| Mago | BolaFuego | CuracionMagica | EscudoMagico | ExplosionMagica | LluviaHielo | Teleportacion | TormentaElectrica |
| Explorador | AtaqueSigiloso | Camuflaje | EscapeRapido | FlechaPrecisa | Rastreo | TrampaCaza | VisionNocturna |
| Paladín | AuraCuracion | Curacion | EscudoSagrado | GolpeSagrado | JuicioDivino | ProteccionDivina | Resurreccion |
| Ladrón | AtaqueSorpresa | CamuflajeRogue | EscapeRapidoRogue | GolpeSigiloso | Invisibilidad | Robo | TrampaMortal |
| Bardo | (ver archivos) | | | | | | |
| Bárbaro | (ver archivos) | | | | | | |
| Hechicero | ClonArcano | DescargaCaotica | Desfase | NucleoInestable | PulsoExpansivo | Singularidad | Sobrecarga |
| Brujo | Apocalipsis | InvocacionOscura | MarcaMaldita | PrisionAlmas | ProyectilWither | RoboVida | Sacrificio |
| Monje | DesviacionKi | ExplosionKi | FlujoKi | GolpeKi | MeditacionRapida | PatadaViento | TranscendenciaKi |
| Druida | (ver archivos) | | | | | | |
| Clérigo | (ver archivos) | | | | | | |
| Pistolero | BalaExplosiva | DisparoPenetrante | DisparoPreciso | MarcaObjetivo | ModoDuelo | Rafaga | Rodar |

---

## 3. Defectos en plugin.yml, config.yml y SpellListener

### plugin.yml
- **BUG**: Dice "12 clases / 84 habilidades" → debe ser 13 clases / 91 habilidades
- **BUG**: Falta permiso `eldencraft.player` (default: true)
- **BUG**: Falta permiso `eldencraft.dm` (default: op)
- **BUG**: Faltan comandos `/ficha`, `/roll`, `/prueba`, `/lanzar`, `/descanso`

### config.yml (si existe)
- Falta sección `legacy-skills` para controlar SpellListener
- Falta sección `world-rules` para reglas de mundo configurables
- Falta sección `hp` para sistema PG D&D vs corazones

### SpellListener
- Es código de prueba ("prueba del palo") — debe desactivarse con flag `legacy-skills.spell-listener: false`
- Registrado incondicionalmente en `EldenCraft.onEnable()` — **CORREGIR**

---

## 4. Estado de la Base de Datos

### Tablas actuales
| Tabla | Columnas |
|---|---|
| `ec_players` | uuid, race, class, level, xp, gold, fatigue |
| `ec_completed_missions` | uuid, mission_id |
| `ec_active_missions` | uuid, mission_id, progress |

### Tablas a crear (via SchemaManager)
| Tabla/Cambio | Versión | Descripción |
|---|---|---|
| `ec_meta` | v0 (bootstrap) | Tabla de metadatos con schema_version |
| Columnas `hp_current`, `hp_max` en `ec_players` | v1 | PG D&D |
| Columnas `base_str..base_cha` en `ec_players` | v2 | Atributos base standard array |
| `ec_spell_slots` | v3 | Espacios de conjuro por jugador |
| `ec_spells_known` | v3 | Hechizos conocidos |
| `ec_spells_prepared` | v3 | Hechizos preparados |

---

## 5. Dependencias Externas

| Dependencia | Tipo | Versión | Obligatoria |
|---|---|---|---|
| Paper API | Compilación | 1.21.1-R0.1-SNAPSHOT | Sí |
| PlaceholderAPI | Runtime opcional | 2.11.5 | No (hook condicional) |
| SQLite JDBC | Incluida en Paper | — | Sí (via Paper) |
| MySQL JDBC | Incluida en Paper | — | No (solo si database.type=MYSQL) |
| JUnit 5 | Test | 5.10.1 | No (solo tests) |

---

## 6. Riesgos de Migración

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Columnas nuevas en `ec_players` (hp, attrs) | MEDIO — jugadores existentes tendrán valores 0 | SchemaManager v1/v2 con ALTER TABLE; migrate en loadPlayer |
| SpellListener desactivado | BAJO — era prueba del palo | Flag config, alias de compatibilidad |
| Tabla XP SRD diferente a la fórmula exponencial actual | MEDIO — jugadores pueden subir/bajar de nivel | Mantener fórmula existente como fallback; SRD como alternativa configurable |
| Nuevos paquetes vacíos | NINGUNO | package-info.java solo para estructura |
| Backup de eldencraft.db antes de migrar | ALTO — pérdida de datos si falla | SchemaManager hace copia automática `.db.bak.vN` |
| PlayerData: standard array sobrescribe stats legacy | MEDIO — mecánica legacy usa race+class bonuses | Campos base separados; getEffective* preserva legado |

---

## 7. Riesgos de Re-asociación de Datos (LOTE 1B: uuid -> character_id)

| Riesgo Identificado | Nivel de Impacto | Estrategia de Mitigación / Requisito |
|---|---|---|
| **Pérdida o desvinculación de progreso del Lote 1** (hechizos, slots, misiones, oro) | **CRÍTICO** | Migración v4 transaccional e idempotente. Mapear cada registro en `ec_players` al slot 1 de su UUID (`character_id = uuid + "_1"` o UUID determinista). Re-asociar tablas a `character_id` preservando los registros existentes. |
| **Fallo en migración a mitad de ejecución** | **ALTO** | Backup automático de `eldencraft.db` a `eldencraft.db.bak.v3` antes de ejecutar DDL/DML. Transacción con rollback en caso de error SQL. |
| **Ejecución repetida de la migración** (doble migración o re-arranque) | **ALTO** | Idempotencia estricta: `INSERT OR IGNORE` / comprobación de existencia previa de `character_id`. No duplicar personajes existentes en el slot 1. |
| **Corrupción o pérdida de inventarios/ender chests** al alternar personajes | **CRÍTICO** | Serialización binaria segura de Paper (`ItemStack.serializeAsBytes()` / BukkitObjectOutputStream) en tabla `ec_character_inventory`. Bloqueo atómico de cambio de personaje para evitar dupes. |
| **Jugadores huérfanos sin campaña** | **ALTO** | Creación automática de la campaña legacy "Principal" vinculada al mundo por defecto (`world`), asignando a todos los jugadores migrados como miembros y a los poseedores de `eldencraft.dm` como DMs. |
| **Comandos del Lote 1 que asumían UUID directo** (`/ficha`, `/lanzar`, etc.) | **MEDIO** | Adaptación de `PlayerData` para representar el personaje activo actual (`activeCharacterId`) en memoria, de forma transparente para los comandos existentes. |

### 7.1 Estado de Verificación Post-Implementación (Lote 1B)
- **Migración v4 y v5**: Verificada e idempotente mediante tests JUnit 5 en base de datos SQLite en memoria (`CharacterMigrationTest`).
- **Backup automático**: Confirmado antes de cada paso de migración.
- **Lock Atómico de Personajes**: Implementado en `CharacterManager` con mapa concurrente de UUIDs en tránsito.
- **Aislamiento de Campaña**: `CampaignContext` filtra chats, tiradas y descansos. Panel DM (`/dm`) validado.

---

## 8. Auditoría y Verificación de Diferencias (LOTE 1C: SPEC 15.1 - 15.6)

### 8.1 Hallazgos y Correcciones Aplicadas
1. **Entrada y Aislamiento (C1)**:
   - Configuración: `lobby.force-on-join` fijado en `true` por defecto. `rejoin.auto` implementado en `LobbyListener` para reconectar al personaje y campaña activos si están disponibles.
   - Visibilidad y Tablist: `campaigns.isolate-visibility: true` oculta recíprocamente a jugadores de diferentes campañas con `Player.hidePlayer(plugin, other)`. Administradores (`eldencraft.admin` o `isOp()`) mantienen visibilidad total.
   - Aislamiento ampliado a: Chat público global (`AsyncPlayerChatEvent`), Bossbars de campaña (`BossManager`), Party system (`PartyManager`), Scoreboards de campaña, `/ficha <jugador>` (solo visible para DMs de su campaña o administradores) y `/lanzar` (bloqueado entre campañas).
   - Verificación de Migración v4: Test unitario `testV4MigrationFillsCharacterIdInAllLegacyTables` confirma la propagación de `character_id` a todas las 5 tablas legacy.
   - Subida de Nivel Offline: `/dm xp` recalcula y aplica la subida de nivel a jugadores desconectados, guardando en `ec_characters` y registrando en `ec_character_history`.
   - Purga de personajes borrados: Tarea periódica cada 24 horas y al arrancar para eliminar registros marcados como borrados hace más de 7 días, registrando la auditoría en `ec_character_history`.

2. **Asistente de Creación y Personalización (C2)**:
   - Asistente por etapas (`CharacterCreationWizard`): 11 fases cubriendo subclases, contador de competencias de habilidad según clase, selección de idiomas SRD, trucos y conjuros iniciales para lanzadores, paquete de equipo inicial u oro, alineamiento, y menú de confirmación/revisión.
   - Métodos de generación de atributos: Integración de `STANDARD_ARRAY`, `ROLL_4D6_DROP_LOWEST` y `POINT_BUY_27` seleccionables por la campaña.
   - Gestión de Nombres e Importación: Opción de renombrar en `CharacterActionsGUI` y comando `/personaje renombrar <slot> <nuevo_nombre>`. Soporte de importación vía `/personaje importar [slot] [json]`.
   - Lanzamiento de conjuros: Uso de `resource-mode: SLOTS` verificado en lanzadores.

3. **Campañas Dinámicas y Control de DM (C3)**:
   - Estados de Campaña: `BORRADOR`, `ABIERTA`, `SOLO_INVITADOS`, `EN_PAUSA`, `CERRADA`, `ARCHIVADA`. Las campañas `SOLO_INVITADOS` se ocultan a los no invitados.
   - Sistema de Invitaciones: Invitaciones persistentes en `ec_campaign_invites` y mensajes clicables por chat (`TextComponent` con botones `[Aceptar]` / `[Rechazar]`).
   - Validación de requisitos: Restricción de acceso por nivel, raza, clase, contenido custom o veto (`vetar`).
   - Flujo de Aprobación de Fichas: Aprobación del DM y rechazo justificado mediante `/dm rechazar <jugador> <motivo>`.
   - Herramientas de Edición y Panel DM: `CampaignEditorGUI`, `/campana editar`, `archivar`, `borrar`, `vetar`, `transferir`, `/dm editar <jugador> <campo> <valor>` y dashboard GUI `DMCampaignPanelGUI`.
   - Gestión Inteligente de Mundos: Carga bajo demanda al entrar el primer jugador, límite máximo de mundos en memoria (`campaigns.max-loaded-worlds`, default 3) y aplicación automática de reglas de juego de mundo.
