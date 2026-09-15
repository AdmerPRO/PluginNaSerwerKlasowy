package pl.admerpro.AntyOPPluginMc;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.entity.EnderPearl;
import org.bukkit.Bukkit;
import java.util.Random;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;

public final class Main extends JavaPlugin implements Listener {

    Random rand = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);


        getLogger().info("Plugin został włączony! Ochrona działa.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Wyłączanie pluginu. Do zobaczenia!");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase().trim();
        Player player = event.getPlayer();

        // Blokowanie /op
        if ((message.equals("/op") || message.startsWith("/op ") ||
                message.equals("/minecraft:op") || message.startsWith("/minecraft:op ")) && getConfig().getBoolean("features.block-op")) {

            event.setCancelled(true);

            player.sendMessage(ChatColor.RED +
                    "Używanie komendy /op jest całkowicie zablokowane na tym serwerze!");

            getLogger().warning(
                    "Gracz " + player.getName() +
                            " próbował użyć zablokowanej komendy /op!"
            );

            return;
        }

        // Blokowanie /teleport
        if ((message.equals("/teleport") || message.startsWith("/teleport ") ||
                message.equals("/minecraft:teleport") ||
                message.startsWith("/minecraft:teleport ")) && getConfig().getBoolean("features.block-teleport")) {

            event.setCancelled(true);

            player.sendMessage(ChatColor.RED +
                    "Używanie komendy /teleport jest całkowicie zablokowane na tym serwerze!");

            getLogger().warning(
                    "Gracz " + player.getName() +
                            " próbował użyć zablokowanej komendy /teleport!"
            );

            return;
        }

        // Blokowanie /give
        if ((message.equals("/give") || message.startsWith("/give ") ||
                message.equals("/minecraft:give") ||
                message.startsWith("/minecraft:give ")) && getConfig().getBoolean("features.block-give")) {

            event.setCancelled(true);

            player.sendMessage(ChatColor.RED +
                    "Używanie komendy /give jest całkowicie zablokowane na tym serwerze!");

            getLogger().warning(
                    "Gracz " + player.getName() +
                            " próbował użyć zablokowanej komendy /give!"
            );

            return;
        }

        // Blokowanie /gamemode
        if ((message.equals("/gamemode") || message.startsWith("/gamemode ") ||
                message.equals("/minecraft:gamemode") ||
                message.startsWith("/minecraft:gamemode ")) && getConfig().getBoolean("features.block-gamemode")) {

            event.setCancelled(true);

            player.sendMessage(ChatColor.RED +
                    "Używanie komendy /gamemode jest całkowicie zablokowane na tym serwerze!");

            getLogger().warning(
                    "Gracz " + player.getName() +
                            " próbował użyć zablokowanej komendy /gamemode!"
            );

            return;
        }

        // Blokowanie /kill
        if ((message.equals("/kill") || message.startsWith("/kill ") ||
                message.equals("/minecraft:kill") ||
                message.startsWith("/minecraft:kill ")) && getConfig().getBoolean("features.block-kill")) {

            event.setCancelled(true);

            player.sendMessage(ChatColor.RED +
                    "Używanie komendy /kill jest całkowicie zablokowane na tym serwerze!");

            getLogger().warning(
                    "Gracz " + player.getName() +
                            " próbował użyć zablokowanej komendy /kill!"
            );
        }
    }

    // Blokowanie Creative i Spectator
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        GameMode mode = event.getNewGameMode();

        if (mode == GameMode.CREATIVE &&
                getConfig().getBoolean("features.block-creative")) {

            event.setCancelled(true);

            event.getPlayer().sendMessage(
                    ChatColor.RED + "Tryb Creative jest zablokowany."
            );

            return;
        }

        if (mode == GameMode.SPECTATOR &&
                getConfig().getBoolean("features.block-spectator")) {

            event.setCancelled(true);

            event.getPlayer().sendMessage(
                    ChatColor.RED + "Tryb Spectator jest zablokowany."
            );
        }
    }

    // Blokowanie ulepszania diamentowego ekwipunku do Netherite
    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (!getConfig().getBoolean("features.block-netherite")) {
            return;
        }

        if (event.getResult() == null) {
            return;
        }

        Material result = event.getResult().getType();

        if (result == Material.NETHERITE_HELMET ||
                result == Material.NETHERITE_CHESTPLATE ||
                result == Material.NETHERITE_LEGGINGS ||
                result == Material.NETHERITE_BOOTS) {

            event.setResult(null);
        }
    }

    // Zmniejszenie obrażeń Mace o 50%
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!getConfig().getBoolean("features.mace-damage-reduction")) {
            return;
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.MACE) {
            double multiplier =
                    getConfig().getDouble("settings.mace-damage-multiplier", 0.5);

            event.setDamage(event.getDamage() * multiplier);
        }
    }

    // Cooldown na riptide
    @EventHandler
    public void onRiptide(PlayerRiptideEvent event) {
        if (!getConfig().getBoolean("features.riptide-cooldown")) {
            return;
        }

        Player player = event.getPlayer();

        if (player.hasCooldown(Material.TRIDENT)) {
            event.setCancelled(true);
            return;
        }

        int cooldownSeconds =
                getConfig().getInt("cooldown.riptide-cooldown-seconds", 20);

        int cooldownTicks = cooldownSeconds * 20;

        player.setCooldown(Material.TRIDENT, cooldownTicks);
    }

    // Cooldown na koksy
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (event.getItem().getType() != Material.ENCHANTED_GOLDEN_APPLE) {
            return;
        }

        int cooldownSeconds = getConfig()
                .getInt("cooldowns.enchanted_golden_apple", 20);

        int cooldownTicks = cooldownSeconds * 20;

        player.setCooldown(Material.ENCHANTED_GOLDEN_APPLE, cooldownTicks);
    }

    // Cooldown na perłe
    @EventHandler
    public void onPearlThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }

        if (!(pearl.getShooter() instanceof Player player)) {
            return;
        }

        int cooldownSeconds = getConfig().getInt("cooldowns.ender_pearl", 8);
        int cooldownTicks = cooldownSeconds * 20;

        Bukkit.getScheduler().runTask(this, () -> {
            player.setCooldown(Material.ENDER_PEARL, cooldownTicks);
        });
    }

    // Drop expa z kamienia (StoneExp)
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType() != Material.STONE) return;

        if (getConfig().getBoolean("stone-exp.only-pickaxe", true)) {
            Material tool = e.getPlayer().getInventory().getItemInMainHand().getType();
            if (!tool.name().endsWith("_PICKAXE")) return;
        }

        // nie dawaj expa jak kamien postawiony przez gracza (anticheat)
        if (!e.isDropItems()) return;

        int min = getConfig().getInt("stone-exp.exp-min", 1);
        int max = getConfig().getInt("stone-exp.exp-max", 3);
        int exp = min + rand.nextInt(max - min + 1);

        // drop exp orb
        b.getWorld().spawn(b.getLocation().add(0.5, 0.5, 0.5), org.bukkit.entity.ExperienceOrb.class, orb -> {
            orb.setExperience(exp);
        });
    }
}