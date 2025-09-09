package com.minekarta.advancedcoresurvival.core.storage;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class MySQLStorage implements Storage {

    private final AdvancedCoreSurvival plugin;
    private HikariDataSource dataSource;
    private ExecutorService executor;

    public MySQLStorage(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect(AdvancedCoreSurvival plugin) {
        FileConfiguration config = plugin.getConfig();
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getString("storage.mysql.host") + ":" +
                config.getInt("storage.mysql.port") + "/" + config.getString("storage.mysql.database") +
                "?useSSL=false&autoReconnect=true");
        hikariConfig.setUsername(config.getString("storage.mysql.username"));
        hikariConfig.setPassword(config.getString("storage.mysql.password"));
        hikariConfig.setPoolName("AdvancedCoreSurvival-HikariPool");
        hikariConfig.setMaximumPoolSize(config.getInt("storage.mysql.pool-size", 10));
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            this.dataSource = new HikariDataSource(hikariConfig);
            this.executor = Executors.newCachedThreadPool();
            plugin.getLogger().info("Successfully connected to MySQL database.");
            initializeTables();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL database!", e);
        }
    }

    private void initializeTables() {
        // Run on an async thread
        executor.submit(() -> {
            try (Connection conn = dataSource.getConnection(); Statement statement = conn.createStatement()) {

                String playerDataSql = "CREATE TABLE IF NOT EXISTS player_data (" +
                        "uuid VARCHAR(36) NOT NULL," +
                        "world VARCHAR(255) NOT NULL," +
                        "balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00," +
                        "PRIMARY KEY (uuid, world)" +
                        ");";

                String serverDataSql = "CREATE TABLE IF NOT EXISTS server_data (" +
                        "data_key VARCHAR(255) PRIMARY KEY NOT NULL," +
                        "data_value TEXT NOT NULL" +
                        ");";

                String playerHomesSql = "CREATE TABLE IF NOT EXISTS player_homes (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "uuid VARCHAR(36) NOT NULL," +
                        "name VARCHAR(32) NOT NULL," +
                        "world VARCHAR(255) NOT NULL," +
                        "x DOUBLE NOT NULL," +
                        "y DOUBLE NOT NULL," +
                        "z DOUBLE NOT NULL," +
                        "yaw FLOAT NOT NULL," +
                        "pitch FLOAT NOT NULL," +
                        "UNIQUE KEY (uuid, name)" +
                        ");";

                String claimsSql = "CREATE TABLE IF NOT EXISTS claims (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "owner_uuid VARCHAR(36) NOT NULL," +
                        "world VARCHAR(255) NOT NULL," +
                        "chunk_x INT NOT NULL," +
                        "chunk_z INT NOT NULL," +
                        "UNIQUE KEY (world, chunk_x, chunk_z)" +
                        ");";

                String claimMembersSql = "CREATE TABLE IF NOT EXISTS claim_members (" +
                        "claim_id INT NOT NULL," +
                        "member_uuid VARCHAR(36) NOT NULL," +
                        "PRIMARY KEY (claim_id, member_uuid)," +
                        "FOREIGN KEY(claim_id) REFERENCES claims(id) ON DELETE CASCADE" +
                        ");";

                String banksSql = "CREATE TABLE IF NOT EXISTS banks (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(32) NOT NULL UNIQUE," +
                        "owner_uuid VARCHAR(36) NOT NULL," +
                        "balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00" +
                        ");";

                String bankMembersSql = "CREATE TABLE IF NOT EXISTS bank_members (" +
                        "bank_id INT NOT NULL," +
                        "member_uuid VARCHAR(36) NOT NULL," +
                        "PRIMARY KEY (bank_id, member_uuid)," +
                        "FOREIGN KEY(bank_id) REFERENCES banks(id) ON DELETE CASCADE" +
                        ");";

                String bankInvitesSql = "CREATE TABLE IF NOT EXISTS acs_bank_invites (" +
                        "bank_id INT NOT NULL," +
                        "invited_uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                        "FOREIGN KEY(bank_id) REFERENCES acs_banks(id) ON DELETE CASCADE" +
                        ");";

                String playerStatsSql = "CREATE TABLE IF NOT EXISTS player_stats (" +
                        "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                        "level INT NOT NULL DEFAULT 1," +
                        "exp DOUBLE NOT NULL DEFAULT 0.0," +
                        "strength INT NOT NULL DEFAULT 5," +
                        "agility INT NOT NULL DEFAULT 5," +
                        "endurance INT NOT NULL DEFAULT 5," +
                        "skillPoints INT NOT NULL DEFAULT 0" +
                        ");";

                String playerSkillsSql = "CREATE TABLE IF NOT EXISTS player_skills (" +
                        "uuid VARCHAR(36) NOT NULL," +
                        "skill_id VARCHAR(64) NOT NULL," +
                        "skill_level INT NOT NULL DEFAULT 1," +
                        "PRIMARY KEY (uuid, skill_id)," +
                        "FOREIGN KEY(uuid) REFERENCES player_stats(uuid) ON DELETE CASCADE" +
                        ");";

                statement.addBatch(playerDataSql);
                statement.addBatch(serverDataSql);
                statement.addBatch(playerHomesSql);
                statement.addBatch(claimsSql);
                statement.addBatch(claimMembersSql);
                statement.addBatch(banksSql);
                statement.addBatch(bankMembersSql);
                statement.addBatch(bankInvitesSql);
                statement.addBatch(playerStatsSql);
                statement.addBatch(playerSkillsSql);

                statement.executeBatch();
                plugin.getLogger().info("MySQL tables initialized successfully.");

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create/update MySQL database tables!", e);
            }
        });
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    @Override
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    // Helper to run a query asynchronously
    private <T> CompletableFuture<T> supplyAsync(SQLSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                return supplier.get(conn);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "A database error occurred.", e);
                return null;
            }
        }, executor);
    }

    // Helper to run an update asynchronously
    private CompletableFuture<Void> runAsync(SQLConsumer consumer) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                consumer.accept(conn);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "A database error occurred.", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Double> getPlayerBalance(UUID playerUUID, String worldName) {
        return supplyAsync(conn -> {
            String sql = "SELECT balance FROM player_data WHERE uuid = ? AND world = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, worldName);
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? rs.getDouble("balance") : 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setPlayerBalance(UUID playerUUID, String worldName, double balance) {
        return runAsync(conn -> {
            String sql = "INSERT INTO player_data (uuid, world, balance) VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE balance = VALUES(balance);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, worldName);
                pstmt.setDouble(3, balance);
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Void> setSpawnLocation(Location location) {
        return runAsync(conn -> {
            String sql = "INSERT INTO server_data (data_key, data_value) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE data_value = VALUES(data_value);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String locString = String.format("%s;%.2f;%.2f;%.2f;%.2f;%.2f",
                        location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                        location.getYaw(), location.getPitch());
                pstmt.setString(1, "spawn_location");
                pstmt.setString(2, locString);
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Location> getSpawnLocation() {
        return supplyAsync(conn -> {
            String sql = "SELECT data_value FROM server_data WHERE data_key = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "spawn_location");
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String[] parts = rs.getString("data_value").split(";");
                    return new Location(
                            Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5])
                    );
                }
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> setHome(UUID playerUUID, String name, Location location) {
        return runAsync(conn -> {
            String sql = "INSERT INTO player_homes (uuid, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, name.toLowerCase());
                pstmt.setString(3, location.getWorld().getName());
                pstmt.setDouble(4, location.getX());
                pstmt.setDouble(5, location.getY());
                pstmt.setDouble(6, location.getZ());
                pstmt.setFloat(7, location.getYaw());
                pstmt.setFloat(8, location.getPitch());
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteHome(UUID playerUUID, String name) {
        return runAsync(conn -> {
            String sql = "DELETE FROM player_homes WHERE uuid = ? AND name = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, name.toLowerCase());
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Location> getHome(UUID playerUUID, String name) {
        return supplyAsync(conn -> {
            String sql = "SELECT * FROM player_homes WHERE uuid = ? AND name = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, name.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Location(
                            Bukkit.getWorld(rs.getString("world")), rs.getDouble("x"), rs.getDouble("y"),
                            rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")
                    );
                }
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Integer> getHomeCount(UUID playerUUID) {
        return supplyAsync(conn -> {
            String sql = "SELECT COUNT(*) FROM player_homes WHERE uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? rs.getInt(1) : 0;
            }
        });
    }

    @Override
    public CompletableFuture<List<String>> listHomes(UUID playerUUID) {
        return supplyAsync(conn -> {
            List<String> homeNames = new ArrayList<>();
            String sql = "SELECT name FROM player_homes WHERE uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    homeNames.add(rs.getString("name"));
                }
            }
            return homeNames;
        });
    }

    // --- Claims ---
    @Override
    public CompletableFuture<Boolean> isChunkClaimed(String world, int chunkX, int chunkZ) {
        return supplyAsync(conn -> {
            String sql = "SELECT id FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                return pstmt.executeQuery().next();
            }
        });
    }

    @Override
    public CompletableFuture<UUID> getClaimOwner(String world, int chunkX, int chunkZ) {
        return supplyAsync(conn -> {
            String sql = "SELECT owner_uuid FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? UUID.fromString(rs.getString("owner_uuid")) : null;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getClaimId(String world, int chunkX, int chunkZ) {
        return supplyAsync(conn -> {
            String sql = "SELECT id FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? rs.getInt("id") : -1;
            }
        });
    }

    @Override
    public CompletableFuture<Void> claimChunk(UUID owner, String world, int chunkX, int chunkZ) {
        return runAsync(conn -> {
            String sql = "INSERT INTO claims (owner_uuid, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, owner.toString());
                pstmt.setString(2, world);
                pstmt.setInt(3, chunkX);
                pstmt.setInt(4, chunkZ);
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Void> unclaimChunk(String world, int chunkX, int chunkZ) {
        return runAsync(conn -> {
            String sql = "DELETE FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Void> unclaimAllChunks(UUID ownerUUID) {
        return runAsync(conn -> {
            String sql = "DELETE FROM claims WHERE owner_uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, ownerUUID.toString());
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Void> addClaimMember(int claimId, UUID memberUUID) {
        return runAsync(conn -> {
            String sql = "INSERT IGNORE INTO claim_members (claim_id, member_uuid) VALUES (?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, claimId);
                pstmt.setString(2, memberUUID.toString());
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeClaimMember(int claimId, UUID memberUUID) {
        return runAsync(conn -> {
            String sql = "DELETE FROM claim_members WHERE claim_id = ? AND member_uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, claimId);
                pstmt.setString(2, memberUUID.toString());
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isMemberOfClaim(int claimId, UUID memberUUID) {
        return supplyAsync(conn -> {
            String sql = "SELECT claim_id FROM claim_members WHERE claim_id = ? AND member_uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, claimId);
                pstmt.setString(2, memberUUID.toString());
                return pstmt.executeQuery().next();
            }
        });
    }

    @Override
    public CompletableFuture<List<UUID>> getClaimMembers(int claimId) {
        return supplyAsync(conn -> {
            List<UUID> members = new ArrayList<>();
            String sql = "SELECT member_uuid FROM claim_members WHERE claim_id = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, claimId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    members.add(UUID.fromString(rs.getString("member_uuid")));
                }
            }
            return members;
        });
    }

    @Override
    public CompletableFuture<Integer> getClaimCount(UUID ownerUUID) {
        return supplyAsync(conn -> {
            String sql = "SELECT COUNT(*) FROM claims WHERE owner_uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, ownerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? rs.getInt(1) : 0;
            }
        });
    }

    @Override
    public CompletableFuture<Map<UUID, Integer>> getAllPlayersAndClaimCounts() {
        return supplyAsync(conn -> {
            Map<UUID, Integer> playerClaimCounts = new HashMap<>();
            String sql = "SELECT owner_uuid, COUNT(*) as claim_count FROM claims GROUP BY owner_uuid;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    playerClaimCounts.put(UUID.fromString(rs.getString("owner_uuid")), rs.getInt("claim_count"));
                }
            }
            return playerClaimCounts;
        });
    }

    // --- RPG ---
    @Override
    public CompletableFuture<Void> savePlayerStats(PlayerStats stats) {
        return runAsync(conn -> {
            String playerUUID = stats.getPlayerUUID().toString();
            String saveStatsSql = "INSERT INTO player_stats (uuid, level, exp, strength, agility, endurance, skillPoints) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                                  "level=VALUES(level), exp=VALUES(exp), strength=VALUES(strength), agility=VALUES(agility), " +
                                  "endurance=VALUES(endurance), skillPoints=VALUES(skillPoints);";
            String deleteSkillsSql = "DELETE FROM player_skills WHERE uuid = ?;";
            String saveSkillSql = "INSERT INTO player_skills (uuid, skill_id, skill_level) VALUES (?, ?, ?);";

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(saveStatsSql)) {
                    pstmt.setString(1, playerUUID);
                    pstmt.setInt(2, stats.getLevel());
                    pstmt.setDouble(3, stats.getExp());
                    pstmt.setInt(4, stats.getStrength());
                    pstmt.setInt(5, stats.getAgility());
                    pstmt.setInt(6, stats.getEndurance());
                    pstmt.setInt(7, stats.getSkillPoints());
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(deleteSkillsSql)) {
                    pstmt.setString(1, playerUUID);
                    pstmt.executeUpdate();
                }

                if (stats.getSkillLevels() != null && !stats.getSkillLevels().isEmpty()) {
                    try (PreparedStatement pstmt = conn.prepareStatement(saveSkillSql)) {
                        for (Map.Entry<String, Integer> entry : stats.getSkillLevels().entrySet()) {
                            pstmt.setString(1, playerUUID);
                            pstmt.setString(2, entry.getKey());
                            pstmt.setInt(3, entry.getValue());
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e; // Re-throw to be caught by the helper
            } finally {
                conn.setAutoCommit(true);
            }
        });
    }

    @Override
    public CompletableFuture<PlayerStats> loadPlayerStats(UUID playerUUID) {
        return supplyAsync(conn -> {
            String statsSql = "SELECT * FROM player_stats WHERE uuid = ?;";
            PlayerStats stats = new PlayerStats(playerUUID); // Create a default one
            try (PreparedStatement pstmt = conn.prepareStatement(statsSql)) {
                pstmt.setString(1, playerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.setLevel(rs.getInt("level"));
                    stats.setExp(rs.getDouble("exp"));
                    stats.setStrength(rs.getInt("strength"));
                    stats.setAgility(rs.getInt("agility"));
                    stats.setEndurance(rs.getInt("endurance"));
                    stats.setSkillPoints(rs.getInt("skillPoints"));
                } else {
                    // No stats exist, so we'll save the default ones we just created
                    savePlayerStats(stats);
                    return stats;
                }
            }

            String skillsSql = "SELECT skill_id, skill_level FROM player_skills WHERE uuid = ?;";
            try (PreparedStatement skillsPstmt = conn.prepareStatement(skillsSql)) {
                skillsPstmt.setString(1, playerUUID.toString());
                ResultSet skillsRs = skillsPstmt.executeQuery();
                while (skillsRs.next()) {
                    stats.setSkillLevel(skillsRs.getString("skill_id"), skillsRs.getInt("skill_level"));
                }
            }
            return stats;
        });
    }

    // --- Banks ---
    @Override
    public CompletableFuture<Boolean> hasBank(String name) {
        return supplyAsync(conn -> {
            String sql = "SELECT id FROM banks WHERE name = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                return pstmt.executeQuery().next();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> createBank(String name, UUID owner) {
        return supplyAsync(conn -> {
            String sql = "INSERT INTO banks (name, owner_uuid) VALUES (?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, owner.toString());
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                return false; // Unique constraint likely failed
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(String name) {
        return supplyAsync(conn -> {
            String sql = "DELETE FROM banks WHERE name = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public CompletableFuture<Double> getBankBalance(String name) {
        return supplyAsync(conn -> {
            String sql = "SELECT balance FROM banks WHERE name = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? rs.getDouble("balance") : 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setBankBalance(String name, double balance) {
        return runAsync(conn -> {
            String sql = "UPDATE banks SET balance = ? WHERE name = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, balance);
                pstmt.setString(2, name);
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isBankOwner(String name, UUID player) {
        return supplyAsync(conn -> {
            String sql = "SELECT id FROM banks WHERE name = ? AND owner_uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, player.toString());
                return pstmt.executeQuery().next();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isBankMember(String name, UUID player) {
        return supplyAsync(conn -> {
            String sql = "SELECT bm.bank_id FROM bank_members bm JOIN banks b ON bm.bank_id = b.id WHERE b.name = ? AND bm.member_uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, player.toString());
                return pstmt.executeQuery().next();
            }
        });
    }

    private CompletableFuture<Integer> getBankId(String name, Connection conn) throws SQLException {
        String sql = "SELECT id FROM banks WHERE name = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            return CompletableFuture.completedFuture(rs.next() ? rs.getInt("id") : -1);
        }
    }

    @Override
    public CompletableFuture<Void> addBankMember(String name, UUID player) {
        return runAsync(conn -> {
            getBankId(name, conn).thenAccept(bankId -> {
                if (bankId == -1) return;
                String sql = "INSERT IGNORE INTO bank_members (bank_id, member_uuid) VALUES (?, ?);";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, bankId);
                    pstmt.setString(2, player.toString());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error adding member to bank " + name, e);
                }
            });
        });
    }

    @Override
    public CompletableFuture<Void> removeBankMember(String name, UUID player) {
        return runAsync(conn -> {
            getBankId(name, conn).thenAccept(bankId -> {
                if (bankId == -1) return;
                String sql = "DELETE FROM bank_members WHERE bank_id = ? AND member_uuid = ?;";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, bankId);
                    pstmt.setString(2, player.toString());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error removing member from bank " + name, e);
                }
            });
        });
    }

    @Override
    public CompletableFuture<List<String>> getBanks() {
        return supplyAsync(conn -> {
            List<String> bankNames = new ArrayList<>();
            String sql = "SELECT name FROM banks;";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    bankNames.add(rs.getString("name"));
                }
            }
            return bankNames;
        });
    }

    // Functional interfaces for cleaner async helpers
    @FunctionalInterface
    interface SQLSupplier<T> {
        T get(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    interface SQLConsumer {
        void accept(Connection conn) throws SQLException;
    }

    // --- Bank Invite Methods ---

    @Override
    public CompletableFuture<Void> addBankInvite(int bankId, UUID invitedUUID) {
        return runAsync(conn -> {
            String sql = "INSERT INTO acs_bank_invites (bank_id, invited_uuid) VALUES (?, ?) ON DUPLICATE KEY UPDATE bank_id = VALUES(bank_id);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bankId);
                pstmt.setString(2, invitedUUID.toString());
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getBankInvite(UUID invitedUUID) {
        return supplyAsync(conn -> {
            String sql = "SELECT bank_id FROM acs_bank_invites WHERE invited_uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, invitedUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? rs.getInt("bank_id") : -1;
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeBankInvite(UUID invitedUUID) {
        return runAsync(conn -> {
            String sql = "DELETE FROM acs_bank_invites WHERE invited_uuid = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, invitedUUID.toString());
                pstmt.executeUpdate();
            }
        });
    }
}
