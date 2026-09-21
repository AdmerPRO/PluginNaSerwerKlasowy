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
import java.util.Iterator;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin implements Listener {

    Random rand = new Random();

    private int MAX_PROT;
    private int MAX_SHARP;

    private int KOX_SECONDS;
    private int PEARL_SECONDS;
    private int SHIELD_SECONDS;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.MAX_PROT = this.getConfig().getInt("max_enchantments.max-protection", 3);
        this.MAX_SHARP = this.getConfig().getInt("max_enchantments.max-sharpness", 4);

        this.KOX_SECONDS = getConfig().getInt("cooldowns.enchanted_golden_apple", 5400);
        this.PEARL_SECONDS = getConfig().getInt("cooldowns.ender_pearl", 8);
        this.SHIELD_SECONDS = getConfig().getInt("cooldowns.shield", 10);

        getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().runTaskTimer(this, () -> this.getServer().getOnlinePlayers().forEach((p) -> {
            Iterator i$ = p.getInventory().iterator();

            while(i$.hasNext()) {
                ItemStack it = (ItemStack)i$.next();
                if (this.isStrength2(it)) {
                    p.getInventory().remove(it);
                    p.sendMessage("§cSiła II jest zablokowana!");
                }

                if (it != null) {
                    if (this.isDiaArmor(it.getType()) && it.getEnchantmentLevel(Enchantment.PROTECTION) > this.MAX_PROT) {
                        it.removeEnchantment(Enchantment.PROTECTION);
                        it.addEnchantment(Enchantment.PROTECTION, this.MAX_PROT);
                    }

                    if (it.getType() == Material.DIAMOND_SWORD && it.getEnchantmentLevel(Enchantment.SHARPNESS) > this.MAX_SHARP) {
                        it.removeEnchantment(Enchantment.SHARPNESS);
                        it.addEnchantment(Enchantment.SHARPNESS, this.MAX_SHARP);
                    } 
                }    
            }
        }), 100L, 100L);

        this.getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player p : getServer().getOnlinePlayers()) {
                List<String> parts = new ArrayList<>();

                if (p.hasCooldown(Material.ENCHANTED_GOLDEN_APPLE)) {
                    int sec = (int) Math.ceil(p.getCooldown(Material.ENCHANTED_GOLDEN_APPLE) / 20.0);

                    if (sec > 0) {
                        parts.add("§6KOX §f" + format(sec));
                    }
                }

                if (p.hasCooldown(Material.ENDER_PEARL)) {
                    int sec = (int) Math.ceil(p.getCooldown(Material.ENDER_PEARL) / 20.0);

                    if (sec > 0) {
                        parts.add("§aPERLA §f" + format(sec));
                    }
                }

                if (p.hasCooldown(Material.SHIELD)) {
                    int sec = (int) Math.ceil(p.getCooldown(Material.SHIELD) / 20.0);

                    if (sec > 0) {
                        parts.add("§9TARCZA §f" + format(sec));
                    }
                }

                if (parts.isEmpty()) {
                    p.sendActionBar("");
                    continue;
                }

                String msg = String.join(" §8| ", parts);
                p.sendActionBar(msg);
        }
    }, 0L, 10L);

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
    // Cooldown na KOXa
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKox(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.ENCHANTED_GOLDEN_APPLE) {
            return;
        }

        Player player = event.getPlayer();

        if (player.hasCooldown(Material.ENCHANTED_GOLDEN_APPLE)) {
            event.setCancelled(true);
            return;
        }

        // Ustawiamy cooldown dopiero po rozpoczęciu jedzenia
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) {
                player.setCooldown(
                        Material.ENCHANTED_GOLDEN_APPLE,
                        KOX_SECONDS * 20
                );
            }
        }, 1L);
    }

    // Cooldown na perłe
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPearlThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }  

        if (!(pearl.getShooter() instanceof Player player)) {
            return;
        }

        if (player.hasCooldown(Material.ENDER_PEARL)) {
            event.setCancelled(true);
            return;
        }

        int cooldownTicks = PEARL_SECONDS * 20;

        Bukkit.getScheduler().runTask(this, () -> {
            if (player.isOnline()) {
                player.setCooldown(
                        Material.ENDER_PEARL,
                        cooldownTicks
                );
            }
        });
    }

    // Cooldown na tarczę
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShieldBlock(PlayerInteractEvent event) {
        if (!getConfig().getBoolean("features.shield-cooldown", true)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.SHIELD) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (player.hasCooldown(Material.SHIELD)) {
            event.setCancelled(true);
            return;
        }

        int cooldownTicks = SHIELD_SECONDS * 20;

        Bukkit.getScheduler().runTask(this, () -> {
            if (player.isOnline()) {
                player.setCooldown(
                        Material.SHIELD,
                        cooldownTicks
                );
            }
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

    // Anty silka 2
    boolean isStrength2(ItemStack item) {
        if (item == null) {
            return false;
        } else if (item.getType() == Material.AIR) {
            return false;
        } else {
            ItemMeta var3 = item.getItemMeta();
            if (var3 instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta)var3;
                return meta.getBasePotionType() == PotionType.STRONG_STRENGTH;
            } else {
                return false;
            }
        }
    }

    @EventHandler
    public void onEffect(EntityPotionEffectEvent e) {
        PotionEffect newEff = e.getNewEffect();
        if (newEff != null) {
            if (newEff.getType().equals(PotionEffectType.STRENGTH) && newEff.getAmplifier() >= 1) {
                e.setCancelled(true);
                Entity var4 = e.getEntity();
                if (var4 instanceof Player) {
                    Player p = (Player)var4;
                    p.sendMessage("§cSiła II jest zablokowana!");
                }   
            }  
        }
    }

    @EventHandler
    public void onCloud(AreaEffectCloudApplyEvent e) {
        for(PotionEffect eff : e.getEntity().getCustomEffects()) {
            if (eff.getType().equals(PotionEffectType.STRENGTH) && eff.getAmplifier() >= 1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    // Anty prot 4 i sh 5
    boolean isDiaArmor(Material m) {
        return m == Material.DIAMOND_HELMET || m == Material.DIAMOND_CHESTPLATE || m == Material.DIAMOND_LEGGINGS || m == Material.DIAMOND_BOOTS;
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public void onEnchant(EnchantItemEvent e) {
        if (this.isDiaArmor(e.getItem().getType()) && (Integer)e.getEnchantsToAdd().getOrDefault(Enchantment.PROTECTION, 0) > this.MAX_PROT) {
            e.getEnchantsToAdd().put(Enchantment.PROTECTION, this.MAX_PROT);
        }

        if (e.getItem().getType() == Material.DIAMOND_SWORD && (Integer)e.getEnchantsToAdd().getOrDefault(Enchantment.SHARPNESS, 0) > this.MAX_SHARP) {
            e.getEnchantsToAdd().put(Enchantment.SHARPNESS, this.MAX_SHARP);
        }

    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public void onAnvil(PrepareAnvilEvent e) {
        ItemStack res = e.getResult();
        if (res != null) {
            if (this.isDiaArmor(res.getType()) && res.getEnchantmentLevel(Enchantment.PROTECTION) > this.MAX_PROT) {
                e.setResult((ItemStack)null);
            }

            if (res.getType() == Material.DIAMOND_SWORD && res.getEnchantmentLevel(Enchantment.SHARPNESS) > this.MAX_SHARP) {
                e.setResult((ItemStack)null);
            }
        }
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent e) {
        if (e.getInventory().getType() == InventoryType.ANVIL) {
            if (e.getSlot() == 2) {
                ItemStack res = e.getCurrentItem();
                if (res != null) {
                    if (this.isDiaArmor(res.getType()) && res.getEnchantmentLevel(Enchantment.PROTECTION) > this.MAX_PROT) {
                        e.setCancelled(true);
                        e.getWhoClicked().sendMessage("§cBlokada! Diax set max Prot " + this.MAX_PROT);
                    }

                    if (res.getType() == Material.DIAMOND_SWORD && res.getEnchantmentLevel(Enchantment.SHARPNESS) > this.MAX_SHARP) {
                        e.setCancelled(true);
                        e.getWhoClicked().sendMessage("§cBlokada! Diax miecz max Sharp " + this.MAX_SHARP);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVillager(VillagerAcquireTradeEvent e) {
        MerchantRecipe r = e.getRecipe();
        ItemStack res = r.getResult();
        if (this.isDiaArmor(res.getType()) && res.getEnchantmentLevel(Enchantment.PROTECTION) > this.MAX_PROT) {
            ItemStack fixed = res.clone();
            fixed.removeEnchantment(Enchantment.PROTECTION);
            fixed.addEnchantment(Enchantment.PROTECTION, this.MAX_PROT);
            MerchantRecipe nr = new MerchantRecipe(fixed, r.getUses(), r.getMaxUses(), r.hasExperienceReward(), r.getVillagerExperience(), r.getPriceMultiplier());
            nr.setIngredients(r.getIngredients());
            e.setRecipe(nr);
        }

        if (res.getType() == Material.DIAMOND_SWORD && res.getEnchantmentLevel(Enchantment.SHARPNESS) > this.MAX_SHARP) {
            ItemStack fixed = res.clone();
            fixed.removeEnchantment(Enchantment.SHARPNESS);
            fixed.addEnchantment(Enchantment.SHARPNESS, this.MAX_SHARP);
            MerchantRecipe nr = new MerchantRecipe(fixed, r.getUses(), r.getMaxUses(), r.hasExperienceReward(), r.getVillagerExperience(), r.getPriceMultiplier());
            nr.setIngredients(r.getIngredients());
            e.setRecipe(nr);
        }

        if (res.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta)res.getItemMeta();
            if (meta == null) {
                return;
            }

            if (meta.getStoredEnchantLevel(Enchantment.PROTECTION) > this.MAX_PROT) {
                ItemStack fixed = res.clone();
                EnchantmentStorageMeta fm = (EnchantmentStorageMeta)fixed.getItemMeta();
                fm.removeStoredEnchant(Enchantment.PROTECTION);
                fm.addStoredEnchant(Enchantment.PROTECTION, this.MAX_PROT, true);
                fixed.setItemMeta(fm);
                MerchantRecipe nr = new MerchantRecipe(fixed, r.getUses(), r.getMaxUses(), r.hasExperienceReward(), r.getVillagerExperience(), r.getPriceMultiplier());
                nr.setIngredients(r.getIngredients());
                e.setRecipe(nr);
            }

            if (meta.getStoredEnchantLevel(Enchantment.SHARPNESS) > this.MAX_SHARP) {
                ItemStack fixed = res.clone();
                EnchantmentStorageMeta fm = (EnchantmentStorageMeta)fixed.getItemMeta();
                fm.removeStoredEnchant(Enchantment.SHARPNESS);
                fm.addStoredEnchant(Enchantment.SHARPNESS, this.MAX_SHARP, true);
                fixed.setItemMeta(fm);
                MerchantRecipe nr = new MerchantRecipe(fixed, r.getUses(), r.getMaxUses(), r.hasExperienceReward(), r.getVillagerExperience(), r.getPriceMultiplier());
                nr.setIngredients(r.getIngredients());
                e.setRecipe(nr);
            }
        }
   }

   private String format(int totalSeconds) {
    if (totalSeconds <= 0) {
        return "0s.";
    }

    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;

    if (minutes > 0) {
        return minutes + "min. " + seconds + "s.";
    }

    return seconds + "s.";
}
}