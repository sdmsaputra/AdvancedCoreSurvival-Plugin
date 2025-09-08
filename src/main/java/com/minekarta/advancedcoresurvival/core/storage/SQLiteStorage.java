package com.minekarta.advancedcoresurvival.core.storage;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SQLiteStorage implements Storage {

    private Connection connection;
    private final AdvancedCoreSurvival plugin;

    public SQLiteStorage(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect(AdvancedCoreSurvival plugin) {
        File dataFolder = new File(plugin.getDataFolder(), "database");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, "data.db");
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create SQLite database file!", e);
                return;
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            plugin.getLogger().info("Successfully connected to SQLite database.");
            initializeTables();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to SQLite database!", e);
        }
    }

    private void initializeTables() {
        // Using try-with-resources to ensure the statement is closed automatically
        try (Statement statement = connection.createStatement()) {
            // Player data table (for economy, etc.)
            String playerDataSql = "CREATE TABLE IF NOT EXISTS player_data (" +
                                   "uuid TEXT PRIMARY KEY NOT NULL," +
                                   "balance REAL NOT NULL DEFAULT 0.0" +
                                   ");";
            statement.execute(playerDataSql);

            // Server data table (for spawn location, etc.)
            String serverDataSql = "CREATE TABLE IF NOT EXISTS server_data (" +
                                   "key TEXT PRIMARY KEY NOT NULL," +
                                   "value TEXT NOT NULL" +
                                   ");";
            statement.execute(serverDataSql);

            // Player homes table
            String playerHomesSql = "CREATE TABLE IF NOT EXISTS player_homes (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "uuid TEXT NOT NULL," +
                                    "name TEXT NOT NULL," +
                                    "world TEXT NOT NULL," +
                                    "x REAL NOT NULL," +
                                    "y REAL NOT NULL," +
                                    "z REAL NOT NULL," +
                                    "yaw REAL NOT NULL," +
                                    "pitch REAL NOT NULL," +
                                    "UNIQUE(uuid, name)" +
                                    ");";
            statement.execute(playerHomesSql);

            // Claims table
            String claimsSql = "CREATE TABLE IF NOT EXISTS claims (" +
                               "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                               "owner_uuid TEXT NOT NULL," +
                               "world TEXT NOT NULL," +
                               "chunk_x INTEGER NOT NULL," +
                               "chunk_z INTEGER NOT NULL," +
                               "UNIQUE(world, chunk_x, chunk_z)" +
                               ");";
            statement.execute(claimsSql);

            // Claim members/trusted players table
            String claimMembersSql = "CREATE TABLE IF NOT EXISTS claim_members (" +
                                     "claim_id INTEGER NOT NULL," +
                                     "member_uuid TEXT NOT NULL," +
                                     "PRIMARY KEY (claim_id, member_uuid)," +
                                     "FOREIGN KEY(claim_id) REFERENCES claims(id) ON DELETE CASCADE" +
                                     ");";
            statement.execute(claimMembersSql);

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database tables!", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while disconnecting from SQLite database.", e);
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public CompletableFuture<Double> getPlayerBalance(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT balance FROM player_data WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("balance");
                } else {
                    // Player doesn't exist in DB yet, return default
                    return 0.0;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error fetching player balance for " + playerUUID, e);
                return 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setPlayerBalance(UUID playerUUID, double balance) {
        return CompletableFuture.runAsync(() -> {
            // "INSERT OR REPLACE" is a convenient SQLite-specific command.
            // For MySQL, this would be "INSERT ... ON DUPLICATE KEY UPDATE".
            String sql = "INSERT OR REPLACE INTO player_data (uuid, balance) VALUES (?, ?);";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setDouble(2, balance);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error setting player balance for " + playerUUID, e);
            }
        });
    }

    // --- Spawn ---

    @Override
    public CompletableFuture<Void> setSpawnLocation(Location location) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO server_data (key, value) VALUES (?, ?);";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                // Serialize location to a string: world,x,y,z,yaw,pitch
                String locString = String.format("%s;%.2f;%.2f;%.2f;%.2f;%.2f",
                        location.getWorld().getName(),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        location.getYaw(),
                        location.getPitch());
                pstmt.setString(1, "spawn_location");
                pstmt.setString(2, locString);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error setting spawn location", e);
            }
        });
    }

    @Override
    public CompletableFuture<Location> getSpawnLocation() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT value FROM server_data WHERE key = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, "spawn_location");
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String[] parts = rs.getString("value").split(";");
                    return new Location(
                            Bukkit.getWorld(parts[0]),
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]),
                            Float.parseFloat(parts[4]),
                            Float.parseFloat(parts[5])
                    );
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error fetching spawn location", e);
            }
            return null; // Or default spawn
        });
    }

    // --- Homes ---

    @Override
    public CompletableFuture<Void> setHome(UUID playerUUID, String name, Location location) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO player_homes (uuid, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, name.toLowerCase());
                pstmt.setString(3, location.getWorld().getName());
                pstmt.setDouble(4, location.getX());
                pstmt.setDouble(5, location.getY());
                pstmt.setDouble(6, location.getZ());
                pstmt.setFloat(7, location.getYaw());
                pstmt.setFloat(8, location.getPitch());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error setting home for " + playerUUID, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteHome(UUID playerUUID, String name) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM player_homes WHERE uuid = ? AND name = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, name.toLowerCase());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error deleting home for " + playerUUID, e);
            }
        });
    }

    @Override
    public CompletableFuture<Location> getHome(UUID playerUUID, String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM player_homes WHERE uuid = ? AND name = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, name.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Location(
                            Bukkit.getWorld(rs.getString("world")),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch")
                    );
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error fetching home for " + playerUUID, e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Integer> getHomeCount(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM player_homes WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error counting homes for " + playerUUID, e);
            }
            return 0;
        });
    }

    @Override
    public CompletableFuture<List<String>> listHomes(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> homeNames = new ArrayList<>();
            String sql = "SELECT name FROM player_homes WHERE uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    homeNames.add(rs.getString("name"));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error listing homes for " + playerUUID, e);
            }
            return homeNames;
        });
    }

    // --- Claims ---

    @Override
    public CompletableFuture<Boolean> isChunkClaimed(String world, int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT id FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                return pstmt.executeQuery().next();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error checking if chunk is claimed", e);
                return false; // Assume not claimed on error
            }
        });
    }

    @Override
    public CompletableFuture<UUID> getClaimOwner(String world, int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT owner_uuid FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return UUID.fromString(rs.getString("owner_uuid"));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting claim owner", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Integer> getClaimId(String world, int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT id FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting claim ID", e);
            }
            return -1; // Return -1 or another invalid indicator
        });
    }

    @Override
    public CompletableFuture<Void> claimChunk(UUID owner, String world, int chunkX, int chunkZ) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO claims (owner_uuid, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?);";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, owner.toString());
                pstmt.setString(2, world);
                pstmt.setInt(3, chunkX);
                pstmt.setInt(4, chunkZ);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error claiming chunk", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> unclaimChunk(String world, int chunkX, int chunkZ) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error unclaiming chunk", e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getClaimCount(UUID ownerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM claims WHERE owner_uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, ownerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error counting claims for " + ownerUUID, e);
            }
            return 0;
        });
    }

    // --- Claim Members ---

    @Override
    public CompletableFuture<Void> addClaimMember(int claimId, UUID memberUUID) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR IGNORE INTO claim_members (claim_id, member_uuid) VALUES (?, ?);";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, claimId);
                pstmt.setString(2, memberUUID.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error adding claim member", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeClaimMember(int claimId, UUID memberUUID) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM claim_members WHERE claim_id = ? AND member_uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, claimId);
                pstmt.setString(2, memberUUID.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error removing claim member", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isMemberOfClaim(int claimId, UUID memberUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT claim_id FROM claim_members WHERE claim_id = ? AND member_uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, claimId);
                pstmt.setString(2, memberUUID.toString());
                return pstmt.executeQuery().next();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error checking claim membership", e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<List<UUID>> getClaimMembers(int claimId) {
        return CompletableFuture.supplyAsync(() -> {
            List<UUID> members = new ArrayList<>();
            String sql = "SELECT member_uuid FROM claim_members WHERE claim_id = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, claimId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    members.add(UUID.fromString(rs.getString("member_uuid")));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting claim members", e);
            }
            return members;
        });
    }

    @Override
    public CompletableFuture<java.util.Map<UUID, Integer>> getAllPlayersAndClaimCounts() {
        return CompletableFuture.supplyAsync(() -> {
            java.util.Map<UUID, Integer> playerClaimCounts = new java.util.HashMap<>();
            String sql = "SELECT owner_uuid, COUNT(*) as claim_count FROM claims GROUP BY owner_uuid;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    UUID ownerUUID = UUID.fromString(rs.getString("owner_uuid"));
                    int count = rs.getInt("claim_count");
                    playerClaimCounts.put(ownerUUID, count);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting all player claim counts", e);
            }
            return playerClaimCounts;
        });
    }

    @Override
    public CompletableFuture<Void> unclaimAllChunks(UUID ownerUUID) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM claims WHERE owner_uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, ownerUUID.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error unclaiming all chunks for " + ownerUUID, e);
            }
        });
    }
}
