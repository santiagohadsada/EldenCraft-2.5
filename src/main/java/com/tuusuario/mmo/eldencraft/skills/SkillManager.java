package com.tuusuario.mmo.eldencraft.skills;

import com.tuusuario.mmo.eldencraft.models.ClassType;
import java.util.HashMap;
import java.util.Map;

import com.tuusuario.mmo.eldencraft.skills.warrior.*;
import com.tuusuario.mmo.eldencraft.skills.mage.*;
import com.tuusuario.mmo.eldencraft.skills.ranger.*;
import com.tuusuario.mmo.eldencraft.skills.paladin.*;
import com.tuusuario.mmo.eldencraft.skills.rogue.*;
import com.tuusuario.mmo.eldencraft.skills.bard.*;
import com.tuusuario.mmo.eldencraft.skills.barbarian.*;
import com.tuusuario.mmo.eldencraft.skills.sorcerer.*;
import com.tuusuario.mmo.eldencraft.skills.warlock.*;
import com.tuusuario.mmo.eldencraft.skills.monk.*;
import com.tuusuario.mmo.eldencraft.skills.druid.*;
import com.tuusuario.mmo.eldencraft.skills.cleric.*;

import com.tuusuario.mmo.eldencraft.skills.gunslinger.*;

public class SkillManager {
    private final Map<String, Skill> skillRegistry = new HashMap<>();

