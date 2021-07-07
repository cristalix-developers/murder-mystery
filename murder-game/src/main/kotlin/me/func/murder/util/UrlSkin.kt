package me.func.murder.util

import org.apache.commons.codec.digest.DigestUtils
import java.io.IOException
import java.net.URL
import java.util.*

object SkitSet {
    val skins = arrayListOf(
        "307264a1-2c69-11e8-b5ea-1cb72caa35fd",
        "6f3f4a2e-7f84-11e9-8374-1cb72caa35fd",
        "e7c13d3d-ac38-11e8-8374-1cb72caa35fd",
        "303dc644-2c69-11e8-b5ea-1cb72caa35fd",
        "3044712b-2c69-11e8-b5ea-1cb72caa35fd",
        "303c31eb-2c69-11e8-b5ea-1cb72caa35fd",
        "bf30a1df-85de-11e8-a6de-1cb72caa35fd",
        "ee476051-dc55-11e8-8374-1cb72caa35fd",
        "303c1f40-2c69-11e8-b5ea-1cb72caa35fd",
        "9040f086-77b9-11eb-acca-1cb72caa35fd",
        "e42389ef-dc51-11e8-8374-1cb72caa35fd",
        "327cac64-8b1e-11e8-a6de-1cb72caa35fd",
        "308380a9-2c69-11e8-b5ea-1cb72caa35fd",
        "34a8936a-6c06-11e9-8374-1cb72caa35fd",
        "842e9e31-9b33-11e8-a6de-1cb72caa35fd",
        "11998811-2c5e-11e9-8374-1cb72caa35fd",
        "f12a63a0-ca64-11e9-80c4-1cb72caa35fd",
        "30586ea4-2c69-11e8-b5ea-1cb72caa35fd",
        "59228ee8-21f0-11e9-8374-1cb72caa35fd",
        "306ffa24-2c69-11e8-b5ea-1cb72caa35fd",
        "30ae09d3-2c69-11e8-b5ea-1cb72caa35fd",
        "303a9ed7-2c69-11e8-b5ea-1cb72caa35fd",
        "303e64de-2c69-11e8-b5ea-1cb72caa35fd",
        "30974e4a-2c69-11e8-b5ea-1cb72caa35fd",
    ).map { UrlSkinData(UUID.fromString(it)) }
}

class UrlSkinData(uuid: UUID) {
    val url: String = "https://webdata.c7x.dev/textures/skin/$uuid"
    val digest: String = getDigest(url)

    companion object {
        private fun getDigest(url: String): String {
            return url.substringAfter('-')
        }
    }
}