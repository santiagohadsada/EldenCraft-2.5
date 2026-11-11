package com.tuusuario.mmo.eldencraft.util;

import com.tuusuario.mmo.eldencraft.skills.Skill;

import java.util.HashMap;
import java.util.Map;

// las descripciones de cada habilidad pa el menu /skills
// Descripciones de las habilidades para el menú /skills
// La clave corresponde al nombre de la clase Java (getClass().getSimpleName())
public final class SkillDescriptions {

    private static final Map<String, String> MAP = new HashMap<>();

    static {
        // === MAGO ===
        MAP.put("BolaFuego",        "Lanza una bola de fuego que explota al impacto. No destruye bloques. Nivel 1.");
        MAP.put("EscudoMagico",     "Genera un escudo arcano que absorbe daño y te envuelve en un aura azul. Nivel 3.");
        MAP.put("Teleportacion",    "Te teletransportas hasta el bloque al que apuntas (máx. 15 bloques). Nivel 6.");
        MAP.put("LluviaHielo",      "Invoca una lluvia helada que daña y ralentiza a todos los enemigos cercanos. Nivel 9.");
        MAP.put("ExplosionMagica",  "Detona una onda mágica devastadora a tu alrededor. Nivel 12.");
        MAP.put("CuracionMagica",   "Restaura una gran cantidad de vida consumiendo maná puro. Nivel 16.");
        MAP.put("TormentaElectrica","Invoca rayos sobre todos los enemigos en 10 bloques. Nivel 20.");

        // === GUERRERO ===
        MAP.put("GolpePoderoso",    "Concentra tu fuerza para potenciar tu próximo ataque con daño extra. Nivel 1.");
        MAP.put("DefensaSolida",    "Adoptas una postura defensiva que reduce el daño recibido. Nivel 3.");
        MAP.put("CargaFuriosa",     "Te impulsas hacia adelante a gran velocidad derribando enemigos. Nivel 6.");
        MAP.put("EscudoProtector",  "Genera un escudo de absorción física que bloquea daño entrante. Nivel 9.");
        MAP.put("FuriaGuerrera",    "Postura de Combate: inmunidad absoluta al daño durante 8 segundos. Nivel 12.");
        MAP.put("SaltoCombate",     "Salto explosivo que daña a todos los enemigos al aterrizar. Nivel 16.");
        MAP.put("RabiaCiega",       "Contraataque: refleja el 50% del daño recibido durante 12 segundos. Nivel 20.");

        // === EXPLORADOR ===
        MAP.put("Rastreo",          "Agudizas tus sentidos: velocidad aumentada y mejor percepción. Nivel 1.");
        MAP.put("FlechaPrecisa",    "Dispara una flecha crítica de gran daño ignorando parte de la defensa. Nivel 3.");
        MAP.put("Camuflaje",        "Compañero Lobo: invoca 2 lobos aliados que combaten a tu lado durante 30s. Nivel 6.");
        MAP.put("TrampaCaza",       "Trampa Natural: coloca una trampa que atrapa al primer hostil cercano (30s). Nivel 9.");
        MAP.put("VisionNocturna",   "Concédete visión nocturna prolongada para ver en la oscuridad. Nivel 12.");
        MAP.put("AtaqueSigiloso",   "Marca del Cazador: revela con brillo a todos los hostiles en 20 bloques (30s). Nivel 16.");
        MAP.put("EscapeRapido",     "Salto del Ciervo: gran salto + velocidad + caída suave. Nivel 20.");

        // === PALADIN ===
        MAP.put("Curacion",         "Te curas instantáneamente con luz divina. Nivel 1.");
        MAP.put("EscudoSagrado",    "Crea un escudo sagrado que absorbe daño y te rodea de un aura dorada. Nivel 3.");
        MAP.put("GolpeSagrado",     "Libera energía divina en área dañando a todos los enemigos cercanos. Nivel 6.");
        MAP.put("ProteccionDivina", "Otorga resistencia al daño durante 10 segundos. Nivel 9.");
        MAP.put("AuraCuracion",     "Emites un aura sanadora que cura lentamente a todos los aliados cercanos. Nivel 12.");
        MAP.put("JuicioDivino",     "Castiga con rayos divinos a todos los enemigos a tu alrededor. Nivel 16.");
        MAP.put("Resurreccion",     "Tótem Divino: durante 5 minutos, el siguiente golpe mortal será cancelado y curará al 50% de HP. Nivel 20.");

        // === LADRON ===
        MAP.put("GolpeSigiloso",    "Apuñalas a un enemigo desde las sombras causando daño crítico. Nivel 1.");
        MAP.put("CamuflajeRogue",   "Te ocultas entre las sombras volviéndote invisible. Nivel 3.");
        MAP.put("TrampaMortal",     "Coloca una trampa explosiva en el suelo que daña a quien la active. Nivel 6.");
        MAP.put("Robo",             "Toxina Corrosiva: lanza veneno al enemigo más cercano (Veneno II + Wither I + Lentitud II). Nivel 9.");
        MAP.put("EscapeRapidoRogue","Velocidad extrema para alejarte del combate rápidamente. Nivel 12.");
        MAP.put("AtaqueSorpresa",   "Ataque devastador por la espalda que multiplica el daño infligido. Nivel 16.");
        MAP.put("Invisibilidad",    "Invisibilidad total prolongada. Atacar o recibir daño la rompe. Nivel 20.");

        // === BARDO ===
        MAP.put("CancionCuracion",  "Canta una melodía curativa que restaura vida a todos los aliados cercanos. Nivel 1.");
        MAP.put("MelodiaProteccion","Melodía protectora que reduce el daño recibido por los aliados cercanos. Nivel 3.");
        MAP.put("RitmoVelocidad",   "Ritmo vibrante que otorga velocidad aumentada a ti y tus aliados. Nivel 6.");
        MAP.put("Persuasion",       "Aturde a enemigos cercanos (lentitud + debilidad). Nivel 9.");
        MAP.put("Ilusion",          "Crea 3 clones tuyos durante 8s. Los enemigos atacan los clones. Nivel 12.");
        MAP.put("AtaqueMusical",    "Lanza notas musicales hostiles que dañan a todos los enemigos cercanos. Nivel 16.");
        MAP.put("Inspiracion",      "Inspira a tus aliados otorgándoles fuerza y resistencia temporales. Nivel 20.");

        // === BARBARO ===
        MAP.put("GolpeBrutal",      "Asesta un golpe demoledor al enemigo más cercano con gran fuerza bruta. Nivel 1.");
        MAP.put("FuriaBarbara",     "Entra en furia: fuerza y haste aumentados durante 15s. Tu cuerpo brilla rojo. Nivel 3.");
        MAP.put("CargaSalvaje",     "Te lanzas hacia adelante con fuerza salvaje embistiendo a todo a tu paso. Nivel 6.");
        MAP.put("RabiaCiegaBarbarian","Rabia Ciega: estado berserker con fuerza y velocidad extremas. Aura roja. Nivel 9.");
        MAP.put("GolpeDemoledor",   "Detona una onda expansiva mágica devastadora. No destruye bloques. Nivel 12.");
        MAP.put("ProteccionBarbara","Tu piel se endurece: resistencia al daño durante 15 segundos. Nivel 16.");
        MAP.put("FuriaExtrema",     "Furia definitiva: fuerza, velocidad y regeneración máximas. Brillo rojo intenso. Nivel 20.");

        // === HECHICERO ===
        MAP.put("PulsoExpansivo",   "Onda arcana que repele y daña a todos los mobs en 6 bloques. Pasiva: Reacción Arcana (5 golpes recibidos → onda de área). Nivel 1.");
        MAP.put("DescargaCaotica",  "Descarga mágica que salta entre hasta 4 enemigos cercanos, reduciendo daño en cada salto. Nivel 3.");
        MAP.put("NucleoInestable",  "Crea un núcleo mágico inestable que explota en 3 segundos causando gran daño en área. Nivel 6.");
        MAP.put("Desfase",          "Intangibilidad: resistencia, velocidad e invisibilidad durante 3 segundos. Nivel 9.");
        MAP.put("Sobrecarga",       "Sobrecarga tu siguiente habilidad para ejecutarla dos veces. Nivel 12.");
        MAP.put("ClonArcano",       "Invoca un Clon Arcano que ataca enemigos automáticamente con gran daño mágico. Cooldown: 120s. Nivel 16.");
        MAP.put("Singularidad",     "Singularidad gravitacional: atrae a todos los mobs en 12 bloques hacia ti y los daña. Nivel 20.");

        // === BRUJO ===
        MAP.put("MarcaMaldita",     "Marca al mob más cercano con Wither, Debilidad y Lentitud. Daño inicial. Pasiva: Pacto Oscuro (sin maná→consume vida). Nivel 1.");
        MAP.put("RoboVida",         "Drena vida de todos los mobs cercanos en 6 bloques y te cura el 50% del daño. Nivel 3.");
        MAP.put("ProyectilWither",  "Lanza una Cabeza de Wither que no destruye bloques. Nivel 6.");
        MAP.put("InvocacionOscura", "Invoca 2 Espíritus Oscuros (lobos) aliados que sirven durante 2 minutos. No dañan aliados. Nivel 9.");
        MAP.put("Sacrificio",       "Ofrenda tu propia sangre (-2 corazones) a cambio de Fuerza III + Resistencia II + Regen (10s). Nivel 12.");
        MAP.put("PrisionAlmas",     "Atrapa a todos los mobs en 7 bloques: los levita, ralentiza, debilita y daña. Nivel 16.");
        MAP.put("Apocalipsis",      "Desata 3 oleadas de daño masivo sobre todos los enemigos cercanos con rayos. Nivel 20.");

        // === MONJE ===
        MAP.put("GolpeKi",          "Golpe de Ki concentrado en área cuerpo a cuerpo. Pasiva: mano vacía→+daño y velocidad; espada→sin daño; hacha→daño mínimo. Nivel 1.");
        MAP.put("MeditacionRapida", "Flujo de Ki: velocidad, resistencia y regeneración temporales. Nivel 3.");
        MAP.put("PatadaViento",     "Patada explosiva que lanza al enemigo más cercano y lo ralentiza. Nivel 6.");
        MAP.put("FlujoKi",          "Curación interior: restaura vida y elimina efectos negativos. Nivel 9.");
        MAP.put("DesviacionKi",     "Escudo de Ki: resistencia máxima + absorción durante 3 segundos. Nivel 12.");
        MAP.put("ExplosionKi",      "Explosión de Ki en área: daña y lanza a todos los mobs en 5 bloques. Nivel 16.");
        MAP.put("TranscendenciaKi", "Transcendencia: velocidad IV, fuerza III, salto IV y regeneración durante 15s. Nivel 20.");

        // === DRUIDA ===
        MAP.put("EnredaRaices",     "Las raíces de la tierra enredan a todos los mobs en 7 bloques (lentitud extrema). Nivel 1.");
        MAP.put("FormaOso",         "Adoptas Forma Oso durante 5 minutos: vida extra, fuerza y resistencia. Nivel 3.");
        MAP.put("FrenesiNatural",   "Tótem de Vida: Invoca un tótem curativo persistente por 15s que cura en área periódicamente. Nivel 6.");
        MAP.put("CuracionNatural",  "Regeneración natural del bosque: cura HP gradualmente y elimina venenos. Nivel 9.");
        MAP.put("LlamadaAnimales",  "Llama a 3 Lobos Druidas aliados que te protegen durante 2 minutos. Nivel 12.");
        MAP.put("TormentaEspinas",  "Tormenta de espinas: daña a mobs cercanos y activa Thorns en tu armadura (10s). Nivel 16.");
        MAP.put("AvatarPrimigenio", "Te transformas en el Avatar del Warden (2:30 min): Fuerza II, Resistencia III y Sonic Boom cada 3s que daña en área. Nivel 20.");

        // === CLERIGO ===
        MAP.put("LuzCurativa",      "Curación directa potenciada por la falta de vida del objetivo. Pasiva: Fe Absoluta. Nivel 1.");
        MAP.put("Purificacion",     "Elimina todos los efectos negativos del clérigo (hasta 9 tipos). Nivel 3.");
        MAP.put("ZonaSagrada",      "Zona sagrada de curación en área durante 10s que cura aliados y otorga resistencia. Nivel 6.");
        MAP.put("BendicionDivina",  "Bendición divina a ti y tus aliados cercanos: Fuerza + Resistencia + Regeneración (10s). Nivel 9.");
        MAP.put("IraDivina",        "Rayos sagrados sobre todos los enemigos: daño x2.5 contra no-muertos. Nivel 12.");
        MAP.put("CampoSagrado",     "Campo sagrado persistente (10s): cura aliados y daña a todos los mobs cercanos por segundo. Nivel 16.");
        MAP.put("MilagroDivino",    "Curación masiva a todos los aliados en 12 bloques + Regen + Absorción. El mayor milagro divino. Nivel 20.");

        // === PISTOLERO ===
        MAP.put("DisparoPreciso",   "Dispara una bala certera que hace gran daño de un solo tiro. Nivel 1.");
        MAP.put("Rodar",            "Una rápida voltereta que te otorga velocidad e invulnerabilidad por un segundo. Nivel 3.");
        MAP.put("BalaExplosiva",    "Carga una bala explosiva en el recamar. Tu próximo disparo explotará al impactar. Nivel 6.");
        MAP.put("DisparoPenetrante","Dispara un proyectil que atraviesa entidades dañando a múltiples objetivos. Nivel 9.");
        MAP.put("MarcaObjetivo",    "Marca a un enemigo en tu línea de visión, haciendo que reciba más daño durante 10s. Nivel 12.");
        MAP.put("Rafaga",           "Vacía tu cargador disparando múltiples balas rápidamente en abanico. Nivel 16.");
        MAP.put("ModoDuelo",        "Entras en Modo Duelo (15s): Velocidad aumentada, ataques más rápidos, pero bloquea curaciones. Nivel 20.");
    }

    private SkillDescriptions() {}

    public static String get(Skill skill) {
        if (skill == null) return "Sin descripción.";
        String key = skill.getClass().getSimpleName();
        return MAP.getOrDefault(key, "Sin descripción.");
    }
}
