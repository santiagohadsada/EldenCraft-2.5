# MIGRACIÓN DE HABILIDADES — EldenCraft (D&D 5e SRD)

Este documento clasifica las 91 habilidades existentes (13 clases × 7 slots) según su correspondencia con rasgos de clase SRD 5.1, hechizos SRD o mecánicas personalizadas.

---

## 1. Clases Lanzadoras (Sustituidas por Hechizos SRD / Legacy)
Las habilidades nativas de estas clases pasan a la categoría `legacy` (activables opcionalmente con `legacy-skills.enabled: true` en `config.yml`, por defecto `false`). El sistema estándar de lanzamiento utiliza la biblioteca de conjuros SRD con espacios de conjuro (`resource-mode: SLOTS`).

| Clase | Slot | Habilidad Original | Tipo en EldenCraft D&D | Mapeo SRD / Destino |
|---|---|---|---|---|
| **Mago** | 1 | BolaFuego | Hechizo SRD | Sustituido por *Bola de Fuego* (Nivel 3) |
| Mago | 2 | EscudoMagico | Hechizo SRD | Sustituido por *Escudo* (Nivel 1, Reacción) |
| Mago | 3 | Teleportacion | Hechizo SRD | Sustituido por *Paso Brumoso* (Nivel 2) / *Puerta Dimensional* |
| Mago | 4 | LluviaHielo | Hechizo SRD | Sustituido por *Tormenta de Hielo* (Nivel 4) |
| Mago | 5 | ExplosionMagica | Hechizo SRD | Sustituido por *Proyectil Mágico* (Nivel 1) |
| Mago | 6 | CuracionMagica | Legacy / Personalizada | Mago no cura en SRD 5.1; conservado como legacy |
| Mago | 7 | TormentaElectrica | Hechizo SRD | Sustituido por *Relámpago* (Nivel 3) / *Cadena de Relámpagos* |
| **Hechicero** | 1 | PulsoExpansivo | Hechizo SRD | Sustituido por *Onda Atronadora* (Nivel 1) |
| Hechicero | 2 | DescargaCaotica | Hechizo SRD | Sustituido por *Saeta Caótica* (Nivel 1) |
| Hechicero | 3 | NucleoInestable | Rasgo Personalizado | Mapeado a Magia Salvaje / Sobrecarga |
| Hechicero | 4 | Desfase | Rasgo SRD | Mapeado a Metamagia: Conjuro Sutil / Desvanecimiento |
| Hechicero | 5 | Sobrecarga | Rasgo SRD | Mapeado a Puntos de Hechicería: Crear Espacios |
| Hechicero | 6 | ClonArcano | Hechizo SRD | Sustituido por *Imagen Múltiple* (Nivel 2) |
| Hechicero | 7 | Singularidad | Hechizo SRD | Sustituido por *Desintegrar* (Nivel 6) |
| **Brujo** | 1 | MarcaMaldita | Hechizo SRD | Sustituido por *Maldición* (Hex, Nivel 1) |
| Brujo | 2 | RoboVida | Rasgo SRD | Mapeado a *Bendición del Oscuro* (Patrón Infernal) |
| Brujo | 3 | ProyectilWither | Hechizo SRD | Sustituido por *Rayo Enfermizo* (Nivel 1) |
| Brujo | 4 | InvocacionOscura | Rasgo SRD | Mapeado a *Invocaciones Ocultas: Pacto de la Cadena* |
| Brujo | 5 | Sacrificio | Personalizada | Conservado como rasgo temático de pacto |
| Brujo | 6 | PrisionAlmas | Rasgo SRD | Mapeado a *Resiliencia de las Sombras* |
| Brujo | 7 | Apocalipsis | Hechizo SRD | Sustituido por *Dedo de la Muerte* (Nivel 7) |
| **Clérigo** | 1 | LuzCurativa | Hechizo SRD | Sustituido por *Palabra Curativa* (Nivel 1) |
| Clérigo | 2 | Purificacion | Hechizo SRD | Sustituido por *Purificar Comida y Bebida* / *Restablecimiento Menor* |
| Clérigo | 3 | ZonaSagrada | Hechizo SRD | Sustituido por *Santuario* (Nivel 1) |
| Clérigo | 4 | BendicionDivina | Hechizo SRD | Sustituido por *Bendición* (Nivel 1) |
| Clérigo | 5 | IraDivina | Rasgo SRD | Mapeado a *Canalizar Divinidad: Ira Divina* |
| Clérigo | 6 | CampoSagrado | Hechizo SRD | Sustituido por *Faro de Esperanza* (Nivel 3) |
| Clérigo | 7 | MilagroDivino | Rasgo SRD | Mapeado a *Intervención Divina* (Nivel 10+) |
| **Druida** | 1 | EnredaRaices | Hechizo SRD | Sustituido por *Enmarañar* (Nivel 1) |
| Druida | 2 | FormaOso | Rasgo SRD | Mapeado a *Forma Salvaje* (Wild Shape, Nivel 2) |
| Druida | 3 | FrenesiNatural | Hechizo SRD | Sustituido por *Crecimiento de Espinas* (Nivel 2) |
| Druida | 4 | CuracionNatural | Hechizo SRD | Sustituido por *Curar Heridas* (Nivel 1) |
| Druida | 5 | LlamadaAnimales | Hechizo SRD | Sustituido por *Conjurar Animales* (Nivel 3) |
| Druida | 6 | TormentaEspinas | Hechizo SRD | Sustituido por *Viento Huracanado* / *Tormenta de Granizo* |
| Druida | 7 | AvatarPrimigenio | Personalizada | Forma Salvaje Primigenia (Warden) conservada como contenido único |
| **Bardo** | 1 | CancionCuracion | Rasgo SRD | Mapeado a *Canción de Descanso* (Song of Rest) |
| Bardo | 2 | MelodiaProteccion| Rasgo SRD | Mapeado a *Contracanto* (Countercharm) |
| Bardo | 3 | RitmoVelocidad | Rasgo SRD | Mapeado a *Inspiración Bárdica* (Bardic Inspiration) |
| Bardo | 4 | Persuasion | Rasgo SRD | Mapeado a *Pericia en Persuasión* (Expertise) |
| Bardo | 5 | Ilusion | Hechizo SRD | Sustituido por *Ilusión Menor* (Truco) / *Imagen Silenciosa* |
| Bardo | 6 | AtaqueMusical | Hechizo SRD | Sustituido por *Burla Dañina* (Truco) |
| Bardo | 7 | Inspiracion | Rasgo SRD | Mapeado a *Fuente de Inspiración* |
| **Paladín** | 1 | Curacion | Rasgo SRD | Mapeado a *Imposición de Manos* (Lay on Hands) |
| Paladín | 2 | EscudoSagrado | Rasgo SRD | Mapeado a *Aura de Protección* |
| Paladín | 3 | GolpeSagrado | Rasgo SRD | Mapeado a *Castigo Divino* (Divine Smite) |
| Paladín | 4 | ProteccionDivina | Rasgo SRD | Mapeado a *Aura de Valentía* |
| Paladín | 5 | AuraCuracion | Rasgo SRD | Mapeado a *Salud Divina* |
| Paladín | 6 | JuicioDivino | Rasgo SRD | Mapeado a *Canalizar Divinidad: Juicio Sagrado* |
| Paladín | 7 | Resurreccion | Hechizo SRD | Sustituido por *Revivir* (Nivel 3) |
| **Explorador** | 1 | Rastreo | Rasgo SRD | Mapeado a *Explorador Natural* (Natural Explorer) |
| Explorador | 2 | FlechaPrecisa | Rasgo SRD | Mapeado a *Estilo de Combate: Disparo* |
| Explorador | 3 | Camuflaje | Rasgo SRD | Mapeado a *Mimetismo* (Hide in Plain Sight) |
| Explorador | 4 | TrampaCaza | Personalizada | Habilidad de trampero conservada como rasgo de subclase |
| Explorador | 5 | VisionNocturna | Rasgo SRD | Mapeado a *Sentidos Primitivos* |
| Explorador | 6 | AtaqueSigiloso | Rasgo SRD | Mapeado a *Zancada Terrenal* |
| Explorador | 7 | EscapeRapido | Rasgo SRD | Mapeado a *Desvanecerse* (Vanish) |

