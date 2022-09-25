package net.playl.uuidwarp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Main extends JavaPlugin {
    private static final Path advancements = Bukkit.getWorld("world").getWorldFolder().toPath().resolve("advancements");
    private static final Path playerdata = Bukkit.getWorld("world").getWorldFolder().toPath().resolve("playerdata");
    private static final Path stats = Bukkit.getWorld("world").getWorldFolder().toPath().resolve("stats");

    public void onEnable() {
        Bukkit.broadcast(Component.text("重要: 正在修改全部玩家文件名称UUID为盗版!!!", NamedTextColor.RED));
        Arrays.asList(Bukkit.getOfflinePlayers()).forEach(p -> {
            Component userName = Component.text(p.getName(), NamedTextColor.GOLD);
            String uuid = p.getUniqueId().toString();
            String offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + p.getName()).getBytes(StandardCharsets.UTF_8)).toString();
            Bukkit.broadcast(Component.text("正在修改玩家: ", NamedTextColor.YELLOW).append(userName).append(Component.text("原UUID: " + uuid)));
            try {
                Bukkit.broadcast(Component.text("状态(adv, playerdata, playerdataold, stats): "
                        + Files.move(advancements.resolve(uuid + ".json"), advancements.resolve(offlineUuid + ".json"))
                        + Files.move(playerdata.resolve(uuid + ".dat"), playerdata.resolve(offlineUuid + ".dat"))
                        + Files.move(playerdata.resolve(uuid + ".dat_old"), playerdata.resolve(offlineUuid + ".dat_old"))
                        + Files.move(stats.resolve(uuid + ".json"), stats.resolve(offlineUuid + ".json"))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Bukkit.broadcast(Component.text("现已完成UUID的更改, 服务器关闭!!!", NamedTextColor.GREEN));
        Bukkit.getServer().shutdown();
    }

    public void onDisable() {
    }

}