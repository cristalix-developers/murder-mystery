import dev.implario.kensuke.Session
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import org.bukkit.entity.Player
import java.util.*

class User(session: Session, stat: Stat?) : IBukkitKensukeUser {

    var tempKills = 0
    var role = Role.NONE

    var stat: Stat
    private var player: Player? = null
    override fun setPlayer(p0: Player?) {
        if (p0 != null) {
            player = p0
        }
    }

    override fun getPlayer(): Player? {
        return player
    }

    private var session: Session
    override fun getSession(): Session {
        return session
    }

    init {
        if (stat == null) {
            this.stat = Stat(UUID.fromString(session.userId), 0, 0, 0, 0)
        } else {
            this.stat = stat
        }
        this.session = session
    }
}