---

## 2. Clases No Lanzadoras (Conservan Habilidades como Rasgos de Clase)
Estas clases conservan sus habilidades activas, reclasificadas con recarga por descanso corto/largo o gasto de recurso de clase (Ki, usos de Furia, usos por descanso).

| Clase | Slot | Habilidad | Equivalente D&D 5e | Recarga / Recurso |
|---|---|---|---|---|
| **Guerrero** | 1 | GolpePoderoso | Estilo de Combate: Gran Arma | Pasiva / 1 uso por turno |
| Guerrero | 2 | DefensaSolida | Estilo Defensivo | Pasiva (+1 CA) |
| Guerrero | 3 | CargaFuriosa | Arremetida (Personalizada) | Energía / Descanso Corto |
| Guerrero | 4 | EscudoProtector | Proteger (Fighting Style: Protection) | Reacción |
| Guerrero | 5 | FuriaGuerrera | Oleada de Acción (Action Surge) | 1 uso por Descanso Corto |
| Guerrero | 6 | SaltoCombate | Movilidad Atlética (Personalizada) | Energía |
| Guerrero | 7 | RabiaCiega | Indomable (Indomitable) | 1 uso por Descanso Largo |
| **Bárbaro** | 1 | GolpeBrutal | Crítico Brutal (Brutal Critical) | Pasiva al asestar crítico |
| Bárbaro | 2 | FuriaBarbara | Furia (Rage) | Usos por día (Descanso Largo) |
| Bárbaro | 3 | CargaSalvaje | Movimiento Rápido | Pasiva (+10 pies velocidad) |
| Bárbaro | 4 | RabiaCiegaBarbarian | Furia Implacable (Relentless Rage) | Usos por Descanso Corto |
| Bárbaro | 5 | GolpeDemoledor | Ataque Temerario (Reckless Attack) | A voluntad en tu turno |
| Bárbaro | 6 | ProteccionBarbara | Defensa sin Armadura (Unarmored Defense) | Pasiva (10 + DES + CON) |
| Bárbaro | 7 | FuriaExtrema | Furia Persistente (Persistent Rage) | Pasiva (nivel 15+) |
| **Ladrón** | 1 | GolpeSigiloso | Ataque Furtivo (Sneak Attack) | 1 vez por turno (arma sutil) |
| Ladrón | 2 | CamuflajeRogue | Acción Astuta: Ocultarse | Acción adicional |
| Ladrón | 3 | TrampaMortal | Trampas Rápidas (Personalizada) | Energía |
| Ladrón | 4 | Robo | Juego de Manos Rápido (Fast Hands) | Acción adicional |
| Ladrón | 5 | EscapeRapidoRogue | Acción Astuta: Destrabarse/Correr | Acción adicional |
| Ladrón | 6 | AtaqueSorpresa | Emboscada de Ladrón / Asesinar | Primer turno de combate |
| Ladrón | 7 | Invisibilidad | Evasión Asombrosa / Paso Fantasmal | Reacción / Descanso Corto |
| **Monje** | 1 | GolpeKi | Ráfaga de Golpes (Flurry of Blows) | 1 Punto de Ki |
| Monje | 2 | MeditacionRapida | Defensa Paciente (Patient Defense) | 1 Punto de Ki |
| Monje | 3 | PatadaViento | Paso del Viento (Step of the Wind) | 1 Punto de Ki |
| Monje | 4 | FlujoKi | Puntos de Ki (Ki Reserve) | Recarga en Descanso Corto |
| Monje | 5 | DesviacionKi | Desviar Proyectiles (Deflect Missiles) | Reacción (Ki si reduce a 0) |
| Monje | 6 | ExplosionKi | Golpe Aturdidor (Stunning Strike) | 1 Punto de Ki |
| Monje | 7 | TranscendenciaKi | Cuerpo Vacío / Diamante del Alma | 4 Puntos de Ki |
| **Pistolero** | 1 | DisparoPreciso | Disparo Certero (Trick Shot: Deadeye) | Puntos de Agallas (Grit) |
| Pistolero | 2 | Rafaga | Fuego Rápido (Burst Fire) | Energía |
| Pistolero | 3 | Rodar | Rodar Táctico (Tactical Roll) | Reacción |
| Pistolero | 4 | BalaExplosiva | Munición Explosiva (Explosive Shot) | 1 punto de Agallas |
| Pistolero | 5 | MarcaObjetivo | Marca de Cazador de Recompensas | Acción Adicional |
| Pistolero | 6 | DisparoPenetrante | Disparo Perforante (Piercing Shot) | 1 punto de Agallas |
| Pistolero | 7 | ModoDuelo | Duelo al Mediodía (High Noon Stance) | 1 uso por Descanso Corto |
