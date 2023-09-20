/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.storage.method.database.sql;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.StorageType;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.CFConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of AbstractSQLDatabase that uses the SQLite database for player data storage.
 */
public class SQLiteImpl extends AbstractSQLDatabase {

    private Connection connection;
    private File databaseFile;

    public SQLiteImpl(CustomFishingPlugin plugin) {
        super(plugin);
    }

    /**
     * Initialize the SQLite database and connection based on the configuration.
     */
    @Override
    public void initialize() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        this.databaseFile = new File(plugin.getDataFolder(), config.getString("SQLite.file", "data") + ".db");
        super.tablePrefix = config.getString("SQLite.table-prefix", "customfishing");
        super.createTableIfNotExist();
    }

    /**
     * Disable the SQLite database by closing the connection.
     */
    @Override
    public void disable() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.SQLite;
    }

    /**
     * Get a connection to the SQLite database.
     *
     * @return A database connection.
     * @throws SQLException If there is an error establishing a connection.
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            setConnection();
        }
        return connection;
    }

    /**
     * Asynchronously retrieve player data from the SQLite database.
     *
     * @param uuid The UUID of the player.
     * @param lock Flag indicating whether to lock the data.
     * @return A CompletableFuture with an optional PlayerData.
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock) {
        var future = new CompletableFuture<Optional<PlayerData>>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(String.format(SqlConstants.SQL_SELECT_BY_UUID, getTableName("data")))
        ) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int lockValue = rs.getInt(2);
                if (lockValue != 0 && getCurrentSeconds() - CFConfig.dataSaveInterval <= lockValue) {
                    connection.close();
                    future.complete(Optional.of(PlayerData.LOCKED));
                    return;
                }
                final byte[] dataByteArray = rs.getBytes("data");
                if (lock) lockOrUnlockPlayerData(uuid, true);
                future.complete(Optional.of(plugin.getStorageManager().fromBytes(dataByteArray)));
            } else if (Bukkit.getPlayer(uuid) != null) {
                var data = PlayerData.empty();
                insertPlayerData(uuid, data, lock);
                future.complete(Optional.of(data));
            } else {
                future.complete(Optional.empty());
            }
        } catch (SQLException e) {
            LogUtils.warn("Failed to get " + uuid + "'s data.", e);
            future.completeExceptionally(e);
        }
        });
        return future;
    }

    /**
     * Asynchronously update player data in the SQLite database.
     *
     * @param uuid       The UUID of the player.
     * @param playerData The player's data to update.
     * @param unlock     Flag indicating whether to unlock the data.
     * @return A CompletableFuture indicating the update result.
     */
    @Override
    public CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean unlock) {
        var future = new CompletableFuture<Boolean>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(String.format(SqlConstants.SQL_UPDATE_BY_UUID, getTableName("data")))
        ) {
            statement.setInt(1, unlock ? 0 : getCurrentSeconds());
            statement.setBytes(2, plugin.getStorageManager().toBytes(playerData));
            statement.setString(3, uuid.toString());
            statement.executeUpdate();
            future.complete(true);
        } catch (SQLException e) {
            LogUtils.warn("Failed to update " + uuid + "'s data.", e);
            future.completeExceptionally(e);
        }
        });
        return future;
    }

    /**
     * Asynchronously update data for multiple players in the SQLite database.
     *
     * @param users  A collection of OfflineUser instances to update.
     * @param unlock Flag indicating whether to unlock the data.
     */
    @Override
    public void updateManyPlayersData(Collection<? extends OfflineUser> users, boolean unlock) {
        String sql = String.format(SqlConstants.SQL_UPDATE_BY_UUID, getTableName("data"));
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (OfflineUser user : users) {
                    statement.setInt(1, unlock ? 0 : getCurrentSeconds());
                    statement.setBytes(2, plugin.getStorageManager().toBytes(user.getPlayerData()));
                    statement.setString(3, user.getUUID().toString());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                LogUtils.warn("Failed to update bag data for online players", e);
            }
        } catch (SQLException e) {
            LogUtils.warn("Failed to get connection when saving online players' data", e);
        }
    }

    /**
     * Insert player data into the SQLite database.
     *
     * @param uuid       The UUID of the player.
     * @param playerData The player's data to insert.
     * @param lock       Flag indicating whether to lock the data.
     */
    @Override
    public void insertPlayerData(UUID uuid, PlayerData playerData, boolean lock) {
        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(String.format(SqlConstants.SQL_INSERT_DATA_BY_UUID, getTableName("data")))
        ) {
            statement.setString(1, uuid.toString());
            statement.setInt(2, lock ? getCurrentSeconds() : 0);
            statement.setBytes(3, plugin.getStorageManager().toBytes(playerData));
            statement.execute();
        } catch (SQLException e) {
            LogUtils.warn("Failed to insert " + uuid + "'s data.", e);
        }
    }

    /**
     * Set up the connection to the SQLite database.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void setConnection() {
        try {
            if (!databaseFile.exists()) databaseFile.createNewFile();
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            config.setSynchronous(SQLiteConfig.SynchronousMode.FULL);
            connection = DriverManager.getConnection(
                    String.format("jdbc:sqlite:%s", databaseFile.getAbsolutePath()),
                    config.toProperties()
            );
        } catch (IOException e) {
            LogUtils.warn("Failed to create the SQLite database.", e);
        } catch (SQLException e) {
            LogUtils.warn("Failed to initialize SQLite database.", e);
        } catch (ClassNotFoundException e) {
            LogUtils.warn("Failed to find SQLite driver.", e);
        }
    }
}