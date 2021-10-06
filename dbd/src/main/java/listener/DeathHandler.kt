package listener

import Status
import activeStatus
import clepto.bukkit.B
import clepto.bukkit.Cycle
import killer
import map
import me.func.commons.donate.impl.Corpse
import me.func.commons.donate.impl.KillMessage
import me.func.commons.mod.ModHelper
import me.func.commons.user.Role
import me.func.commons.user.User
import me.func.commons.util.LocalModHelper
import me.func.commons.util.Music
import me.func.commons.util.StandHelper
import murder
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object DeathHandler : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.handle() {
        if (
            entity is CraftPlayer &&
            damager == killer?.player &&
            !(entity as CraftPlayer).hasPotionEffect(PotionEffectType.SPEED) &&
            !(entity as CraftPlayer).hasPotionEffect(PotionEffectType.INVISIBILITY)
        ) {
            val victim = murder.getUser(entity as CraftPlayer)

            if (victim.hearts > 1) {
                victim.hearts--
                (entity as CraftPlayer).addPotionEffect(
                    PotionEffect(
                        PotionEffectType.SPEED,
                        20 * 5,
                        3
                    )
                )
                Music.DBD_RUN.play(victim)
                B.postpone(20 * 15) { Music.DBD_GAME.play(victim) }
            } else {
                B.bc("  §l> §cИгрок §e${victim.player!!.name} §cпал! Идите к нему, чтобы его спасти!")
                ModHelper.sendTitle(victim, "§cВас ранили!\n§eЖдите помощи")
                ModHelper.makeCorpse(victim)

                victim.player!!.gameMode = GameMode.SPECTATOR

                val location = victim.player!!.location.clone().add(0.0, 1.3, 0.0)
                location.pitch = 90f

                Cycle.run(1, 20 * 15) { time ->
                    if (time == 20 * 15 - 1) {
                        kill(victim)
                        return@run
                    }

                    victim.player!!.teleport(location)

                    Bukkit.getOnlinePlayers()
                        .filter {
                            it.isSneaking &&
                                    it.location.distanceSquared(location) < 10 &&
                                    it != killer?.player &&
                                    it.gameMode != GameMode.SPECTATOR
                        }.forEach {
                            victim.hearts = 1
                            victim.player!!.gameMode = GameMode.ADVENTURE
                            victim.player!!.addPotionEffect(
                                PotionEffect(
                                    PotionEffectType.CONFUSION,
                                    20 * 3,
                                    1
                                )
                            )
                            B.bc("  §l> §e${victim.player!!.name} §aспасен благодаря помощи  §e${it.name}")
                            ModHelper.sendTitle(victim, "§cВас ранили!\n§eЖдите помощи")
                            Cycle.exit()
                        }
                }
            }
        } else {
            isCancelled = true
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        if (activeStatus == Status.GAME)
            kill(murder.getUser(player))
    }

    private fun kill(victim: User) {
        if (victim.role != Role.MURDER) {
            if (killer != null && Math.random() < 0.35) {
                B.bc("  > " + killer!!.stat.activeKillMessage.texted(victim.name))
            } else {
                B.bc("  > " + KillMessage.NONE.texted(victim.name))
            }
            ModHelper.sendTitle(victim, "Вас убили!")
            killer?.stat!!.kills++
            killer?.giveMoney(1)

            val location = victim.player!!.location.clone()
            if (victim.stat.activeCorpse != Corpse.NONE)
                StandHelper(location.clone().subtract(0.0, 1.5, 0.0))
                    .marker(true)
                    .invisible(true)
                    .gravity(false)
                    .slot(EnumItemSlot.HEAD, victim.stat.activeCorpse.getIcon())
                    .markTrash()
        }
        victim.sendPlayAgain("§cСмерть!", map)
        victim.player!!.gameMode = GameMode.SPECTATOR
        victim.player!!.inventory.clear()
        victim.role = Role.NONE
        LocalModHelper.updateOnline()

        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.ENTITY_PLAYER_DEATH, 1f, 1f)
        }
    }

}