    public SkillManager() {
        // === Guerrero ===
        register(ClassType.WARRIOR, 1, new GolpePoderoso());
        register(ClassType.WARRIOR, 2, new DefensaSolida());
        register(ClassType.WARRIOR, 3, new CargaFuriosa());
        register(ClassType.WARRIOR, 4, new EscudoProtector());
        register(ClassType.WARRIOR, 5, new FuriaGuerrera());
        register(ClassType.WARRIOR, 6, new SaltoCombate());
        register(ClassType.WARRIOR, 7, new RabiaCiega());

        // === Mago ===
        register(ClassType.MAGE, 1, new BolaFuego());
        register(ClassType.MAGE, 2, new EscudoMagico());
        register(ClassType.MAGE, 3, new Teleportacion());
        register(ClassType.MAGE, 4, new LluviaHielo());
        register(ClassType.MAGE, 5, new ExplosionMagica());
        register(ClassType.MAGE, 6, new CuracionMagica());
        register(ClassType.MAGE, 7, new TormentaElectrica());

        // === Explorador ===
        register(ClassType.RANGER, 1, new Rastreo());
        register(ClassType.RANGER, 2, new FlechaPrecisa());
        register(ClassType.RANGER, 3, new Camuflaje());
        register(ClassType.RANGER, 4, new TrampaCaza());
        register(ClassType.RANGER, 5, new VisionNocturna());
        register(ClassType.RANGER, 6, new AtaqueSigiloso());
        register(ClassType.RANGER, 7, new EscapeRapido());

        // === Paladin ===
        register(ClassType.PALADIN, 1, new Curacion());
        register(ClassType.PALADIN, 2, new EscudoSagrado());
        register(ClassType.PALADIN, 3, new GolpeSagrado());
        register(ClassType.PALADIN, 4, new ProteccionDivina());
        register(ClassType.PALADIN, 5, new AuraCuracion());
        register(ClassType.PALADIN, 6, new JuicioDivino());
        register(ClassType.PALADIN, 7, new Resurreccion());

        // === Ladron ===
        register(ClassType.ROGUE, 1, new GolpeSigiloso());
        register(ClassType.ROGUE, 2, new CamuflajeRogue());
        register(ClassType.ROGUE, 3, new TrampaMortal());
        register(ClassType.ROGUE, 4, new Robo());
        register(ClassType.ROGUE, 5, new EscapeRapidoRogue());
        register(ClassType.ROGUE, 6, new AtaqueSorpresa());
        register(ClassType.ROGUE, 7, new Invisibilidad());

        // === Bardo ===
        register(ClassType.BARD, 1, new CancionCuracion());
        register(ClassType.BARD, 2, new MelodiaProteccion());
        register(ClassType.BARD, 3, new RitmoVelocidad());
        register(ClassType.BARD, 4, new Persuasion());
        register(ClassType.BARD, 5, new Ilusion());
        register(ClassType.BARD, 6, new AtaqueMusical());
        register(ClassType.BARD, 7, new Inspiracion());

        // === Barbaro ===
        register(ClassType.BARBARIAN, 1, new GolpeBrutal());
        register(ClassType.BARBARIAN, 2, new FuriaBarbara());
        register(ClassType.BARBARIAN, 3, new CargaSalvaje());
        register(ClassType.BARBARIAN, 4, new RabiaCiegaBarbarian());
        register(ClassType.BARBARIAN, 5, new GolpeDemoledor());
        register(ClassType.BARBARIAN, 6, new ProteccionBarbara());
        register(ClassType.BARBARIAN, 7, new FuriaExtrema());

        // === Hechicero ===
        register(ClassType.SORCERER, 1, new PulsoExpansivo());
        register(ClassType.SORCERER, 2, new DescargaCaotica());
        register(ClassType.SORCERER, 3, new NucleoInestable());
        register(ClassType.SORCERER, 4, new Desfase());
        register(ClassType.SORCERER, 5, new Sobrecarga());
        register(ClassType.SORCERER, 6, new ClonArcano());
        register(ClassType.SORCERER, 7, new Singularidad());

        // === Brujo ===
        register(ClassType.WARLOCK, 1, new MarcaMaldita());
        register(ClassType.WARLOCK, 2, new RoboVida());
        register(ClassType.WARLOCK, 3, new ProyectilWither());
        register(ClassType.WARLOCK, 4, new InvocacionOscura());
        register(ClassType.WARLOCK, 5, new Sacrificio());
        register(ClassType.WARLOCK, 6, new PrisionAlmas());
        register(ClassType.WARLOCK, 7, new Apocalipsis());

        // === Monje ===
        register(ClassType.MONK, 1, new GolpeKi());
        register(ClassType.MONK, 2, new MeditacionRapida());
        register(ClassType.MONK, 3, new PatadaViento());
        register(ClassType.MONK, 4, new FlujoKi());
        register(ClassType.MONK, 5, new DesviacionKi());
        register(ClassType.MONK, 6, new ExplosionKi());
        register(ClassType.MONK, 7, new TranscendenciaKi());

        // === Druida ===
        register(ClassType.DRUID, 1, new EnredaRaices());
        register(ClassType.DRUID, 2, new FormaOso());
        register(ClassType.DRUID, 3, new FrenesiNatural());
        register(ClassType.DRUID, 4, new CuracionNatural());
        register(ClassType.DRUID, 5, new LlamadaAnimales());
        register(ClassType.DRUID, 6, new TormentaEspinas());
        register(ClassType.DRUID, 7, new AvatarPrimigenio());

        // === Clerigo ===
        register(ClassType.CLERIC, 1, new LuzCurativa());
        register(ClassType.CLERIC, 2, new Purificacion());
        register(ClassType.CLERIC, 3, new ZonaSagrada());
        register(ClassType.CLERIC, 4, new BendicionDivina());
        register(ClassType.CLERIC, 5, new IraDivina());
        register(ClassType.CLERIC, 6, new CampoSagrado());
        register(ClassType.CLERIC, 7, new MilagroDivino());

        // === Pistolero ===
        register(ClassType.GUNSLINGER, 1, new DisparoPreciso());
        register(ClassType.GUNSLINGER, 2, new Rafaga());
        register(ClassType.GUNSLINGER, 3, new Rodar());
        register(ClassType.GUNSLINGER, 4, new BalaExplosiva());
        register(ClassType.GUNSLINGER, 5, new MarcaObjetivo());
        register(ClassType.GUNSLINGER, 6, new DisparoPenetrante());
        register(ClassType.GUNSLINGER, 7, new ModoDuelo());
    }

    private void register(ClassType type, int slot, Skill skill) {
        skillRegistry.put(type.name() + "_" + slot, skill);
    }

    public Skill getSkill(ClassType type, int slot) {
        return skillRegistry.get(type.name() + "_" + slot);
    }
}
