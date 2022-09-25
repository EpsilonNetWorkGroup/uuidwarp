package net.playl.uuidwarp;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private final HttpClient client = HttpClient.newHttpClient();

    public void onEnable() {
        getLogger().warning("[重要]不得在生产环境中使用, 使用前必须备份, 一分钟后执行...");
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            final Path advancements = Bukkit.getWorld("world").getWorldFolder().toPath().resolve("advancements");
            final Path playerdata = Bukkit.getWorld("world").getWorldFolder().toPath().resolve("playerdata");
            final Path stats = Bukkit.getWorld("world").getWorldFolder().toPath().resolve("stats");
            Bukkit.broadcast(Component.text("重要: 正在修改全部玩家文件名称UUID为盗版!!!", NamedTextColor.RED));
            Arrays.stream(Bukkit.getOfflinePlayers()).forEach(p -> {
                String userName;
                String Uuid = p.getUniqueId().toString();
                String offlineUuid;

                userName = p.getName();
                if (userName == null) {
                    Bukkit.broadcast(Component.text("玩家名称不存在! 在线查找: " + Uuid, NamedTextColor.RED));
                    userName = queryName(Uuid);
                    if (userName == null) {
                        Bukkit.broadcast(Component.text("在线查找失败, 不存在此正版用户: " + Uuid, NamedTextColor.RED));
                        return;
                    }
                }

                offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + userName).getBytes(StandardCharsets.UTF_8)).toString();
                Bukkit.broadcast(Component.text("正在修改玩家: ", NamedTextColor.YELLOW).append(Component.text(userName, NamedTextColor.GOLD)).append(Component.text(" 原UUID: " + Uuid)));
                try {
                    Bukkit.broadcast(Component.text("状态(adv, playerdata, playerdataold, stats): "
                            + Files.move(advancements.resolve(Uuid + ".json"), advancements.resolve(offlineUuid + ".json"))
                            + Files.move(playerdata.resolve(Uuid + ".dat"), playerdata.resolve(offlineUuid + ".dat"))
                            + Files.move(playerdata.resolve(Uuid + ".dat_old"), playerdata.resolve(offlineUuid + ".dat_old"))
                            + Files.move(stats.resolve(Uuid + ".json"), stats.resolve(offlineUuid + ".json"))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Bukkit.broadcast(Component.text("现已完成UUID的更改, 服务器关闭!!!", NamedTextColor.GREEN));
            Bukkit.getServer().shutdown();
        }, 1200L);
    }

    public void onDisable() {
    }

    private String queryName(String uuid) {
        String userName = null;

        try {
            HttpResponse<String> Res = this.client.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s", URLEncoder.encode(uuid, StandardCharsets.UTF_8))))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            if (Res.statusCode() != 200) {
                Bukkit.broadcast(Component.text("没有找到此UUID? 错误代码: "+Res.statusCode(), NamedTextColor.RED));
                return userName;
            }
            String bodyStr = Res.body();
            int startIndex = bodyStr.indexOf("name")+9;
            int endIndex = bodyStr.indexOf("\"",startIndex);
            userName = bodyStr.substring(startIndex, endIndex);
        } catch (IOException | InterruptedException e) {
            Bukkit.broadcast(Component.text("与Mojang服务器联系时出错", NamedTextColor.RED));
        }

        return userName;
    }

}